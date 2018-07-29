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
```

## Commands
`/nerfphantoms reload`: Reload options from the configuration file
`/nerfphantoms kill`: Kill all Phantoms in your current world.

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
nerfphantoms.reload:
  description: Reload plugin config
  default: false
nerfphantoms.kill:
  description: Kill all phantoms in current world
  default: false
```
