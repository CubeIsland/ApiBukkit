package de.codeinfection.quickwango.ApiBukkit.Request;

/**
 *
 * @author CodeInfection
 */
public class RequestException extends Exception
{
    int errCode;
    
    public RequestException(String msg, int errCode)
    {
        super(msg);
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
