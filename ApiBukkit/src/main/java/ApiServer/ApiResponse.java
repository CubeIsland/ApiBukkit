package ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.ApiResponseFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CodeInfection
 */
public class ApiResponse
{
    private final Map<String, String> headers;

    private ApiResponseFormat responseFormat;

    private Object content;

    public ApiResponse(ApiResponseFormat format)
    {
        this.headers = new HashMap<String, String>();
        this.content = null;
    }

    public String getHeader(String name)
    {
        return this.headers.get(name.toLowerCase());
    }

    public ApiResponse setHeader(String name, String value)
    {
        this.headers.put(name, value);
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
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            this.headers.put(header.getKey().toLowerCase(), header.getValue());
        }
        return this;
    }

    public ApiResponseFormat getResponseFormat()
    {
        return this.responseFormat;
    }

    public ApiResponse setResponseFormat(ApiResponseFormat format)
    {
        this.responseFormat = format;
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
