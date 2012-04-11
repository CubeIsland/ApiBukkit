package de.cubeisland.ApiBukkit.ApiServer.Serializer;

import de.cubeisland.ApiBukkit.ApiServer.ApiResponseSerializer;
import de.cubeisland.ApiBukkit.ApiServer.MimeType;

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
