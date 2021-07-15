/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHencoder WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.nixian.http.client.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

/**
 * HttpMultipart represents a collection of MIME multipart encoded content bodies. This class is
 * capable of operating either in the strict (RFC 822, RFC 2045, RFC 2046 compliant) or
 * the browser compatible modes.
 *
 *@author nixian
 * @since 4.0
 */
public class NativeHeadHttpMultipart extends NAbstractContentMultipart{

    private static Logger logger = Logger.getLogger(NativeHeadHttpMultipart.class.getName());
    
    private Mode bodyTransferWay = Mode.NCM;
    
    private HttpMultipart multipart = null;
    
    public NativeHeadHttpMultipart(final String subType, final Charset charset, final String boundary, HttpMultipartMode mode) {
        super(subType,charset,boundary,mode);
        this.multipart = new HttpMultipart(subType, charset, boundary, mode);
        this.bodyTransferWay = Mode.NCM;
    }
    
    void doWriteTo(final OutputStream out) throws IOException {
        long start2 = System.currentTimeMillis();
        this.multipart.writeTo(out);
        logger.config("Native Body send cost is:"+(System.currentTimeMillis()-start2));
    }
    
    void addBodyPart(final FormBodyPart part) {
        if (part == null) {
            return;
        }
        this.multipart.addBodyPart(part);
    }
    
    long writeBytes(
            final ByteBuffer b, ContentEncoder encoder) throws IOException {
        b.rewind();
        return encoder.write(b);
    }

    long writeBytes(
            final String s, final Charset charset, ContentEncoder encoder) throws IOException {
        ByteBuffer b = encode(charset, s);
        return writeBytes(b, encoder);
    }

    long writeBytes(
            final String s,  ContentEncoder encoder) throws IOException {
        ByteBuffer b = encode(MIME.DEFAULT_CHARSET, s);
        return writeBytes(b, encoder);
    }

    long writeField(
            final MinimalField field,  ContentEncoder encoder) throws IOException {
        long writed = 0l;
        writed += writeBytes(field.getName(), encoder);
        writed += writeBytes(FIELD_SEP, encoder);
        writed += writeBytes(field.getBody(), encoder);
        writed += writeBytes(CR_LF, encoder);
        return writed;
    }

    long writeField(
            final MinimalField field, final Charset charset,  ContentEncoder encoder) throws IOException {
        long writed = 0l;
        writed += writeBytes(field.getName(), charset, encoder);
        writed += writeBytes(FIELD_SEP, encoder);
        writed += writeBytes(field.getBody(), charset, encoder);
        writed += writeBytes(CR_LF, encoder);
        return writed;
    }
    
    public long getTotalLength() {
        return multipart.getTotalLength();
    }
    
    long doWriteToNativeHead(
            final HttpMultipartMode mode,
            final  ContentEncoder encoder,
            final  IOControl ioControl,
            boolean writeContent) throws IOException {

            long writed = 0l;
            
//            long start1 = System.currentTimeMillis();        
            ByteBuffer boundary = encode(this.charset, getBoundary());
            for (FormBodyPart part: this.parts) {
                writed += writeBytes(TWO_DASHES, encoder);
                writed += writeBytes(boundary, encoder);
                writed += writeBytes(CR_LF, encoder);
                Header header = part.getHeader();

                switch (mode) {
                case STRICT:
                    for (MinimalField field: header) {
                        writed += writeField(field, encoder);
                    }
                    break;
                case BROWSER_COMPATIBLE:
                    // Only write Content-Disposition
                    // Use content charset
                    MinimalField cd = part.getHeader().getField(MIME.CONTENT_DISPOSITION);
                    writed += writeField(cd, this.charset, encoder);
                    String filename = part.getBody().getFilename();
                    if (filename != null) {
                        MinimalField ct = part.getHeader().getField(MIME.CONTENT_TYPE);
                        writed += writeField(ct, this.charset, encoder);
                    }
                    break;
                }
                writed += writeBytes(CR_LF, encoder);
                if (writeContent) {
                    long start2 = System.currentTimeMillis();
                    produceContent(encoder,ioControl,part.getBody());
                    logger.config("Buffer Body【Native Head】 send cost is:"+(System.currentTimeMillis()-start2));
                }
                writed += writeBytes(CR_LF, encoder);
            }
            writed += writeBytes(TWO_DASHES, encoder);
            writed += writeBytes(boundary, encoder);
            writed += writeBytes(TWO_DASHES, encoder);
            writed += writeBytes(CR_LF, encoder);
            return writed;
    }
    
    public void produceContent(final ContentEncoder encoder, final IOControl ioControl) throws IOException {
        if(this.bodyTransferWay==Mode.NCM)
            doWriteToNativeHead(this.mode, encoder,ioControl, true);
    }

}
