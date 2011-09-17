package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
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
    
    public ApiRequestController(Plugin plugin, boolean authNeeded)
    {
        this.plugin = plugin;
        this.authNeeded = authNeeded;
        this.actions = new HashMap<String, ApiRequestAction>();
    }
    
    public Plugin getPlugin()
    {
        return this.plugin;
    }
    
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }
    
    public void setAction(String name, ApiRequestAction action)
    {
        this.actions.put(name, action);
        ApiBukkit.debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getName().replaceFirst(this.getClass().getPackage().getName() + ".", "")));
    }
    
    protected boolean setActionAlias(String alias, String action)
    {
        if (!this.actions.containsKey(alias) && this.actions.containsKey(action))
        {
            this.actions.put(alias, this.actions.get(action));
            return true;
        }
        return false;
    }
    
    public ApiRequestAction getAction(String name)
    {
        return this.actions.get(name);
    }
    
    public Map<String, ApiRequestAction> getActions()
    {
        return this.actions;
    }
    
    abstract public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException;
}
