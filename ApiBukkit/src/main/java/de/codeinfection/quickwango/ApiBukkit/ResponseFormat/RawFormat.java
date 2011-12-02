package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.Net.MimeType;

/**
 *
 * @author CodeInfection
 */
public class RawFormat implements ApiResponseFormat
{
    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }

    public String format(Object o)
    {
        return String.valueOf(o);
    }
}
