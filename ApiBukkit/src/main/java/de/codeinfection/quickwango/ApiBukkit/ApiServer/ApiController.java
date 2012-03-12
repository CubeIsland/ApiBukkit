package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.error;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ApiController is the base class for all controllers The extending class
 * must at least call the super contructor with a Plugin instance. To be able to
 * register the controller, the class also needs to be annotated with the
 * @Controller annotation
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public abstract class ApiController
{
    private final Plugin plugin;
    private final boolean authNeeded;
    private final Map<String, ApiAction> actions;
    private final String name;
    private final String serializer;

    /**
     * Initializes the controllers
     *
     * @param plugin the plugin this controllers corresponds to
     * @param authNeeded whether the controllers actions need authentication by
     * default or not
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
        this.serializer = controllerAnnotation.serializer();

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

                        debug("  Found action: " + this.name + "/" + actionName);
                        this.actions.put(actionName, new ApiAction(this, actionName, method, actionAnnotation.authenticate(), actionAnnotation.parameters(), actionAnnotation.serializer()));
                    }
                    else
                    {
                        error("Annotated method " + method.getName() + " has wrong parameters");
                    }
                }
                else
                {
                    error("Annotated method " + method.getName() + " has too few or too many parameters");
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
     * Returns the default serializer of te default action
     *
     * @return the serializer
     */
    public final String getSerializer()
    {
        return this.serializer;
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
