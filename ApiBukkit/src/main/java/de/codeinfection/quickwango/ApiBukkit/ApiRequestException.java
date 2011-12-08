package de.codeinfection.quickwango.ApiBukkit;

/**
 *
 * @author CodeInfection
 */
public class ApiRequestException extends Exception
{
    int errCode;

    public ApiRequestException(String msg, int errCode)
    {
        super(msg);
        this.errCode = errCode;
    }

    public ApiRequestException(String msg, int errCode, Throwable cause)
    {
        super(msg, cause);
        this.errCode = errCode;
    }
    
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
