package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

/**
 *
 * @author CodeInfection
 */
public interface ApiResponseFormat
{
    public String format(Object o);
    public String getMime();
}
