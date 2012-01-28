package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.ApiSerializable;
import de.codeinfection.quickwango.ApiBukkit.Net.MimeType;
import java.util.Iterator;
import java.util.Map;

public class JsonFormat implements ApiResponseFormat
{
    public MimeType getMime()
    {
        return MimeType.JSON;
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
        else if (o instanceof ApiSerializable)
        {
            response += this.format(((ApiSerializable)o).serialize());
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
                String name = "";
                if (entry.getKey() != null)
                {
                    name = entry.getKey().toString();
                }
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
                    response += "\"";
                    response += escape(String.valueOf(o));
                    response += "\"";
                }
                if (firstLevel)
                {
                    response += "]";
                }
            }
        }
        
        return response;
    }

    private static String escape(String string)
    {
        return string.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\t", "\\t")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r");
    }
}
