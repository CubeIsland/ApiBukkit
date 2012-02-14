package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CodeInfection
 */
public final class ApiResponse
{
    private final Map<String, String> headers;
    private ApiResponseSerializer serializer;
    private Object content;

    public ApiResponse(ApiResponseSerializer format)
    {
        this.headers = new HashMap<String, String>();
        this.content = null;
    }

    public String getHeader(String name)
    {
        if (name != null)
        {
            return this.headers.get(name.toLowerCase());
        }
        return null;
    }

    public ApiResponse setHeader(String name, String value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name must not be null!");
        }
        if (value == null)
        {
            this.headers.remove(name);
        }
        else
        {
            this.headers.put(name, value);
        }
        return this;
    }

    public Map<String, String> getHeaders()
    {
        return new HashMap<String, String>(this.headers);
    }

    public ApiResponse clearHeaders()
    {
        this.headers.clear();
        return this;
    }

    public ApiResponse setHeaders(Map<String, String> headers)
    {
        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                this.headers.put(header.getKey().toLowerCase(), header.getValue());
            }
        }
        return this;
    }

    public ApiResponseSerializer getSerializer()
    {
        return this.serializer;
    }

    public ApiResponse setSerializer(ApiResponseSerializer serializer)
    {
        if (serializer == null)
        {
            throw new IllegalArgumentException("serializer must not be null!");
        }
        this.serializer = serializer;
        return this;
    }

    public Object getContent()
    {
        return this.content;
    }

    public ApiResponse setContent(Object content)
    {
        this.content = content;
        return this;
    }
}
