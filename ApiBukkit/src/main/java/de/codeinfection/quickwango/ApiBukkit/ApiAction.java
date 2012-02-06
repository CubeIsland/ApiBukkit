package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public final class ApiAction
{
    private final ApiController controller;

    private final String name;
    private final Method method;
    private final boolean authNeeded;

    /**
     * Initializes the request action.
     *
     * @param authNeeded whether authentication is needed
     */
    public ApiAction(ApiController controller, String name, Method method, boolean authNeeded)
    {
        this.controller = controller;
        this.name = name;
        this.method = method;
        this.authNeeded = authNeeded;
    }

    /**
     * Returns whether this action requires authentication.
     *
     * @return
     */
    public Boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * This method handles the request.
     *
     * @param server a org.bukkit.Server instance
     * @return the response
     * @throws ApiRequestException
     */
    public Object execute(Parameters params, Server server) throws Throwable
    {
        try
        {
            return this.method.invoke(this.controller, params, server);
        }
        catch (InvocationTargetException ex)
        {
            throw ex.getCause();
        }
    }
}
