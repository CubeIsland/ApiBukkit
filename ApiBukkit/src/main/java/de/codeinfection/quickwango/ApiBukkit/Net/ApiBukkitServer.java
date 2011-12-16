package de.codeinfection.quickwango.ApiBukkit.Net;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiConfiguration;
import de.codeinfection.quickwango.ApiBukkit.ApiLogLevel;
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.*;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApibukkitController;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

public class ApiBukkitServer extends WebServer
{
    protected String authenticationKey;
    protected final ConcurrentHashMap<String, ApiResponseFormat> responseFormats;
    protected final Map<String, ApiRequestController> requestControllers;
    protected final Map<String, String> requestControllerAliases;
    protected String defaultResponseFormat = "plain";

    public ApiBukkitServer(ApiConfiguration config) throws IOException
    {
        super(config);

        this.responseFormats = new ConcurrentHashMap<String, ApiResponseFormat>();
        
        this.addResponseFormat("plain", new PlainFormat());
        this.addResponseFormat("json", new JsonFormat());
        this.addResponseFormat("xml", new XMLFormat());
        this.addResponseFormat("raw", new RawFormat());
        
        this.requestControllers = new ConcurrentHashMap<String, ApiRequestController>();
        this.requestControllerAliases = new ConcurrentHashMap<String, String>();
        this.requestControllers.put("apibukkit", new ApibukkitController(null));
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
        
        Object response = null;
        ApiRequestController controller = null;

        if (this.requestControllerAliases.containsKey(controllerName))
        {
            controller = this.requestControllers.get(this.requestControllerAliases.get(controllerName));
        }
        if (controller == null)
        {
            controller = this.requestControllers.get(controllerName);
        }
        if (controller != null)
        {
            ApiBukkit.debug("Selected controller '" + controller.getClass().getSimpleName() + "'");
            try
            {
                String authKey = params.getString("authkey");
                params.remove("authkey");
                
                ApiRequestAction action = controller.getActionByAlias(actionName);
                if (action == null)
                {
                    action = controller.getAction(actionName);
                }
                if (this.config.disabledActions.containsKey(controllerName))
                {
                    List<String> disabledActions = this.config.disabledActions.get(controllerName);
                    if (disabledActions.contains(actionName) || disabledActions.contains("*"))
                    {
                        ApiBukkit.error("Requested action is disabled!");
                        return new Response(Status.FORBIDDEN, MimeType.PLAIN, this.error(ApiError.ACTION_DISABLED));
                    }
                }
                boolean authNeeded = controller.isAuthNeeded();
                if (action != null)
                {
                    Boolean actionAuthNeeded = action.isAuthNeeded();
                    if (actionAuthNeeded != null)
                    {
                        authNeeded = actionAuthNeeded;
                    }
                    
                    this.authorized(authKey, authNeeded);
                    
                    ApiBukkit.debug("Running action '" + actionName + "'");
                    response = action.execute(params, Bukkit.getServer());
                }
                else
                {
                    this.authorized(authKey, authNeeded);
                    
                    ApiBukkit.debug("Runnung default action");
                    response = controller.defaultAction(actionName, params, Bukkit.getServer());
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
            String formatProperty = params.getProperty("format", defaultResponseFormat);
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

    private void authorized(String key, boolean needed) throws UnauthorizedRequestException
    {
        ApiBukkit.debug("Authkey: " + key);
        if (needed && !this.authenticationKey.equals(key))
        {
            throw new UnauthorizedRequestException();
        }
    }

    protected String error(ApiError error)
    {
        ApiResponseFormat formater = this.getResponseFormat("plain");
        return formater.format(error);
    }

    protected String error(ApiError error, int errCode)
    {
        ApiResponseFormat formater = this.getResponseFormat("plain");
        return formater.format(new Object[] {
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
    public ApiRequestController getRequestController(String name)
    {
        if (name != null)
        {
            return this.requestControllers.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Returns a request controller by an alias.
     *
     * @param alias the alias
     * @return the controllers refered by the alias
     */
    public ApiRequestController getRequestControllerByAlias(String alias)
    {
        if (alias != null)
        {
            return this.getRequestController(this.requestControllerAliases.get(alias.toLowerCase()));
        }
        return null;
    }

    /**
     * Returns all controllers.
     *
     * @return a map of all controllers
     */
    public Map<String, ApiRequestController> getAllRequestControllers()
    {
        return this.requestControllers;
    }

    /**
     * Sets a request controller.
     *
     * @param name the name of hte controller
     * @param controller the controller
     * @return false an failure
     */
    public boolean setRequestController(String name, ApiRequestController controller)
    {
        if (controller != null)
        {
            name = name.toLowerCase();
            if (!name.equals("apibukkit"))
            {
                this.requestControllers.put(name, controller);
                ApiBukkit.debug(String.format("Set the controller '%s' on '%s'", controller.getClass().getSimpleName(), name));
                return true;
            }
        }
        return false;
    }

    /**
     * Sets an alias for controller.
     *
     * @param alias the name of the alias
     * @param controller the name of the controller to refer
     * @return false on failure
     */
    public boolean setRequestControllerAlias(String alias, String controller)
    {
        if (alias != null && controller != null)
        {
            alias = alias.toLowerCase();
            controller = controller.toLowerCase();
            if (this.requestControllers.containsKey(controller) && !alias.equals("apibukkit"))
            {
                this.requestControllerAliases.put(alias, controller);
                ApiBukkit.debug(String.format("Set the alias '%s' for the controller '%s'", alias, controller));
                return true;
            }
        }
        return false;
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
            this.requestControllers.remove(name);
            // remove aliases of the deleted controllers as well
            for (Map.Entry<String, String> entry : this.requestControllerAliases.entrySet())
            {
                if (entry.getValue().equals(name))
                {
                    this.requestControllerAliases.remove(entry.getKey());
                }
            }
            ApiBukkit.debug("Removed the controller '" + name + "' and all its aliases");
        }
    }

    /**
     * Removes a controller alias.
     *
     * @param name the name of the alias
     */
    public void removeRequestControllerAlias(String name)
    {
        if (name != null)
        {
            name = name.toLowerCase();
            this.requestControllerAliases.remove(name);
            ApiBukkit.debug("Removed the controller alias '" + name + "'");
        }
    }

    /**
     * Removes all controllers and aliases.
     */
    public void clearRequestControllers()
    {
        this.requestControllers.clear();
        this.requestControllerAliases.clear();
        ApiBukkit.debug("Cleared the controllers and aliases");
    }

    /**
     * Removes all aliases.
     */
    public void clearRequestControllerAliases()
    {
        this.requestControllerAliases.clear();
        ApiBukkit.debug("Cleared the aliases");
    }
}
