package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiLogLevel;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
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
            final String uri = request.getUri();
            ApiRequest apiRequest = new ApiRequest(Bukkit.getServer());
            apiRequest.SERVER.put("REQUEST_URI", uri);
            QueryStringDecoder queryStringDecoder;
            final Map<String, String> headers = new HashMap<String, String>();


            queryStringDecoder = new QueryStringDecoder(request.getUri());
            apiRequest.GET.putAll(queryStringDecoder.getParameters());
            for (Entry<String, String> entry :  request.getHeaders())
            {
                headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            ChannelBuffer content = request.getContent();
            if (content.readable())
            {
                queryStringDecoder = new QueryStringDecoder(content.toString());
                apiRequest.POST.putAll(queryStringDecoder.getParameters());
            }

            apiRequest.REQUEST.putAll(apiRequest.GET);
            apiRequest.REQUEST.putAll(apiRequest.POST);


            apiRequest.SERVER.put("REQUEST_PATH", uri);
            apiRequest.SERVER.put("REQUEST_METHOD", method);
            apiRequest.SERVER.put("REMOTE_ADDR", remoteAddress);
            ApiBukkit.log(String.format("'%s' requested '%s'", remoteAddress.getAddress().getHostAddress(), uri), ApiLogLevel.INFO);
            String useragent = apiRequest.headers.get("apibukkit-useragent");
            if (useragent != null)
            {
                apiRequest.SERVER.put("HTTP_USER_AGENT", useragent);
                ApiBukkit.log("Useragent: " + useragent, ApiLogLevel.INFO);
            }

            String route = uri.substring(1);
            if (route.length() == 0)
            {
                ApiBukkit.error("Invalid route requested!");
                return this.toResponse(ApiError.INVALID_PATH);
            }
            String[] pathParts = uri.split("/");

            String controllerName = null;
            String actionName = null;
            if (pathParts.length >= 2)
            {
                actionName = pathParts[1];
            }
            else if (pathParts.length >= 1)
            {
                controllerName = pathParts[0];
            }
            else
            {
                ApiBukkit.error("Invalid route requested!");
                return this.toResponse(ApiError.INVALID_PATH);
            }
            
            ApiResponseSerializer serializer = null, actionSerializer;
            String formatParam = apiRequest.REQUEST.getString("format");
            if (formatParam != null)
            {
                serializer = manager.getSerializer(formatParam);
            }
            if (serializer == null)
            {
                serializer = manager.getDefaultSerializer();
            }

            ApiController controller = manager.getController(controllerName);
            ApiResponse apiResponse = new ApiResponse(serializer);
            if (controller != null)
            {
                ApiBukkit.debug("Selected controller '" + controller.getClass().getSimpleName() + "'");

                try
                {
                    final String AUTHKEY_PARAM_NAME = "authkey";
                    String authKey = null;
                    if (apiRequest.GET.containsKey(AUTHKEY_PARAM_NAME))
                    {
                        authKey = apiRequest.GET.get(AUTHKEY_PARAM_NAME).get(0);
                    }
                    else if (apiRequest.POST.containsKey(AUTHKEY_PARAM_NAME))
                    {
                        authKey = apiRequest.POST.get(AUTHKEY_PARAM_NAME).get(0);
                    }
                    apiRequest.GET.remove(AUTHKEY_PARAM_NAME);
                    apiRequest.POST.remove(AUTHKEY_PARAM_NAME);

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
                            if (!apiRequest.REQUEST.containsKey(param))
                            {
                                return this.toResponse(ApiError.MISSING_PARAMETERS);
                            }
                        }

                        actionSerializer = manager.getSerializer(action.getSerializer());
                        if (actionSerializer != null)
                        {
                            apiResponse.setSerializer(serializer);
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
                            apiResponse.setSerializer(serializer);
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
        
        return this.toResponse(status, response.getSerializer(), content);
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

    private HttpResponse toResponse(HttpResponseStatus status, ApiResponseSerializer serializer, Object o)
    {
        HashMap<String, String> headers = new HashMap<String, String>(1);
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
        
        return this.toResponse(error.getRepsonseStatus(), manager.getSerializer("plain"), o);
    }
}
