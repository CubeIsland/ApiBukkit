package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.Abstraction.PluginManager;
import java.util.HashSet;
import java.util.Set;
import org.spout.api.Spout;

/**
 *
 * @author CodeInfection
 */
class SpoutPluginManager implements PluginManager
{
    private org.spout.api.plugin.PluginManager pm;

    public SpoutPluginManager(org.spout.api.plugin.PluginManager pluginManager)
    {
        this.pm = Spout.getGame().getPluginManager();
    }

    public Plugin getPlugin(String name)
    {
        org.spout.api.plugin.Plugin plugin = this.pm.getPlugin(name);
        if (plugin != null)
        {
            return new SpoutPlugin(plugin);
        }
        return null;
    }

    public Set<Plugin> getPlugins()
    {
        org.spout.api.plugin.Plugin[] plugins = this.pm.getPlugins();
        Set<Plugin> wrappedPlugins = new HashSet<Plugin>(plugins.length);

        for (int i = 0; i < plugins.length; ++i)
        {
            wrappedPlugins.add(new SpoutPlugin(plugins[i]));
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
