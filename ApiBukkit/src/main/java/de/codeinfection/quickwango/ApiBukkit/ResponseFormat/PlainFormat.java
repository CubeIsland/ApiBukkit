package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.ApiSerializable;
import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class PlainFormat implements ApiResponseFormat
{
    
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_PLAINTEXT;
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object o)
    {
        String response = "";
        if (o == null)
        {
            response = "";
        }
        else if (o instanceof ApiSerializable)
        {
            response += this.format(((ApiSerializable)o).serialize());
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            int dataSize = data.size();
            int counter = 0;
            for (Map.Entry entry : data.entrySet())
            {
                ++counter;
                Object value = entry.getValue();
                response += this.format(value);
                if (counter < dataSize)
                {
                    response += ",";
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
                response += this.format(value);
                if (iter.hasNext())
                {
                    response += ",";
                }
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            int end = data.length - 1;
            for (int i = 0; i < data.length; i++)
            {
                response += this.format(data[i]);
                if (i < end)
                {
                    response += ",";
                }
            }
        }
        else
        {
            response = encode(String.valueOf(o));
        }
        return response;
    }
    
    protected static String encode(String string)
    {
        return string.replaceAll("%", "%25").replaceAll(",", "%2C");
    }
}
