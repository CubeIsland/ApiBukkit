package de.codeinfection.quickwango.ApiBukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;

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
    public final Map<String, List<String>> disabledActions;
    public final boolean blacklistEnabled;
    public final List<String> blacklist;
    public final boolean whitelistEnabled;
    public final List<String> whitelist;

    public ApiConfiguration(Configuration config)
    {
        ApiLogLevel tmpLogLevel = ApiLogLevel.DEFAULT;
        try
        {
            tmpLogLevel = ApiLogLevel.getLogLevel(config.getString("General.logLevel"));
        }
        catch (Exception e)
        {
            ApiBukkit.logException(e);
        }
        this.logLevel = tmpLogLevel;
        this.port = config.getInt("Network.port");
        this.authKey = config.getString("Network.authKey");
        this.maxContentLength = config.getInt("Network.maxContentLength");

        this.whitelistEnabled = config.getBoolean("Whitelist.enabled", this.whitelistEnabled);
        this.whitelist = (List<String>)config.getList("Whitelist.IPs");

        this.blacklistEnabled = config.getBoolean("Blacklist.enabled");
        this.blacklist = (List<String>)config.getList("Blacklist.IPs");
        
        Map<String, Object> sectionValues = config.getConfigurationSection("DisabledActions").getValues(true);
        this.disabledActions = new HashMap<String, List<String>>();
        for (Map.Entry<String, Object> entry : sectionValues.entrySet())
        {
            if (entry.getValue() instanceof List)
            {
                List<String> list = (List<String>)entry.getValue();
                disabledActions.put(entry.getKey(), list);
            }
        }
    }
}
