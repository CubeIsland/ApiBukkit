package de.codeinfection.quickwango.ApiBukkit.ApiServer;

/**
 * This interface must be implemented by response serializers
 *
 * @author Philllip Schichtel
 * @since 1.0.0
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
