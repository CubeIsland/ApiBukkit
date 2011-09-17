package de.codeinfection.quickwango.ApiBukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiRequestController
{
    protected Plugin plugin;
    private boolean authNeeded;
    private HashMap<String, ApiRequestAction> actions;
    private HashMap<String, String> actionAliases;

    /**
     * Initializes the controllers
     *
     * @param plugin the plugin this controllers corresponds to
     * @param authNeeded whether the controllers actions need authentication by default or not
     */
    public ApiRequestController(Plugin plugin, boolean authNeeded)
    {
        this.plugin = plugin;
        this.authNeeded = authNeeded;
        this.actions = new HashMap<String, ApiRequestAction>();
        this.actionAliases = new HashMap<String, String>();
    }

    /**
     * Returns the corresponding plugin.
     *
     * @return the currensponding plugin
     */
    public Plugin getPlugin()
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
     * Sets an action for the given name.
     *
     * @param name the name
     * @param action the action
     */
    public void setAction(String name, ApiRequestAction action)
    {
        this.actions.put(name, action);
        ApiBukkit.debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getSimpleName()));
    }

    /**
     * Stes an alias for an action.
     *
     * @param alias the alias
     * @param action the action to refer to
     * @return false on failure
     */
    public boolean setActionAlias(String alias, String action)
    {
        if (this.actions.containsKey(action))
        {
            this.actionAliases.put(alias, action);
            return true;
        }
        return false;
    }

    /**
     * Returns the action with given name.
     *
     * @param name the name
     * @return the action
     */
    public ApiRequestAction getAction(String name)
    {
        return this.actions.get(name);
    }

    /**
     * Returns the action refered by the given alias.
     *
     * @param alias the alias
     * @return the refered action
     */
    public ApiRequestAction getActionByAlias(String alias)
    {
        return this.getAction(this.actionAliases.get(alias));
    }

    /**
     * Returns all actions.
     *
     * @return a map of all actions
     */
    public Map<String, ApiRequestAction> getActions()
    {
        return this.actions;
    }

    /**
     * This method will be called if the requested actions was not found.
     *
     * @param action the name of the action which was requested
     * @param params the parameters of the request
     * @param server a org.bukkit.Server instance
     * @return the response as an Object
     * @throws ApiRequestException
     */
    abstract public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException;
}
