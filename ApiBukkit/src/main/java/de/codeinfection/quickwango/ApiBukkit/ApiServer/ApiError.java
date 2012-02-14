package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;


/**
 *
 * @author CodeInfection
 */
public enum ApiError
{
    UNKNONW_ERROR(-1, HttpResponseStatus.INTERNAL_SERVER_ERROR),
    INVALID_PATH(1, HttpResponseStatus.BAD_REQUEST),
    AUTHENTICATION_FAILURE(2, HttpResponseStatus.UNAUTHORIZED),
    REQUEST_EXCEPTION(3, HttpResponseStatus.BAD_REQUEST),
    ACTION_NOT_IMPLEMENTED(4, HttpResponseStatus.NOT_IMPLEMENTED),
    CONTROLLER_NOT_FOUND(5, HttpResponseStatus.NOT_FOUND),
    ACTION_DISABLED(6, HttpResponseStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(7, HttpResponseStatus.METHOD_NOT_ALLOWED),
    MISSING_PARAMETERS(8, HttpResponseStatus.BAD_REQUEST);
    
    
    private final int errorCode;
    private final HttpResponseStatus responseStatus;
    
    private ApiError(int errorCode, HttpResponseStatus responseStatus)
    {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
    }
    
    public int getCode()
    {
        return this.errorCode;
    }

    public HttpResponseStatus getRepsonseStatus()
    {
        return this.responseStatus;
    }
    
    @Override
    public String toString()
    {
        return String.valueOf(this.errorCode);
    }
}
