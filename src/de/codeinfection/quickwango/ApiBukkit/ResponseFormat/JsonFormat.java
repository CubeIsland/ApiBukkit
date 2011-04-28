package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiError;

public class JsonFormat implements IResponseFormat
{
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_JSON;
    }
    
    public String format(ApiError error)
    {
        return this.format(error.asList());
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object o)
    {
        String response = "";
        if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            int dataSize = data.size();
            int counter = 0;
            response += "{";
            for (Map.Entry entry : data.entrySet())
            {
                counter++;
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += "\"" + name + "\":" + this.format(value);
                }
                else if (value instanceof Number)
                {
                    response += "\"" + name + "\":" + String.valueOf(value);
                }
                else
                {
                    response += "\"" + name + "\":\"" + String.valueOf(value) + "\"";
                }
                if (counter < dataSize)
                {
                    response += ",";
                }
            }
            response += "}";
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            response += "[";
            while (iter.hasNext())
            {
                Object value = iter.next();
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value);
                }
                else if (value instanceof Number)
                {
                    response += String.valueOf(value);
                }
                else
                {
                    response += "\"" + String.valueOf(value) + "\"";
                }
                if (iter.hasNext())
                {
                    response += ",";
                }
            }
            response += "]";
        }
        else if (o instanceof Number)
        {
            Number number = (Number) o;
            response += "[" + String.valueOf(number) + "]";
        }
        else
        {
            response += "[\"" + (String) o + "\"]";
        }
        
        return response;
    }
}
