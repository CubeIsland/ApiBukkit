package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 *
 * @author CodeInfection
 */
public final class ApiServer implements Runnable
{
    private static ApiServer instance = null;

    private int port;
    private int maxContentLength;
    private String authenticationKey;
    private ServerBootstrap bootstrap;
    private Thread executionThread;
    private boolean started;

    public ApiServer()
    {
        this.port = 6561;
        this.maxContentLength = 1048576;
        this.bootstrap = null;
        this.executionThread = null;
        this.started = false;
    }

    public static ApiServer getInstance()
    {
        if (instance == null)
        {
            instance = new ApiServer();
        }
        return instance;
    }

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

    public int getPort()
    {
        return this.port;
    }

    public ApiServer setPort(int port)
    {
        this.port = port;
        return this;
    }

    public int getMaxContentLength()
    {
        return this.maxContentLength;
    }

    public ApiServer setMaxContentLength(int maxContentLength)
    {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    public ApiServer setAuthenticationKey(String authkey)
    {
        this.authenticationKey = authkey;
        return this;
    }

    public ApiServer start()
    {
        this.executionThread = new Thread(this);
        this.executionThread.start();
        this.started = true;
        return this;
    }

    public ApiServer stop()
    {
        this.bootstrap.releaseExternalResources();
        this.executionThread.interrupt();
        this.bootstrap = null;
        this.executionThread = null;
        this.started = false;
        return this;
    }

    public void run()
    {
        this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new ApiServerPipelineFactory());
    }
}
