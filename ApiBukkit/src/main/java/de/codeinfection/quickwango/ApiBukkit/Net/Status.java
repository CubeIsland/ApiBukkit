package de.codeinfection.quickwango.ApiBukkit.Net;

/**
 * Some HTTP response status codes
 */
public enum Status
{
    OK("200 OK"),
    NOCONTENT("204 No Content"),
    MOVED_PERMANENTLY("301 Moved Permanently"),
    BADREQUEST("400 Bad Request"),
    UNAUTHORIZED("401 Unauthorized"),
    FORBIDDEN("403 Forbidden"),
    NOTFOUND("404 Not Found"),
    INTERNALERROR("500 Internal Server Error"),
    NOTIMPLEMENTED("501 Not Implemented");

    private final String message;

    Status(String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return this.message;
    }
}
