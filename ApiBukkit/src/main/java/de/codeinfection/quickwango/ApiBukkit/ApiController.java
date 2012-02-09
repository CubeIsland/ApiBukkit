package de.codeinfection.quickwango.ApiBukkit;

import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiController
{
    private final Plugin plugin;
    private boolean authNeeded;
    private Map<String, ApiAction> actions;

    /**
     * Initializes the controllers
     *
     * @param plugin the plugin this controllers corresponds to
     * @param authNeeded whether the controllers actions need authentication by default or not
     */
    public ApiController(Plugin plugin)
    {
        this.plugin = plugin;
        this.authNeeded = true;
        this.actions = null;
    }

    public final void initialize(Map<String, ApiAction> actions)
    {
        if (this.actions == null)
        {
            this.actions = actions;
        }
    }

    public final boolean isInitialized()
    {
        return (actions != null);
    }

    /**
     * Returns the corresponding plugin.
     *
     * @return the currensponding plugin
     */
    public final Plugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Returns whether this actions needs authentication.
     *
     * @return true if auth is needed, otherwise false
     */
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * Returns whether this actions needs authentication.
     *
     * @return true if auth is needed, otherwise false
     */
    public final void setAuthNeeded(boolean authNeeded)
    {
        this.authNeeded = authNeeded;
    }

    /**
     * Sets an action for the given name.
     *
     * @param name the name
     * @param action the action
     */
    public final void setAction(String name, ApiAction action)
    {
        if (name != null && action != null)
        {
            name = name.toLowerCase();
            this.actions.put(name, action);
            debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getSimpleName()));
        }
    }

    /**
     * Returns the action with given name.
     *
     * @param name the name
     * @return the action
     */
    public final ApiAction getAction(String name)
    {
        if (name != null)
        {
            return this.actions.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Returns all actions.
     *
     * @return a map of all actions
     */
    public final Map<String, ApiAction> getActions()
    {
        return this.actions;
    }

    /**
     * This method will be called if the requested action was not found.
     *
     * @param action the name of the action which was requested
     * @param server a org.bukkit.Server instance
     * @return the response as an Object
     * @throws ApiRequestException
     */
    public Object defaultAction(String action, Parameters params, Server server)
    {
        return this.getActions().keySet();
    }
}
