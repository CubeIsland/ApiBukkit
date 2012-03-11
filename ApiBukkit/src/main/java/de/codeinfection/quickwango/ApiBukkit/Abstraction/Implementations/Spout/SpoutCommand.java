package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Command;

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
