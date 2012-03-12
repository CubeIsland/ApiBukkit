package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CodeInfection
 */
public class ApiConfiguration
{
    public final ApiLogLevel logLevel;
    public final int port;
    public final String authKey;
    public final int maxContentLength;
    public final Map<String, Collection<String>> disabledActions;
    public final boolean blacklistEnabled;
    public final List<String> blacklist;
    public final boolean whitelistEnabled;
    public final List<String> whitelist;

    public ApiConfiguration(Configuration config)
    {
        ApiLogLevel tmpLogLevel = ApiLogLevel.DEFAULT;
        try
        {
            tmpLogLevel = ApiLogLevel.getLogLevel(config.<String>get("General.logLevel"));
        }
        catch (Exception e)
        {
            ApiBukkit.logException(e);
        }
        this.logLevel = tmpLogLevel;
        this.port = config.<Integer>get("Network.port");
        this.authKey = config.<String>get("Network.authKey");
        this.maxContentLength = config.<Integer>get("Network.maxContentLength");

        this.whitelistEnabled = config.<Boolean>get("Whitelist.enabled", this.whitelistEnabled);
        this.whitelist = config.<String>getList("Whitelist.IPs");

        this.blacklistEnabled = config.<Boolean>get("Blacklist.enabled");
        this.blacklist = config.<String>getList("Blacklist.IPs");

        Map<String, Object> map = config.getMap("DisabledActions");
        this.disabledActions = new HashMap<String, Collection<String>>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof List)
            {
                List<String> list = (List<String>)entry.getValue();
                disabledActions.put(entry.getKey(), list);
            }
        }
    }
}
