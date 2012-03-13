package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.Configuration;
import de.codeinfection.quickwango.Abstraction.PluginDescription;
import java.io.File;
import org.spout.api.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class SpoutPlugin implements de.codeinfection.quickwango.Abstraction.Plugin
{
    private final Plugin plugin;
    private final PluginDescription desc;
    private final Configuration config;

    public SpoutPlugin(Plugin plugin)
    {
        this(plugin, null);
    }

    public Plugin getHandle()
    {
        return this.plugin;
    }

    public SpoutPlugin(Plugin plugin, Configuration config)
    {
        this.plugin = plugin;
        this.desc = new SpoutPluginDescription(plugin.getDescription());
        this.config = config;
    }

    public String getName()
    {
        return this.desc.getName();
    }

    public String getVersion()
    {
        return this.desc.getVersion();
    }

    public void enable()
    {
        this.plugin.getGame().getPluginManager().enablePlugin(this.plugin);
    }

    public void disable()
    {
        this.plugin.getGame().getPluginManager().disablePlugin(this.plugin);
    }

    public void reload()
    {
        this.disable();
        this.enable();
    }

    public File getDataFolder()
    {
        return this.plugin.getDataFolder();
    }

    public Configuration getConfiguration()
    {
        return this.config;
    }

    public boolean isEnabled()
    {
        return this.plugin.isEnabled();
    }

    public PluginDescription getDescription()
    {
        return this.desc;
    }
}
