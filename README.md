# NerfPhantoms
Bukkit plugin which allows nerfing Phantom mobs introduced in Minecraft 1.13

## Configuration
```
# Disable or enable natural phantom spawn
allowNaturalSpawn: true

# Only apply nerf actions below to naturally spawned phantoms? A non natural spawn would be a spawnegg
onlyNerfNatural: true

# Mute phantom sound (This isn't fully working yet. Only mute some of the phantom sounds)
muteSound: false

# Disable phantom AI. Causes them to hover in one position and not move.
disableAI: false

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
