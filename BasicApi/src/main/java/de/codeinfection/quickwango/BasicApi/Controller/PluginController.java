package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Parameters;
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
@Controller(name = "plugin")
public class PluginController extends ApiController
{
    public PluginController(Plugin plugin)
    {
        super(plugin);
    }

    @Action
    public Object list(Parameters params, Server server)
    {
        ArrayList<String> data = new ArrayList<String>();
        Plugin[] plugins = server.getPluginManager().getPlugins();
        for (Plugin currentPlugin : plugins)
        {
            data.add(currentPlugin.getDescription().getName());
        }
        return data;
    }
    
    @Action
    public Object info(Parameters params, Server server)
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

    @Action
    public Object available(Parameters params, Server server)
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
    
    @Action
    public Object execute(Parameters params, Server server)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
