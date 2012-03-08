package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configration;
import java.io.File;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author CodeInfection
 */
public class BukkitPlugin implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin
{
    private final Plugin plugin;
    private final PluginDescriptionFile pdf;
    private final Configration config;

    public BukkitPlugin(Plugin plugin)
    {
        this.plugin = plugin;
        this.pdf = plugin.getDescription();
        this.config = new BukkitConfigration(plugin.getConfig());
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
        this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
    }

    public void disable()
    {
        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
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
