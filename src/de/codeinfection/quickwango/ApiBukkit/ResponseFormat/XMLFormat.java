package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import java.util.Iterator;
import java.util.Map;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

public class XMLFormat implements IResponseFormat
{
    private final static String XMLDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    private static int depth = 0;
    
    public String getMime()
    {
        return ApiBukkitServer.MIME_XML;
    }
    
    public String format(Object o)
    {
        return this.format(o, "response");
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object o, String rootNodeName)
    {
        depth++;
        String response = "";
        response += "<" + rootNodeName + ">";
        if (o == null)
        {} // null -> do nothing
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>) o;
            for (Map.Entry entry : data.entrySet())
            {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                response += this.format(value, name);
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>) o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                response += this.format(value, rootNodeName);
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[]) o;
            for (int i = 0; i < data.length; i++)
            {
                response += this.format(data[i], rootNodeName);
            }
        }
        else
        {
            response += String.valueOf(o);
        }
        
        response += "</" + rootNodeName + ">";
        depth--;
        
        if (depth == 0)
        {
            response = XMLDeclaration + response;
        }
        
        return response;
    }

    private static String addTabs(int indent)
    {
        String response = "";
        if (indent > 0)
        {
            for (int k = 0; k < indent; k++)
            {
                response += "\t";
            }
        }
        return response;
    }
}
