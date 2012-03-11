package de.codeinfection.quickwango.ApiBukkit.Abstraction;

/**
 *
 * @author CodeInfection
 */
public interface CommandExecutor
{
    public boolean executeCommand(CommandSender sender, Command command, String label, String args[]);
}
