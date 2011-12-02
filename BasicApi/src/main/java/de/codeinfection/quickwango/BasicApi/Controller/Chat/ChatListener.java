package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author CodeInfection
 */
public class ChatListener extends PlayerListener
{
    private final Map<Long, ChatSession> sessions;

    public ChatListener(Map<Long, ChatSession> sessions)
    {
        this.sessions = sessions;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event)
    {
        if (!event.isCancelled())
        {
            Player player = event.getPlayer();
            String message = event.getMessage();
            for (ChatSession session : this.sessions.values())
            {
                if (session.onChat(player, message, event))
                {
                    break;
                }
            }
        }
    }
}
