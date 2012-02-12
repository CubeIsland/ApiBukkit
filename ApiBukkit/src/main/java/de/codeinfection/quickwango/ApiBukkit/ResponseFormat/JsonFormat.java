package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.ApiSerializable;
import de.codeinfection.quickwango.ApiBukkit.Server.MimeType;
import java.util.Iterator;
import java.util.Map;

public class JsonFormat implements ApiResponseFormat
{
    public MimeType getMime()
    {
        return MimeType.JSON;
    }

    /**
     * Serializes an object
     *
     * @param o the object to serialize
     * @return the JSON representation of this given object
     */
    public String format(Object o)
    {
        StringBuilder buffer = new StringBuilder();
        this.format(buffer, o, true);
        return buffer.toString();
    }

    private void format(StringBuilder buffer, Object o)
    {
        this.format(buffer, o, false);
    }
    
    @SuppressWarnings("unchecked")
    private void format(StringBuilder buffer, Object o, boolean firstLevel)
    {
        if (o == null)
        {
            buffer.append(firstLevel ? "[null]" : "null");
        }
        else if (o instanceof ApiSerializable)
        {
            this.format(buffer, ((ApiSerializable)o).serialize());
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            int dataSize = data.size();
            int counter = 0;
            buffer.append("{");
            for (Map.Entry entry : data.entrySet())
            {
                counter++;
                String name = "";
                if (entry.getKey() != null)
                {
                    name = entry.getKey().toString();
                }
                Object value = entry.getValue();
                buffer.append("\"").append(name).append("\":");
                this.format(buffer, value);
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
            while (iter.hasNext())
            {
                Object value = iter.next();
                this.format(buffer, value);
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
                this.format(buffer, data[i]);
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
                this.format(buffer, o);
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
