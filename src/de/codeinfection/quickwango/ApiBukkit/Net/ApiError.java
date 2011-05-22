package de.codeinfection.quickwango.ApiBukkit.Net;

/**
 *
 * @author CodeInfection
 */
public enum ApiError
{
    UNKNONW_ERROR(-1),
    INVALID_PATH(1),
    WRONG_API_PASSWORD(2),
    REQUEST_EXCEPTION(3),
    ACTION_NOT_IMPLEMENTED(4),
    CONTROLLER_NOT_FOUND(5);
    
    
    protected int errorCode;
    
    private ApiError(int errorCode)
    {
        this.errorCode = errorCode;
    }
    
    public int getCode()
    {
        return this.errorCode;
    }
    
    @Override
    public String toString()
    {
        return String.valueOf(this.errorCode);
    }
}
