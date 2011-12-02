package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ApibukkitController extends ApiRequestController
{
    public ApibukkitController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("combined", new CombinedAction());
    }
    
    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    private class CombinedAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return params;
        }
    }
}
