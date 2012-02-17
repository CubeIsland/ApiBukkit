package de.codeinfection.quickwango.ApiBukkit.ApiServer;

/**
 * This exception should be used to express an error during the action execution.
 * For example when the action expects a number as parameter, but a letter was given.
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public class ApiRequestException extends RuntimeException
{
    private int errCode;

    /**
     * Initializes the exception with a message and an error code
     * The given message won't be send to the client, it's just used to inform the console
     * 
     * @param msg the message
     * @param errCode the error code
     */
    public ApiRequestException(String msg, int errCode)
    {
        super(msg);
        this.errCode = errCode;
    }

    /**
     * Initializes the exception with a message and an error code and the cause of the error
     * The given message won't be send to the client, it's just used to inform the console
     * 
     * @param msg the message
     * @param errCode the error code
     * @param cause the cause of te error
     */
    public ApiRequestException(String msg, int errCode, Throwable cause)
    {
        super(msg, cause);
        this.errCode = errCode;
    }
    
    /**
     * Returns the error code
     * 
     *  @return the error code
     */
    public int getErrCode()
    {
        return this.errCode;
    }
    
    @Override
    public String toString()
    {
        return this.getMessage();
    }
}
