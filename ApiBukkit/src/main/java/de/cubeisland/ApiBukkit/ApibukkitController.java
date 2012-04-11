package de.cubeisland.ApiBukkit;

import static de.cubeisland.ApiBukkit.ApiBukkit.debug;
import de.cubeisland.ApiBukkit.ApiServer.Action;
import de.cubeisland.ApiBukkit.ApiServer.ApiAction;
import de.cubeisland.ApiBukkit.ApiServer.ApiController;
import de.cubeisland.ApiBukkit.ApiServer.ApiManager;
import de.cubeisland.ApiBukkit.ApiServer.ApiRequest;
import de.cubeisland.ApiBukkit.ApiServer.ApiResponse;
import de.cubeisland.ApiBukkit.ApiServer.Controller;
import de.cubeisland.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import de.cubeisland.ApiBukkit.ApiServer.Parameters;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "apibukkit", authenticate = true, serializer = "json")
public class ApibukkitController extends ApiController
{
    ApiManager manager;

    public ApibukkitController(Plugin plugin)
    {
        super(plugin);
        this.manager = ApiManager.getInstance();
    }

    @Action(authenticate = true, parameters = {"routes"})
    public void combined(ApiRequest request, ApiResponse response)
    {
        HashMap<String, Object> responses;
        Map<Object, Object> routes = request.params.getJSONDecoded("routes");
        if (routes != null)
        {
            responses = new HashMap<String, Object>();
            
            ApiResponse apiResponse = new ApiResponse(this.manager.getDefaultSerializer());

            for (Map.Entry entry : routes.entrySet())
            {
                String route = String.valueOf(entry.getKey());
                debug("Route: " + route);
                String controllerName = route;
                String actionName = null;
                int delimPosition = route.indexOf("/");
                if (delimPosition > -1)
                {
                    controllerName = route.substring(0, delimPosition);
                    actionName = route.substring(delimPosition + 1);
                }

                ApiController controller = this.manager.getController(controllerName);
                if (controller != null)
                {
                    debug("Got controller '" + controller.getClass().getSimpleName() + "'");
                    ApiAction action = controller.getAction(actionName);

                    Object routeParameters = entry.getValue();
                    if (routeParameters != null && routeParameters instanceof Parameters)
                    {
                        request.params.putAll((Parameters)entry.getValue());
                    }
                    try
                    {
                        if (action != null)
                        {
                            debug("Running action '" + action.getClass().getSimpleName() + "'");
                            action.execute(request, apiResponse);
                            responses.put(route, apiResponse.getContent());
                        }
                        else
                        {
                            debug("Running default action");
                            controller.defaultAction(request, apiResponse);
                            responses.put(route, apiResponse.getContent());
                        }
                        apiResponse.clearHeaders().setContent(null);
                    }
                    catch (ApiRequestException e)
                    {
                        throw new ApiRequestException("A route has thrown an ApiRequestAction!", 2);
                    }
                    catch (Throwable t)
                    {
                        throw new ApiRequestException("A route has thrown an unknown exception!", 2);
                    }
                }
                else
                {
                    throw new ApiRequestException("Controller '" + controllerName + "' could not be found!", 1);
                }
            }
        }
    }

    @Action(authenticate = false)
    public void testing(ApiRequest request, ApiResponse response)
    {
        //response.setContent(request.params);
    }
}
