package de.codeinfection.quickwango.BasicApi.FakeEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author CodeInfection
 */
public class FakePlayerChatEvent extends PlayerChatEvent
{
    public FakePlayerChatEvent(Player player, String message)
    {
        super(player, message);
    }
}
