package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * This enum contains the different API errors which get return by the server if
 * something goes wrong while processing the request
 *
 * @author Phillip Schichtel
 * @since 1.0.0
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

    /**
     * initializes the ApiError with an error code and an HttpResponseStatus
     */
    private ApiError(int errorCode, HttpResponseStatus responseStatus)
    {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
    }

    /**
     * Returns the error code of the ApiError instance
     *
     * @return the code
     */
    public int getCode()
    {
        return this.errorCode;
    }

    /**
     * Returns the HttpResponseStatus of the ApiError instance
     *
     * @return the response status
     */
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
