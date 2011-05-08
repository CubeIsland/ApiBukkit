package de.codeinfection.quickwango.ApiBukkit.Net;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import org.bukkit.util.config.Configuration;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.*;
import de.codeinfection.quickwango.ApiBukkit.Net.NanoHTTPD.Response;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController.RequestAction;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;

public class ApiBukkitServer extends NanoHTTPD
{
    protected ApiBukkit plugin;
    protected String APIPassword;
    protected final static ConcurrentHashMap<String, IResponseFormat> responseFormats;
    static
    {
        responseFormats = new ConcurrentHashMap<String, IResponseFormat>();
    }
    protected final ConcurrentHashMap<String, AbstractRequestController> requestControllers;
    protected static String defaultResponseFormat = "plain";

    public ApiBukkitServer(ApiBukkit plugin, Configuration config) throws IOException
    {
        super(config.getInt("Configuration.webServerPort", 6561));
        this.APIPassword = config.getString("Configuration.APIPassword", "changeMe");
        this.plugin = plugin;
        
        responseFormats.put("plain", new PlainFormat());
        responseFormats.put("json", new JsonFormat());
        responseFormats.put("xml", new XMLFormat());
        
        requestControllers = new ConcurrentHashMap<String, AbstractRequestController>();
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties params, Properties files)
    {
        params.put("__REQUEST_PATH__", uri);
        ApiBukkit.log(uri + " was requested...");
        uri = uri.substring(1);
        if (uri.length() == 0)
        {
            ApiBukkit.error(ApiError.INVALID_PATH.getMessage());
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, this.error(ApiError.INVALID_PATH));
        }
        String[] pathParts = uri.split("/");
        
        String controllerName = null;
        String action = null;
        if (pathParts.length >= 1)
        {
            controllerName = pathParts[0];
        }
        if (pathParts.length >= 2)
        {
            action = pathParts[1];
        }
        if (pathParts.length < 1 || controllerName == null)
        {
            ApiBukkit.error(ApiError.INVALID_PATH.getMessage());
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, this.error(ApiError.INVALID_PATH));
        }
        
        Object response = null;
        if (requestControllers.containsKey(pathParts[0]))
        {
            try
            {
                ApiBukkit.debug("Selecting controller '" + pathParts[0] + "'");
                AbstractRequestController controller = requestControllers.get(pathParts[0]);
                String password = params.getProperty("password", "");
                params.remove("password");
                
                RequestAction requestAction = controller.getAction(action);
                if (requestAction != null)
                {
                    Boolean controllerAuthNeeded = controller.isAuthNeeded();
                    Boolean actionAuthNeeded = requestAction.isAuthNeeded();
                    if (actionAuthNeeded == null)
                    {
                        actionAuthNeeded = controllerAuthNeeded;
                    }
                    if (actionAuthNeeded && !password.equals(this.APIPassword))
                    {
                        ApiBukkit.error(ApiError.WRONG_API_PASSWORD.getMessage());
                        return new Response(HTTP_UNAUTHORIZED, MIME_PLAINTEXT, this.error(ApiError.WRONG_API_PASSWORD));
                    }
                    ApiBukkit.debug("Running action '" + action + "'");
                    response = requestAction.run(params, this.plugin.getServer());
                }
                else
                {
                    ApiBukkit.debug("Runnung default action");
                    response = controller.defaultAction(action, params, this.plugin.getServer());
                }
            }
            catch (RequestException e)
            {
                ApiBukkit.error("ControllerException: " + e.getMessage());
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, this.error(ApiError.CONTROLLER_EXCEPTION.setMessage(e.getMessage())));
            }
            catch (NotImplementedException e)
            {
                ApiBukkit.error("action not implemented");
                return new Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, this.error(ApiError.ACTION_NOT_IMPLEMENTED));
            }
            catch (Throwable t)
            {
                ApiBukkit.logException(t);
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, this.error(ApiError.UNKNONW_ERROR));
            }
        }
        else
        {
            ApiBukkit.error(ApiError.CONTROLLER_NOT_FOUND.getMessage());
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, this.error(ApiError.CONTROLLER_NOT_FOUND));
        }
        
        if (response != null)
        {
            String formatProperty = params.getProperty("format", defaultResponseFormat);
            IResponseFormat responseFormat = getResponseFormat(formatProperty);
            
            ApiBukkit.debug("Responding normally: HTTP 200");
            return new Response(HTTP_OK, responseFormat.getMime(), responseFormat.format(response));
        }
        else
        {
            ApiBukkit.debug("Responding without content: HTTP 204");
            return new Response(HTTP_NOCONTENT, MIME_PLAINTEXT, "");
        }
    }
    
    public static IResponseFormat getResponseFormat(String name)
    {
        IResponseFormat format = null;
        if (responseFormats.containsKey(name))
        {
            format = responseFormats.get(name);
        }
        else if (responseFormats.containsKey(defaultResponseFormat))
        {
            format = responseFormats.get(defaultResponseFormat);
        }
        else
        {
            format = responseFormats.get("plain");
        }
        
        return format;
    }
    
    public static void addResponseFormat(String name, IResponseFormat format)
    {
        responseFormats.put(name, format);
    }
    
    public static void removeResponseFormat(String name)
    {
        responseFormats.remove(name);
    }
    
    public static void setDefaultResponseFormat(String format)
    {
        if (responseFormats.containsKey(format))
        {
            defaultResponseFormat = format;
        }
    }
    
    public static String getDefaultResponseFormat()
    {
        return defaultResponseFormat;
    }
    
    public void setRequestController(String name, AbstractRequestController controller)
    {
        if (controller != null)
        {
            this.requestControllers.put(name, controller);
        }
    }
    
    public boolean setControllerAlias(String alias, String controller)
    {
        if (!this.requestControllers.containsKey(alias) && this.requestControllers.containsKey(controller))
        {
            this.requestControllers.put(alias, this.requestControllers.get(controller));
            return true;
        }
        return false;
    }
    
    public void removeRequestController(String name)
    {
        this.requestControllers.remove(name);
    }
    
    protected String error(ApiError error)
    {
        IResponseFormat formater = getResponseFormat("plain");
        return formater.format(error.asList());
    }
    
    public Collection<AbstractRequestController> getAllRequestControllers()
    {
        return this.requestControllers.values();
    }
}
