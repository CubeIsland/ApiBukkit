package de.codeinfection.quickwango.ApiBukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author CodeInfection
 */
public class ApibukkitCommand implements CommandExecutor
{
    protected final ApiBukkit plugin;

    public ApibukkitCommand(ApiBukkit plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            String action = "help";
            if (args.length > 0)
            {
                action = args[0];
            }

            if (action.equalsIgnoreCase("help"))
            {
                ApiBukkit.log("Command: apibukkit <help|info|reload>");
                ApiBukkit.log("");
                ApiBukkit.log("help: prints this text");
                ApiBukkit.log("info: prints information about the API");
                ApiBukkit.log("reload: reloads the API web server");
            }
            else if (action.equalsIgnoreCase("info"))
            {
                if (!this.plugin.isZombie())
                {
                    sender.sendMessage("API Port:    " + this.plugin.getApiConfig().port);
                    sender.sendMessage("API Authkey: " + this.plugin.getApiConfig().authKey);
                }
                else
                {
                    sender.sendMessage("The API is currenty in a zombie state. Check your log for errors and try to reload the plugin and/or the server.");
                }
            }
            else if (action.equalsIgnoreCase("reload"))
            {
                this.plugin.onDisable(false);
                this.plugin.onEnable();
            }
            else
            {
                sender.sendMessage("Unknown action specified!");
            }
        }
        else
        {
            sender.sendMessage("This command cannot be executed as a player!");
        }
        return true;
    }
}
