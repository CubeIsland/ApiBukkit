package de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiSerializable;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.MimeType;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ResponseSerializer;
import java.util.Iterator;
import java.util.Map;

@ResponseSerializer(name = "json")
public class JsonSerializer implements ApiResponseSerializer
{
    public MimeType getMime()
    {
        return MimeType.JSON;
    }

    public String serialize(Object o)
    {
        StringBuilder buffer = new StringBuilder();
        this.serialize(buffer, o, true);
        return buffer.toString();
    }

    private void serialize(StringBuilder buffer, Object o)
    {
        this.serialize(buffer, o, false);
    }
    
    @SuppressWarnings("unchecked")
    private void serialize(StringBuilder buffer, Object o, boolean firstLevel)
    {
        if (o == null)
        {
            buffer.append(firstLevel ? "[null]" : "null");
        }
        else if (o instanceof ApiSerializable)
        {
            this.serialize(buffer, ((ApiSerializable)o).serialize());
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            int dataSize = data.size();
            int counter = 0;
            buffer.append("{");
            Object value;
            String name;
            for (Map.Entry entry : data.entrySet())
            {
                counter++;
                name = "";
                if (entry.getKey() != null)
                {
                    name = entry.getKey().toString();
                }
                value = entry.getValue();
                buffer.append("\"").append(name).append("\":");
                this.serialize(buffer, value);
                if (counter < dataSize)
                {
                    buffer.append(",");
                }
            }
            buffer.append("}");
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            buffer.append("[");
            Object value;
            while (iter.hasNext())
            {
                value = iter.next();
                this.serialize(buffer, value);
                if (iter.hasNext())
                {
                    buffer.append(",");
                }
            }
            buffer.append("]");
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            int end = data.length - 1;
            buffer.append("[");
            for (int i = 0; i < data.length; i++)
            {
                this.serialize(buffer, data[i]);
                if (i < end)
                {
                    buffer.append(",");
                }
            }
            buffer.append("]");
        }
        else
        {
            // TODO check this
            if (o instanceof Iterable || o instanceof Map || o.getClass().isArray())
            {
                this.serialize(buffer, o);
            }
            else
            {
                if (firstLevel)
                {
                    buffer.append("[");
                }
                if (o instanceof Number || o instanceof Boolean)
                {
                    buffer.append(String.valueOf(o));
                }
                else
                {
                    buffer.append("\"").append(escape(String.valueOf(o))).append("\"");
                }
                if (firstLevel)
                {
                    buffer.append("]");
                }
            }
        }
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
