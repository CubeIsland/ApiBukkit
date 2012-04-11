package de.codeinfection.ApiBukkit.ApiServer.Exceptions;

/**
 * This exception should be used to express an error during the action
 * execution. For example when the action expects a number as parameter, but a
 * letter was given.
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public class ApiRequestException extends RuntimeException
{
    private int reason;

    /**
     * Initializes the exception with a message and a reason. The given
     * message won't be send to the client, it's just used to inform the console
     *
     * @param msg the message
     * @param reason the reason for this error
     */
    public ApiRequestException(String msg, int reason)
    {
        super(msg);
        this.reason = reason;
    }

    /**
     * Initializes the exception with a message, a reason and the cause
     * of the error The given message won't be send to the client, it's just
     * used to inform the console
     *
     * @param message the message
     * @param reason the reason for this error
     * @param cause the cause of te error
     */
    public ApiRequestException(String message, int reason, Throwable cause)
    {
        super(message, cause);
        this.reason = reason;
    }

    /**
     * Returns the error code
     *
     * @return the error code
     */
    public int getReason()
    {
        return this.reason;
    }

    @Override
    public String toString()
    {
        return this.getMessage();
    }
}
