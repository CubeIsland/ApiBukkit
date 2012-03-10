package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author CodeInfection
 */
class BukkitPluginManager implements de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager
{
    private final PluginManager pm;

    public BukkitPluginManager(PluginManager pluginManager)
    {
        this.pm = pluginManager;
    }

    public Plugin getPlugin(String name)
    {
        org.bukkit.plugin.Plugin plugin = this.pm.getPlugin(name);
        if (plugin != null)
        {
            return new BukkitPlugin(plugin);
        }
        return null;
    }

    public Set<Plugin> getPlugins()
    {
        org.bukkit.plugin.Plugin[] plugins = this.pm.getPlugins();
        Set<Plugin> wrappedPlugins = new HashSet<Plugin>(plugins.length);

        for (int i = 0; i < plugins.length; ++i)
        {
            wrappedPlugins.add(new BukkitPlugin(plugins[i]));
        }

        return wrappedPlugins;
    }

    public void enablePlugin(Plugin plugin)
    {
        plugin.enable();
    }

    public void disablePlugin(Plugin plugin)
    {
        plugin.disable();
    }

    public void reloadPlugin(Plugin plugin)
    {
        plugin.reload();
    }

}
