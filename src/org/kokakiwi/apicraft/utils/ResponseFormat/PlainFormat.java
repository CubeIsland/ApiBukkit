package org.kokakiwi.apicraft.utils.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import org.kokakiwi.apicraft.net.ApiWebServer;

public class PlainFormat implements IResponseFormat
{
    
    public String getMime()
    {
        return ApiWebServer.MIME_PLAINTEXT;
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
                    response += String.valueOf(value);
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
                    response += String.valueOf(value);
                }
                if (iter.hasNext())
                {
                    response += ",";
                }
            }
        }
        else
        {
            response = String.valueOf(o);
        }
        return response;
    }
}
