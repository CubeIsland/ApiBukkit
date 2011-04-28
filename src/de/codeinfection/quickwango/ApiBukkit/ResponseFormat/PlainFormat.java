package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class PlainFormat implements IResponseFormat
{
    
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_PLAINTEXT;
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
            for (Map.Entry entry : data.entrySet())
            {
                counter++;
                Object value = entry.getValue();
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value);
                }
                else
                {
                    response += encode(String.valueOf(value));
                }
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
                
                if (value instanceof Iterable || value instanceof Map)
                {
                    response += this.format(value);
                }
                else
                {
                    response += encode(String.valueOf(value));
                }
                if (iter.hasNext())
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
