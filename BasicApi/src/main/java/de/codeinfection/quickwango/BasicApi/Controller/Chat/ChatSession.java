package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.BasicApi.BasicApiConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author CodeInfection
 */
public class ChatSession
{
    private final BasicApiConfiguration config;
    private final List<Player> players;
    private final Map<Player, PlayerState> playerStates;
    private final Queue<ChatMessage> chatLog;
    private final String chatFormat;

    public ChatSession(BasicApiConfiguration config, List<Player> players)
    {
        this.config = config;
        this.players = players;
        this.playerStates = new HashMap<Player, PlayerState>(players.size());
        this.chatLog = new ConcurrentLinkedQueue<ChatMessage>();
        this.chatFormat = this.config.chatFormat;
        
        for (Player player : this.players)
        {
            this.addPlayer(player);
        }
    }

    private void logMessage(ChatMessage message)
    {
        this.chatLog.add(message);
        if (this.chatLog.size() + 1 >= 100)
        {
            this.chatLog.poll();
        }
    }

    private void removePlayer(Player player)
    {
        this.playerStates.remove(player);
        this.players.remove(player);
    }

    public boolean onChat(Player player, String message, PlayerChatEvent event)
    {
        if (this.players.contains(player))
        {
            if (player instanceof ChatPlayer)
            {
                this.logMessage(new ChatMessage(player.getName(), event.getMessage(), true));
            }
            else
            {
                switch (this.playerStates.get(player))
                {
                    case ACCEPTED:
                        this.logMessage(new ChatMessage(player.getName(), message, false));
                        break;
                    case REQUESTED:
                        if (message.equalsIgnoreCase("ok"))
                        {
                            this.playerStates.put(player, PlayerState.ACCEPTED);
                        }
                        else
                        {
                            player.sendMessage("Chat request was declined!");
                            this.removePlayer(player);
                        }
                        break;
                }
            }
        }
        //chatLog.add(new String[] {event.getPlayer().getName(), event.getMessage().replaceAll("\u00A7([a-f0-9])", "&$1")});
        //chatLog.add(new ChatMessage(event.getPlayer().getName(), event.getMessage(), (event instanceof FakePlayerChatEvent)));
        return true;
    }

    public Queue<ChatMessage> getChatlog()
    {
        return this.chatLog;
    }

    public void chat(String name, String messages)
    {
        String parsedMessage = String.format(this.chatFormat, name, messages);
        for (Player player : this.getAcceptedPlayers())
        {
            player.sendMessage(parsedMessage);
        }
    }

    public List<Player> getAcceptedPlayers()
    {
        List<Player> acceptedPlayers = new ArrayList<Player>();
        for (Player player : this.players)
        {
            if (this.playerStates.get(player) == PlayerState.ACCEPTED)
            {
                acceptedPlayers.add(player);
            }
        }

        return acceptedPlayers;
    }

    public List<Player> getPlayers()
    {
        return this.players;
    }

    public PlayerState getPlayerState(Player player)
    {
        return this.playerStates.get(player);
    }

    public final void addPlayer(Player player)
    {
        player.sendMessage("");
        player.sendMessage("You have been invited to a chat!");
        player.sendMessage("Type &2ok&f to accept or &canything else&f to decline.");
        this.players.add(player);
        this.playerStates.put(player, PlayerState.REQUESTED);
    }

    public enum PlayerState
    {
        REQUESTED,
        ACCEPTED
    }
}
