# Builds the Farmer's Delight showcase around the executing position (runs once per world).
# Floor and clear space
fill ~-10 ~-1 ~-10 ~10 ~-1 ~10 minecraft:smooth_stone
fill ~-10 ~ ~-10 ~10 ~6 ~10 minecraft:air

# --- Kitchen (north side) ---
# Stove + cooking pot
setblock ~-6 ~ ~-6 farmersdelight:stove[lit=true,facing=south]
setblock ~-6 ~1 ~-6 farmersdelight:cooking_pot[facing=south]
# Stove + skillet
setblock ~-4 ~ ~-6 farmersdelight:stove[lit=true,facing=south]
setblock ~-4 ~1 ~-6 farmersdelight:skillet[facing=south]
# Campfire + cooking pot (also a heat source)
setblock ~-2 ~ ~-6 minecraft:campfire[lit=true]
setblock ~-2 ~1 ~-6 farmersdelight:cooking_pot[facing=south]
# Empty stove to grill food on
setblock ~0 ~ ~-6 farmersdelight:stove[lit=true,facing=south]
# Cutting boards
setblock ~2 ~ ~-6 farmersdelight:cutting_board[facing=south]
setblock ~3 ~ ~-6 farmersdelight:cutting_board[facing=south]
# Storage
setblock ~5 ~ ~-6 farmersdelight:oak_cabinet[facing=south]
setblock ~6 ~ ~-6 farmersdelight:pale_oak_cabinet[facing=south]
setblock ~7 ~ ~-6 farmersdelight:bamboo_basket[facing=up]

# Ingredient chests
setblock ~-6 ~ ~-4 minecraft:chest[facing=south]{Items:[{Slot:0b,id:"farmersdelight:tomato",count:64},{Slot:1b,id:"farmersdelight:cabbage",count:64},{Slot:2b,id:"farmersdelight:onion",count:64},{Slot:3b,id:"farmersdelight:rice",count:64},{Slot:4b,id:"minecraft:beef",count:64},{Slot:5b,id:"minecraft:porkchop",count:64},{Slot:6b,id:"minecraft:chicken",count:64},{Slot:7b,id:"minecraft:cod",count:64},{Slot:8b,id:"minecraft:salmon",count:64},{Slot:9b,id:"minecraft:potato",count:64},{Slot:10b,id:"minecraft:carrot",count:64},{Slot:11b,id:"minecraft:beetroot",count:64},{Slot:12b,id:"minecraft:wheat",count:64},{Slot:13b,id:"minecraft:egg",count:16},{Slot:14b,id:"minecraft:milk_bucket",count:1},{Slot:15b,id:"minecraft:bowl",count:64},{Slot:16b,id:"minecraft:glass_bottle",count:64},{Slot:17b,id:"minecraft:pumpkin",count:64},{Slot:18b,id:"minecraft:cake",count:1},{Slot:19b,id:"minecraft:sugar",count:64},{Slot:20b,id:"minecraft:cocoa_beans",count:64},{Slot:21b,id:"minecraft:sweet_berries",count:64},{Slot:22b,id:"minecraft:apple",count:64},{Slot:23b,id:"minecraft:brown_mushroom",count:64},{Slot:24b,id:"minecraft:red_mushroom",count:64},{Slot:25b,id:"minecraft:bone",count:64},{Slot:26b,id:"minecraft:mutton",count:64}]}
setblock ~-5 ~ ~-4 minecraft:chest[facing=south]{Items:[{Slot:0b,id:"farmersdelight:flint_knife",count:1},{Slot:1b,id:"farmersdelight:copper_knife",count:1},{Slot:2b,id:"farmersdelight:iron_knife",count:1},{Slot:3b,id:"farmersdelight:golden_knife",count:1},{Slot:4b,id:"farmersdelight:diamond_knife",count:1},{Slot:5b,id:"farmersdelight:netherite_knife",count:1},{Slot:6b,id:"farmersdelight:skillet",count:1},{Slot:7b,id:"farmersdelight:cooking_pot",count:1},{Slot:8b,id:"farmersdelight:cutting_board",count:4},{Slot:9b,id:"farmersdelight:stove",count:4},{Slot:10b,id:"minecraft:iron_axe",count:1},{Slot:11b,id:"minecraft:iron_pickaxe",count:1},{Slot:12b,id:"minecraft:iron_shovel",count:1},{Slot:13b,id:"minecraft:iron_hoe",count:1},{Slot:14b,id:"minecraft:shears",count:1},{Slot:15b,id:"minecraft:flint_and_steel",count:1},{Slot:16b,id:"minecraft:fire_charge",count:16},{Slot:17b,id:"minecraft:bone_meal",count:64},{Slot:18b,id:"minecraft:oak_log",count:64},{Slot:19b,id:"minecraft:bricks",count:16},{Slot:20b,id:"farmersdelight:rope",count:64},{Slot:21b,id:"farmersdelight:canvas",count:16},{Slot:22b,id:"farmersdelight:canvas_sign",count:16},{Slot:23b,id:"farmersdelight:hanging_canvas_sign",count:16},{Slot:24b,id:"farmersdelight:straw",count:64},{Slot:25b,id:"farmersdelight:organic_compost",count:16},{Slot:26b,id:"farmersdelight:rich_soil",count:64}]}
setblock ~-4 ~ ~-4 minecraft:chest[facing=south]{Items:[{Slot:0b,id:"farmersdelight:cabbage_seeds",count:64},{Slot:1b,id:"farmersdelight:tomato_seeds",count:64},{Slot:2b,id:"farmersdelight:rice_panicle",count:64},{Slot:3b,id:"farmersdelight:wild_cabbages",count:16},{Slot:4b,id:"farmersdelight:wild_onions",count:16},{Slot:5b,id:"farmersdelight:wild_tomatoes",count:16},{Slot:6b,id:"farmersdelight:wild_carrots",count:16},{Slot:7b,id:"farmersdelight:wild_potatoes",count:16},{Slot:8b,id:"farmersdelight:wild_beetroots",count:16},{Slot:9b,id:"farmersdelight:wild_rice",count:16},{Slot:10b,id:"farmersdelight:sandy_shrub",count:16},{Slot:11b,id:"farmersdelight:brown_mushroom_colony",count:16},{Slot:12b,id:"farmersdelight:red_mushroom_colony",count:16},{Slot:13b,id:"minecraft:water_bucket",count:1},{Slot:14b,id:"minecraft:villager_spawn_egg",count:4},{Slot:15b,id:"minecraft:wandering_trader_spawn_egg",count:2},{Slot:16b,id:"minecraft:pig_spawn_egg",count:4},{Slot:17b,id:"minecraft:cow_spawn_egg",count:4},{Slot:18b,id:"minecraft:chicken_spawn_egg",count:4},{Slot:19b,id:"minecraft:spider_spawn_egg",count:2},{Slot:20b,id:"minecraft:zombie_spawn_egg",count:2},{Slot:21b,id:"minecraft:composter",count:4},{Slot:22b,id:"minecraft:hopper",count:4},{Slot:23b,id:"minecraft:dispenser",count:4},{Slot:24b,id:"minecraft:short_grass",count:64},{Slot:25b,id:"minecraft:tall_grass",count:32},{Slot:26b,id:"minecraft:stick",count:64}]}

# Feasts and pies (on a counter)
fill ~-6 ~ ~-2 ~0 ~ ~-2 minecraft:polished_andesite
setblock ~-6 ~1 ~-2 farmersdelight:roast_chicken_block[facing=south]
setblock ~-5 ~1 ~-2 farmersdelight:stuffed_pumpkin_block[facing=south]
setblock ~-4 ~1 ~-2 farmersdelight:honey_glazed_ham_block[facing=south]
setblock ~-3 ~1 ~-2 farmersdelight:shepherds_pie_block[facing=south]
setblock ~-2 ~1 ~-2 farmersdelight:apple_pie[facing=south]
setblock ~-1 ~1 ~-2 farmersdelight:sweet_berry_cheesecake[facing=south]
setblock ~0 ~1 ~-2 farmersdelight:chocolate_pie[facing=south]

# Signs
setblock ~2 ~ ~-3 farmersdelight:canvas_sign[rotation=8]
setblock ~3 ~ ~-3 farmersdelight:red_canvas_sign[rotation=8]
setblock ~4 ~ ~-3 farmersdelight:black_canvas_sign[rotation=8]

# --- Farm (south side) ---
# Rich soil farmland with mature crops, water channel in the middle
fill ~-8 ~-1 ~3 ~0 ~-1 ~8 farmersdelight:rich_soil_farmland[moisture=7]
fill ~-4 ~-1 ~3 ~-4 ~-1 ~8 minecraft:water
fill ~-8 ~ ~3 ~-5 ~ ~4 farmersdelight:cabbages[age=7]
fill ~-8 ~ ~5 ~-5 ~ ~6 farmersdelight:onions[age=7]
fill ~-3 ~ ~3 ~0 ~ ~4 minecraft:wheat[age=7]
fill ~-3 ~ ~5 ~0 ~ ~6 farmersdelight:budding_tomatoes[age=0]
fill ~-8 ~ ~7 ~-5 ~ ~8 minecraft:carrots[age=7]
fill ~-3 ~ ~7 ~0 ~ ~8 minecraft:potatoes[age=7]
# Tomatoes climbing ropes
fill ~2 ~-1 ~3 ~5 ~-1 ~3 farmersdelight:rich_soil_farmland[moisture=7]
fill ~2 ~-1 ~4 ~5 ~-1 ~4 minecraft:water
fill ~2 ~ ~3 ~5 ~ ~3 farmersdelight:tomatoes[age=3]
fill ~2 ~1 ~3 ~5 ~2 ~3 farmersdelight:rope
# Rice in water
fill ~2 ~-2 ~6 ~5 ~-2 ~8 minecraft:dirt
fill ~2 ~-1 ~6 ~5 ~-1 ~8 minecraft:water
# Rich soil + mushroom colonies
fill ~7 ~-1 ~3 ~9 ~-1 ~5 farmersdelight:rich_soil
setblock ~7 ~ ~3 farmersdelight:brown_mushroom_colony
setblock ~9 ~ ~3 farmersdelight:red_mushroom_colony
# Grass for straw harvesting with a knife
fill ~7 ~-1 ~6 ~9 ~-1 ~8 minecraft:grass_block
fill ~7 ~ ~6 ~9 ~ ~8 minecraft:short_grass

# Light
setblock ~-10 ~2 ~-10 minecraft:lantern[hanging=false]
setblock ~10 ~2 ~10 minecraft:lantern[hanging=false]

tellraw @a ["",{"text":"[FD Playtest] ","color":"gold"},{"text":"Showcase built: kitchen north, farm south. Chests have ingredients, tools and seeds.","color":"yellow"}]
scoreboard players set #built fd_playtest 1
