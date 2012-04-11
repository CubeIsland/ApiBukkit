package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * This class contains all the information of the API request. It is only used
 * to pass the information the to executing action, nothing more.
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public final class ApiRequest
{
    private static final String AUTHKEY_PARAM_NAME = "authkey";

    private final InetSocketAddress remoteAddress;
    private final RequestMethod method;
    private final String uri;
    private final String path;
    private final String queryString;
    private final String format;
    private final String authenticationKey;
    private final String controller;
    private final String action;
    private final String userAgent;

    public final Parameters params;
    public final Map<String, String> headers;

    /**
     * Initializes the ApiRequest with an Server instance
     */
    public ApiRequest(final InetSocketAddress remoteAddress, final HttpRequest request)
    {
        int offset;
        this.remoteAddress = remoteAddress;

        String tempUri = request.getUri();
        // we process a maximum of 1024 characters
        this.uri = tempUri.substring(0, Math.min(1024, tempUri.length())).replace(';', '&');

        String tempPath;
        if ((offset = this.uri.indexOf("?")) < 0)
        {
            tempPath = this.uri;
            this.queryString = null;
        }
        else
        {
            tempPath = this.uri.substring(0, offset);
            this.queryString = this.uri.substring(offset + 1);
        }

        if ((offset = tempPath.lastIndexOf(".")) < 0)
        {
            this.format = null;
        }
        else
        {
            this.format = tempPath.substring(offset + 1);
            tempPath = tempPath.substring(0, offset);
        }

        this.path = tempPath;

        Map<String, String> tempParams = new HashMap<String, String>();
        parseQueryString(queryString, tempParams);
        final ChannelBuffer content = request.getContent();
        if (content.readable())
        {
            parseQueryString(content.toString(), tempParams);
        }

        this.authenticationKey = tempParams.get(AUTHKEY_PARAM_NAME);
        tempParams.remove(AUTHKEY_PARAM_NAME);

        this.params = new Parameters(tempParams);

        RequestMethod tempMethod = RequestMethod.getByName(params.get("http_method"));
        if (tempMethod == null)
        {
            tempMethod = RequestMethod.getByName(request.getMethod().getName());
        }
        if (tempMethod == null)
        {
            tempMethod = RequestMethod.GET;
        }
        this.method = tempMethod;

        final Map<String, String> tempHeaders = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : request.getHeaders())
        {
            tempHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        this.headers = Collections.unmodifiableMap(tempHeaders);
        this.userAgent = this.headers.get("user-agent");
        
        String route = this.path.substring(1);
        String[] routeSegments = explode("/", route);
        if (routeSegments.length >= 2)
        {
            this.controller = routeSegments[0];
            this.action = routeSegments[1];
        }
        else if (routeSegments.length >= 1)
        {
            this.controller = routeSegments[0];
            this.action = null;
        }
        else
        {
            this.controller = null;
            this.action = null;
        }
    }

    public RequestMethod getMethod()
    {
        return this.method;
    }

    public String getUri()
    {
        return this.uri;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getQueryString()
    {
        return this.queryString;
    }

    public String getFormat()
    {
        return this.format;
    }

    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    public InetSocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }

    public String getController()
    {
        return this.controller;
    }

    public String getAction()
    {
        return this.action;
    }

    public String getUserAgent()
    {
        return this.userAgent;
    }

    private static void parseQueryString(String queryString, Map<String, String> params)
    {
        if (queryString.length() > 0)
        {
            String token;
            int offset;
            StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
            while (tokenizer.hasMoreTokens())
            {
                token = tokenizer.nextToken();
                if ((offset = token.indexOf("=")) > 0)
                {
                    params.put(urlDecode(token.substring(0, offset)), urlDecode(token.substring(offset + 1)));
                }
                else
                {
                    params.put(urlDecode(token), null);
                }
            }
        }
    }

    /**
     * Decodes the percent encoding scheme. <br/> For example:
     * "an+example%20string" -> "an example string"
     */
    private static String urlDecode(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (Exception e)
        {
            return string;
        }
    }

    private static String[] explode(String delim, String string)
    {
        int pos, offset = 0, delimLen = delim.length();
        List<String> tokens = new ArrayList<String>();

        while ((pos = string.indexOf(delim, offset)) > -1)
        {
            tokens.add(string.substring(offset, pos));
            offset = pos + delimLen;
        }
        tokens.add(string.substring(offset));

        return tokens.toArray(new String[tokens.size()]);
    }
}
