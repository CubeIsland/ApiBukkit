package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer.PlainSerializer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
        this.controllers         = Collections.synchronizedMap(new HashMap<String, ApiController>());
        this.responseSerializers = Collections.synchronizedMap(new HashMap<String, ApiResponseSerializer>());
        this.disabledActions     = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
        this.whitelist           = Collections.synchronizedList(new ArrayList<String>());
        this.blacklist           = Collections.synchronizedList(new ArrayList<String>());

        this.whitelistEnabled = false;
        this.blacklistEnabled = false;

        this.defaultSerializer = new PlainSerializer();
        this.registerSerializer("plain", this.defaultSerializer);
    }

    public static ApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiManager();
        }
        return instance;
    }

    public ApiManager registerController(ApiController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        this.controllers.put(controller.getName(), controller);

        return this;
    }

    public ApiManager unregisterController(String controller)
    {
        this.controllers.remove(controller);
        return this;
    }

    public ApiManager unregisterController(ApiController controller)
    {
        if (controller != null)
        {
            this.unregisterController(controller.getName());
        }
        return this;
    }

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

    public ApiController getController(String name)
    {
        if (name != null)
        {
            
        }
        return null;
    }

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
    }

    public ApiManager registerSerializer(ApiResponseSerializer serializer)
    {
        ResponseSerializer formatAnnotation = serializer.getClass().getAnnotation(ResponseSerializer.class);
        if (formatAnnotation == null)
        {
            throw new IllegalArgumentException("The class of serializer must be annotated with @ResponseSerializer");
        }
        this.registerSerializer(formatAnnotation.name(), serializer);
        return this;
    }

    public ApiManager registerSerializer(String name, ApiResponseSerializer format)
    {
        this.responseSerializers.put(name, format);
        return this;
    }

    public ApiManager unregisterSerializer(String name)
    {
        this.responseSerializers.remove(name);
        return this;
    }

    public ApiManager clearSerializers()
    {
        this.responseSerializers.clear();
        return this;
    }

    public ApiResponseSerializer getSerializer(String name)
    {
        return this.responseSerializers.get(name);
    }

    public ApiResponseSerializer getDefaultSerializer()
    {
        return this.defaultSerializer;
    }

    public ApiManager setDefaultSerializer(ApiResponseSerializer serializer)
    {
        if (serializer == null)
        {
            throw new IllegalArgumentException("serializer must not be null!");
        }
        this.defaultSerializer = serializer;
        return this;
    }

    public boolean isWhitelistEnabled()
    {
        return this.whitelistEnabled;
    }

    public ApiManager setWhitelistEnabled(boolean state)
    {
        this.whitelistEnabled = state;
        return this;
    }

    public ApiManager setWhitelist(Collection whitelist)
    {
        this.whitelist.clear();
        this.whitelist.addAll(whitelist);
        return this;
    }

    public boolean isWhitelisted(InetSocketAddress ip)
    {
        return this.isWhitelisted(ip.getAddress());
    }

    public boolean isWhitelisted(InetAddress ip)
    {
        return this.isWhitelisted(ip.getHostAddress());
    }

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

    public ApiManager setBlacklistEnabled(boolean state)
    {
        this.blacklistEnabled = state;
        return this;
    }

    public boolean isBlacklistEnabled()
    {
        return this.blacklistEnabled;
    }

    public ApiManager setBlacklist(Collection blacklist)
    {
        this.blacklist.clear();
        this.blacklist.addAll(blacklist);
        return this;
    }

    public boolean isBlacklisted(InetSocketAddress ip)
    {
        return this.isBlacklisted(ip.getAddress());
    }

    public boolean isBlacklisted(InetAddress ip)
    {
        return this.isBlacklisted(ip.getHostAddress());
    }

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

    public ApiManager setDisabledActions(Map<String, Collection<String>> disabledActions)
    {
        this.disabledActions.clear();
        this.disabledActions.putAll(disabledActions);
        return this;
    }

    public boolean isActionDisabled(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (action == null)
        {
            return false;
        }
        return (actions.contains(action) || actions.contains("*"));
    }
    
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

    public ApiManager removeDisabledActions(String controller)
    {
        this.disabledActions.remove(controller);
        return this;
    }

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
