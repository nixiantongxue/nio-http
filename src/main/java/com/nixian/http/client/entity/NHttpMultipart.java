package com.nixian.http.client.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

import com.nixian.http.client.codecs.BestEncoder;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class NHttpMultipart extends NativeHeadHttpMultipart{

    private static Logger logger = Logger.getLogger(NHttpMultipart.class.getName());
    
    private Mode bodyTransferWay = Mode.BEST;
    
    private boolean bufferAvailable() {
        return buffer.isAvailable();
    }
    
    private void bufferEnd() {
//        logger.info("bufferEnd------");
        buffer.end(TWO_DASHES);
        buffer.end(encode(MIME.DEFAULT_CHARSET,boundary));
        buffer.end(TWO_DASHES);
        buffer.end(CR_LF);
    }
    
    private void bufferCursor() {
        buffer.current(String.valueOf(context.partIdx));
    }
    
    private long bufferLength() {
        return buffer.getLength();
    }
    
    private void bufferHead(ByteBuffer b) throws IOException{
        buffer.writeHead(b);
    }
    
    private void bufferFoot(ByteBuffer b) throws IOException{
        buffer.writeFoot(b);
    }
    
    private void readHead(ContentEncoder encoder) throws IOException {
        if(encoder instanceof BestEncoder && ((BestEncoder)encoder).completeStep() == BestEncoder.CompleteBody
                                          && context.step == BestEncoder.CompleteBody)
        {
//            logger.info("resetStep------");
            ((BestEncoder)encoder).resetStep();
        }
        bufferCursor();
        int len=0;
        if((len=buffer.flushHead(encoder))>0) {
//            logger.config("readHead------"+"id:"+context.partIdx+",name:"+buffer.currentName()+",len:"+len);
            context.step = BestEncoder.CompleteHead;
        }
    } 
    
    private int readFoot(ContentEncoder encoder) throws IOException {
        
        if(encoder instanceof BestEncoder && ((BestEncoder)encoder).completeStep() == BestEncoder.CompleteBody)
        {
            int i = buffer.flushFoot(encoder);
            context.step = BestEncoder.CompleteBody;
            context.partIdx++;
            context.producer = null;
//            logger.info("readFoot------"+"len:"+i);
        }
        return 0;
    }
    
    private boolean notCompletedAllPart(ContentEncoder encoder) {
        return notCompletedPart(encoder) &&
                    context.partIdx == context.partTot;
    }
    
    private boolean notCompletedPart(ContentEncoder encoder) {
        if(encoder instanceof BestEncoder && ((BestEncoder)encoder).completeStep() != BestEncoder.CompleteBody)
            return true;
        return false;
    }
    
    private void readEnd(ContentEncoder encoder) throws IOException {
        if(encoder instanceof BestEncoder && ((BestEncoder)encoder).completeStep() == BestEncoder.CompleteBody)  
        {
            int i = buffer.flushEnd(encoder);
            context.step = BestEncoder.CompleteEnd;
            context.producer = null;
            ((BestEncoder)encoder).complete(BestEncoder.CompleteEnd);
//            logger.info("readEnd------"+"len:"+i);
        }  
    }
    
    
    private void writeBytesHead(
            final ByteBuffer b) throws IOException {
        b.rewind();
        bufferHead(b);
    }
    
    private void writeBytesFoot(
            final ByteBuffer b) throws IOException {
        b.rewind();
        bufferFoot(b);
    }
    
    private void writeBytesHead(
            final String s, final Charset charset) throws IOException {
        ByteBuffer b = encode(charset, s);
        writeBytesHead(b);
    }
    
    private void writeBytesFoot(
            final String s, final Charset charset) throws IOException {
        ByteBuffer b = encode(charset, s);
        writeBytesFoot(b);
    }
    
    private void writeBytesHead(
            final String s) throws IOException {
        ByteBuffer b = encode(getCharset(), s);
        writeBytesHead(b);
    }
    
    
    private void writeFieldHead(
            final MinimalField field) throws IOException {
        writeBytesHead(field.getName());
        writeBytesHead(FIELD_SEP);
        writeBytesHead(field.getBody());
        writeBytesHead(CR_LF);
    }

    private void writeFieldHead(
            final MinimalField field, final Charset charset) throws IOException {
        writeBytesHead(field.getName(), charset);
        writeBytesHead(FIELD_SEP);
        writeBytesHead(field.getBody(), charset);
        writeBytesHead(CR_LF);
    }
    
    private  void doWriteToByBuffer(
            final HttpMultipartMode mode,
            final  ContentEncoder encoder,
            final  IOControl ioControl,
            boolean writeContent) throws IOException {
        if(!bufferAvailable && !bufferAvailable() && !encoder.isCompleted())
            doWriteToBuffer(this.mode);
        
        for (FormBodyPart part: this.parts.subList(context.partIdx,context.partTot)) {
            
            readHead(encoder);
            if (writeContent) {
                produceContent(encoder,ioControl,part.getBody());
                if(notCompletedPart(encoder))
                    return;
            }
            readFoot(encoder);
        }
        if(notCompletedAllPart(encoder))return;
        readEnd(encoder);
    }
    
    
    private long doWriteToBuffer(
            final HttpMultipartMode mode) throws IOException{
            
        if(bufferAvailable && bufferAvailable())
            return bufferLength();
        
        
        ByteBuffer boundary = encode(this.charset, getBoundary());
        
        for (FormBodyPart part: this.parts) {
            bufferCursor();
            writeBytesHead(TWO_DASHES);
            writeBytesHead(boundary);
            writeBytesHead(CR_LF);
            Header header = part.getHeader();

            switch (mode) {
            case STRICT:
                for (MinimalField field: header) {
                    writeFieldHead(field);
                }
                break;
            case BROWSER_COMPATIBLE:
                // Only write Content-Disposition
                // Use content charset
                MinimalField cd = part.getHeader().getField(MIME.CONTENT_DISPOSITION);
                writeFieldHead(cd, this.charset);
                String filename = part.getBody().getFilename();
                if (filename != null) {
                    MinimalField ct = part.getHeader().getField(MIME.CONTENT_TYPE);
                    writeFieldHead(ct, this.charset);
                }
                break;
            }
            writeBytesHead(CR_LF);
//write content
            writeBytesFoot(CR_LF);
            context.partIdx++;
        }
        
        bufferEnd();
        
        context.partTot = this.parts.size();
        context.partIdx = 0;
        bufferAvailable = true;
        return bufferLength();
    }
    
    public NHttpMultipart(final String subType, final Charset charset, final String boundary, HttpMultipartMode mode) {
        super(subType,charset,boundary,mode);
    }

    public NHttpMultipart(final String subType, final Charset charset, final String boundary) {
        this(subType, charset, boundary, HttpMultipartMode.STRICT);
    }

    public NHttpMultipart(final String subType, final String boundary) {
        this(subType, null, boundary);
    }

    public long getTotalLength() {
        long contentLen = 0;
        for (FormBodyPart part: this.parts) {
            ContentBody body = part.getBody();
            long len = body.getContentLength();
            if (len >= 0) {
                contentLen += len;
            } else {
                return -1;
            }
        }
        
        try {
            return contentLen + doWriteToBuffer(this.mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentLen;
    }
    
    public void produceContent(final ContentEncoder encoder, final IOControl ioControl) throws IOException {
        if(this.bodyTransferWay==Mode.NCM)
            doWriteToNativeHead(this.mode, encoder,ioControl, true);
        else {
            doWriteToByBuffer(this.mode, encoder,ioControl, true);
        }
    }
    
    public void addBodyPart(final FormBodyPart part) {
        if (part == null) {
            return;
        }
        this.parts.add(part);
        super.addBodyPart(part);
    }

}
