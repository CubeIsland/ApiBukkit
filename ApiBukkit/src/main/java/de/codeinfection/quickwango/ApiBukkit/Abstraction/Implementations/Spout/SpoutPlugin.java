package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import java.io.File;
import org.spout.api.plugin.Plugin;
import org.spout.api.plugin.PluginDescriptionFile;

/**
 *
 * @author CodeInfection
 */
public class SpoutPlugin implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin
{
    private final Plugin plugin;
    private final PluginDescriptionFile pdf;
    private final Configuration config;

    public SpoutPlugin(Plugin plugin)
    {
        this(plugin, null);
    }

    public Plugin getPlugin()
    {
        return this.plugin;
    }

    public SpoutPlugin(Plugin plugin, Configuration config)
    {
        this.plugin = plugin;
        this.pdf = plugin.getDescription();
        this.config = config;
    }

    public String getName()
    {
        return this.pdf.getName();
    }

    public String getVersion()
    {
        return this.pdf.getVersion();
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

}
