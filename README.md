# NerfPhantoms
Bukkit plugin which allows nerfing Phantom mobs introduced in Minecraft 1.13

[SpigotMC plugin page](https://www.spigotmc.org/resources/nerfphantoms.59218/)

## Configuration
``` yaml
# Whitelist of worlds to enable the plugin in. Includes all worlds by default
enabledWorlds:
- world
- world_nether
- world_the_end

# Disable or enable natural phantom spawn
allowNaturalSpawn: true

# Only apply nerf actions below to naturally spawned phantoms? A non natural spawn would be a spawnegg
onlyNerfNatural: true

# Mute phantom sound (This isn't fully working yet. Only mute some of the phantom sounds)
muteSound: false

# Disable phantom AI. Causes them to hover in one position and not move.
disableAI: false

# Phantom damage to player modifier. PhantomDamage * damageModifier = Damage to player
damageModifier: 1.0

# Set phantom health, default is 20. If you set it to 0 they die instantly.
health: 20.0

# Should all phantoms have a fixed size?
fixedSize:
  enabled: false
  # How big should phantoms be?
  value: 1

# Database configuration
# The database is used to remember if the player toggled phantoms on or off.
# Defaults to sqlite where 'name' is the database file name. The other fields are ignored.
# Change type to 'mysql' if you want the plugin to connect to a mySQL server.
database:
  enabled: false
  type: sqlite
  name: nerfphantoms
  host: localhost
  port: 3306
  username: root
  password: '123456'
```

With version 1.4.0, you can also use sqlite. Set `type` to sqlite and `name` to your file name. `host` and `port` will be ignored in that case.

## Commands
`/nerfphantoms reload`: Reload options from the configuration file\
`/nerfphantoms kill`: Kill all Phantoms in your current world.\
`/nerfphantoms togglespawn`: Toggle phantoms spawn for yourself.\
`/nerfphantoms togglespawn <player>`: Toggle phantoms spawn for another player.

## Permissions
``` yaml
  nerfphantoms.*:
    description: Gives access to all NerfPhantoms commands
    default: op
    children:
      nerfphantoms.all: true
  nerfphantoms.all:
    description: Gives access to all NerfPhantoms commands
    children:
      nerfphantoms.reload: true
      nerfphantoms.kill: true
      nerfphantoms.disablespawn.self: true
      nerfphantoms.disablespawn.other: true
  nerfphantoms.reload:
    description: Reload plugin config
    default: false
  nerfphantoms.kill:
    description: Kill all phantoms in current world
    default: false
  nerfphantoms.disablespawn.self:
    description: Command to disable phantom spawning for self
    default: op
  nerfphantoms.disablespawn.others:
    description: Command to disable phantom spawning for other players
    default: op
  nerfphantoms.disablespawn.auto:
    description: Phantom spawn disable active by default for player
    default: false
```
