package ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.ApiResponseFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ApiControllerManager
{
    private static ApiControllerManager instance = null;

    private final Map<String, ApiController> controllers;
    private final Map<Plugin, Collection<ApiController>> pluginControllerMap;
    private final Map<String, ApiResponseFormat> formats;

    private ApiControllerManager()
    {
        this.controllers = new HashMap<String, ApiController>();
        this.pluginControllerMap = new HashMap<Plugin, Collection<ApiController>>();
        this.formats = new HashMap<String, ApiResponseFormat>();
    }

    public static ApiControllerManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiControllerManager();
        }
        return instance;
    }

    public ApiControllerManager registerController(ApiController controller)
    {
        return this;
    }

    public ApiControllerManager unregisterController(String controller)
    {
        this.controllers.remove(controller);
        return this;
    }

    public ApiControllerManager unregisterController(ApiController controller)
    {
        return this;
    }

    public ApiControllerManager unregisterControllers(Plugin plugin)
    {
        return this;
    }

    public ApiController getController(String name)
    {
        return null;
    }

    public Collection<ApiController> getControllers(Plugin plugin)
    {
        return null;
    }

    public Collection<ApiController> getControllers()
    {
        return this.controllers.values();
    }

    public Map<String, Collection<ApiController>> getControllerMap()
    {
        return null;
    }

    public void clearControllers()
    {
        this.controllers.clear();
        this.pluginControllerMap.clear();
    }



    // annotation -> (name, format)
    public ApiControllerManager registerResponeFormat(Class<ApiResponseFormat> format)
    {
        return this;
    }

    public ApiControllerManager registerResponseFormat(String name, ApiResponseFormat format)
    {
        return this;
    }

    public ApiControllerManager unregisterResponseFormat(Class<ApiResponseFormat> format)
    {
        return this;
    }

    public ApiControllerManager unregisterResponseFormat(String name)
    {
        return this;
    }

    public ApiResponseFormat getResponseFormat(String name)
    {
        return this.formats.get(name);
    }
}
