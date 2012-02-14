package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiAction;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "apibukkit", authenticate = true, serializer = "json")
public class ApibukkitController extends ApiController
{
    public ApibukkitController(ApiBukkit plugin)
    {
        super(plugin);
    }

    @Action(authenticate = true)
    public void combined(ApiRequest request, ApiResponse response)
    {
        HashMap<String, Object> responses = null;
        Parameters routes = params.getParameters("routes");
        if (routes != null)
        {
            responses = new HashMap<String, Object>();

            for (Map.Entry<String, Object> entry : routes.entrySet())
            {
                String route = entry.getKey();
                debug("Route: " + route);
                String controllerName = route;
                String actionName = null;
                int delimPosition = route.indexOf("/");
                if (delimPosition > -1)
                {
                    controllerName = route.substring(0, delimPosition);
                    actionName = route.substring(delimPosition + 1);
                }

                ApiController controller = ApiBukkit.getInstance().getController(controllerName);
                if (controller != null)
                {
                    debug("Got controller '" + controller.getClass().getSimpleName() + "'");
                    ApiAction action = controller.getAction(actionName);

                    Object routeParameters = entry.getValue();
                    Parameters actionParams;
                    if (routeParameters != null && routeParameters instanceof Parameters)
                    {
                        actionParams = (Parameters)entry.getValue();
                    }
                    else
                    {
                        actionParams = new Parameters(3);
                        actionParams.put("__REQUEST_PATH__",    params.get("__REQUEST_PATH__"));
                        actionParams.put("__REQUEST_METHOD__",  params.get("__REQUEST_METHOD__"));
                        actionParams.put("__REMOTE_ADDR__",     params.get("__REMOTE_ADDR__"));
                    }
                    try
                    {
                        if (action != null)
                        {
                            debug("Running action '" + action.getClass().getSimpleName() + "'");
                            responses.put(route, action.execute(actionParams));
                        }
                        else
                        {
                            debug("Running default action");
                            responses.put(route, controller.defaultAction(actionName, params));
                        }
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
        return responses;
    }

    @Action
    public Object testing(Parameters params, Server server)
    {
        params.remove("__REQUEST_PATH__");
        params.remove("__REQUEST_USERAGENT__");
        params.remove("__REQUEST_METHOD__");
        params.remove("__REMOTE_ADDR__");

        if (params.get("format") == null)
        {
            params.put("format", "json");
        }

        return params;
    }
}
