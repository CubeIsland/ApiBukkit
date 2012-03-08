package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configration;
import java.io.File;
import org.spout.api.plugin.Plugin;
import org.spout.api.plugin.PluginDescriptionFile;
import org.spout.api.util.config.Configuration;

/**
 *
 * @author CodeInfection
 */
public class SpoutPlugin implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin
{
    private final Plugin plugin;
    private final PluginDescriptionFile pdf;
    private final Configration config;

    public SpoutPlugin(Plugin plugin)
    {
        this.plugin = plugin;
        this.pdf = plugin.getDescription();
        this.config = new SpoutConfiguration(new Configuration(new File(plugin.getDataFolder(), "config.yml")));
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

    public void relead()
    {
        this.disable();
        this.enable();
    }

    public File getDataFolder()
    {
        return this.plugin.getDataFolder();
    }

    public Configration getConfig()
    {
        return this.config;
    }

}
