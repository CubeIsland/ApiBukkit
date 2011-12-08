package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public class ApibukkitController extends ApiRequestController
{
    public ApibukkitController(ApiBukkit plugin)
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
            ArrayList<Object> responses = null;
            Parameters routes = params.getParameters("routes");
            if (routes != null)
            {
                 responses = new ArrayList<Object>();

                for (Map.Entry<String, Object> entry : routes.entrySet())
                {
                    String route = entry.getKey();
                    ApiBukkit.debug("Route: " + route);
                    String controllerName = route;
                    String actionName = null;
                    int delimPosition = route.indexOf("/");
                    if (delimPosition > -1)
                    {
                        controllerName = route.substring(0, delimPosition);
                        actionName = route.substring(delimPosition + 1);
                    }

                    ApiRequestController controller = ApiBukkit.getInstance().getRequestControllerByAlias(controllerName);
                    if (controller == null)
                    {
                        controller = ApiBukkit.getInstance().getRequestController(controllerName);
                    }
                    if (controller != null)
                    {
                        ApiBukkit.debug("Got controller '" + controller.getClass().getSimpleName() + "'");
                        ApiRequestAction action = controller.getActionByAlias(actionName);
                        if (action == null)
                        {
                            action = controller.getAction(actionName);
                        }

                        Object routeParameters = entry.getValue();
                        Parameters actionParams = null;
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
                                ApiBukkit.debug("Running action '" + action.getClass().getSimpleName() + "'");
                                responses.add(action.execute(actionParams, server));
                            }
                            else
                            {
                                ApiBukkit.debug("Running default action");
                                responses.add(controller.defaultAction(actionName, params, server));
                            }
                        }
                        catch (ApiRequestException e)
                        {
                            throw new ApiRequestException("A route has thrown a ApiRequestAction!", 2);
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
    }
}
