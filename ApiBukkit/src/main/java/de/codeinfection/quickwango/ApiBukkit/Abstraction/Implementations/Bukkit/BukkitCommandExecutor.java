package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.Abstraction.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
class BukkitCommandExecutor implements org.bukkit.command.CommandExecutor
{
    private final CommandExecutor executor;

    public BukkitCommandExecutor(CommandExecutor executor)
    {
        this.executor = executor;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return this.executor.executeCommand(new BukkitCommandSender(sender), new BukkitCommand(command), label, args);
    }
}
