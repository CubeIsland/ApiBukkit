package org.kokakiwi.apicraft.utils.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import org.kokakiwi.apicraft.net.ApiWebServer;

public class XMLFormat implements IResponseFormat
{
    
    public String getMime()
    {
        return ApiWebServer.MIME_XML;
    }
    
    public String format(Object o)
    {
        return this.format(o, "response");
    }
    
    public String format(Object o, String rootNodeName)
    {
        return this.format(o, rootNodeName, 0);
    }
    
    public String format(Object o, String rootNodeName, int indent)
    {
        String response = "";
        response += addTabs(indent) + "<" + rootNodeName + ">\r\n";
        if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            for (Map.Entry entry : data.entrySet())
            {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value, name, indent + 1);
                }
                else
                {
                    response += addTabs(indent + 1) + "<" + name + ">" + String.valueOf(value) + "</" + name + ">\r\n";
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
                    response += this.format(value, rootNodeName, indent + 1);
                }
                else
                {
                    response += addTabs(indent + 1) + "<" + rootNodeName + ">" + String.valueOf(value) + "</" + rootNodeName + ">\r\n";
                }
            }
        }
        else
        {
            response += addTabs(indent + 1) + String.valueOf(0);
        }
        
        response += addTabs(indent) + "</" + rootNodeName + ">\r\n";
        
        return response;
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
