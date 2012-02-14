package de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiSerializable;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.MimeType;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ResponseSerializer;
import java.util.Iterator;
import java.util.Map;

@ResponseSerializer(name = "plain")
public class PlainSerializer implements ApiResponseSerializer
{
    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }
    
    @SuppressWarnings("unchecked")
    public String serialize(Object o)
    {
        String response = "";
        if (o == null)
        {
            response = "";
        }
        else if (o instanceof ApiSerializable)
        {
            response += this.serialize(((ApiSerializable)o).serialize());
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
                response += this.serialize(value);
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
                response += this.serialize(value);
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
                response += this.serialize(data[i]);
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
    
    private static String encode(String string)
    {
        return string.replaceAll("%", "%25").replaceAll(",", "%2C");
    }
}
