package com.tallcraft.nerfphantoms;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public final class NerfPhantoms extends JavaPlugin implements Listener {
    private final Logger logger = Logger.getLogger(this.getName());
    private FileConfiguration config;


    @Override
    public void onEnable() {
        new Metrics(this);
        initConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nerfphantoms.reload")) {
                sender.sendMessage(cmd.getPermissionMessage());
                return true;
            }
            this.reloadConfig();
            config = this.getConfig();

            logger.info("Reloaded configuration");
            if (sender instanceof Player) {
                sender.sendMessage("Reloaded configuration");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("kill")) {
            if (!sender.hasPermission("nerfphantoms.kill")) {
                sender.sendMessage(cmd.getPermissionMessage());
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Command has be executed by a player");
                return true;
            }
            Player player = (Player) sender;
            int n = killAllPhantoms(player.getWorld());
            player.sendMessage("Killed " + n + " phantoms.");
            return true;
        }

        return false;
    }

    private int killAllPhantoms(World world) {
        Collection<Phantom> phantoms = world.getEntitiesByClass(Phantom.class);
        int n = 0;
        for (Phantom phantom : phantoms) {
            phantom.remove();
            n++;
        }
        return n;
    }


    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        if (config.getList("enabledWorlds").contains(world.getName())) {
            nerf(event);
        }
    }

    private void nerf(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.PHANTOM) {
            return;
        }
        // Phantom spawn
        // Natural spawn?
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (!config.getBoolean("allowNaturalSpawn")) {
                // Disable natural spawn
                event.setCancelled(true);
                return;
            }
        } else {
            if (config.getBoolean("onlyNerfNatural")) return;
        }
        // Nerf
        Phantom phantom = (Phantom) event.getEntity();

        phantom.setSilent(config.getBoolean("muteSound"));
        phantom.setAI(!config.getBoolean("disableAI"));
        phantom.setHealth(config.getDouble("health"));
        if (config.getBoolean("fixedSize.enabled")) {
            phantom.setSize(config.getInt("fixedSize.value"));
        }


    }


    private void initConfig() {
        config = this.getConfig();

        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        ArrayList<String> worldNames = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worldNames.add(world.getName());
        }

        defaultConfig.set("enabledWorlds", worldNames);
        defaultConfig.set("allowNaturalSpawn", true);
        defaultConfig.set("onlyNerfNatural", true);
        defaultConfig.set("muteSound", false);
        defaultConfig.set("disableAI", false);
        defaultConfig.set("health", 20d);
        defaultConfig.set("fixedSize.enabled", false);
        defaultConfig.set("fixedSize.value", 1);

        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }
}
