package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ConfigurationController extends ApiRequestController
{
    protected List<String> availableConfigs;

    public ConfigurationController(Plugin plugin, List<String> paths)
    {
        super(plugin, true);
        ApiBukkit.debug("Got " + paths.size() + " config paths...");

        this.availableConfigs = paths;

        this.setAction("write", new WriteAction());
        this.setAction("read", new ReadAction());
        this.setAction("remove", new RemoveAction());
        this.setAction("exists", new ExistsAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }

    private class WriteAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String fileParam = params.getProperty("file");
            String appendParam = params.getProperty("append");
            String dataParam = params.getProperty("data");
            if (fileParam != null)
            {
                if (availableConfigs.contains(fileParam))
                {
                    if (dataParam != null)
                    {
                        try
                        {
                            System.out.println(dataParam);
                            boolean append = false;
                            if (appendParam != null && appendParam.equalsIgnoreCase("true"))
                            {
                                append = true;
                            }
                            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileParam)));
                            if (append)
                            {
                                writer.append(dataParam);
                            }
                            else
                            {
                                writer.write(dataParam);
                            }
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            throw new ApiRequestException("Failed to write the data", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("No data given", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Access denied for the requested file", 2);
                }
                return null;
            }
            else
            {
                throw new ApiRequestException("No file given!", 1);
            }
        }
    }

    private class ReadAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String fileParam = params.getProperty("file");
            if (fileParam != null)
            {
                if (availableConfigs.contains(fileParam))
                {
                    File file = new File(fileParam);
                    if (file.exists())
                    {
                        try
                        {
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null)
                            {
                                stringBuilder.append(line).append("\n");
                            }

                            params.setProperty("format", "raw");
                            return stringBuilder.toString();
                        }
                        catch (IOException e)
                        {
                            throw new ApiRequestException("Failed to read the file", 4);
                        }
                    }
                    else
                    {
                        throw new ApiRequestException("File not found!", 3);
                    }
                }
                else
                {
                    throw new ApiRequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No file given!", 1);
            }
        }
    }

    private class RemoveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String fileParam = params.getProperty("file");
            if (fileParam != null)
            {
                if (availableConfigs.contains(fileParam))
                {
                    (new File(fileParam)).delete();
                }
                else
                {
                    throw new ApiRequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No file given!", 1);
            }
            return null;
        }
    }

    private class ExistsAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String fileParam = params.getProperty("file");
            if (fileParam != null)
            {
                if (availableConfigs.contains(fileParam))
                {
                    return (new File(fileParam).exists());
                }
                else
                {
                    throw new ApiRequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No file given!", 1);
            }
        }
    }
}
