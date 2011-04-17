package org.kokakiwi.apicraft.net;

import java.io.IOException;
import java.util.Properties;
import org.bukkit.util.config.Configuration;

import org.kokakiwi.apicraft.*;
import org.kokakiwi.apicraft.events.ApiEvent;
import org.kokakiwi.apicraft.net.NanoHTTPD.Response;
import org.kokakiwi.apicraft.utils.JsonFormat;
import org.kokakiwi.apicraft.utils.PlainFormat;
import org.kokakiwi.apicraft.utils.XMLFormat;

public class WebServer extends NanoHTTPD
{
    private ApiCraft plugin;
    private String APIPassword;

    public WebServer(ApiCraft plugin, Configuration config) throws IOException
    {
        super(config.getInt("Configuration.webServerPort", 6561));
        this.APIPassword = config.getString("Configuration.APIPassword", "changeMe");
        this.plugin = plugin;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files)
    {
        System.out.println(uri + " was requested...");
        if (!parms.getProperty("password").equals(this.APIPassword))
        {
            System.out.println("Wrong API password");
            return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "Wrong API password");
        }
        
        ApiEvent event = new ApiEvent(uri, parms, this.plugin);
        plugin.getServer().getPluginManager().callEvent(event);
        
        if (event.isActionTaken())
        {
            if (event.getResponse() instanceof Integer)
            {
                event.setResponse(event.getResponse() + "");
            }
            if (parms.getProperty("format") != null)
            {
                String format = parms.getProperty("format");
                if(format.equalsIgnoreCase("xml") || event.getFormat().equalsIgnoreCase("xml"))
                {
                    return new Response(HTTP_OK, MIME_XML, XMLFormat.format(event.getResponse()));
                }
                else if(format.equalsIgnoreCase("json") || event.getFormat().equalsIgnoreCase("json"))
                {
                    return new Response(HTTP_OK, MIME_JSON, JsonFormat.format(event.getResponse()));
                }
                else
                {
                    return new Response(HTTP_OK, MIME_PLAINTEXT, PlainFormat.format(event.getResponse()));
                }
            }
            else
            {
                return new Response(HTTP_OK, MIME_PLAINTEXT, PlainFormat.format(event.getResponse()));
            }
        }
        else
        {
            System.out.println("No action was taken!");
            if (parms.getProperty("format") != null)
            {
                String format = parms.getProperty("format");
                if (format.equalsIgnoreCase("xml"))
                {
                    return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, XMLFormat.format("Listener not found"));
                }
                else if (format.equalsIgnoreCase("json") || event.getFormat().equalsIgnoreCase("json"))
                {
                    return new Response(HTTP_NOTFOUND, MIME_JSON, JsonFormat.format("Listener not found"));
                }
                else
                {
                    return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, PlainFormat.format("Listener not found"));
                }
            }
            else
            {
                return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Listener not found");
            }
        }
    }
}
