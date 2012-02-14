package de.codeinfection.quickwango.ApiBukkit.ApiServer;

/**
 *
 * @author CodeInfection
 */
public interface ApiResponseSerializer
{
    /**
     * Serializes an object
     *
     * @param o the object to serialize
     * @return the string representation
     */
    public String serialize(Object o);

    /**
     * Returns the mime type in which this format should be delivered
     *
     * @return the mime type
     */
    public MimeType getMime();
}
