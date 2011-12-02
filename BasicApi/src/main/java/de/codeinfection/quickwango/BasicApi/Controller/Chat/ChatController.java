package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import de.codeinfection.quickwango.BasicApi.BasicApiConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author CodeInfection
 */
public class ChatController extends ApiRequestController
{
    private BasicApiConfiguration config;

    private final Map<Long, ChatSession> chatSessions;

    public ChatController(BasicApi plugin)
    {
        super(plugin, true);
        this.config = plugin.getBasicApiConfig();
        this.chatSessions = new HashMap<Long, ChatSession>();

        this.setAction("create", new CreateAction());
        this.setAction("participants", new ParticipantsAction());
        this.setAction("post", new PostAction());
        this.setAction("get", new GetAction());
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
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
    
    private class GetAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String sessionParam = params.getProperty("session");
            try
            {
                Long sessionID = Long.valueOf(sessionParam);
                ChatSession session = chatSessions.get(sessionID);
                if (session != null)
                {
                    return session.getChatlog();
                }
                else
                {
                    throw new ApiRequestException("The given session is not available!", 2);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("The given Session ID is invalid!", 1);
            }
        }
    }
    
    private class PublicAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
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

    private class PostAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String sessionParam = params.getProperty("session");
            try
            {
                Long sessionID = Long.valueOf(sessionParam);
                ChatSession session = chatSessions.get(sessionID);
                if (session != null)
                {
                    String nameParam = params.getProperty("name");
                    if (nameParam != null)
                    {
                        String messageParam = params.getProperty("message");
                        if (messageParam != null)
                        {
                            session.chat(nameParam, messageParam);
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
                    throw new ApiRequestException("The given session is not available!", 2);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("The given Session ID is invalid!", 1);
            }
            return null;
        }
    }

    private class ParticipantsAction extends ApiRequestAction
    {
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String sessionParam = params.getProperty("session");
            try
            {
                Long sessionID = Long.valueOf(sessionParam);
                ChatSession session = chatSessions.get(sessionID);
                if (session != null)
                {
                    List<String> playerNames = new ArrayList<String>();
                    for (Player player : session.getAcceptedPlayers())
                    {
                        playerNames.add(player.getName());
                    }
                    return playerNames;
                }
                else
                {
                    throw new ApiRequestException("The given session is not available!", 2);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("The given Session ID is invalid!", 1);
            }
        }
    }

    private class CreateAction extends ApiRequestAction
    {
        private final CRC32 crc = new CRC32();
        
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playersParam = params.getProperty("players");
            if (playersParam != null)
            {
                String[] playerNames = playersParam.split(",");
                List<Player> players = new ArrayList<Player>();
                for (String playerName : playerNames)
                {
                    Player player = server.getPlayerExact(playerName);
                    if (player != null)
                    {
                        players.add(player);
                    }
                }

                if (players.size() > 0)
                {
                    ChatSession session = new ChatSession(config, players);
                    this.crc.update((byte)System.currentTimeMillis());
                    long sessionID = this.crc.getValue();
                    chatSessions.put(sessionID, session);

                    return sessionID;
                }
                else
                {
                    throw  new ApiRequestException("No valid players given!", 3);
                }
            }
            else
            {
                throw new ApiRequestException("No players given!", 1);
            }
        }
    }
}
