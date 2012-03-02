package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    @Action(serializer = "json")
    public void list(ApiRequest request, ApiResponse response)
    {
        ArrayList<String> data = new ArrayList<String>();
        Plugin[] plugins = request.server.getPluginManager().getPlugins();
        for (Plugin currentPlugin : plugins)
        {
            data.add(currentPlugin.getDescription().getName());
        }
        response.setContent(data);
    }

    @Action(parameters = {"plugin"}, serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        String pluginName = request.params.getString("plugin");
        Plugin targetPlugin = request.server.getPluginManager().getPlugin(pluginName);
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
}
