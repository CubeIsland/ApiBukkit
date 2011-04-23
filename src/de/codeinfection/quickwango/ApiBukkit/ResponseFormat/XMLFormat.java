package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class XMLFormat implements IResponseFormat
{
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_XML;
    }
    
    public String format(Object o)
    {
        return this.format(o, "response");
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object o, String rootNodeName)
    {
        String response = "";
        response += "<" + rootNodeName + ">";
        if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            for (Map.Entry entry : data.entrySet())
            {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value, name);
                }
                else
                {
                    response += "<" + name + ">" + String.valueOf(value) + "</" + name + ">";
                }
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value, rootNodeName);
                }
                else
                {
                    response += "<" + rootNodeName + ">" + String.valueOf(value) + "</" + rootNodeName + ">";
                }
            }
        }
        else
        {
            response += String.valueOf(o);
        }
        
        response += "</" + rootNodeName + ">";
        
        return "<?xml version=\"1.0\" ?>\n" + response;
    }

    private static String addTabs(int indent)
    {
        String response = "";
        if (indent > 0)
        {
            for (int k = 0; k < indent; k++)
            {
                response += "\t";
            }
        }
        return response;
    }
}
