package de.codeinfection.quickwango.ApiBukkit;

import java.util.Properties;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiRequestAction
{
    private Boolean authNeeded;

    public ApiRequestAction()
    {
        this.authNeeded = null;
    }

    public ApiRequestAction(boolean authNeeded)
    {
        this.authNeeded = authNeeded;
    }

    public Boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    public abstract Object execute(Properties params, Server server) throws ApiRequestException;
}
