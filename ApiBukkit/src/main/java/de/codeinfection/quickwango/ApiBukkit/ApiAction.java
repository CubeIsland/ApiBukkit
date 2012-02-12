package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    private final String[] parameters;

    /**
     * Initializes the request action.
     *
     * @param controller the parent
     * @param name the name of the action
     * @param method the method to invoke
     * @param authNeeded whether authentication is needed
     */
    public ApiAction(ApiController controller, String name, Method method, boolean authNeeded, String[] parameters)
    {
        this.controller = controller;
        this.name = name;
        this.method = method;
        this.authNeeded = authNeeded;
        this.parameters = parameters;
    }

    /**
     * Returns the name of action
     *
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns whether this action requires authentication.
     *
     * @return whether authentication is needed
     */
    public Boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     *
     */
    public String[] getParameters()
    {
        return this.parameters;
    }

    /**
     * This method handles the request.
     *
     * @param server a org.bukkit.Server instance
     * @return the response
     * @throws ApiRequestException
     */
    public Object execute(ApiRequest request) throws Throwable
    {
        try
        {
            return this.method.invoke(this.controller, request);
        }
        catch (InvocationTargetException ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
