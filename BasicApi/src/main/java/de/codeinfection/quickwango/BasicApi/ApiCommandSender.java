package de.codeinfection.quickwango.BasicApi;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
public class ApiCommandSender implements CommandSender
{
    protected boolean active;
    protected final Server server;
    protected final List<String> messages;

    public ApiCommandSender(final Server server)
    {
        this.active = false;
        this.server = server;
        this.messages = new ArrayList<String>();
    }

    public void toggleActive()
    {
        if (active)
        {
            this.active = false;
        }
        else
        {
            this.messages.clear();
            this.active = true;
        }
    }

    @Override
    public void sendMessage(String message)
    {
        if (active)
        {
            this.messages.add(message.replaceAll("\u00A7([a-f0-9])", "&$1"));
        }
    }

    public List<String> getResponse()
    {
        if (!active)
        {
            return this.messages;
        }
        return null;
    }

    public boolean isOp()
    {
        return true;
    }

    public Server getServer()
    {
        return this.server;
    }
}
