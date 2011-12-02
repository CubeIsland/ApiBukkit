package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiRequestAction
{
    private Boolean authNeeded;

    /**
     * Initializes the request action.
     */
    public ApiRequestAction()
    {
        this.authNeeded = null;
    }

    /**
     * Initializes the request action.
     *
     * @param authNeeded whether authentication is needed
     */
    public ApiRequestAction(boolean authNeeded)
    {
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
    public abstract Object execute(Parameters params, Server server) throws ApiRequestException;
}
