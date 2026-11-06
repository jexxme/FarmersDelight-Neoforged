// Isometric renders for the docs: every Farmer's Delight block, growth stages, feast servings, pie bites and the
// small scenes used in the mechanic diagrams (pot on a stove, heat sources...). Output: public/renders/**.png and
// src/data/gen/renders.json (which renders exist).
import fs from 'node:fs';
import path from 'node:path';
import { renderScene, blockstateModel } from './lib/render.mjs';

const SITE = path.resolve(import.meta.dirname, '..');
const runtime = JSON.parse(fs.readFileSync(path.join(SITE, 'src/data/runtime.json'), 'utf8'));
const OUT = path.join(SITE, 'public/renders');
const GRASS = [[0x91, 0xbd, 0x59]];
const manifest = {};

/** place(blockId, state, [x, y, z]) -> scene parts */
function place(block, state = {}, offset = [0, 0, 0], tint) {
	// Fire is a multipart model with OR conditions; its floor model is what you see on a block
	if (block === 'minecraft:fire') return [{ model: 'minecraft:block/fire_floor0', offset }];
	const parts = blockstateModel(block, state);
	if (!parts) throw new Error('no blockstate for ' + block);
	return parts.map((p) => ({ ...p, offset, tint }));
}
async function save(name, parts, opts = {}) {
	try {
		const png = await renderScene(parts, { size: 256, ...opts });
		if (!png) throw new Error('empty');
		const file = path.join(OUT, name + '.png');
		fs.mkdirSync(path.dirname(file), { recursive: true });
		fs.writeFileSync(file, png);
		manifest[name] = true;
	} catch (e) {
		console.log(`! ${name}: ${e.message}`);
	}
}
const ageValues = (id, prop = 'age') => runtime.blocks[id]?.properties[prop] ?? [];
const facingNorth = (id) => (runtime.blocks[id]?.properties.facing ? { facing: 'north' } : {});

// Every block in its default look
for (const [id, b] of Object.entries(runtime.blocks)) {
	const slug = id.split(':')[1];
	if (/wall_|_wall|hanging/.test(slug) && /sign/.test(slug)) continue;
	const state = { ...b.defaultState, ...facingNorth(id) };
	if ('lit' in state) state.lit = 'true';
	await save(`blocks/${slug}`, place(id, state));
}

// Growth stages, standing on the block they grow on
const crops = [
	['farmersdelight:cabbages', 'age', 'minecraft:farmland', { moisture: 7 }],
	['farmersdelight:onions', 'age', 'minecraft:farmland', { moisture: 7 }],
	['farmersdelight:budding_tomatoes', 'age', 'minecraft:farmland', { moisture: 7 }],
	['farmersdelight:tomatoes', 'age', 'minecraft:farmland', { moisture: 7 }],
	['farmersdelight:rice', 'age', 'minecraft:dirt', {}],
	['farmersdelight:rice_panicles', 'age', null, {}],
	['farmersdelight:brown_mushroom_colony', 'age', 'farmersdelight:rich_soil', {}],
	['farmersdelight:red_mushroom_colony', 'age', 'farmersdelight:rich_soil', {}],
];
for (const [id, prop, ground, gstate] of crops) {
	for (const v of ageValues(id, prop)) {
		const parts = [];
		if (ground) parts.push(...place(ground, gstate, [0, -1, 0]));
		parts.push(...place(id, { [prop]: v }));
		if (id.endsWith(':rice') && v === '3') parts.push(...place('farmersdelight:rice_panicles', { age: '0' }, [0, 1, 0]));
		await save(`stages/${id.split(':')[1]}-${v}`, parts);
	}
}
// Tomatoes climbing rope
for (const v of ageValues('farmersdelight:tomatoes_on_rope')) {
	await save(`stages/tomatoes_on_rope-${v}`, [
		...place('minecraft:farmland', { moisture: 7 }, [0, -1, 0]),
		...place('farmersdelight:tomatoes', { age: '3', ropelogged: 'true' }),
		...place('farmersdelight:tomatoes_on_rope', { age: v }, [0, 1, 0]),
		...place('farmersdelight:rope', {}, [0, 2, 0]),
	]);
}
for (const v of ageValues('farmersdelight:organic_compost', 'composting')) await save(`stages/organic_compost-${v}`, place('farmersdelight:organic_compost', { composting: v }));
for (const id of runtime.tags.block['farmersdelight:feasts']) {
	for (const v of ageValues(id, 'servings')) await save(`stages/${id.split(':')[1]}-${v}`, place(id, { servings: v, facing: 'north' }));
}
for (const id of runtime.tags.block['farmersdelight:pies']) {
	for (const v of ageValues(id, 'bites')) await save(`stages/${id.split(':')[1]}-${v}`, place(id, { bites: v, facing: 'north' }));
}
for (const v of ageValues('farmersdelight:rich_soil_farmland', 'moisture').filter((v) => v === '0' || v === '7')) await save(`stages/rich_soil_farmland-${v}`, place('farmersdelight:rich_soil_farmland', { moisture: v }));

// Heat sources and conductors, from the real tags
for (const id of [...runtime.tags.block['farmersdelight:heat_sources'], ...runtime.tags.block['farmersdelight:heat_conductors']]) {
	const slug = id.replace(':', '-');
	const state = { lit: 'true', facing: 'north', level: '3', age: '0' };
	if (id === 'minecraft:lava') continue; // a fluid, no block model
	await save(`heat/${slug}`, place(id, state));
}

// Scenes for the mechanic pages
const scenes = {
	'pot-on-stove': [...place('farmersdelight:stove', { lit: 'true', facing: 'north' }), ...place('farmersdelight:cooking_pot', { facing: 'north', support: 'none' }, [0, 1, 0])],
	'pot-on-campfire': [...place('minecraft:campfire', { lit: 'true', facing: 'north' }), ...place('farmersdelight:cooking_pot', { facing: 'north', support: 'tray' }, [0, 1, 0])],
	'pot-on-hopper': [...place('minecraft:magma_block'), ...place('minecraft:hopper', { facing: 'down' }, [0, 1, 0]), ...place('farmersdelight:cooking_pot', { facing: 'north', support: 'none' }, [0, 2, 0])],
	'pot-hanging': [...place('minecraft:oak_planks', {}, [0, 2, 0]), ...place('minecraft:iron_chain', { axis: 'y' }, [0, 1, 0]), ...place('farmersdelight:cooking_pot', { facing: 'north', support: 'handle' }), ...place('minecraft:campfire', { lit: 'true', facing: 'north' }, [0, -1, 0])],
	'skillet-on-stove': [...place('farmersdelight:stove', { lit: 'true', facing: 'north' }), ...place('farmersdelight:skillet', { facing: 'north', support: 'false' }, [0, 1, 0])],
	'skillet-on-campfire': [...place('minecraft:campfire', { lit: 'true', facing: 'north' }), ...place('farmersdelight:skillet', { facing: 'north', support: 'true' }, [0, 1, 0])],
	'compost-to-soil': [...place('farmersdelight:organic_compost', { composting: '0' }), ...place('farmersdelight:organic_compost', { composting: '4' }, [1, 0, 0]), ...place('farmersdelight:rich_soil', {}, [2, 0, 0])],
	'colony-farm': [
		...place('farmersdelight:rich_soil', {}, [0, 0, 0]), ...place('farmersdelight:rich_soil', {}, [1, 0, 0]), ...place('farmersdelight:rich_soil', {}, [0, 0, 1]), ...place('farmersdelight:rich_soil', {}, [1, 0, 1]),
		...place('farmersdelight:brown_mushroom_colony', { age: '3' }, [0, 1, 0]), ...place('farmersdelight:red_mushroom_colony', { age: '3' }, [1, 1, 0]), ...place('farmersdelight:red_mushroom_colony', { age: '1' }, [0, 1, 1]), ...place('farmersdelight:brown_mushroom_colony', { age: '0' }, [1, 1, 1]),
	],
	'kitchen': [
		...place('farmersdelight:oak_cabinet', { facing: 'north' }, [0, 0, 0]),
		...place('farmersdelight:stove', { lit: 'true', facing: 'north' }, [1, 0, 0]),
		...place('farmersdelight:cooking_pot', { facing: 'north', support: 'none' }, [1, 1, 0]),
		...place('farmersdelight:oak_cabinet', { facing: 'north' }, [2, 0, 0]),
		...place('farmersdelight:cutting_board', { facing: 'north' }, [2, 1, 0]),
		...place('farmersdelight:skillet', { facing: 'north', support: 'false' }, [0, 1, 0]),
		...place('farmersdelight:tomato_crate', {}, [3, 0, 0]),
		...place('farmersdelight:cabbage_crate', {}, [3, 1, 0]),
		...place('farmersdelight:onion_crate', {}, [3, 0, 1]),
	],
	'garden': [
		...[0, 1, 2].flatMap((x) => [0, 1].flatMap((z) => place('farmersdelight:rich_soil_farmland', { moisture: '7' }, [x, 0, z]))),
		...place('farmersdelight:cabbages', { age: '7' }, [0, 1, 0]),
		...place('farmersdelight:cabbages', { age: '5' }, [0, 1, 1]),
		...place('farmersdelight:onions', { age: '7' }, [1, 1, 0]),
		...place('farmersdelight:onions', { age: '4' }, [1, 1, 1]),
		...place('farmersdelight:tomatoes', { age: '3', ropelogged: 'true' }, [2, 1, 0]),
		...place('farmersdelight:tomatoes_on_rope', { age: '3' }, [2, 2, 0]),
		...place('farmersdelight:rope', {}, [2, 3, 0]),
		...place('farmersdelight:budding_tomatoes', { age: '4' }, [2, 1, 1]),
	],
	'feast-table': [
		...place('farmersdelight:roast_chicken_block', { servings: '4', facing: 'north' }, [0, 0, 0]),
		...place('farmersdelight:honey_glazed_ham_block', { servings: '4', facing: 'north' }, [1, 0, 0]),
		...place('farmersdelight:shepherds_pie_block', { servings: '4', facing: 'north' }, [2, 0, 0]),
		...place('farmersdelight:stuffed_pumpkin_block', { servings: '4', facing: 'north' }, [0, 0, 1]),
		...place('farmersdelight:gleaming_salad_block', { servings: '4', facing: 'north' }, [1, 0, 1]),
		...place('farmersdelight:rice_roll_medley_block', { servings: '8', facing: 'north' }, [2, 0, 1]),
	],
	'wild-crops': [
		...place('minecraft:sand', {}, [0, 0, 0]), ...place('minecraft:sand', {}, [1, 0, 0]), ...place('minecraft:grass_block', {}, [0, 0, 1], GRASS), ...place('minecraft:grass_block', {}, [1, 0, 1], GRASS), ...place('minecraft:grass_block', {}, [2, 0, 1], GRASS), ...place('minecraft:sand', {}, [2, 0, 0]),
		...place('farmersdelight:wild_cabbages', {}, [0, 1, 0]), ...place('farmersdelight:sandy_shrub', {}, [1, 1, 0]), ...place('farmersdelight:wild_beetroots', {}, [2, 1, 0]),
		...place('farmersdelight:wild_onions', {}, [0, 1, 1]), ...place('farmersdelight:wild_carrots', {}, [1, 1, 1]), ...place('farmersdelight:wild_potatoes', {}, [2, 1, 1]),
	],
};
for (const [name, parts] of Object.entries(scenes)) await save(`scenes/${name}`, parts, { size: 512 });

fs.writeFileSync(path.join(SITE, 'src/data/gen/renders.json'), JSON.stringify(manifest, null, '\t'));
console.log(`renders: ${Object.keys(manifest).length}`);
