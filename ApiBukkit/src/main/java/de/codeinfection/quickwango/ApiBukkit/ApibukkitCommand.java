package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.Abstraction.Plugin;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.log;
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
    private final Plugin plugin;
    private final ApiConfiguration config;

    public ApibukkitCommand(Plugin plugin)
    {
        this.plugin = plugin;
        this.config = ApiBukkit.getInstance().getApiConfiguration();
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
                log("Command: apibukkit <help|info|reload>");
                log("");
                log("help: prints this text");
                log("info: prints information about the API");
                log("reload: reloads the API web server");
            }
            else if (action.equalsIgnoreCase("info"))
            {
                sender.sendMessage("API Port:    " + this.config.port);
                sender.sendMessage("API Authkey: " + this.config.authKey);
            }
            else if (action.equalsIgnoreCase("reload"))
            {
                this.plugin.disable();
                this.plugin.enable();
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
