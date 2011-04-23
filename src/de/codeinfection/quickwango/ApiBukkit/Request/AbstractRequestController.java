package de.codeinfection.quickwango.ApiBukkit.Request;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author CodeInfection
 */
public abstract class AbstractRequestController
{
    protected JavaPlugin plugin;
    private boolean authNeeded;
    private HashMap<String, RequestAction> actions;
    
    public AbstractRequestController(JavaPlugin plugin, boolean authNeeded)
    {
        this.plugin = plugin;
        this.authNeeded = authNeeded;
        this.actions = new HashMap<String, RequestAction>();
    }
    
    public JavaPlugin getPlugin()
    {
        return this.plugin;
    }
    
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }
    
    protected void registerAction(String name, RequestAction action)
    {
        this.actions.put(name, action);
        ApiBukkit.debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getName()));
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
    
    public RequestAction getAction(String name)
    {
        return this.actions.get(name);
    }
    
    public Map<String, RequestAction> getActions()
    {
        return this.actions;
    }
    
    abstract public Object defaultAction(String action, Properties params, Server server) throws RequestException;
    
    public interface RequestAction
    {
        public Object run(Properties params, Server server) throws RequestException;
    }
}
