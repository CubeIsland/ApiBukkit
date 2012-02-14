package de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.MimeType;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ResponseSerializer;

/**
 *
 * @author CodeInfection
 */
@ResponseSerializer(name = "raw")
public class RawSerializer implements ApiResponseSerializer
{
    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }

    public String serialize(Object o)
    {
        return String.valueOf(o);
    }
}
