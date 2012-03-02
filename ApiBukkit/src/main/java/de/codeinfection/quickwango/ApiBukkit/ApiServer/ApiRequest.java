package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;

/**
 * This class contains all the information of the API request.
 * It is only used to pass the information the to executing action, nothing more.
 * 
 * @author Phillip Schichtel
 * @since 1.0.0
 * @todo rename vars
 */
public final class ApiRequest
{
    public final Parameters params;
    public final Map<String, Object> SERVER;
    public final Map<String, String> headers;
    public final Server server;

    /**
     * Initializes the ApiRequest with an Server instance
     */
    public ApiRequest(Server server)
    {
        this.params = new Parameters();
        this.SERVER = new HashMap<String, Object>();
        this.headers = new HashMap<String, String>();
        
        this.server = server;
    }
}
