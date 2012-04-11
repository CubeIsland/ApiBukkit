package de.cubeisland.BasicApi;

import java.util.List;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author CodeInfection
 */
public class BasicApiConfiguration
{
    public final List<String> configFiles;

    public BasicApiConfiguration(Configuration config)
    {
        this.configFiles = (List<String>)config.getList("configfiles");
    }
}
