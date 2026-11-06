// Copies the GUI textures the recipe cards and diagrams use: Farmer's Delight's own JEI/GUI textures and effect
// icons, plus a few vanilla GUI sprites (furnace flame and arrow, hunger and heart icons, effect icons).
import fs from 'node:fs';
import path from 'node:path';
import { readAsset } from './lib/assets.mjs';

const SITE = path.resolve(import.meta.dirname, '..');
const OUT = path.join(SITE, 'public/gui');
const files = {
	'cooking_pot.png': 'assets/farmersdelight/textures/gui/jei/cooking_pot.png',
	'cutting_board.png': 'assets/farmersdelight/textures/gui/jei/cutting_board.png',
	'decomposition.png': 'assets/farmersdelight/textures/gui/jei/decomposition.png',
	'cooking_pot_gui.png': 'assets/farmersdelight/textures/gui/cooking_pot.png',
	'fd_icons.png': 'assets/farmersdelight/textures/gui/fd_icons.png',
	'effect/nourishment.png': 'assets/farmersdelight/textures/mob_effect/nourishment.png',
	'effect/comfort.png': 'assets/farmersdelight/textures/mob_effect/comfort.png',
	'mc/lit_progress.png': 'assets/minecraft/textures/gui/sprites/container/furnace/lit_progress.png',
	'mc/burn_progress.png': 'assets/minecraft/textures/gui/sprites/container/furnace/burn_progress.png',
	'mc/food_full.png': 'assets/minecraft/textures/gui/sprites/hud/food_full.png',
	'mc/food_half.png': 'assets/minecraft/textures/gui/sprites/hud/food_half.png',
	'mc/food_empty.png': 'assets/minecraft/textures/gui/sprites/hud/food_empty.png',
	'mc/heart_full.png': 'assets/minecraft/textures/gui/sprites/hud/heart/full.png',
	'mc/heart_half.png': 'assets/minecraft/textures/gui/sprites/hud/heart/half.png',
	'mc/heart_container.png': 'assets/minecraft/textures/gui/sprites/hud/heart/container.png',
	'mc/trade_arrow.png': 'assets/minecraft/textures/gui/sprites/container/villager/trade_arrow.png',
};
for (const e of ['absorption', 'fire_resistance', 'glowing', 'hunger', 'nausea', 'poison', 'regeneration', 'saturation', 'speed', 'strength', 'resistance', 'jump_boost', 'water_breathing', 'mining_fatigue', 'instant_health']) {
	files[`effect/${e}.png`] = `assets/minecraft/textures/mob_effect/${e}.png`;
}
let n = 0;
for (const [to, from] of Object.entries(files)) {
	const buf = readAsset(from);
	if (!buf) {
		console.log('missing', from);
		continue;
	}
	fs.mkdirSync(path.dirname(path.join(OUT, to)), { recursive: true });
	fs.writeFileSync(path.join(OUT, to), buf);
	n++;
}
console.log(`gui: ${n} files`);
