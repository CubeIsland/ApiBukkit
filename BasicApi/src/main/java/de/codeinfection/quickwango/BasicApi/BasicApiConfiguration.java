package de.codeinfection.quickwango.BasicApi;

import java.util.List;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author CodeInfection
 */
public class BasicApiConfiguration
{
    public final List<String> configFiles;
    public final String chatFormat;

    public BasicApiConfiguration(Configuration config)
    {
        this.configFiles = (List<String>)config.getList("configfiles");
        this.chatFormat = config.getString("chatformat").replaceAll("\\{NAME\\}", "%1\\$s").replaceAll("\\{MESSAGE\\}", "%2\\$s");
    }
}
