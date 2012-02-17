package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.error;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * This class represents the API server and provides methods to configure and controll it
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public final class ApiServer implements Runnable
{
    private static ApiServer instance = null;

    private int port;
    private int maxContentLength;
    private String authenticationKey;
    private InetAddress ip;

    private ServerBootstrap bootstrap;
    private Thread executionThread;
    private boolean started;

    private ApiServer()
    {
        this.port = 6561;
        this.maxContentLength = 1048576;
        try
        {
            this.ip = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            error("Could not receive the localhost...");
        }

        this.bootstrap = null;
        this.executionThread = null;
        this.started = false;
    }

    /**
     * Returns the singlton instance of the ApiServer
     * 
     * @return the ApiServer instance
     */
    public static ApiServer getInstance()
    {
        if (instance == null)
        {
            instance = new ApiServer();
        }
        return instance;
    }

    /**
     * Returns whether the server is running or not
     * 
     * @return true if it is running
     */
    public boolean isRunning()
    {
        if (this.started)
        {
            if (this.executionThread != null)
            {
                return this.executionThread.isAlive();
            }
        }
        return false;
    }

    /**
     * Returns the address the server is bound/will bind to
     * 
     * @return the address
     */
    public InetAddress getIp()
    {
        return this.ip;
    }

    /**
     * Sets the address the server will bind to on the next start
     * 
     * @param ip the address
     * @return fluent interface
     */
    public ApiServer setIp(InetAddress ip)
    {
        if (ip != null)
        {
            this.ip = ip;
        }
        return this;
    }

    /**
     * Returns the port the server is/will be listening on
     * 
     * @return the post
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * Sets the port to listen on after the next start
     * 
     * @param port the port
     * @return fluent interface
     */
    public ApiServer setPort(int port)
    {
        this.port = port;
        return this;
    }

    /**
     * Returns the maximum content length the client may send
     * 
     * @return the maximum content length
     */
    public int getMaxContentLength()
    {
        return this.maxContentLength;
    }

    /**
     * Sets the maximum content length the clients may send after the next start
     * 
     * @param maxContentLength the maximum content length
     * @return fluent interface
     */
    public ApiServer setMaxContentLength(int maxContentLength)
    {
        this.maxContentLength = maxContentLength;
        return this;
    }

    /**
     * Returns the authentication key
     * 
     * @return the key
     */
    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    /**
     * Sets the authentication key which will be used instantly
     * 
     * @param the key
     * @return fluent interface
     */
    public ApiServer setAuthenticationKey(String authkey)
    {
        this.authenticationKey = authkey;
        return this;
    }

    /**
     * Starts the server
     * 
     * @return fluent interface
     */
    public ApiServer start()
    {
        if (!this.isRunning())
        {
            this.executionThread = new Thread(this);
            this.executionThread.start();
            this.started = true;
        }
        return this;
    }

    /**
     * Stops the server
     * 
     * @return fluent interface
     */
    public ApiServer stop()
    {
        if (this.isRunning())
        {
            this.bootstrap.releaseExternalResources();
            this.executionThread.interrupt();
            this.bootstrap = null;
            this.executionThread = null;
            this.started = false;
        }
        return this;
    }

    /**
     * This is invoked from the execution thread of the server
     */
    public void run()
    {
        this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new ApiServerPipelineFactory());

        bootstrap.bind(new InetSocketAddress(this.ip, port));
    }
}
