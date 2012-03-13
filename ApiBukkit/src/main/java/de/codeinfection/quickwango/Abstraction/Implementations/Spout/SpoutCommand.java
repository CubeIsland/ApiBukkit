package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.Command;

/**
 *
 * @author CodeInfection
 */
class SpoutCommand implements Command
{
    private final org.spout.api.command.Command command;

    public SpoutCommand(org.spout.api.command.Command command)
    {
        this.command = command;
    }
}
