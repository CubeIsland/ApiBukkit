package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public abstract class ApiController
{
    private final Plugin plugin;
    private boolean authNeeded;
    private final Map<String, ApiAction> actions;
    private final String name;

    /**
     * Initializes the controllers
     *
     * @param plugin the plugin this controllers corresponds to
     * @param authNeeded whether the controllers actions need authentication by default or not
     */
    public ApiController(Plugin plugin)
    {
        this.plugin = plugin;
        this.authNeeded = true;
        this.actions = new ConcurrentHashMap<String, ApiAction>();
        
        
        Class<? extends ApiController> clazz = this.getClass();
        Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
        if (controllerAnnotation == null)
        {
            throw new IllegalArgumentException("Missing annotation for controller " + clazz.getSimpleName());
        }
        this.name = controllerAnnotation.name().trim().toLowerCase();

        for (final Method method : clazz.getDeclaredMethods())
        {
            Action actionAnnotation = method.getAnnotation(Action.class);
            if (actionAnnotation != null)
            {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 2)
                {
                    if (parameterTypes[0].equals(ApiRequest.class) && parameterTypes[1].equals(ApiResponse.class))
                    {
                        String actionName = actionAnnotation.name().trim();
                        if (actionName.length() == 0)
                        {
                            actionName = method.getName();
                        }
                        actionName = actionName.toLowerCase();

                        debug("  Found action: " + actionName);
                        this.actions.put(actionName, new ApiAction(this, actionName, method, actionAnnotation.authenticate(), actionAnnotation.parameters(), actionAnnotation.serializer()));
                    }
                }
            }
        }
    }

    /**
     * Returns the name of this controller
     *
     * @return the name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the corresponding plugin.
     *
     * @return the currensponding plugin
     */
    public final Plugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Returns whether this actions needs authentication.
     *
     * @return true if auth is needed, otherwise false
     */
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * Returns whether this actions needs authentication.
     *
     * @return true if auth is needed, otherwise false
     */
    public final void setAuthNeeded(boolean authNeeded)
    {
        this.authNeeded = authNeeded;
    }

    /**
     * Sets an action for the given name.
     *
     * @param name the name
     * @param action the action
     */
    public final void setAction(String name, ApiAction action)
    {
        if (name != null && action != null)
        {
            name = name.toLowerCase();
            this.actions.put(name, action);
            debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getSimpleName()));
        }
    }

    /**
     * Returns the action with given name.
     *
     * @param name the name
     * @return the action
     */
    public final ApiAction getAction(String name)
    {
        if (name != null)
        {
            return this.actions.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Returns all actions.
     *
     * @return a map of all actions
     */
    public final Map<String, ApiAction> getActions()
    {
        return this.actions;
    }

    /**
     * This method will be called if the requested action was not found.
     *
     * @param action the name of the action which was requested
     * @param server a org.bukkit.Server instance
     * @return the response as an Object
     * @throws ApiRequestException
     */
    public void defaultAction(String action, ApiRequest request, ApiResponse response)
    {
        response.setContent(this.getActions().keySet());
    }
}
