package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
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
public class ConfigurationController extends AbstractRequestController
{
    protected List<String> availableConfigs;

    public ConfigurationController(Plugin plugin, List<String> paths)
    {
        super(plugin, true);
        ApiBukkit.debug("Got " + paths.size() + " config paths...");

        this.availableConfigs = paths;

        this.registerAction("write", new WriteAction());
        this.registerAction("read", new ReadAction());
        this.registerAction("remove", new RemoveAction());
        this.registerAction("exists", new ExistsAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }

    private class WriteAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
                            throw new RequestException("Failed to write the data", 4);
                        }
                    }
                    else
                    {
                        throw new RequestException("No data given", 3);
                    }
                }
                else
                {
                    throw new RequestException("Access denied for the requested file", 2);
                }
                return null;
            }
            else
            {
                throw new RequestException("No file given!", 1);
            }
        }
    }

    private class ReadAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
                            throw new RequestException("Failed to read the file", 3);
                        }
                    }
                    else
                    {
                        return "";
                    }
                }
                else
                {
                    throw new RequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new RequestException("No file given!", 1);
            }
        }
    }

    private class RemoveAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
                    throw new RequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new RequestException("No file given!", 1);
            }
            return null;
        }
    }

    private class ExistsAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
                    throw new RequestException("Access denied for the requested file", 2);
                }
            }
            else
            {
                throw new RequestException("No file given!", 1);
            }
        }
    }
}
