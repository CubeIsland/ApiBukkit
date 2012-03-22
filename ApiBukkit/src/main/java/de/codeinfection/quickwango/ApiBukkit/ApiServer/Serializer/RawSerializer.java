package de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.MimeType;

/**
 *
 * @author CodeInfection
 */
public class RawSerializer implements ApiResponseSerializer
{
    public String getName()
    {
        return "raw";
    }

    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }

    public String serialize(Object o)
    {
        return String.valueOf(o);
    }
}
