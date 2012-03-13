package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.Abstraction.Scheduler;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 * @author CodeInfection
 */
class SpoutScheduler implements Scheduler
{
    private final org.spout.api.scheduler.Scheduler scheduler;

    public SpoutScheduler(org.spout.api.scheduler.Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task)
    {
        return this.scheduler.callSyncMethod(((SpoutPlugin)plugin).getHandle(), task);
    }

    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task)
    {
        return this.scheduleAsyncDelayedTask(plugin, task, 0);
    }

    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delay)
    {
        return this.scheduleAsyncRepeatingTask(plugin, task, 0, -1);
    }

    public int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period)
    {
        return this.scheduler.scheduleAsyncRepeatingTask(((SpoutPlugin)plugin).getHandle(), task, delay, period);
    }

    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task)
    {
        return this.scheduleSyncDelayedTask(plugin, task, 0);
    }

    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay)
    {
        return this.scheduleSyncRepeatingTask(plugin, task, 0, -1);
    }

    public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period)
    {
        return this.scheduler.scheduleSyncRepeatingTask(((SpoutPlugin)plugin).getHandle(), task, delay, period);
    }

    public void cancelTask(int id)
    {
        this.scheduler.cancelTask(id);
    }

    public void cancelTasks(Plugin plugin)
    {
        this.scheduler.cancelTasks(((SpoutPlugin)plugin).getHandle());
    }

    public void cancelAllTasks()
    {
        this.scheduler.cancelAllTasks();
    }
}
