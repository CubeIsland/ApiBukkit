package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.BasicApi.FakeEvents.FakePlayerChatEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author CodeInfection
 */
public class ChatListener extends PlayerListener
{
    public final Queue<ChatMessage> chatLog;
    private final List<Player> players;
    private final Map<Player, PlayerState> playerStates;

    public ChatListener(List<Player> players)
    {
        this.players = players;
        this.playerStates = new HashMap<Player, PlayerState>(this.players.size());
        for (Player player : players)
        {
            playerStates.put(player, PlayerState.NORMAL);
        }
        this.chatLog = new ConcurrentLinkedQueue<ChatMessage>();
    }

    public Map<Player, PlayerState> getPlayerStates()
    {
        return this.playerStates;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (this.players.contains(player))
        {
            if (player instanceof ChatPlayer)
            {
                this.chatLog.add(new ChatMessage(player.getName(), event.getMessage(), true));
            }
            else
            {
                switch (this.playerStates.get(player))
                {
                    case CHATTING:
                        this.chatLog.add(new ChatMessage(player.getName(), message, false));
                        break;
                    case CHAT_REQUESTED:
                        if (message.equalsIgnoreCase("ok"))
                        {
                            this.playerStates.put(player, PlayerState.CHATTING);
                        }
                        else
                        {
                            this.playerStates.put(player, PlayerState.NORMAL);
                        }
                        event.setCancelled(true);
                        break;
                    case NORMAL:
                        break;
                }
            }
        }
        if (event.isCancelled())
        {
            return;
        }
        if (chatLog.size() + 1 >= 100)
        {
            chatLog.poll();
        }
        //chatLog.add(new String[] {event.getPlayer().getName(), event.getMessage().replaceAll("\u00A7([a-f0-9])", "&$1")});
        chatLog.add(new ChatMessage(event.getPlayer().getName(), event.getMessage(), (event instanceof FakePlayerChatEvent)));
    }

    public enum PlayerState
    {
        NORMAL,
        CHAT_REQUESTED,
        CHATTING
    }
}
