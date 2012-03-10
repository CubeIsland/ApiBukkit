package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Command;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author CodeInfection
 */
class BukkitServer implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Server
{
    private Server server;

    public BukkitServer(Server server)
    {
        this.server = server;
    }

    public void registerCommand(Plugin plugin, Command command)
    {
        org.bukkit.plugin.Plugin bukkitPlugin = ((BukkitPlugin)plugin).getPlugin();
        if (bukkitPlugin instanceof JavaPlugin)
        {
            ((JavaPlugin)bukkitPlugin).getCommand(null);
        }
    }
}
