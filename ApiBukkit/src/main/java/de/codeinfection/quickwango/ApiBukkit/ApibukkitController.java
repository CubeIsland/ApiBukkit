package de.codeinfection.quickwango.ApiBukkit;

import java.util.HashMap;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.Map;
import org.bukkit.Server;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;

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
        this.setAction("testing", new TestingAction());
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

                    ApiRequestController controller = ApiBukkit.getInstance().getRequestControllerByAlias(controllerName);
                    if (controller == null)
                    {
                        controller = ApiBukkit.getInstance().getRequestController(controllerName);
                    }
                    if (controller != null)
                    {
                        debug("Got controller '" + controller.getClass().getSimpleName() + "'");
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
                                debug("Running action '" + action.getClass().getSimpleName() + "'");
                                responses.put(route, action.execute(actionParams, server));
                            }
                            else
                            {
                                debug("Running default action");
                                responses.put(route, controller.defaultAction(actionName, params, server));
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
    
    private class TestingAction extends ApiRequestAction
    {
        public TestingAction()
        {
            super(false);
        }

        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            params.remove("__REQUEST_PATH__");
            params.remove("__REQUEST_USERAGENT__");
            params.remove("__REQUEST_METHOD__");
            params.remove("__REMOTE_ADDR__");

            params.put("format", "json");
            
            return params;
        }
    }
}
