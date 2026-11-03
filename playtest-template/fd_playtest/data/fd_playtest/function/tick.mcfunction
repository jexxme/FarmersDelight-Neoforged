# Build the showcase once, at the first player who joins
execute unless score #built fd_playtest matches 1 as @a[limit=1] at @s run function fd_playtest:build
# Every player: creative-friendly setup on first join
execute as @a[tag=!fd_playtest_seen] run gamemode creative @s
execute as @a[tag=!fd_playtest_seen] run tellraw @s ["",{"text":"[FD Playtest] ","color":"gold"},{"text":"Creative mode on. Use /gamemode survival to test survival behaviour. JEI shows all recipes.","color":"yellow"}]
tag @a[tag=!fd_playtest_seen] add fd_playtest_seen
