// Extracted from Crazy-Crew/CrazyEnchantments project, under MIT license.
package com.projectkorra.items.processors;

import com.projectkorra.items.schedulers.FoliaRunnable;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class PoolProcessor {

    private final Plugin plugin;

    private ThreadPoolExecutor executor = null;

    private final int maxQueueSize = 10000;

    private ScheduledTask taskId;

    public PoolProcessor(Plugin plugin) {
        this.plugin = plugin;
        start();
    }

    /**
     * Adds the task into the thread pool to be processed.
     * @param process The {@link Runnable} to process.
     */
    public void add(final Runnable process) {
        executor.submit(process);
    }

    /**
     * Creates the thread pool used to process tasks.
     */
    public void start() {
        if (executor == null) executor = new ThreadPoolExecutor(1, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(maxQueueSize));
        executor.allowCoreThreadTimeOut(true);
        resizeChecker();
    }

    /**
     * Terminates the thread pool.
     */
    public void stop() {
        taskId.cancel();
        executor.shutdown();
        executor = null;
    }

    /**
     * Used to increase the default workers in the thread pool.
     * This should ensure that with a higher player count, that all tasks are processed.
     */
    private void resizeChecker() {
        taskId = new FoliaRunnable(this.plugin.getServer().getAsyncScheduler(), TimeUnit.SECONDS) {
            @Override
            public void run() {
                if ((executor.getQueue().size() / executor.getCorePoolSize() > maxQueueSize / 5) && !(executor.getMaximumPoolSize() <= executor.getCorePoolSize() + 1)) {
                    executor.setCorePoolSize(executor.getCorePoolSize() + 1);
                }
            }
        }.runAtFixedRate(plugin, 20, 100);
    }
}
