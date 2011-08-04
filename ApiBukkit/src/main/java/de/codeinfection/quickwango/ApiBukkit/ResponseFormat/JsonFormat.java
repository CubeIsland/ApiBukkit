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

    public String format(Object o)
    {
        return this.format(o, true);
    }
    
    @SuppressWarnings("unchecked")
    protected String format(Object o, boolean firstLevel)
    {
        String response = "";
        if (o == null)
        {
            response += (firstLevel ? "[null]" : "null");
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
                response += "\"" + name + "\":" + this.format(value, false);
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
                response += this.format(value, false);
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
                response += this.format(data[i], false);
                if (i < end)
                {
                    response += ",";
                }
            }
            response += "]";
        }
        else
        {

            if (o instanceof Iterable || o instanceof Map || o.getClass().isArray())
            {
                response += this.format(o, false);
            }
            else
            {
                if (firstLevel)
                {
                    response += "[";
                }
                if (o instanceof Number || o instanceof Boolean)
                {
                    response += String.valueOf(o);
                }
                else
                {
                    response += "\"" + String.valueOf(o).replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\"") + "\"";
                }
                if (firstLevel)
                {
                    response += "]";
                }
            }
        }
        
        return response;
    }
}
