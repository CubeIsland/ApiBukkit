package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public final class ApiRequest
{
    public final Parameters GET;
    public final Parameters POST;
    public final Parameters REQUEST;
    public final Map<String, Object> SERVER;
    public final Map<String, String> headers;
    public final Server server;

    public ApiRequest(Server server)
    {
        this.GET = new Parameters();
        this.POST = new Parameters();
        this.REQUEST = new Parameters();
        this.SERVER = new HashMap<String, Object>();
        this.headers = new HashMap<String, String>();
        
        this.server = server;
    }
}
