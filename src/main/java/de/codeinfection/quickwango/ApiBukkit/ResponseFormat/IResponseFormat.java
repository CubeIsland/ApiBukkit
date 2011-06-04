package de.codeinfection.quickwango.ApiBukkit.ResponseFormat;

/**
 *
 * @author CodeInfection
 */
public interface IResponseFormat
{
    public String format(Object o);
    public String getMime();
}
