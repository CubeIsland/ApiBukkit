package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer.PlainSerializer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public final class ApiManager
{
    private static ApiManager instance = null;

    private final Map<String, ApiController> controllers;
    private final Map<String, ApiResponseSerializer> responseSerializers;
    private final Map<String, Collection<String>> disabledActions;

    private boolean whitelistEnabled;
    private final Collection<String> whitelist;
    private boolean blacklistEnabled;
    private final Collection<String> blacklist;

    private ApiResponseSerializer defaultSerializer;

    private ApiManager()
    {
        this.controllers         = new ConcurrentHashMap<String, ApiController>();
        this.responseSerializers = new ConcurrentHashMap<String, ApiResponseSerializer>();
        this.disabledActions     = new ConcurrentHashMap<String, Collection<String>>();
        this.whitelist           = Collections.synchronizedList(new ArrayList<String>());
        this.blacklist           = Collections.synchronizedList(new ArrayList<String>());

        this.whitelistEnabled = false;
        this.blacklistEnabled = false;

        this.defaultSerializer = new PlainSerializer();
        this.registerSerializer("plain", this.defaultSerializer);
    }

    /**
     * Returns the singleton instance of the ApiManager
     *
     * @return the manager instance
     */
    public static ApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiManager();
        }
        return instance;
    }

    /**
     * Checks whether there is a controller with the given name
     *
     * return true if it exists
     */
    public boolean isControllerRegistered(String controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        return this.controllers.containsKey(controller.toLowerCase());
    }

    /**
     * checks whether the given controller is registered
     *
     * @return true if it is registered
     */
    public boolean isControllerRegistered(ApiController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        return this.isControllerRegistered(controller.getName());
    }

    /**
     * Registeres a controller
     *
     * @return fluent interface
     */
    public ApiManager registerController(ApiController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        this.controllers.put(controller.getName(), controller);

        return this;
    }

    /**
     * Unregisteres a controller by name
     *
     * @return fluent interface
     */
    public ApiManager unregisterController(String controller)
    {
        this.controllers.remove(controller);
        return this;
    }

    /**
     * Unregisteres a controller
     *
     * @return fluent interface
     */
    public ApiManager unregisterController(ApiController controller)
    {
        if (controller != null)
        {
            this.unregisterController(controller.getName());
        }
        return this;
    }

    /**
     * Unregisteres all controllers of the given plugin
     *
     * @return fluent interface
     */
    public ApiManager unregisterControllers(Plugin plugin)
    {
        if (plugin != null)
        {
            for (ApiController controller : this.getControllers(plugin))
            {
                this.unregisterController(controller);
            }
        }
        return this;
    }

    /**
     * Gets a controller by name
     *
     * @return the registered controller or null if it does not exist
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
     * Gets all controllers of a plugin
     * 
     * @return a collection of all the controllers
     */
    public Collection<ApiController> getControllers(Plugin plugin)
    {
        Collection<ApiController> controllersOfPlugin = new ArrayList<ApiController>();
        for (ApiController contorller : this.getControllers())
        {
            if (contorller.getPlugin().equals(plugin))
            {
                controllersOfPlugin.add(contorller);
            }
        }
        return controllersOfPlugin;
    }

    /**
     * Returns all controllers
     *
     * @return a collection of all controllers
     */
    public Collection<ApiController> getControllers()
    {
        return this.controllers.values();
    }

    /**
     * Returns a copy of the name-controller map
     *
     * return the name-controller map
     */
    public Map<String, ApiController> getControllerMap()
    {
        return new HashMap<String, ApiController>(this.controllers);
    }

    /**
     * Clears the controllers
     *
     * @return fluent interface
     */
    public ApiManager clearControllers()
    {
        this.controllers.clear();
        return this;
    }

    /**
     * Checks whether there is a serializer with the given name
     *
     * @return true if there is one
     */
    public boolean isSerializerRegistered(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name must not be null!");
        }
        return this.responseSerializers.containsKey(name.toLowerCase());
    }

    /**
     * Registeres a serializer
     *
     * @return fluent interface
     */
    public ApiManager registerSerializer(ApiResponseSerializer serializer)
    {
        ResponseSerializer annotation = serializer.getClass().getAnnotation(ResponseSerializer.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("The class of serializer must be annotated with @ResponseSerializer");
        }
        this.registerSerializer(annotation.name().toLowerCase(), serializer);
        return this;
    }

    /**
     * Registeres a serializer with the given name
     *
     * @return fluent interface
     */
    public ApiManager registerSerializer(String name, ApiResponseSerializer serializer)
    {
        if (name != null && serializer != null)
        {
            this.responseSerializers.put(name.toLowerCase(), serializer);
        }
        return this;
    }

    /**
     * Unregisteres a serializer by name
     *
     * @return fluent interface
     */
    public ApiManager unregisterSerializer(String name)
    {
        if (name != null)
        {
            this.responseSerializers.remove(name.toLowerCase());
        }
        return this;
    }

    /**
     * Clears al serializers
     *
     * @return fluent interface
     */
    public ApiManager clearSerializers()
    {
        this.responseSerializers.clear();
        return this;
    }

    /**
     * Gets a serializer by name
     *
     * @return the serializer or null if it does not exist
     */
    public ApiResponseSerializer getSerializer(String name)
    {
        return this.responseSerializers.get(name);
    }

    /**
     * Returns the default serializer
     * 
     * @return the serializer
     */
    public ApiResponseSerializer getDefaultSerializer()
    {
        return this.defaultSerializer;
    }

    /**
     * Sets the default serializer
     *
     * @return fluent interface
     */
    public ApiManager setDefaultSerializer(ApiResponseSerializer serializer)
    {
        if (serializer == null)
        {
            throw new IllegalArgumentException("serializer must not be null!");
        }
        this.defaultSerializer = serializer;
        return this;
    }

    /**
     * Returns whether whitelisting is enabled
     *
     * @return true if enabled
     */
    public boolean isWhitelistEnabled()
    {
        return this.whitelistEnabled;
    }

    /**
     * Sets the enabled state of the whitelisting
     *
     * @return fluent interface
     */
    public ApiManager setWhitelistEnabled(boolean state)
    {
        this.whitelistEnabled = state;
        return this;
    }

    /**
     * Sets the whitelist
     *
     * @return fluent interface
     */
    public ApiManager setWhitelist(Collection whitelist)
    {
        this.whitelist.clear();
        this.whitelist.addAll(whitelist);
        return this;
    }

    /**
     * Checks whether an InetSocketAddress is whitelisted
     *
     * @return true if it is
     */
    public boolean isWhitelisted(InetSocketAddress ip)
    {
        return this.isWhitelisted(ip.getAddress());
    }

    /**
     * Checks whether an InetAddress is whitelisted
     *
     * @return true if it is
     */
    public boolean isWhitelisted(InetAddress ip)
    {
        return this.isWhitelisted(ip.getHostAddress());
    }

    /**
     * Checks whether an string representation of an IP is whitelisted
     *
     * @return true if it is
     */
    public boolean isWhitelisted(String ip)
    {
        if (this.whitelistEnabled)
        {
            return this.whitelist.contains(ip);
        }
        else
        {
            return true;
        }
    }

    /**
     * Sets the enabled state of the blacklisting
     *
     * @return fluent interface
     */
    public ApiManager setBlacklistEnabled(boolean state)
    {
        this.blacklistEnabled = state;
        return this;
    }

    /**
     * Returns whether blacklisting is enabled
     *
     * @return true if it is
     */
    public boolean isBlacklistEnabled()
    {
        return this.blacklistEnabled;
    }

    /**
     * Sets the blacklist
     *
     * @return fluent interface
     */
    public ApiManager setBlacklist(Collection blacklist)
    {
        this.blacklist.clear();
        this.blacklist.addAll(blacklist);
        return this;
    }

    /**
     * Checks whether an InetSocketAddress is blacklisted
     *
     * @return true if it is
     */
    public boolean isBlacklisted(InetSocketAddress ip)
    {
        return this.isBlacklisted(ip.getAddress());
    }

    /**
     * Checks whether an InetAddress is blacklisted
     *
     * @return true if it is
     */
    public boolean isBlacklisted(InetAddress ip)
    {
        return this.isBlacklisted(ip.getHostAddress());
    }

    /**
     * Checks whether a string representation of an IP is blacklisted
     *
     * @return true if it is
     */
    public boolean isBlacklisted(String ip)
    {
        if (this.blacklistEnabled)
        {
            return this.blacklist.contains(ip);
        }
        else
        {
            return false;
        }
    }

    /**
     * 
     *
     * @return fluent interface
     */
    public ApiManager setDisabledActions(Map<String, Collection<String>> disabledActions)
    {
        this.disabledActions.clear();
        this.disabledActions.putAll(disabledActions);
        return this;
    }

    /**
     * 
     */
    public boolean isActionDisabled(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions == null)
        {
            return false;
        }
        return (actions.contains(action) || actions.contains("*"));
    }

    /**
     * 
     *
     * @return fluent interface
     */
    public ApiManager disableController(String controller)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            actions.clear();
            actions.add("*");
        }
        else
        {
            actions = new ArrayList(1);
            actions.add("*");
            this.disabledActions.put(controller, actions);
        }
        return this;
    }

    /**
     * 
     *
     * @return fluent interface
     */
    public ApiManager disableAction(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            if (!actions.contains(action))
            {
                actions.add(action);
            }
        }
        else
        {
            actions = new ArrayList(1);
            actions.add(action);
            this.disabledActions.put(controller, actions);
        }
        return this;
    }

    /**
     * 
     *
     * @return fluent interface
     */
    public ApiManager removeDisabledActions(String controller)
    {
        this.disabledActions.remove(controller);
        return this;
    }

    /**
     * 
     *
     * @return fluent interface
     */
    public ApiManager removeDisabledAction(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            actions.remove(action);
        }
        return this;
    }
}
