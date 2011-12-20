package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author CodeInfection
 */
public class PluginController extends ApiRequestController
{
    public PluginController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("list",         new ListAction());
        this.setAction("reloadall",    new ReloadallAction());
        this.setAction("info",         new InfoAction());
        this.setAction("available",    new AvailableAction());
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    private class ListAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            ArrayList<String> data = new ArrayList<String>();
            Plugin[] plugins = server.getPluginManager().getPlugins();
            for (Plugin currentPlugin : plugins)
            {
                data.add(currentPlugin.getDescription().getName());
            }
            return data;
        }
    }
    
    private class ReloadallAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            server.reload();
            return null;
        }
    }
    
    private class InfoAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String pluginName = params.getString("plugin");
            if (pluginName != null)
            {
                Plugin targetPlugin = server.getPluginManager().getPlugin(pluginName);
                if (targetPlugin != null)
                {
                    Map<String, Object> data = new HashMap<String, Object>();
                    PluginDescriptionFile description = targetPlugin.getDescription();
                    data.put("name",        description.getName());
                    data.put("fullName",    description.getFullName());
                    data.put("version",     description.getVersion());
                    data.put("description", description.getDescription());
                    data.put("website",     description.getWebsite());
                    data.put("authors",     description.getAuthors());
                    data.put("depend",      description.getDepend());
                    data.put("commands",    description.getCommands());
                    data.put("enabled",     targetPlugin.isEnabled());
                    data.put("dataFolder",  targetPlugin.getDataFolder().getAbsolutePath());
                    return data;
                }
                else
                {
                    throw new ApiRequestException("Plugin not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No plugin given!", 1);
            }
        }
    }

    private class AvailableAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String pluginName = params.getString("plugin");
            if (pluginName != null)
            {
                return (server.getPluginManager().getPlugin(pluginName) != null);
            }
            else
            {
                throw new ApiRequestException("No plugin given!", 1);
            }
        }
    }
    
    private class InstallAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
