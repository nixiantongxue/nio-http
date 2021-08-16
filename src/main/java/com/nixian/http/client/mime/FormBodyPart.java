package com.nixian.http.client.mime;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.util.Args;

public class FormBodyPart extends org.apache.http.entity.mime.FormBodyPart{

    public FormBodyPart(String name, ContentBody body) {
        super(name, body);
    }

    protected void generateContentType(final ContentBody body) {
        final ContentType contentType;
        if (body instanceof AbstractContentBody) {
            contentType = ((AbstractContentBody) body).getContentType();
        } else {
            contentType = null;
        }
        if (contentType != null) {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(Mimes.get(body.getMimeType())); // MimeType cannot be null
            if (body.getCharset() != null) { // charset may legitimately be null
                buffer.append("; charset=");
                buffer.append(body.getCharset());
            }
            addField(MIME.CONTENT_TYPE, buffer.toString());
        }
    }

}