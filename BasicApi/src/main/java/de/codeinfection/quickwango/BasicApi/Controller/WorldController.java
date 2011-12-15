package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class WorldController extends ApiRequestController
{
    public WorldController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("info",         new InfoAction());
        this.setAction("create",       new CreateAction());
        this.setAction("time",         new TimeAction());
        this.setAction("pvp",          new PvpAction());
        this.setAction("storm",        new StormAction());
        this.setAction("spawn",        new SpawnAction());
        this.setAction("list",         new ListAction());
        this.setAction("players",      new PlayersAction());
        this.setAction("spawnflags",   new SpawnflagsAction());
        this.setAction("save",         new SaveAction());
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    private class InfoAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
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

                    return data;
                }
                else
                {
                    throw new ApiRequestException("World not found!", 1);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
        }
    }
    
    private class CreateAction extends ApiRequestAction implements Runnable
    {
        protected Server server;
        protected String worldName;
        protected World.Environment env;
        protected Long seed;
        protected ChunkGenerator generator;
        protected Throwable exception;

        private Thread executionThread;

        public CreateAction()
        {
            this.server = plugin.getServer();
            this.resetVars();
        }

        private void resetVars()
        {
            this.worldName = null;
            this.env = null;
            this.seed = null;
            this.generator = null;
        }

        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            this.executionThread = Thread.currentThread();
            this.worldName = params.getProperty("world");
            if (this.worldName != null)
            {
                World world = server.getWorld(this.worldName);
                if (world == null)
                {
                    String envParam = params.getProperty("environment");
                    if (envParam != null)
                    {
                        try
                        {
                            this.env = World.Environment.getEnvironment(Integer.valueOf(envParam));
                        }
                        catch (NumberFormatException e)
                        {}
                    }
                    else
                    {
                        throw new ApiRequestException("No environment specified!", 3);
                    }
                    
                    if (this.env == null)
                    {
                        throw new ApiRequestException("Invalid environment specified!", 4);
                    }

                    String generatorParam = params.getProperty("generator");
                    if (generatorParam != null)
                    {
                        generatorParam = generatorParam.trim();
                        if (generatorParam.length() > 0)
                        {
                            String[] split = generatorParam.split(":", 2);
                            String id = (split.length > 1) ? split[1] : null;
                            Plugin plugin = server.getPluginManager().getPlugin(split[0]);

                            if (plugin == null || !plugin.isEnabled())
                            {
                                ApiBukkit.error("Could not set generator for default world '" + this.worldName + "': Plugin '" + split[0] + "' does not exist");
                                throw new ApiRequestException("Failed to load generator plugin", 5);
                            }
                            else
                            {
                                this.generator = plugin.getDefaultWorldGenerator(this.worldName, id);
                            }
                        }
                    }

                    String seedParam = params.getProperty("seed");
                    if (seedParam != null)
                    {
                        if (seedParam.matches("/^\\d+$/"))
                        {
                            this.seed = Long.valueOf(seedParam);
                        }
                        else
                        {
                            this.seed = (long)seedParam.hashCode();
                        }
                    }
                    else
                    {
                        this.seed = (new Random()).nextLong();
                    }
                    if (this.server.getScheduler().scheduleSyncDelayedTask(plugin, this, 0L) < 0)
                    {
                        throw new ApiRequestException("Failed to schedule creation task!", 6);
                    }
                    try
                    {
                        synchronized (this.executionThread)
                        {
                            this.executionThread.wait();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        ApiBukkit.error("World generation was interupted!", e);
                    }
                    if (this.exception != null)
                    {
                        ApiBukkit.logException(this.exception);
                        this.exception = null;
                        throw new ApiRequestException("World generation failed due to an unknown error!", 7);
                    }
                }
                else
                {
                    throw new ApiRequestException("World does already exist!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            return null;
        }

        public void run()
        {
            try
            {
                WorldCreator creator = new WorldCreator(this.worldName);
                creator.environment(this.env).seed(this.seed).generator(this.generator).createWorld();
            }
            catch (Throwable t)
            {
                ApiBukkit.logException(t);
                this.exception = t;
            }
            synchronized (this.executionThread)
            {
                this.executionThread.notify();
            }
            this.resetVars();
        }
    }

    private class TimeAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String timeParam = params.getProperty("time");
                    if (timeParam != null)
                    {
                        try
                        {
                            long time = Long.valueOf(timeParam);
                            world.setTime(time);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new ApiRequestException("Time must be a valid number!", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No time given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("World not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class PvpAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String state = params.getProperty("pvp");
                    if (state != null)
                    {
                        if (state.equalsIgnoreCase("on"))
                        {
                            world.setPVP(true);
                        }
                        else if (state.equalsIgnoreCase("off"))
                        {
                            world.setPVP(false);
                        }
                        else
                        {
                            throw new ApiRequestException("Invalid state given! Use on or off", 3);
                        }
                    }
                    else
                    {
                        world.setPVP(world.getPVP() ? false : true);
                    }
                }
                else
                {
                    throw new ApiRequestException("World not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class StormAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String state = params.getProperty("storm");
                    if (state != null)
                    {
                        if (state.equalsIgnoreCase("on"))
                        {
                            world.setStorm(true);
                        }
                        else if (state.equalsIgnoreCase("off"))
                        {
                            world.setStorm(false);
                        }
                        else
                        {
                            throw new ApiRequestException("Invalid state given! Use on or off", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No state given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("World not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class SpawnAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String locationParam = params.getProperty("location");
                    String playerName = params.getProperty("player");
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
                                throw new ApiRequestException("No valid location given!", 5);
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            throw new ApiRequestException("No valid location given!", 5);
                        }
                    }
                    else if (playerName != null)
                    {
                        Player player = server.getPlayerExact(playerName);
                        if (player != null)
                        {
                            Location playerLocation = player.getLocation();
                            world.setSpawnLocation(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
                        }
                        else
                        {
                            throw new ApiRequestException("Given player not found", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No location given!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("World not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class ListAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            List<World> worlds = server.getWorlds();
            List<String> data = new ArrayList<String>();
            for (World currentWorld : worlds)
            {
                data.add(currentWorld.getName());
            }
            return data;
        }
    }

    private class PlayersAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    List<String> data = new ArrayList<String>();
                    Iterator<Player> playerIter = world.getPlayers().listIterator();
                    while (playerIter.hasNext())
                    {
                        data.add(playerIter.next().getName());
                    }
                    return data;
                }
                else
                {
                    throw new ApiRequestException("World " + worldName + " not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
        }
    }

    private class SpawnflagsAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    boolean monsters = world.getAllowMonsters();
                    boolean animals = world.getAllowAnimals();

                    String state = params.getProperty("monsters");
                    if (state != null)
                    {
                        if (state.equalsIgnoreCase("on"))
                        {
                            monsters = true;
                        }
                        else if (state.equalsIgnoreCase("off"))
                        {
                            monsters = false;
                        }
                    }
                    state = params.getProperty("animals");
                    if (state != null)
                    {
                        if (state.equalsIgnoreCase("on"))
                        {
                            animals = true;
                        }
                        else if (state.equalsIgnoreCase("off"))
                        {
                            animals = false;
                        }
                    }

                    world.setSpawnFlags(monsters, animals);
                    return null;
                }
                else
                {
                    throw new ApiRequestException("World " + worldName + " not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
        }
    }

    private class SaveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    world.save();
                }
                else
                {
                    throw new ApiRequestException("Given world not found!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No world given!", 1);
            }
            
            return null;
        }
    }
}
