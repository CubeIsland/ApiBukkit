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
        ApiBukkit.log(uri + " was requested...");
        uri = uri.substring(1);
        if (uri.length() == 0)
        {
            ApiBukkit.error("Fail: Invalid path");
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Invalid path");
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
            ApiBukkit.error("Invalid path");
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Invalid path");
        }
        
        Object response = null;
        if (requestControllers.containsKey(pathParts[0]))
        {
            try
            {
                AbstractRequestController controller = requestControllers.get(pathParts[0]);
                if (controller.isAuthNeeded() && !params.getProperty("password", "").equals(this.APIPassword))
                {
                    ApiBukkit.error("Wrong API password");
                    return new Response(HTTP_UNAUTHORIZED, MIME_PLAINTEXT, "Wrong API password");
                }
                params.remove("password");
                
                RequestAction requestAction = controller.getAction(action);
                if (requestAction != null)
                {
                    response = requestAction.run(params, this.plugin.getServer());
                }
                else
                {
                    response = controller.defaultAction(action, params, this.plugin.getServer());
                }
            }
            catch (RequestException e)
            {
                ApiBukkit.logException(e);
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getMessage());
            }
            catch (NotImplementedException e)
            {
                ApiBukkit.logException(e);
                return new Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, "Not implemented");
            }
            catch (Throwable t)
            {
                ApiBukkit.logException(t);
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "Unknown error occurred while processing the controller!\nPlease notify the server adminstrator of this error.");
            }
        }
        else
        {
            ApiBukkit.error("Controller not found");
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Controller not found");
        }
        
        if (response != null)
        {
            String formatProperty = params.getProperty("format", defaultResponseFormat);
            IResponseFormat responseFormat = null;
            if (responseFormats.containsKey(formatProperty))
            {
                responseFormat = responseFormats.get(formatProperty);
            }
            else if (responseFormats.containsKey(defaultResponseFormat))
            {
                responseFormat = responseFormats.get(defaultResponseFormat);
            }
            if (responseFormat != null)
            {
                ApiBukkit.debug("Responding normaly: HTTP 200");
                return new Response(HTTP_OK, responseFormat.getMime(), responseFormat.format(response));
            }
            else
            {
                ApiBukkit.error("No valid response format found");
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "No valid response format found!");
            }
        }
        else
        {
            ApiBukkit.debug("Responding without content: HTTP 204");
            return new Response(HTTP_NOCONTENT, MIME_PLAINTEXT, "");
        }
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
        synchronized (this.requestControllers)
        {
            this.requestControllers.put(name, controller);
        }
    }
    
    public boolean setControllerAlias(String alias, String controller)
    {
        synchronized (this.requestControllers)
        {
            if (!this.requestControllers.containsKey(alias) && this.requestControllers.containsKey(controller))
            {
                this.requestControllers.put(alias, this.requestControllers.get(controller));
                return true;
            }
            return false;
        }
    }
    
    public void removeRequestController(String name)
    {
        synchronized (this.requestControllers)
        {
            this.requestControllers.remove(name);
        }
    }
    
    public Collection<AbstractRequestController> getAllRequestControllers()
    {
        synchronized (this.requestControllers)
        {
            return this.requestControllers.values();
        }
    }
}
