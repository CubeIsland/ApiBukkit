package de.codeinfection.quickwango.BasicApi.Controller;

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
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "world")
public class WorldController extends ApiController
{
    public WorldController(Plugin plugin)
    {
        super(plugin);
    }
    
    @Action(parameters = {"world"}, serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name",                world.getName());
            data.put("time",                world.getTime());
            data.put("fullTime",            world.getFullTime());
            data.put("seed",                world.getSeed());
            data.put("pvp",                 world.getPVP());
            data.put("environment",         world.getEnvironment().getId());
            data.put("thunderDuration",     world.getThunderDuration());
            data.put("weatherDuration",     world.getWeatherDuration());
            data.put("allowAnimals",        world.getAllowAnimals());
            data.put("allowMonsters",       world.getAllowMonsters());
            data.put("keepSpawnInMemory",   world.getKeepSpawnInMemory());
            data.put("entities",            world.getEntities().size());
            data.put("livingEntities",      world.getLivingEntities().size());
            data.put("difficulty",          world.getDifficulty().getValue());
            data.put("loadedChunks",        world.getLoadedChunks().length);
            data.put("players",             world.getPlayers().size());
            data.put("players",             world.getPlayers().size());
            data.put("maxHeight",           world.getMaxHeight());
            data.put("seaLevel",            world.getSeaLevel());
            data.put("seaLevel",            world.getSeaLevel());
            data.put("worldFolder",         world.getWorldFolder().toString());

            Location spawnLoc = world.getSpawnLocation();
            data.put("spawnLocation", new Integer[] {
                spawnLoc.getBlockX(),
                spawnLoc.getBlockY(),
                spawnLoc.getBlockZ()
            });
            data.put("players", world.getPlayers().size());

            response.setContent(data);
        }
        else
        {
            throw new ApiRequestException("World not found!", 1);
        }
    }
    
    @Action(parameters = {"world", "environment"})
    public void create(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world == null)
        {
            String environmentParam = request.REQUEST.getString("environment");
            Environment env = null;
            try
            {
                env = World.Environment.getEnvironment(Integer.valueOf(environmentParam));
            }
            catch (NumberFormatException e)
            {}

            if (env == null)
            {
                throw new ApiRequestException("Invalid environment specified!", 2);
            }

            ChunkGenerator generator = null;
            String generatorParam = request.REQUEST.getString("generator");
            if (generatorParam != null)
            {
                generatorParam = generatorParam.trim();
                if (generatorParam.length() > 0)
                {
                    String[] split = generatorParam.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = request.server.getPluginManager().getPlugin(split[0]);

                    if (plugin == null || !plugin.isEnabled())
                    {
                        BasicApi.error("Could not set generator for default world '" + worldName + "': Plugin '" + split[0] + "' does not exist");
                        throw new ApiRequestException("Failed to load generator plugin", 3);
                    }
                    else
                    {
                        generator = plugin.getDefaultWorldGenerator(worldName, id);
                    }
                }
            }

            long seed;
            String seedParam = request.REQUEST.getString("seed");
            if (seedParam != null)
            {
                if (seedParam.matches("/^\\d+$/"))
                {
                    seed = Long.valueOf(seedParam);
                }
                else
                {
                    seed = (long)seedParam.hashCode();
                }
            }
            else
            {
                seed = (new Random()).nextLong();
            }


            Utils.createWorldSync(getPlugin(), worldName, env, seed, generator);
        }
        else
        {
            throw new ApiRequestException("World does already exist!", 1);
        }
    }

    @Action(parameters = {"world", "time"})
    public void time(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            String timeParam = request.REQUEST.getString("time");
            try
            {
                long time = Long.valueOf(timeParam);
                world.setTime(time);
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("Time must be a valid number!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("World not found!", 1);
        }
    }
    
    @Action(parameters = {"world"})
    public void pvp(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            String state = request.REQUEST.getString("state");
            if (state != null)
            {
                if (state.equalsIgnoreCase("1"))
                {
                    world.setPVP(true);
                }
                else if (state.equalsIgnoreCase("0"))
                {
                    world.setPVP(false);
                }
                else
                {
                    throw new ApiRequestException("Invalid state given! Use on or off", 2);
                }
            }
            else
            {
                world.setPVP(world.getPVP() ? false : true);
            }
        }
        else
        {
            throw new ApiRequestException("World not found!", 1);
        }
    }

    @Action(parameters = {"world", "state"})
    public void storm(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            String state = request.REQUEST.getString("state");
            if (state.equalsIgnoreCase("1"))
            {
                world.setStorm(true);
            }
            else if (state.equalsIgnoreCase("0"))
            {
                world.setStorm(false);
            }
            else
            {
                /**
                 * @TODO implement toggling
                 */
                throw new ApiRequestException("Invalid state given! Use on or off", 2);
            }
        }
        else
        {
            throw new ApiRequestException("World not found!", 1);
        }
    }

    @Action(parameters = {"world"})
    public Object spawn(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            String locationParam = request.REQUEST.getString("location");
            String playerName = request.REQUEST.getString("player");
            if (locationParam != null)
            {
                String[] locationParts = locationParam.split(",");
                try
                {
                    int x, y, z;
                    if (locationParts.length > 2)
                    {
                        x = Integer.valueOf(locationParts[0]);
                        y = Integer.valueOf(locationParts[1]);
                        z = Integer.valueOf(locationParts[2]);
                        world.setSpawnLocation(x, y, z);
                    }
                    else
                    {
                        throw new ApiRequestException("No valid location given!", 4);
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new ApiRequestException("No valid location given!", 4);
                }
            }
            else if (playerName != null)
            {
                Player player = request.server.getPlayerExact(playerName);
                if (player != null)
                {
                    Location playerLocation = player.getLocation();
                    world.setSpawnLocation(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
                }
                else
                {
                    throw new ApiRequestException("Given player not found", 3);
                }
            }
            else
            {
                throw new ApiRequestException("No location given!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("World not found!", 1);
        }
        return null;
    }
    
    @Action(serializer = "json")
    public void list(ApiRequest request, ApiResponse response)
    {
        List<String> data = new ArrayList<String>();
        for (World currentWorld : request.server.getWorlds())
        {
            data.add(currentWorld.getName());
        }
        response.setContent(data);
    }
    
    @Action(parameters = {"world"}, serializer = "json")
    public void players(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            List<String> data = new ArrayList<String>();
            for (Player player : world.getPlayers())
            {
                data.add(player.getName());
            }
            response.setContent(data);
        }
        else
        {
            throw new ApiRequestException("World " + worldName + " not found!", 1);
        }
    }

    @Action(parameters = {"world"})
    public Object spawnflags(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            boolean monsters = world.getAllowMonsters();
            boolean animals = world.getAllowAnimals();

            String state = request.REQUEST.getString("monsters");
            if (state != null)
            {
                if (state.equals("1"))
                {
                    monsters = true;
                }
                else if (state.equals("0"))
                {
                    monsters = false;
                }
            }
            state = request.REQUEST.getString("animals");
            if (state != null)
            {
                if (state.equals("1"))
                {
                    animals = true;
                }
                else if (state.equals("0"))
                {
                    animals = false;
                }
            }

            world.setSpawnFlags(monsters, animals);
            return null;
        }
        else
        {
            throw new ApiRequestException("World " + worldName + " not found!", 1);
        }
    }

    @Action(parameters = {"world"})
    public void save(ApiRequest request, ApiResponse response)
    {
        String worldName = request.REQUEST.getString("world");
        World world = request.server.getWorld(worldName);
        if (world != null)
        {
            world.save();
        }
        else
        {
            throw new ApiRequestException("Given world not found!", 1);
        }
    }
}
