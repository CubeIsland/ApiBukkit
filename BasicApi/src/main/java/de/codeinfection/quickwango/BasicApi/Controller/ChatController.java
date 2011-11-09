package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiPlayer;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiSerializable;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import de.codeinfection.quickwango.BasicApi.BasicApiConfiguration;
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

/**
 *
 * @author CodeInfection
 */
public class ChatController extends ApiRequestController
{
    private Queue<ChatMessage> chatLog;
    private BasicApiConfiguration config;

    public ChatController(BasicApi plugin)
    {
        super(plugin, true);
        this.config = plugin.getBasicApiConfig();
        this.chatLog = new ConcurrentLinkedQueue<ChatMessage>();

        this.plugin.getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, new ChatListener(), Priority.Monitor, this.plugin);

        this.setAction("get", new GetAction());
        this.setAction("public", new PublicAction());
        this.setAction("private", new PrivateAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }

    private class BasicApiPlayerChatEvent extends PlayerChatEvent
    {
        public BasicApiPlayerChatEvent(Player player, String message)
        {
            super(player, message);
        }
    }

    private final class ChatMessage implements ApiSerializable
    {
        public final String author;
        public final String message;
        public final boolean api;

        public ChatMessage(final String author, final String message, final boolean api)
        {
            this.author = author;
            this.message = message;
            this.api = api;
        }

        public Object serialize()
        {
            return new String[] {this.author, this.message, (this.api ? "api" : "user")};
        }
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
            chatLog.add(new ChatMessage(event.getPlayer().getName(), event.getMessage(), (event instanceof BasicApiPlayerChatEvent)));
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
    
    private class PublicAction extends ApiRequestAction
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
                    PlayerChatEvent event = new BasicApiPlayerChatEvent(chatPlayer, messageParam);
                    server.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                    {
                        throw new ApiRequestException("Chat was cancelled!", 3);
                    }

                    for (Player player : event.getRecipients())
                    {
                        player.sendMessage(String.format(config.chatFormat, event.getPlayer().getDisplayName(), event.getMessage()));
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
    
    private class PrivateAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String targetParam = params.getProperty("target");
            if (targetParam != null)
            {
                Player target = server.getPlayerExact(targetParam);
                if (target != null)
                {
                    String nameParam = params.getProperty("name");
                    if (nameParam != null)
                    {
                        String messageParam = params.getProperty("message");
                        if (messageParam != null)
                        {
                            String message = String.format(config.chatFormat, nameParam, messageParam);
                            target.sendMessage(message);
                        }
                        else
                        {
                            throw new ApiRequestException("No message given!", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No name given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Target player not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No target player given!", 1);
            }
            return null;
        }
    }
}
