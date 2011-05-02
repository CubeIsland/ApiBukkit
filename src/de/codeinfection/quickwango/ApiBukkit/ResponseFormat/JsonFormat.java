package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class JsonFormat implements IResponseFormat
{
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_JSON;
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object o)
    {
        String response = "";
        if (o == null || o instanceof Number || o instanceof Boolean)
        {
            response += "[" + String.valueOf(o) + "]";
        }
        else if (o instanceof Map)
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
                if (value == null || value instanceof Number || value instanceof Boolean)
                {
                    response += "\"" + name + "\":" + String.valueOf(value);
                }
                else if (value instanceof Iterable || value instanceof Map || value.getClass().isArray())
                {
                    response += "\"" + name + "\":" + this.format(value);
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
                if (value == null || value instanceof Number || value instanceof Boolean)
                {
                    response += String.valueOf(value);
                }
                else if (value instanceof Iterable || value instanceof Map || value.getClass().isArray())
                {
                    response += this.format(value);
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
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            int end = data.length - 1;
            response += "[";
            for (int i = 0; i < data.length; i++)
            {
                if (data[i] == null || data[i] instanceof Number || data[i] instanceof Boolean)
                {
                    response += String.valueOf(data[i]);
                }
                else if (data[i] instanceof Iterable || data[i] instanceof Map || data[i].getClass().isArray())
                {
                    response += this.format(data[i]);
                }
                else
                {
                    response += "\"" + String.valueOf(data[i]) + "\"";
                }
                if (i < end)
                {
                    response += ",";
                }
            }
            response += "]";
        }
        else
        {
            response += "[\"" + String.valueOf(o) + "\"]";
        }
        
        return response;
    }
}
