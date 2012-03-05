package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import de.codeinfection.quickwango.ApiBukkit.ApiLogLevel;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.bukkit.Bukkit;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 *
 * @author CodeInfection
 */
public class ApiServerHandler extends SimpleChannelUpstreamHandler
{
    private final static ApiServer server = ApiServer.getInstance();
    private final static ApiManager manager = ApiManager.getInstance();

    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent message) throws Exception
    {
        HttpResponse response = this.processRequest(message, (HttpRequest)message.getMessage());
        if (response != null)
        {
            message.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        }
        else
        {
            message.getChannel().close();
        }
    }

    private HttpResponse processRequest(final MessageEvent message, final HttpRequest request)
    {
        final InetSocketAddress remoteAddress = (InetSocketAddress)message.getRemoteAddress();

        if (manager.isBlacklisted(remoteAddress) || !manager.isWhitelisted(remoteAddress))
        {
            return null;
        }

        final HttpMethod method = request.getMethod();

        if (method == HttpMethod.GET || method == HttpMethod.POST)
        {
            final String requestUri = request.getUri();
            List<String> stringParts = explode("?", requestUri);
            String requestPath;
            String queryString = "";
            if (stringParts.size() > 1)
            {
                requestPath = stringParts.get(0);
                queryString = stringParts.get(1);
            }
            else
            {
                requestPath = requestUri;
            }

            /**
             * @TODO get rid of the direct bukkit dependency
             */
            ApiRequest apiRequest = new ApiRequest(Bukkit.getServer());
            apiRequest.SERVER.put("REQUEST_URI", requestPath);
            
            final Map<String, String> headers = new HashMap<String, String>();
            for (Entry<String, String> entry :  request.getHeaders())
            {
                headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            parseQueryString(queryString, apiRequest.params);

            ChannelBuffer content = request.getContent();
            if (content.readable())
            {
                parseQueryString(content.toString(), apiRequest.params);
            }


            apiRequest.SERVER.put("REQUEST_PATH", requestUri);
            apiRequest.SERVER.put("REQUEST_METHOD", method);
            apiRequest.SERVER.put("REMOTE_ADDR", remoteAddress);
            ApiBukkit.log(String.format("'%s' requested '%s'", remoteAddress.getAddress().getHostAddress(), requestPath), ApiLogLevel.INFO);
            String useragent = apiRequest.headers.get("apibukkit-useragent");
            if (useragent != null)
            {
                apiRequest.SERVER.put("HTTP_USER_AGENT", useragent);
                ApiBukkit.log("Useragent: " + useragent, ApiLogLevel.INFO);
            }

            String route = requestPath.substring(1);
            if (route.length() == 0)
            {
                ApiBukkit.error("Invalid route requested!");
                return this.toResponse(ApiError.INVALID_PATH);
            }
            stringParts = explode("/", route);

            String controllerName;
            String actionName = null;
            if (stringParts.size() >= 2)
            {
                controllerName = stringParts.get(0);
                actionName = stringParts.get(1);
            }
            else if (stringParts.size() >= 1)
            {
                controllerName = stringParts.get(0);
            }
            else
            {
                ApiBukkit.error("Invalid route requested!");
                return this.toResponse(ApiError.INVALID_PATH);
            }
            
            ApiResponseSerializer serializer = null, actionSerializer;
            String formatParam = apiRequest.params.getString("format");
            if (formatParam != null)
            {
                serializer = manager.getSerializer(formatParam);
            }
            if (serializer == null)
            {
                serializer = manager.getDefaultSerializer();
            }

            debug("Controllername: " + controllerName);
            debug("Actionname: " + actionName);

            ApiController controller = manager.getController(controllerName);
            ApiResponse apiResponse = new ApiResponse(serializer);
            if (controller != null)
            {
                ApiBukkit.debug("Selected controller '" + controller.getClass().getSimpleName() + "'");

                try
                {
                    final String AUTHKEY_PARAM_NAME = "authkey";
                    String authKey = null;
                    if (apiRequest.params.containsKey(AUTHKEY_PARAM_NAME))
                    {
                        authKey = apiRequest.params.getString(AUTHKEY_PARAM_NAME);
                    }
                    apiRequest.params.remove(AUTHKEY_PARAM_NAME);

                    ApiAction action = controller.getAction(actionName);
                    if (manager.isActionDisabled(controllerName, actionName))
                    {
                        ApiBukkit.error("Requested action is disabled!");
                        return this.toResponse(ApiError.ACTION_DISABLED);
                    }
                    if (action != null)
                    {
                        this.authorized(authKey, action);

                        for (String param : action.getParameters())
                        {
                            if (!apiRequest.params.containsKey(param))
                            {
                                ApiBukkit.error("Request had to few arguments!");
                                return this.toResponse(ApiError.MISSING_PARAMETERS);
                            }
                        }

                        actionSerializer = manager.getSerializer(action.getSerializer());
                        if (actionSerializer != null)
                        {
                            apiResponse.setSerializer(actionSerializer);
                        }

                        ApiBukkit.debug("Running action '" + actionName + "'");
                        action.execute(apiRequest, apiResponse);
                    }
                    else
                    {
                        this.authorized(authKey, controller);

                        actionSerializer = manager.getSerializer(controller.getSerializer());
                        if (actionSerializer != null)
                        {
                            apiResponse.setSerializer(actionSerializer);
                        }

                        ApiBukkit.debug("Runnung default action");
                        controller.defaultAction(actionName, apiRequest, apiResponse);
                    }
                }
                catch (UnauthorizedRequestException e)
                {
                    ApiBukkit.error("Wrong authentication key!");
                    return this.toResponse(ApiError.AUTHENTICATION_FAILURE);
                }
                catch (ApiRequestException e)
                {
                    ApiBukkit.error("ControllerException: " + e.getMessage());
                    return this.toResponse(ApiError.REQUEST_EXCEPTION, e.getErrCode());
                }
                catch (UnsupportedOperationException e)
                {
                    ApiBukkit.error("action not implemented");
                    return this.toResponse(ApiError.ACTION_NOT_IMPLEMENTED);
                }
                catch (Throwable t)
                {
                    ApiBukkit.logException(t);
                    return this.toResponse(ApiError.UNKNONW_ERROR);
                }
            }
            else
            {
                ApiBukkit.error("Controller not found!");
                return this.toResponse(ApiError.CONTROLLER_NOT_FOUND);
            }

            return this.toResponse(apiResponse);
        }
        else
        {
            return this.toResponse(ApiError.METHOD_NOT_ALLOWED);
        }
    }

    private static void authorized(String key, ApiController controller)
    {
        ApiBukkit.debug("Authkey: " + key);
        if (controller.isAuthNeeded() && !server.getAuthenticationKey().equals(key))
        {
            throw new UnauthorizedRequestException();
        }
    }

    private static void authorized(String key, ApiAction action)
    {
        ApiBukkit.debug("Authkey: " + key);
        if (action.isAuthNeeded() && !server.getAuthenticationKey().equals(key))
        {
            throw new UnauthorizedRequestException();
        }
    }

    private HttpResponse toResponse(ApiResponse response)
    {
        final Object content = response.getContent();
        HttpResponseStatus status = (content == null ? HttpResponseStatus.NO_CONTENT : HttpResponseStatus.OK);
        
        return this.toResponse(status, response.getHeaders(), response.getSerializer(), content);
    }

    private HttpResponse toResponse(HttpVersion version, HttpResponseStatus status, Map<String, String> headers, final String content)
    {
        HttpResponse response = new DefaultHttpResponse(version, status);
        response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        for (Entry<String, String> header : headers.entrySet())
        {
            response.setHeader(header.getKey(), header.getValue());
        }

        return response;
    }

    private HttpResponse toResponse(HttpResponseStatus status, Map<String, String> headers, String content)
    {
        return this.toResponse(HttpVersion.HTTP_1_0, status, headers, content);
    }

    private HttpResponse toResponse(HttpResponseStatus status, Map<String, String> headers, ApiResponseSerializer serializer, Object o)
    {
        headers.put("content-type", serializer.getMime().toString());
        
        return this.toResponse(status, headers, serializer.serialize(o));
    }

    private HttpResponse toResponse(ApiError error)
    {
        return this.toResponse(error, null);
    }

    private HttpResponse toResponse(ApiError error, Integer minor)
    {
        Object o;

        if (minor == null)
        {
            o = error.getCode();
        }
        else
        {
            o = new Object[] {error.getCode(), minor};
        }
        
        return this.toResponse(error.getRepsonseStatus(), new HashMap<String, String>(1), manager.getSerializer("plain"), o);
    }


    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     */
    private static String urlDecode(String string)
    {
        try
        {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (Exception e)
        {
            return string;
        }
    }

    /**
     * parses a querystring
     */
    private static void parseQueryString(String queryString, Parameters params)
    {
        parseQueryString(queryString, params, "&");
    }

    /**
     * parses a querystring
     */
    private static void parseQueryString(String queryString, Parameters params, String pairDelim)
    {
        parseQueryString(queryString, params, pairDelim, "=");
    }

    /**
     * parses a querystring
     */
    private static void parseQueryString(String queryString, Parameters params, String pairDelim, String valueDelim)
    {
        if (queryString == null || queryString.length() == 0)
        {
            return;
        }

        int queryDelimIndex = queryString.indexOf("?");
        if (queryDelimIndex > -1)
        {
            queryString = queryString.substring(queryDelimIndex + 1);
        }

        StringTokenizer tokenizer = new StringTokenizer(queryString, pairDelim);
        while (tokenizer.hasMoreTokens())
        {
            parseKeyValuePair(tokenizer.nextToken(), params, valueDelim);
        }
    }

    private static void parseKeyValuePair(String keyValuePair, Parameters params, String valueDelim)
    {
        int delimPosition = keyValuePair.indexOf(valueDelim);
        if (delimPosition > -1)
        {
            String key = keyValuePair.substring(0, delimPosition);
            String value = urlDecode(keyValuePair.substring(delimPosition + 1));

            params.put(parseKey(key), value);
        }
        else
        {
            List<String> path = parseKey(keyValuePair);
            if (!params.containsKey(path))
            {
                params.put(path, null);
            }
        }
    }

    private static List<String> parseKey(String key)
    {
        List<String> path = new ArrayList<String>();
        int firstOpenBracketPosition = key.indexOf("[");
        if (firstOpenBracketPosition > -1)
        {
            String indicesString = key.substring(firstOpenBracketPosition);
            int lastCloseBracketPosition = indicesString.lastIndexOf("]");
            if (lastCloseBracketPosition == indicesString.length() - 1)
            {
                key = urlDecode(key.substring(0, firstOpenBracketPosition));
                String delimitedIndices = indicesString.substring(1, lastCloseBracketPosition);

                path.add(key);
                for (String token : explode("][", delimitedIndices))
                {
                    path.add(urlDecode(token));
                }
                return path;
            }
        }

        path.add(urlDecode(key));
        return path;
    }

    private static List<String> explode(String delim, String string)
    {
        int pos, offset = 0, delimLen = delim.length();
        List<String> tokens = new ArrayList<String>();

        while ((pos = string.indexOf(delim, offset)) > -1)
        {
            tokens.add(string.substring(offset, pos));
            offset = pos + delimLen;
        }
        tokens.add(string.substring(offset));

        return tokens;
    }
}
