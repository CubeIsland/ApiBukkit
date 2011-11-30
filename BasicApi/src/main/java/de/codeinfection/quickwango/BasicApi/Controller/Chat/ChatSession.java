package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

/**
 *
 * @author CodeInfection
 */
public class ChatSession
{
    private final List<Player> players;
    private final ChatListener chatListener;
    private String chatFormat;

    public ChatSession(List<Player> players)
    {
        this.players = players;
        this.chatListener = new ChatListener(this.players);
    }

    public void init(BasicApi plugin)
    {
        plugin.getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, this.chatListener, Priority.Monitor, plugin);

        for (Player player : this.players)
        {
            player.sendMessage("");
            player.sendMessage("You have been invited to a chat!");
            player.sendMessage("Type &2ok&f to accept or &canything else&f decline.");
        }
    }

    public Queue<ChatMessage> getChatlog()
    {
        return this.chatListener.chatLog;
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
        Map<Player, ChatListener.PlayerState> states = this.chatListener.getPlayerStates();
        List<Player> acceptedPlayers = new ArrayList<Player>();
        for (Player player : this.players)
        {
            if (states.get(player) == ChatListener.PlayerState.CHATTING)
            {
                acceptedPlayers.add(player);
            }
        }

        return acceptedPlayers;
    }
}
