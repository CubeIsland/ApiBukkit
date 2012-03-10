package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.ImplementationProvider;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Server;
import java.io.File;
import org.bukkit.Bukkit;

/**
 *
 * @author CodeInfection
 */
public class BukkitImplementationProvider implements ImplementationProvider
{
    public String getImplementationName()
    {
        return "Bukkit";
    }

    public Server getServer()
    {
        return new BukkitServer(Bukkit.getServer());
    }

    public PluginManager getPluginManager()
    {
        return new BukkitPluginManager(Bukkit.getPluginManager());
    }

    public Configuration loadConfiguration(File file)
    {
        return new BukkitConfigration(file);
    }
}
