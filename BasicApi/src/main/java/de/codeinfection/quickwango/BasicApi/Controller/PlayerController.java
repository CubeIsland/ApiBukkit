package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class PlayerController extends AbstractRequestController
{
    public PlayerController(Plugin plugin)
    {
        super(plugin, true);
        
        this.registerAction("burn",             new BurnAction());
        this.registerAction("clearinventory",   new ClearinventoryAction());
        this.registerAction("give",             new GiveAction());
        this.registerAction("heal",             new HealAction());
        this.registerAction("info",             new InfoAction());
        this.registerAction("kick",             new KickAction());
        this.registerAction("kill",             new KillAction());
        this.registerAction("list",             new ListAction());
        this.registerAction("teleport",         new TeleportAction());
        this.registerAction("tell",             new TellAction());
        this.registerAction("displayname",      new DisplaynameAction());
        
        this.setActionAlias("msg",              "tell");
        this.setActionAlias("clearinv",         "clearinventory");
        this.setActionAlias("tp",               "teleport");
        this.setActionAlias("slay",             "kill");
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }
    
    private class ListAction extends RequestAction
    {
        public ListAction()
        {
            super(false);
        }
        
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            Player[] online = server.getOnlinePlayers();
            List<String> players = new ArrayList<String>();
            for (Player player : online)
            {
                players.add(player.getName());
            }

            return players;
        }
    }
    
    private class InfoAction extends RequestAction
    {
        private final double armorPoints[] = {1.5, 3.0, 4.0, 1.5};

        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("name", player.getName());
                    data.put("displayName", player.getDisplayName());
                    int health = player.getHealth();
                    data.put("health", health < 0 ? 0 : health);
                    data.put("armor", this.getArmorPoints(player));

                    Location playerLoc = player.getLocation();
                    data.put("world", playerLoc.getWorld().getName());
                    data.put("position", new Double[] {
                        playerLoc.getX(),
                        playerLoc.getY(),
                        playerLoc.getZ(),
                        (double)playerLoc.getYaw(),  // horizontal
                        (double)playerLoc.getPitch() // vertical
                    });
                    data.put("ip", player.getAddress().getAddress().getHostAddress());

                    return data;
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }

        public final int getArmorPoints(Player player)
        {
            int currentDurability = 0;
            int baseDurability = 0;
            double baseArmorPoints = 0;
            ItemStack inventory[] = player.getInventory().getArmorContents();
            for (int i = 0; i < inventory.length; ++i)
            {
                if (inventory[i] == null)
                {
                    continue;
                }
                Material material = inventory[i].getType();
                if (material == null)
                {
                    continue;
                }
                final short maxDurability = material.getMaxDurability();
                if (maxDurability < 0)
                {
                    continue;
                }
                final short durability = inventory[i].getDurability();
                baseDurability += maxDurability;
                currentDurability += maxDurability - durability;
                baseArmorPoints += armorPoints[i];
            }
            return (int)Math.round(2 * baseArmorPoints * currentDurability / baseDurability);
        }
    }
    
    private class KillAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    player.setHealth(0);
                    ApiBukkit.log("killed player " + playerName);
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class BurnAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    int seconds = 5;
                    String duration = params.getProperty("duration");
                    if (duration != null)
                    {
                        try
                        {
                            seconds = Integer.valueOf(duration);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new RequestException("The duration must be a valid number!", 3);
                        }
                    }
                    player.setFireTicks(seconds * 20);
                    ApiBukkit.log("burned player " + playerName + " for " + seconds + " seconds");
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class TeleportAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    World world = null;
                    String worldName = params.getProperty("world");
                    if (worldName != null)
                    {
                        world = server.getWorld(worldName);
                        if (world == null)
                        {
                            throw new RequestException("World '" + worldName + "' not found!", 3);
                        }
                    }
                    else
                    {
                        world = player.getWorld();
                    }


                    Location targetLocation = null;
                    String targetPlayerName = params.getProperty("targetplayer");
                    if (targetPlayerName != null)
                    {
                        Player targetPlayer = server.getPlayer(targetPlayerName);
                        if (targetPlayer != null)
                        {
                            targetLocation = targetPlayer.getLocation();
                        }
                    }
                    if (targetLocation == null)
                    {
                        String locationParam = params.getProperty("location");
                        if (locationParam != null)
                        {
                            try
                            {
                                String[] locationParts = locationParam.split(",");
                                if (locationParts.length > 2)
                                {
                                    targetLocation = new Location(
                                        world,
                                        Double.valueOf(locationParts[0]),
                                        Double.valueOf(locationParts[1]),
                                        Double.valueOf(locationParts[2])
                                    );
                                    if (locationParts.length > 3)
                                    {
                                        ApiBukkit.debug("String to convert to Float: " + locationParts[3]);
                                        targetLocation.setYaw(Float.valueOf(locationParts[3]));
                                    }
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                ApiBukkit.debug(e.getMessage());
                                throw new RequestException("Invalid location given!", 4);
                            }
                        }
                    }
                    if (targetLocation == null)
                    {
                        throw new RequestException("Could not get any valid location!", 5);
                    }

                    player.teleport(targetLocation);
                    ApiBukkit.log(String.format(
                        "teleported player %s to %s,%s,%s in world %s",
                        playerName,
                        targetLocation.getX(),
                        targetLocation.getY(),
                        targetLocation.getZ(),
                        worldName
                    ));
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class HealAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    player.setHealth(20);
                    ApiBukkit.log("healed player " + playerName);
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class GiveAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    String itemID = params.getProperty("itemid");
                    if (itemID == null)
                    {
                        System.out.println("No block ID given!");
                    }
                    try
                    {
                        int item = Integer.valueOf(itemID);
                        String blockData = params.getProperty("data");
                        short data = 0;
                        if (blockData != null)
                        {
                            data = Short.valueOf(blockData);
                        }

                        String amountParam = params.getProperty("amount");
                        int amount = 1;
                        if (amountParam != null)
                        {
                            amount = Integer.valueOf(amountParam);
                        }

                        ItemStack itemStack = new ItemStack(item);
                        if (itemStack.getType() == null)
                        {
                            throw new RequestException("The given item ID is unknown!", 4);
                        }
                        itemStack.setAmount(amount);
                        itemStack.setDurability(data);

                        player.getInventory().addItem(itemStack);
                        ApiBukkit.log("gave player " + playerName + " " + amount + " of block " + item + ":" + data);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new RequestException("Invalid block ID given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class KickAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    player.kickPlayer(params.getProperty("reason"));
                    ApiBukkit.log("kicked player " + playerName);
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class TellAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    String msg = params.getProperty("message");
                    if (msg != null)
                    {
                        if (msg.length() > 100)
                        {
                            msg = msg.substring(0, 99);
                        }
                        player.sendMessage(msg.replaceAll("&([0-9a-f])", "§$1"));
                        ApiBukkit.log("sent a the message " + msg + " to player " + playerName + "!");
                    }
                    else
                    {
                        throw new RequestException("No message given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class ClearinventoryAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    player.getInventory().clear();
                    ApiBukkit.log("cleared inventory of player " + playerName);
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }

    private class DisplaynameAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    String newDisplayName = params.getProperty("displayname");
                    if (newDisplayName != null)
                    {
                        player.setDisplayName(newDisplayName);
                        ApiBukkit.log("changed the display name of player " + playerName + "to '" + newDisplayName + "' !");
                    }
                    else
                    {
                        throw new RequestException("No display name given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
        }
    }
}
