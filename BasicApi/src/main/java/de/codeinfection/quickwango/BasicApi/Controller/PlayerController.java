package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class PlayerController extends ApiRequestController
{
    public PlayerController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("burn",              new BurnAction());
        this.setAction("clearinventory",    new ClearinventoryAction());
        this.setAction("give",              new GiveAction());
        this.setAction("heal",              new HealAction());
        this.setAction("info",              new InfoAction());
        this.setAction("kick",              new KickAction());
        this.setAction("kill",              new KillAction());
        this.setAction("list",              new ListAction());
        this.setAction("teleport",          new TeleportAction());
        this.setAction("tell",              new TellAction());
        this.setAction("displayname",       new DisplaynameAction());
        this.setAction("inventory",         new InventoryAction());
        
        this.setActionAlias("msg",              "tell");
        this.setActionAlias("clearinv",         "clearinventory");
        this.setActionAlias("tp",               "teleport");
        this.setActionAlias("slay",             "kill");
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    private class ListAction extends ApiRequestAction
    {
        public ListAction()
        {
            super(false);
        }
        
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
    
    private class InfoAction extends ApiRequestAction
    {
        private final double armorPoints[] = {3, 6, 8, 3};

        public final int getArmorPoints(Player player)
        {
            int currentDurability = 0;
            int baseDurability = 0;
            int baseArmorPoints = 0;
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armorItems = new ItemStack[4];
            armorItems[0] = inventory.getBoots();
            armorItems[1] = inventory.getLeggings();
            armorItems[2] = inventory.getChestplate();
            armorItems[3] = inventory.getHelmet();
            for (int i = 0; i < 4; ++i)
            {
                if(armorItems[i] == null)
                {
                    continue;
                }
                int dur = armorItems[i].getDurability();
                int max = armorItems[i].getType().getMaxDurability();
                if (max <= 0)
                {
                    continue;
                }
                if (i == 2)
                {
                    max = max + 1; /* Always 1 too low for chestplate */
                }
                else
                {
                    max = max - 3; /* Always 3 too high, versus how client calculates it */
                }
                baseDurability += max;
                currentDurability += max - dur;
                baseArmorPoints += this.armorPoints[i];
            }
            int ap = 0;
            if (baseDurability > 0)
            {
                ap = ((baseArmorPoints - 1) * currentDurability) / baseDurability + 1;
            }
            return ap;
        }

        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    Map<String, Object> data = new HashMap<String, Object>();
                    
                    data.put("name",                player.getName());
                    data.put("displayName",         player.getDisplayName());
                    data.put("listName",            player.getPlayerListName());
                    data.put("health",              player.getHealth());
                    data.put("armor",               this.getArmorPoints(player));
                    data.put("ip",                  player.getAddress().getAddress().getHostAddress());
                    data.put("operator",            player.isOp());
                    data.put("banned",              player.isBanned());
                    data.put("whitelisted",         player.isWhitelisted());
                    data.put("gamemode",            player.getGameMode().getValue());
                    data.put("experience",          player.getExperience());
                    data.put("totalExperience",     player.getTotalExperience());
                    data.put("level",               player.getLevel());
                    data.put("foodLevel",           player.getFoodLevel());
                    data.put("remainingAir",        player.getRemainingAir());
                    data.put("velocity",            player.getVelocity());
                    data.put("exhaustion",          player.getExhaustion());
                    data.put("gamemode",            player.getGameMode().getValue());
                    data.put("heldItem",            player.getItemInHand().getType().getId());
                    data.put("heldItemSlot",        player.getInventory().getHeldItemSlot());
                    data.put("saturation",          player.getSaturation());
                    data.put("firstPlayed",         player.getFirstPlayed());
                    data.put("lastPlayed",          player.getLastPlayed());
                    data.put("playedBefore",        player.hasPlayedBefore());

                    Location playerLoc = player.getLocation();
                    data.put("world", playerLoc.getWorld().getName());
                    data.put("position", new Double[] {
                        playerLoc.getX(),
                        playerLoc.getY(),
                        playerLoc.getZ()
                    });
                    data.put("blockPosition", new Integer[] {
                        playerLoc.getBlockX(),
                        playerLoc.getBlockY(),
                        playerLoc.getBlockZ()
                    });

                    Location compassTarget = player.getCompassTarget();
                    data.put("compassTarget", new Object[] {
                        compassTarget.getWorld(),
                        compassTarget.getX(),
                        compassTarget.getY(),
                        compassTarget.getZ()
                    });

                    Location bedSpawnLocation = player.getBedSpawnLocation();
                    data.put("bedSpawnLocation", new Object[] {
                        bedSpawnLocation.getWorld(),
                        bedSpawnLocation.getX(),
                        bedSpawnLocation.getY(),
                        bedSpawnLocation.getZ()
                    });

                    HashMap<String, Object> orientation = new HashMap<String, Object>();
                    orientation.put("yaw", playerLoc.getYaw()); // horizontal
                    orientation.put("pitch", playerLoc.getPitch()); // vertical
                    orientation.put("cardinalDirection", getCardinalDirection(playerLoc.getYaw()));
                    data.put("orientation", orientation);

                    return data;
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
        }

        private String getCardinalDirection(float yaw)
        {
            //yaw = (yaw + 90) % 360;
            if (yaw < 0)
            {
                yaw += 360.0;
            }
            if (0 <= yaw && yaw < 22.5)
            {
                return "N";
            }
            else if (22.5 <= yaw && yaw < 67.5)
            {
                return "NE";
            }
            else if (67.5 <= yaw && yaw < 112.5)
            {
                return "E";
            }
            else if (112.5 <= yaw && yaw < 157.5)
            {
                return "SE";
            }
            else if (157.5 <= yaw && yaw < 202.5)
            {
                return "S";
            }
            else if (202.5 <= yaw && yaw < 247.5)
            {
                return "SW";
            }
            else if (247.5 <= yaw && yaw < 292.5)
            {
                return "W";
            }
            else if (292.5 <= yaw && yaw < 337.5)
            {
                return "NW";
            }
            else if (337.5 <= yaw && yaw < 360.0)
            {
                return "N";
            }
            else
            {
                return null;
            }
        }
    }
    
    private class KillAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    player.setHealth(0);
                    ApiBukkit.log("killed player " + playerName);
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class BurnAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
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
                            throw new ApiRequestException("The duration must be a valid number!", 3);
                        }
                    }
                    player.setFireTicks(seconds * 20);
                    ApiBukkit.log("burned player " + playerName + " for " + seconds + " seconds");
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class TeleportAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    World world = null;
                    String worldName = params.getProperty("world");
                    if (worldName != null)
                    {
                        world = server.getWorld(worldName);
                        if (world == null)
                        {
                            throw new ApiRequestException("World '" + worldName + "' not found!", 3);
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
                        Player targetPlayer = server.getPlayerExact(targetPlayerName);
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
                                throw new ApiRequestException("Invalid location given!", 4);
                            }
                        }
                    }
                    if (targetLocation == null)
                    {
                        throw new ApiRequestException("Could not get any valid location!", 5);
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
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class HealAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    player.setHealth(20);
                    ApiBukkit.log("healed player " + playerName);
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class GiveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    String itemidParam = params.getProperty("itemid");
                    if (itemidParam != null)
                    {
                        int item = 0;
                        try
                        {
                            item = Integer.valueOf(itemidParam);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new ApiRequestException("Invalid item ID '" + itemidParam + "' given!", 4);
                        }
                        short data = 0;
                        String dataParam = params.getProperty("data");
                        if (dataParam != null)
                        {
                            try
                            {
                                data = Short.valueOf(dataParam);
                            }
                            catch (NumberFormatException e)
                            {
                                throw new ApiRequestException("Invalid block data '" + dataParam + "'", 5);
                            }
                        }

                        int amount = 1;
                        String amountParam = params.getProperty("amount");
                        if (amountParam != null)
                        {
                            try
                            {
                                amount = Integer.valueOf(amountParam);
                            }
                            catch (NumberFormatException e)
                            {
                                throw new ApiRequestException("Invalid amount '" + amountParam + "'", 6);
                            }
                        }

                        Material itemMaterial = Material.getMaterial(item);
                        if (itemMaterial != null)
                        {
                            ItemStack itemStack = new ItemStack(itemMaterial);
                            itemStack.setAmount(amount);
                            itemStack.setDurability(data);

                            player.getInventory().addItem(itemStack);
                            ApiBukkit.log("gave player " + player.getName() + " " + amount + " of block " + item + ":" + data);
                        }
                        else
                        {
                            throw new ApiRequestException("The given item ID is unknown!", 7);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No block ID given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class KickAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    player.kickPlayer(params.getProperty("reason"));
                    ApiBukkit.log("kicked player " + playerName);
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class TellAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
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
                        throw new ApiRequestException("No message given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class ClearinventoryAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
                if (player != null)
                {
                    player.getInventory().clear();
                    ApiBukkit.log("cleared inventory of player " + playerName);
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }

    private class DisplaynameAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                Player player = server.getPlayerExact(playerName);
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
                        throw new ApiRequestException("No display name given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }
    
    private class InventoryAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerParam = params.getProperty("player");
            if (playerParam != null)
            {
                Player player = server.getPlayerExact(playerParam);
                if (player != null)
                {
                    List<List<Number>> contents = new ArrayList<List<Number>>();
                    List<List<Number>> armor = new ArrayList<List<Number>>();
                    PlayerInventory inventory = player.getInventory();
                    for (ItemStack itemStack : inventory.getContents())
                    {
                        if (itemStack == null)
                        {
                            contents.add(null);
                        }
                        else
                        {
                            List<Number> item = new ArrayList<Number>();
                            item.add(itemStack.getTypeId());
                            item.add(itemStack.getDurability());
                            contents.add(item);
                        }
                    }
                    for (ItemStack itemStack : inventory.getArmorContents())
                    {
                        if (itemStack == null)
                        {
                            armor.add(null);
                        }
                        else
                        {
                            List<Number> item = new ArrayList<Number>();
                            item.add(itemStack.getTypeId());
                            item.add(itemStack.getDurability());
                            armor.add(item);
                        }
                    }
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("contents", contents);
                    data.put("armor", armor);
                    data.put("helditemslot", inventory.getHeldItemSlot());
                    return data;
                }
                else
                {
                    throw new ApiRequestException("Given player not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
        }
    }
}
