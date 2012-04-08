package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.Abstraction.Abstraction;
import de.codeinfection.Abstraction.Implementations.Bukkit.BukkitPluginDescription;
import de.codeinfection.Abstraction.Plugin;
import de.codeinfection.Abstraction.PluginDescription;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiNotImplementedException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @Action(serializer = "json")
    public void list(ApiRequest request, ApiResponse response)
    {
        ArrayList<String> data = new ArrayList<String>();
        Set<Plugin> plugins = Abstraction.getPluginManager().getPlugins();
        for (Plugin currentPlugin : plugins)
        {
            data.add(currentPlugin.getName());
        }
        response.setContent(data);
    }

    @Action(parameters = {"plugin"}, serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        String pluginName = request.params.getString("plugin");
        Plugin targetPlugin = Abstraction.getPluginManager().getPlugin(pluginName);
        if (targetPlugin != null)
        {
            Map<String, Object> data = new HashMap<String, Object>();
            PluginDescription description = targetPlugin.getDescription();
            data.put("name", description.getName());
            data.put("fullName", description.getFullName());
            data.put("version", description.getVersion());
            data.put("description", description.getDescription());
            data.put("website", description.getWebsite());
            data.put("authors", description.getAuthors());
            data.put("depend", description.getDepends());
            // TODO add abstraction to get commands
            data.put("commands", ((BukkitPluginDescription)description).getHandle().getCommands());
            data.put("enabled", targetPlugin.isEnabled());
            data.put("dataFolder", targetPlugin.getDataFolder().getAbsolutePath());
            response.setContent(data);
        }
        else
        {
            throw new ApiRequestException("Plugin not found!", 1);
        }
    }

    @Action(parameters = {"plugin"}, serializer = "json")
    public void available(ApiRequest request, ApiResponse response)
    {
        response.setContent(request.server.getPluginManager().getPlugin(request.params.getString("plugin")) != null);
    }

    @Action(parameters = {"plugin"})
    public void enable(ApiRequest request, ApiResponse response)
    {
        throw new ApiNotImplementedException();
    }

    @Action(parameters = {"plugin"})
    public void disable(ApiRequest request, ApiResponse response)
    {
        throw new ApiNotImplementedException();
    }
}
