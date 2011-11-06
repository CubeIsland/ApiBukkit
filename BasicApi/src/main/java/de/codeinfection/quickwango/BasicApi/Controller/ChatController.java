package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiPlayer;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ChatController extends ApiRequestController
{
    private Queue<String[]> chatLog;

    public ChatController(Plugin plugin)
    {
        super(plugin, true);
        this.chatLog = new ConcurrentLinkedQueue<String[]>();

        this.plugin.getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, new ChatListener(), Priority.Monitor, this.plugin);

        this.setAction("get", new GetAction());
        this.setAction("send", new SendAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }

    private class ChatPlayer extends ApiPlayer
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

    private class ChatListener extends PlayerListener
    {
        @Override
        public void onPlayerChat(PlayerChatEvent event)
        {
            if (event.isCancelled())
            {
                return;
            }
            if (chatLog.size() + 1 >= 100)
            {
                chatLog.poll();
            }
            //chatLog.add(new String[] {event.getPlayer().getName(), event.getMessage().replaceAll("\u00A7([a-f0-9])", "&$1")});
            chatLog.add(new String[] {event.getPlayer().getName(), event.getMessage()});
        }
    }
    
    private class GetAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            return chatLog;
        }
    }
    
    private class SendAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String nameParam = params.getProperty("name");
            if (nameParam != null)
            {
                String messageParam = params.getProperty("message");
                if (messageParam != null)
                {
                    Player chatPlayer = new ChatPlayer(nameParam, server);
                    PlayerChatEvent event = new PlayerChatEvent(chatPlayer, messageParam);
                    server.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                    {
                        throw new ApiRequestException("Chat was cancelled!", 3);
                    }

                    String message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                    for (Player player : event.getRecipients())
                    {
                        player.sendMessage(message);
                    }
                }
                else
                {
                    throw new ApiRequestException("No chat message given!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No chat name given!", 1);
            }
            return null;
        }
    }
}
