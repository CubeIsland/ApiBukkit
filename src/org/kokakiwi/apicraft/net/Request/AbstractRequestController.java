package org.kokakiwi.apicraft.net.Request;

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
    
    public AbstractRequestController(JavaPlugin plugin, boolean authNeeded)
    {
        this.plugin = plugin;
        this.authNeeded = authNeeded;
    }
    
    public JavaPlugin getPlugin()
    {
        return this.plugin;
    }
    
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }
    
    abstract public Object execute(String action, Properties params, Server server) throws RequestException;
}
