package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import de.codeinfection.quickwango.BasicApi.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "player")
public class PlayerController extends ApiController
{
    public PlayerController(Plugin plugin)
    {
        super(plugin);
    }

    @Action(authenticate = false, serializer = "json")
    public void list(ApiRequest request, ApiResponse response)
    {
        Player[] online = request.server.getOnlinePlayers();
        List<String> players = new ArrayList<String>();
        for (Player player : online)
        {
            players.add(player.getName());
        }

        response.setContent(players);
    }

    @Action(parameters =
    {
        "player"
    }, serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        final String playerName = request.params.getString("player");
        final Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            Map<String, Object> data = new HashMap<String, Object>();

            data.put("name", player.getName());
            data.put("displayName", player.getDisplayName());
            data.put("listName", player.getPlayerListName());
            data.put("health", player.getHealth());
            data.put("armor", Utils.getArmorPoints(player));
            data.put("ip", player.getAddress().getAddress().getHostAddress());
            data.put("operator", player.isOp());
            data.put("banned", player.isBanned());
            data.put("whitelisted", player.isWhitelisted());
            data.put("gamemode", player.getGameMode().getValue());
            data.put("experience", player.getExp());
            data.put("totalExperience", player.getTotalExperience());
            data.put("level", player.getLevel());
            data.put("foodLevel", player.getFoodLevel());
            data.put("remainingAir", player.getRemainingAir());
            data.put("velocity", player.getVelocity());
            data.put("exhaustion", player.getExhaustion());
            data.put("gamemode", player.getGameMode().getValue());
            data.put("heldItem", player.getItemInHand().getType().getId());
            data.put("heldItemSlot", player.getInventory().getHeldItemSlot());
            data.put("saturation", player.getSaturation());
            data.put("firstPlayed", player.getFirstPlayed());
            data.put("lastPlayed", player.getLastPlayed());
            data.put("playedBefore", player.hasPlayedBefore());

            Location playerLoc = player.getLocation();
            if (playerLoc != null)
            {
                data.put("world", playerLoc.getWorld().getName());
                data.put("position", new Double[]
                    {
                        playerLoc.getX(),
                        playerLoc.getY(),
                        playerLoc.getZ()
                    });
                data.put("blockPosition", new Integer[]
                    {
                        playerLoc.getBlockX(),
                        playerLoc.getBlockY(),
                        playerLoc.getBlockZ()
                    });
            }

            Location compassTarget = player.getCompassTarget();
            if (compassTarget != null)
            {
                data.put("compassTarget", new Object[]
                    {
                        compassTarget.getWorld().getName(),
                        compassTarget.getX(),
                        compassTarget.getY(),
                        compassTarget.getZ()
                    });
            }

            Location bedSpawnLocation = player.getBedSpawnLocation();
            if (bedSpawnLocation != null)
            {
                data.put("bedSpawnLocation", new Object[]
                    {
                        bedSpawnLocation.getWorld().getName(),
                        bedSpawnLocation.getX(),
                        bedSpawnLocation.getY(),
                        bedSpawnLocation.getZ()
                    });
            }

            HashMap<String, Object> orientation = new HashMap<String, Object>();
            orientation.put("yaw", playerLoc.getYaw()); // horizontal
            orientation.put("pitch", playerLoc.getPitch()); // vertical
            orientation.put("cardinalDirection", Utils.getCardinalDirection(playerLoc.getYaw()));
            data.put("orientation", orientation);

            response.setContent(data);
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void kill(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            player.setHealth(0);
            BasicApi.log("killed player " + playerName);
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void burn(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            int seconds = 5;
            String duration = request.params.getString("duration");
            if (duration != null)
            {
                try
                {
                    seconds = Integer.valueOf(duration);
                }
                catch (NumberFormatException e)
                {
                    throw new ApiRequestException("The duration must be a valid number!", 2);
                }
            }
            player.setFireTicks(seconds * 20);
            BasicApi.log("burned player " + playerName + " for " + seconds + " seconds");
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void teleport(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            World world;
            String worldName = request.params.getString("world");
            if (worldName != null)
            {
                world = request.server.getWorld(worldName);
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
            String targetPlayerName = request.params.getString("targetplayer");
            if (targetPlayerName != null)
            {
                Player targetPlayer = request.server.getPlayerExact(targetPlayerName);
                if (targetPlayer != null)
                {
                    targetLocation = targetPlayer.getLocation();
                }
            }
            if (targetLocation == null)
            {
                String locationParam = request.params.getString("location");
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
                                Double.valueOf(locationParts[2]));
                            if (locationParts.length > 3)
                            {
                                BasicApi.debug("String to convert to Float: " + locationParts[3]);
                                targetLocation.setYaw(Float.valueOf(locationParts[3]));
                            }
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        BasicApi.debug(e.getMessage());
                        throw new ApiRequestException("Invalid location given!", 4);
                    }
                }
            }
            if (targetLocation == null)
            {
                throw new ApiRequestException("Could not get any valid location!", 5);
            }

            player.teleport(targetLocation);
            BasicApi.log(String.format(
                "teleported player %s to %s,%s,%s in world %s",
                playerName,
                targetLocation.getX(),
                targetLocation.getY(),
                targetLocation.getZ(),
                worldName));
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void heal(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            player.setHealth(20);
            BasicApi.log("healed player " + playerName);
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 2);
        }
    }

    @Action(parameters =
    {
        "player", "itemid"
    })
    public void give(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            String itemidParam = request.params.getString("itemid");
            int item = 0;
            try
            {
                item = Integer.valueOf(itemidParam);
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("Invalid item ID '" + itemidParam + "' given!", 2);
            }
            short data = 0;
            String dataParam = request.params.getString("data");
            if (dataParam != null)
            {
                try
                {
                    data = Short.valueOf(dataParam);
                }
                catch (NumberFormatException e)
                {
                    throw new ApiRequestException("Invalid block data '" + dataParam + "'", 3);
                }
            }

            int amount = 1;
            String amountParam = request.params.getString("amount");
            if (amountParam != null)
            {
                try
                {
                    amount = Integer.valueOf(amountParam);
                }
                catch (NumberFormatException e)
                {
                    throw new ApiRequestException("Invalid amount '" + amountParam + "'", 4);
                }
            }

            Material itemMaterial = Material.getMaterial(item);
            if (itemMaterial != null)
            {
                ItemStack itemStack = new ItemStack(itemMaterial);
                itemStack.setAmount(amount);
                itemStack.setDurability(data);

                player.getInventory().addItem(itemStack);
                BasicApi.log("gave player " + player.getName() + " " + amount + " of block " + item + ":" + data);
            }
            else
            {
                throw new ApiRequestException("The given item ID is unknown!", 5);
            }
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void kick(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            player.kickPlayer(request.params.getString("reason"));
            BasicApi.log("kicked player " + playerName);
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player", "message"
    })
    public void tell(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            String message = request.params.getString("message");
            if (message.length() > 100)
            {
                message = message.substring(0, 99);
            }
            player.sendMessage(message.replaceAll("&([0-9a-f])", "§$1"));
            BasicApi.log("sent a the message " + message + " to player " + playerName + "!");
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void clearinventory(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            player.getInventory().clear();
            BasicApi.log("cleared inventory of player " + playerName);
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player", "displayname"
    })
    public void displayname(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerName);
        if (player != null)
        {
            String displayname = request.params.getString("displayname");
            player.setDisplayName(displayname);
            BasicApi.log("changed the display name of player " + playerName + "to '" + displayname + "' !");
        }
        else
        {
            throw new ApiRequestException("Player '" + playerName + "' not found!", 1);
        }
    }

    @Action(parameters =
    {
        "player"
    }, serializer = "json")
    public void inventory(ApiRequest request, ApiResponse response)
    {
        String playerParam = request.params.getString("player");
        Player player = request.server.getPlayerExact(playerParam);
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
            response.setContent(data);
        }
        else
        {
            throw new ApiRequestException("Given player not found!", 2);
        }
    }
}
