package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.ApiBukkit.ApiPlayer;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.permissions.Permission;

/**
 *
 * @author CodeInfection
 */
public class ChatPlayer extends ApiPlayer
{
    private World world;

    public ChatPlayer(String name, Server server) {
        super(name, server);
        this.world = server.getWorlds().get(0);
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission)
    {
        return true;
    }

    @Override
    public World getWorld()
    {
        return this.world;
    }

    @Override
    public void sendMessage(String message)
    {
        this.sendRawMessage(message);
    }

    @Override
    public void sendRawMessage(String message)
    {
        BasicApi.log(message);
    }

    @Override
    public Location getLocation()
    {
        return this.world.getSpawnLocation();
    }
}
