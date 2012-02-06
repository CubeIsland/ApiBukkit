package de.codeinfection.quickwango.ApiBukkit.Server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * HTTP response.
 * Return one of these from serve().
 */
public class Response
{
    /**
     * HTTP status code after processing, e.g. "200 OK", HTTP_OK
     */
    public Status status;

    /**
     * MIME type of content, e.g. "text/html"
     */
    public MimeType mimeType;

    /**
     * Data of the response, may be null.
     */
    public InputStream data;

    /**
     * Headers for the HTTP response. Use addHeader()
     * to add lines.
     */
    public HashMap<String, String> header = new HashMap<String, String>();

    /**
     * Default constructor: response = HTTP_OK, data = mime = 'null'
     */
    public Response()
    {
        this.status = Status.OK;
    }

    /**
     * Basic constructor.
     */
    public Response(Status status, MimeType mimeType, InputStream data)
    {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
    }

    /**
     * Convenience method that makes an InputStream out of
     * given text.
     */
    public Response(Status status, MimeType mimeType, String txt)
    {
        this.status = status;
        this.mimeType = mimeType;
        try
        {
            this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value)
    {
        header.put(name, value);
    }
}

