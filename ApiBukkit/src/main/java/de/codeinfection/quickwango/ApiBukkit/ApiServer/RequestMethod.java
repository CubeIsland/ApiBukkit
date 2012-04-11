package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.util.HashMap;

/**
 *
 * @author CodeInfection
 */
public enum RequestMethod
{
    GET,
    POST,
    PUT,
    DELETE,
    OPTIONS,
    HEAD,
    PATCH,
    TRACE,
    CONNECT;

    private static final HashMap<String, RequestMethod> BY_NAME;

    static
    {
        BY_NAME = new HashMap<String, RequestMethod>(values().length);

        for (RequestMethod method : values())
        {
            BY_NAME.put(method.name(), method);
        }
    }

    public static RequestMethod getByName(String name)
    {
        if (name == null)
        {
            return null;
        }
        return BY_NAME.get(name.toUpperCase());
    }
}
