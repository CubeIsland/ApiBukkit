package de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiSerializable;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.MimeType;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ResponseSerializer;
import java.util.Iterator;
import java.util.Map;

@ResponseSerializer(name = "xml")
public class XmlSerializer implements ApiResponseSerializer
{
    private final static String XMLDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    
    public MimeType getMime()
    {
        return MimeType.XML;
    }
    
    public String serialize(Object o)
    {
        return this.serialize(o, "response", true);
    }
    
    @SuppressWarnings("unchecked")
    private String serialize(Object o, String nodeName, boolean firstLevel)
    {
        String response = "";
        response += "<" + nodeName + ">";
        if (o == null)
        {} // null -> do nothing
        else if (o instanceof ApiSerializable)
        {
            response += this.serialize(((ApiSerializable)o).serialize());
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            for (Map.Entry entry : data.entrySet())
            {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                response += this.serialize(value, name, false);
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                response += this.serialize(value, nodeName, false);
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            for (int i = 0; i < data.length; i++)
            {
                response += this.serialize(data[i], nodeName, false);
            }
        }
        else
        {
            response += String.valueOf(o).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        
        response += "</" + nodeName + ">";
        if (firstLevel)
        {
            response = XMLDeclaration + response;
        }
        
        return response;
    }
}
