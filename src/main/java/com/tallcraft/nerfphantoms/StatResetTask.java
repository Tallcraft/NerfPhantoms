package com.tallcraft.nerfphantoms;

import org.bukkit.Statistic;
import org.bukkit.scheduler.BukkitRunnable;

public class StatResetTask extends BukkitRunnable {

    private NerfPhantoms plugin;

    StatResetTask(NerfPhantoms plugin) {
        this.plugin = plugin;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        plugin.phantomDisabled.forEach((player) -> {
            if (plugin.isWorldEnabled(player.getWorld())) {
                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }
        });

    }
}
