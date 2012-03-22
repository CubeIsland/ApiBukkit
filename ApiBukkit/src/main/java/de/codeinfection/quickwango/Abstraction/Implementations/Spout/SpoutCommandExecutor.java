package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.CommandExecutor;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.exception.CommandException;

/**
 *
 * @author CodeInfection
 */
public class SpoutCommandExecutor implements org.spout.api.command.CommandExecutor
{
    private final CommandExecutor executor;

    public SpoutCommandExecutor(CommandExecutor executor)
    {
        this.executor = executor;
    }

    public boolean processCommand(CommandSource source, Command command, CommandContext args) throws CommandException
    {
        return this.executor.executeCommand(new SpoutCommandSender(source), new SpoutCommand(command), args.getCommand(), new String[]
            {
            });
    }
}
