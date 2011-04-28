package de.codeinfection.quickwango.ApiBukkit.Net;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CodeInfection
 */
public enum ApiError
{
    UNKNONW_ERROR(-1, "Unknown error occurred while processing the controller!\nPlease notify the server adminstrator of this error."),
    INVALID_PATH(1, "Invalid path requested!"),
    WRONG_API_PASSWORD(2, "Wrong or no API password given!"),
    CONTROLLER_EXCEPTION(3),
    ACTION_NOT_IMPLEMENTED(4, "The requested action is not implemented!"),
    CONTROLLER_NOT_FOUND(5, "Controller not found!");
    
    
    protected int errorCode;
    protected String errorMessage;
    
    private ApiError(int errorCode)
    {
        this.errorCode = errorCode;
        this.errorMessage = null;
    }
    
    private ApiError(int errorCode, String errorMessage)
    {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public List<Object> asList()
    {
        List<Object> list = new ArrayList<Object>();
        list.add(this.errorCode);
        list.add(this.errorMessage);
        return list;
    }
    
    public int getCode()
    {
        return this.errorCode;
    }
    
    public String getMessage()
    {
        return this.errorMessage;
    }
    
    public ApiError setMessage(String message)
    {
        this.errorMessage = message;
        return this;
    }
    
    @Override
    public String toString()
    {
        return this.errorMessage;
    }
}
