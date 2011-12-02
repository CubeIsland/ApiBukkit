package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

import de.codeinfection.quickwango.ApiBukkit.Net.MimeType;

/**
 *
 * @author CodeInfection
 */
public interface ApiResponseFormat
{
    public String format(Object o);
    public MimeType getMime();
}
