package org.kokakiwi.apicraft.net;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import org.bukkit.util.config.Configuration;

import org.kokakiwi.apicraft.*;
import org.kokakiwi.apicraft.utils.ResponseFormat.*;
import org.kokakiwi.apicraft.net.NanoHTTPD.Response;
import org.kokakiwi.apicraft.net.Request.AbstractRequestController;
import org.kokakiwi.apicraft.net.Request.RequestException;

public class ApiWebServer extends NanoHTTPD
{
    protected ApiCraft plugin;
    protected String APIPassword;
    protected static HashMap<String, IResponseFormat> responseFormats;
    protected HashMap<String, AbstractRequestController> requestControllers;
    protected static String defaultResponseFormat = "plain";

    public ApiWebServer(ApiCraft plugin, Configuration config) throws IOException
    {
        super(config.getInt("Configuration.webServerPort", 6561));
        this.APIPassword = config.getString("Configuration.APIPassword", "changeMe");
        this.plugin = plugin;
        
        responseFormats = new HashMap<String, IResponseFormat>();
        responseFormats.put("plain", new PlainFormat());
        responseFormats.put("json", new JsonFormat());
        responseFormats.put("xml", new XMLFormat());
        
        requestControllers = new HashMap<String, AbstractRequestController>();
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties params, Properties files)
    {   
        System.out.println(uri + " was requested...");
        uri = uri.substring(1);
        if (uri.length() == 0)
        {
            System.out.println("Fail: Invalid path");
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
                    System.out.println("Wrong API password");
                    return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "Wrong API password");
                }
                params.remove("password");
                response = controller.execute(action, params, this.plugin.getServer());
            }
            catch (RequestException e)
            {
                System.out.println("Fail: " + e.getMessage());
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getMessage());
            }
        }
        else
        {
            System.out.println("Fail: Controller not found");
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Controller not found");
        }
        
        
        
        if (response != null)
        {
            String formatProperty = params.getProperty("format");
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
                return new Response(HTTP_OK, responseFormat.getMime(), responseFormat.format(response));
            }
            else
            {
                System.out.println("Fail: No valid response format found");
                return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "No valid response format found!");
            }
        }
        else
        {
            System.out.println("No response to respond with!");
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "No response");
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
        this.requestControllers.put(name, controller);
    }
    
    public void removeRequestController(String name)
    {
        this.requestControllers.remove(name);
    }
    
    public Collection<AbstractRequestController> getAllRequestControllers()
    {
        return this.requestControllers.values();
    }
}
