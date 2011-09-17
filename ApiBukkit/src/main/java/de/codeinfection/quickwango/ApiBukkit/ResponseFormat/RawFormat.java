package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;

/**
 *
 * @author CodeInfection
 */
public class RawFormat implements ApiResponseFormat
{
    public String getMime()
    {
        return ApiBukkitServer.MIME_PLAINTEXT;
    }

    public String format(Object o)
    {
        return String.valueOf(o);
    }
}
