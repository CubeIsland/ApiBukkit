package de.codeinfection.quickwango.BasicApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

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
        System.out.println(message);
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

    public boolean isPermissionSet(String name)
    {
        return true;
    }

    public boolean isPermissionSet(Permission perm)
    {
        return true;
    }

    public boolean hasPermission(String name)
    {
        return true;
    }

    public boolean hasPermission(Permission perm)
    {
        return true;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return null;
    }

    public void removeAttachment(PermissionAttachment attachment)
    {}

    public void recalculatePermissions()
    {}

    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return null;
    }

    public void setOp(boolean value)
    {}
}
