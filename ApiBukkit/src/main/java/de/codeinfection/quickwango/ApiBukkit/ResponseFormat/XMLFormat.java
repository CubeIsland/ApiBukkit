package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class XMLFormat implements ApiResponseFormat
{
    protected final static String XMLDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_XML;
    }
    
    public String format(Object o)
    {
        return this.format(o, "response", true);
    }
    
    @SuppressWarnings("unchecked")
    protected String format(Object o, String nodeName, boolean firstLevel)
    {
        String response = "";
        response += "<" + nodeName + ">";
        if (o == null)
        {} // null -> do nothing
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            for (Map.Entry entry : data.entrySet())
            {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                response += this.format(value, name, false);
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                response += this.format(value, nodeName, false);
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            for (int i = 0; i < data.length; i++)
            {
                response += this.format(data[i], nodeName, false);
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
