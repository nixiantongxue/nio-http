package com.nixian.http.client.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.entity.ProducingNHttpEntity;
import org.apache.http.protocol.HTTP;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class NMultipartEntity implements ProducingNHttpEntity,HttpAsyncContentProducer,
                                            SupportableHttpEntity,HttpEntity{

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private final static char[] MULTIPART_CHARS =
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();

    private final NHttpMultipart multipart;
    private final Header contentType;

    // @GuardedBy("dirty") // we always read dirty before accessing length
    private long length;
    private volatile boolean dirty; // used to decide whether to recalculate length

    private ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
    /**
     * Creates an instance using the specified parameters
     * @param mode the mode to use, may be {@code null}, in which case {@link HttpMultipartMode#STRICT} is used
     * @param boundary the boundary string, may be {@code null}, in which case {@link #generateBoundary()} is invoked to create the string
     * @param charset the character set to use, may be {@code null}, in which case {@link MIME#DEFAULT_CHARSET} - i.e. US-ASCII - is used.
     */
    public NMultipartEntity(
            HttpMultipartMode mode,
            String boundary,
            Charset charset) {
        super();
        if (boundary == null) {
            boundary = generateBoundary();
        }
        if (mode == null) {
            mode = HttpMultipartMode.STRICT;
        }
        this.multipart = new NHttpMultipart("form-data", charset, boundary, mode);
        this.contentType = new BasicHeader(
                HTTP.CONTENT_TYPE,
                generateContentType(boundary, charset));
        this.dirty = true;
    }

    /**
     * Creates an instance using the specified {@link HttpMultipartMode} mode.
     * Boundary and charset are set to {@code null}.
     * @param mode the desired mode
     */
    public NMultipartEntity(final HttpMultipartMode mode) {
        this(mode, null, null);
    }

    /**
     * Creates an instance using mode {@link HttpMultipartMode#STRICT}
     */
    public NMultipartEntity() {
        this(HttpMultipartMode.STRICT, null, null);
    }

    protected String generateContentType(
            final String boundary,
            final Charset charset) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("multipart/form-data; boundary=");
        buffer.append(boundary);
        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset.name());
        }
        return buffer.toString();
    }

    protected String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    public void addPart(final FormBodyPart bodyPart) {
        this.multipart.addBodyPart(bodyPart);
        this.dirty = true;
    }

    public void addPart(final String name, final ContentBody contentBody) {
        addPart(new com.nixian.http.client.mime.FormBodyPart(name, contentBody));
    }

    public boolean isRepeatable() {
        for (FormBodyPart part: this.multipart.getBodyParts()) {
            ContentBody body = part.getBody();
            if (body.getContentLength() < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isChunked() {
        return !isRepeatable();
    }

    public boolean isStreaming() {
        return !isRepeatable();
    }

    public long getContentLength() {
        if (this.dirty) {
            this.length = this.multipart.getTotalLength();
            this.dirty = false;
        }
        return this.length;
    }

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return null;
    }

    public void consumeContent()
        throws IOException, UnsupportedOperationException{
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        writeTo(this.baoStream);
        HttpEntity nByteEntity = new NByteArrayEntity(baoStream.toByteArray(), ContentType.MULTIPART_FORM_DATA);
        return nByteEntity.getContent();
    }

    @Override
    public void close() throws IOException {
        this.multipart.close();
    }

    /* 
     *   Multipart 基于N-IO  优化方法
                *   内置两种模式对比:
     *   1. only extra content 的缓存
     *   2. extra cotnent + body transfer new way 
     *   
     * @see org.apache.http.nio.entity.HttpAsyncContentProducer#produceContent(org.apache.http.nio.ContentEncoder, org.apache.http.nio.IOControl)
     */
    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        this.multipart.produceContent(encoder, ioctrl);
    }

    /* 
     * Multipart 基于O-IO调用方式  兼容直写
     */
    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        this.multipart.doWriteTo(outstream);
    }
    
    public void finish() throws IOException {
        close();
    }
    
    @Override
    public boolean supportWriteTo() {
        return true;
    }
    
    @Override
    public boolean isWindowsFile() {
        return false;
    }


}
