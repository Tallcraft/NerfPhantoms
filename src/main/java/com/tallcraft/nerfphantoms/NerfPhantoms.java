package com.tallcraft.nerfphantoms;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class NerfPhantoms extends JavaPlugin implements Listener {
    private final Logger logger = Logger.getLogger(this.getName());
    private Storage storage;
    private FileConfiguration config;
    Set<Player> phantomDisabled = ConcurrentHashMap.newKeySet();


    @Override
    public void onEnable() {
        new Metrics(this);
        initConfig();

        // Initialize database if enabled
        ConfigurationSection databaseCfg = config.getConfigurationSection("database");
        if(databaseCfg != null && databaseCfg.getBoolean("enabled")) {
            storage = new Storage(databaseCfg);
            try {
                storage.init(this);
                logger.info("Database connection established");
            } catch(SQLException ex) {
                storage = null;
                logger.info("Error while connection to database");
                ex.printStackTrace();
            }
        }

        getCommand("nerfphantoms").setTabCompleter(new TabCompletion());
        getServer().getPluginManager().registerEvents(this, this);
        new StatResetTask(this).runTaskTimerAsynchronously(this, 0L, 1200L);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            return false;
        }

        String permissionMessage = cmd.getPermissionMessage();
        assert (permissionMessage != null);

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nerfphantoms.reload")) {
                sender.sendMessage(permissionMessage);
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
                sender.sendMessage(permissionMessage);
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Command has to be executed by a player");
                return true;
            }
            Player player = (Player) sender;
            int n = killAllPhantoms(player.getWorld());
            player.sendMessage("Killed " + n + " phantoms.");
            return true;
        }

        if (args[0].equalsIgnoreCase("togglespawn")) {
            if (args.length == 1) {
                if (!sender.hasPermission("nerfphantoms.disablespawn.self")) {
                    sender.sendMessage(permissionMessage);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Command has to be executed by a player");
                    return true;
                }
                Player player = (Player) sender;
                boolean state = togglePhantomSpawn(player);
                player.sendMessage((state ? "Disabled" : "Enabled")
                        + " phantom spawn for " + player.getDisplayName() + ".");
                return true;
            }
            if (!sender.hasPermission("nerfphantoms.disablespawn.others")) {
                sender.sendMessage(permissionMessage);
                return true;
            }
            Player victim = Bukkit.getPlayer(args[1]);
            if (victim == null) {
                sender.sendMessage("Unable to find player!");
                return true;
            }
            boolean state = togglePhantomSpawn(victim);
            sender.sendMessage((state ? "Disabled" : "Enabled")
                    + " phantom spawn for " + victim.getDisplayName() + ".");
            return true;
        }

        return false;
    }

    private boolean togglePhantomSpawn(Player player) {
        return this.togglePhantomSpawn(player, true);
    }

    private boolean togglePhantomSpawn(Player player, boolean persist) {
        boolean isDisabled = phantomDisabled.contains(player);

        if (isDisabled) {
            phantomDisabled.remove(player);
        } else {
            // Initial stat reset, subsequent calls will be done by scheduled task
            if (isWorldEnabled(player.getWorld())) {
                // We could be in an async context here, schedule the bukkit api access sync.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                }.runTask(this);
            }
            phantomDisabled.add(player);
        }

        // Store setting in database (async)
        if (persist && storage != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        storage.setPhantomDisabled(player.getUniqueId(), !isDisabled);
                    } catch (SQLException throwables) {
                        logger.info("Error while updating player data in storage");
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(this);
        }

        return !isDisabled;
    }

    boolean isWorldEnabled(World world) {
        if (world == null) {
            return false;
        }
        List<String> enabledWorlds = config.getStringList("enabledWorlds");
        // If no worlds are defined in "enabledWorlds", disable allowlist functionality and treat
        // all worlds as enabled.
        return enabledWorlds.size() == 0 || enabledWorlds.contains(world.getName());
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
        if (isWorldEnabled(world)) {
            nerf(event);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (victim.getType() != EntityType.PLAYER || damager.getType() != EntityType.PHANTOM) {
            return;
        }

        if (!isWorldEnabled(victim.getWorld())) {
            return;
        }

        // Phantom damages player
        // => Modify damage
        double damageModifier = config.getDouble("damageModifier");
        double nerfedDamage = roundToHalf(event.getDamage() * damageModifier);
        event.setDamage(nerfedDamage);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        phantomDisabled.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("nerfphantoms.disablespawn.auto")) {
            togglePhantomSpawn(player, false);
            return;
        }

        if(storage == null) {
            return;
        }
        // Check storage for disabled player setting (async)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Import phantom disabled state from storage (if enabled)
                try {
                    if (storage.getPhantomDisabled(player.getUniqueId())) {
                        togglePhantomSpawn(player, false);
                    }
                } catch (SQLException throwables) {
                    logger.info("Error while fetching player data from storage");
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
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

        // Warn if "enabledWorlds" contains unknown worlds.
        // If config isn't initialized at this point "enabledWorlds" will be an empty list.
        List<String> enabledWorlds = config.getStringList("enabledWorlds");
        for(String worldName : enabledWorlds) {
            if(Bukkit.getWorld(worldName) == null) {
                logger.warning("Config entry \"enabledWorlds\" contains unknown world '" + worldName + "'.");
            }
        }

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
        defaultConfig.set("damageModifier", 1.0);
        defaultConfig.set("fixedSize.enabled", false);
        defaultConfig.set("fixedSize.value", 1);

        ConfigurationSection db = defaultConfig.createSection("database");
        db.set("enabled", true);
        db.set("type", "sqlite");
        db.set("host", "localhost");
        db.set("port", 3306);
        db.set("name", "nerfphantoms");
        db.set("username", "user");
        db.set("password", "123456");

        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }
}
