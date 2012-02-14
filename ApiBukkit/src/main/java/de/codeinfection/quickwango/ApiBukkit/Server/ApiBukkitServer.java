package de.codeinfection.quickwango.ApiBukkit.Server;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Parameters;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.UnauthorizedRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiAction;
import de.codeinfection.quickwango.ApiBukkit.*;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class ApiBukkitServer extends WebServer
{
    private String authenticationKey;
    private final ConcurrentHashMap<String, ApiResponseFormat> responseFormats;
    private final Map<String, ApiController> controllers;
    private String defaultResponseFormat = "plain";

    public ApiBukkitServer(ApiConfiguration config) throws IOException
    {
        super(config);

        this.responseFormats = new ConcurrentHashMap<String, ApiResponseFormat>();
        
        this.addResponseFormat("plain", new PlainFormat());
        this.addResponseFormat("json", new JsonFormat());
        this.addResponseFormat("xml", new XMLFormat());
        this.addResponseFormat("raw", new RawFormat());
        
        this.controllers = new ConcurrentHashMap<String, ApiController>();
        this.registerController(new ApibukkitController(ApiBukkit.getInstance()));
    }

    public void start(int port, int maxSessions, String authKey) throws IOException
    {
        this.authenticationKey = authKey;
        this.start(port, maxSessions);
    }

    @Override
    public Response processRequest(String uri, InetAddress remoteIp, String method, Map<String, String> header, Parameters params)
    {
        params.put("__REQUEST_PATH__", uri);
        params.put("__REQUEST_METHOD__", method);
        params.put("__REMOTE_ADDR__", remoteIp.getHostAddress());
        ApiBukkit.log(String.format("'%s' requested '%s'", remoteIp.getHostAddress(), uri), ApiLogLevel.INFO);
        String useragent = header.get("apibukkit-useragent");
        if (useragent != null)
        {
            params.put("__REQUEST_USERAGENT__", useragent);
            ApiBukkit.log("Useragent: " + useragent, ApiLogLevel.INFO);
        }
        uri = uri.substring(1);
        if (uri.length() == 0)
        {
            ApiBukkit.error("Invalid path requested!");
            return new Response(Status.BADREQUEST, MimeType.PLAIN, this.error(ApiError.INVALID_PATH));
        }
        String[] pathParts = uri.split("/");
        
        String controllerName = null;
        String actionName = null;
        if (pathParts.length >= 1)
        {
            controllerName = pathParts[0];
        }
        if (pathParts.length >= 2)
        {
            actionName = pathParts[1];
        }
        if (pathParts.length < 1 || controllerName == null)
        {
            ApiBukkit.error("Invalid path requested!");
            return new Response(Status.BADREQUEST, MimeType.PLAIN, this.error(ApiError.INVALID_PATH));
        }
        
        Object response;
        ApiController controller = this.controllers.get(controllerName);
        if (controller != null)
        {
            ApiBukkit.debug("Selected controller '" + controller.getClass().getSimpleName() + "'");

            try
            {
                String authKey = params.getString("authkey");
                params.remove("authkey");
                
                ApiAction action = controller.getAction(actionName);
                if (this.config.disabledActions.containsKey(controllerName))
                {
                    List<String> disabledActions = this.config.disabledActions.get(controllerName);
                    if (disabledActions.contains(actionName) || disabledActions.contains("*"))
                    {
                        ApiBukkit.error("Requested action is disabled!");
                        return new Response(Status.FORBIDDEN, MimeType.PLAIN, this.error(ApiError.ACTION_DISABLED));
                    }
                }
                if (action != null)
                {
                    this.authorized(authKey, action);
                    
                    ApiBukkit.debug("Running action '" + actionName + "'");
                    response = action.execute(params, Bukkit.getServer());
                }
                else
                {
                    this.authorized(authKey, controller);
                    
                    ApiBukkit.debug("Runnung default action");
                    response = controller.defaultAction(actionName, params);
                }
            }
            catch (UnauthorizedRequestException e)
            {
                ApiBukkit.error("Wrong authentication key!");
                return new Response(Status.UNAUTHORIZED, MimeType.PLAIN, this.error(ApiError.AUTHENTICATION_FAILURE));
            }
            catch (ApiRequestException e)
            {
                ApiBukkit.error("ControllerException: " + e.getMessage());
                return new Response(Status.BADREQUEST, MimeType.PLAIN, this.error(ApiError.REQUEST_EXCEPTION, e.getErrCode()));
            }
            catch (UnsupportedOperationException e)
            {
                ApiBukkit.error("action not implemented");
                return new Response(Status.NOTIMPLEMENTED, MimeType.PLAIN, this.error(ApiError.ACTION_NOT_IMPLEMENTED));
            }
            catch (Throwable t)
            {
                ApiBukkit.logException(t);
                return new Response(Status.INTERNALERROR, MimeType.PLAIN, this.error(ApiError.UNKNONW_ERROR));
            }
        }
        else
        {
            ApiBukkit.error("Controller not found!");
            return new Response(Status.NOTFOUND, MimeType.PLAIN, this.error(ApiError.CONTROLLER_NOT_FOUND));
        }
        
        if (response != null)
        {
            String formatProperty = params.getString("format", defaultResponseFormat);
            ApiResponseFormat responseFormat = this.getResponseFormat(formatProperty);
            
            ApiBukkit.debug("Responding normally: HTTP 200");
            return new Response(Status.OK, responseFormat.getMime(), responseFormat.format(response));
        }
        else
        {
            ApiBukkit.debug("Responding without content: HTTP 204");
            return new Response(Status.NOCONTENT, MimeType.PLAIN, "");
        }
    }

    private void authorized(String key, ApiController controller)
    {
        ApiBukkit.debug("Authkey: " + key);
        if (controller.isAuthNeeded() && !this.authenticationKey.equals(key))
        {
            throw new UnauthorizedRequestException();
        }
    }

    private void authorized(String key, ApiAction action)
    {
        ApiBukkit.debug("Authkey: " + key);
        if (action.isAuthNeeded() && !this.authenticationKey.equals(key))
        {
            throw new UnauthorizedRequestException();
        }
    }

    protected String error(ApiError error)
    {
        return this.getResponseFormat("plain").format(error);
    }

    protected String error(ApiError error, int errCode)
    {
        return this.getResponseFormat("plain").format(new Object[] {
            error,
            errCode
        });
    }


    /*
     * Public API
     */

    /**
     * Returns the response format with the given name, the default response format or the plain response format.
     *
     * @param name the name of the response format
     * @return see description
     */
    public ApiResponseFormat getResponseFormat(String name)
    {
        if (name != null)
        {
            name = name.toLowerCase();
            if (this.responseFormats.containsKey(name))
            {
                return this.responseFormats.get(name);
            }
            if (this.responseFormats.containsKey(this.defaultResponseFormat))
            {
                return this.responseFormats.get(this.defaultResponseFormat);
            }
        }
        
        return this.responseFormats.get("plain");
    }

    /**
     * Adds a new response format.
     *
     * @param name the name of the response format
     * @param format the response format
     */
    public final void addResponseFormat(String name, ApiResponseFormat format)
    {
        if (name != null && format != null)
        {
            name = name.toLowerCase();
            this.responseFormats.put(name, format);
            ApiBukkit.debug(String.format("Response format '%s' (%s) was added", name, format.getClass().getSimpleName()));
        }
    }

    /**
     * Removes a response format.
     *
     * @param name the name of the format to remove
     */
    public void removeResponseFormat(String name)
    {
        if (name != null)
        {
            name = name.toLowerCase();
            this.responseFormats.remove(name);
            ApiBukkit.debug(String.format("Response format '%s' was removed", name));
        }
    }

    /**
     * Sets the default response format.
     *
     * @param format the name of a registered format
     * @return whether or not the default response format was set
     */
    public boolean setDefaultResponseFormat(String format)
    {
        if (format != null)
        {
            format = format.toLowerCase();
            if (this.responseFormats.containsKey(format))
            {
                this.defaultResponseFormat = format;
                ApiBukkit.debug(String.format("Response format '%s' was set as default", format));
                return true;
            }
        }
        return false;
    }

    public final void registerController(final ApiController controller)
    {
        Controller controllerAnnotation = controller.getClass().getAnnotation(Controller.class);
        if (controllerAnnotation == null)
        {
            throw new IllegalArgumentException("Missing annotation for controller " + controller.getClass().getSimpleName());
        }
        String controllerName = controllerAnnotation.name().trim();
        if (controllerName.length() == 0)
        {
            controllerName = controller.getClass().getSimpleName();
        }
        controllerName = controllerName.toLowerCase();

        if (controllerName.equals("apibukkit") && this.controllers.containsKey("apibukkit"))
        {
            debug("apibukkit is reserved!");
            return;
        }

        debug("Registering " + controllerName + "...");

        if (!controller.isInitialized())
        {
            debug("The controller isn't initialized, let's do that...");
            Map<String, ApiAction> actions = new ConcurrentHashMap<String, ApiAction>();

            for (final Method method : controller.getClass().getMethods())
            {
                Action actionAnnotation = method.getAnnotation(Action.class);
                if (actionAnnotation != null)
                {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 2)
                    {
                        if (parameterTypes[0].equals(Parameters.class) && parameterTypes[1].equals(Server.class))
                        {
                            String name = actionAnnotation.name().trim();
                            if (name.length() == 0)
                            {
                                name = method.getName();
                            }
                            name = name.toLowerCase();

                            debug("  Found action: " + name);
                            actions.put(name, new ApiAction(controller, name, method, actionAnnotation.authenticate(), actionAnnotation.parameters()));
                        }
                    }
                }
            }

            controller.initialize(actions);
            controller.setAuthNeeded(controllerAnnotation.authenticate());
        }

        this.controllers.put(controllerName, controller);
    }

    /**
     * Returns the default responce format.
     *
     * @return the default response format
     */
    public String getDefaultResponseFormat()
    {
        return this.defaultResponseFormat;
    }

    /**
     * Returns a request controller.
     *
     * @param name the name of the request controller
     * @return a request controller or null
     */
    public ApiController getController(String name)
    {
        if (name != null)
        {
            return this.controllers.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Returns all controllers.
     *
     * @return a map of all controllers
     */
    public Map<String, ApiController> getAllControllers()
    {
        return this.controllers;
    }

    /**
     * Removes a controller.
     * This also removes any alias which refered to the deleted controller.
     *
     * @param name the name of the controller
     */
    public void removeRequestController(String name)
    {
        if (name != null)
        {
            name = name.toLowerCase();
            this.controllers.remove(name);
            ApiBukkit.debug("Removed the controller '" + name + "' and all its aliases");
        }
    }

    /**
     * Removes all controllers and aliases.
     */
    public void clearRequestControllers()
    {
        this.controllers.clear();
        ApiBukkit.debug("Cleared the controllers and aliases");
    }
}
