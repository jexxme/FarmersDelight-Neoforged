// Builds the docs site's data (src/data/gen/*.json) from the mod's real data: the generated and hand-written
// resources, Configuration.java, FoodValues.java and the runtime export (src/data/runtime.json, written by the
// Gradle task runDocsExport). Nothing in here is typed in by hand: if a value is not in the mod, it is not on the site.
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { REPO, readJson, vanillaLang, vanillaData } from './lib/assets.mjs';

const SITE = path.resolve(import.meta.dirname, '..');
const OUT = path.join(SITE, 'src/data/gen');
fs.mkdirSync(OUT, { recursive: true });
const runtime = JSON.parse(fs.readFileSync(path.join(SITE, 'src/data/runtime.json'), 'utf8'));
const GEN = path.join(REPO, 'src/generated/resources/data');
const MAIN = path.join(REPO, 'src/main/resources/data');
const write = (name, data) => fs.writeFileSync(path.join(OUT, name), JSON.stringify(data, null, '\t'));
const walk = (dir) => (fs.existsSync(dir) ? fs.readdirSync(dir, { recursive: true }).filter((f) => f.endsWith('.json')).map((f) => f.replaceAll('\\', '/')) : []);
const readFile = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const short = (id) => id.replace(/^minecraft:/, '');
const fdLang = readFile(path.join(REPO, 'src/main/resources/assets/farmersdelight/lang/en_us.json'));
const mcLang = vanillaLang();
const lang = { ...mcLang, ...fdLang };
const props = Object.fromEntries(fs.readFileSync(path.join(REPO, 'gradle.properties'), 'utf8').split(/\r?\n/).filter((l) => /^\w+=/.test(l)).map((l) => l.split(/=(.*)/s).slice(0, 2)));

// ------------------------------------------------------------------ names
function itemName(id) {
	const it = runtime.items[id];
	const key = it?.components?.['minecraft:item_name']?.translate;
	const [ns, p] = id.split(':');
	return lang[key] ?? lang[`item.${ns}.${p}`] ?? lang[`block.${ns}.${p}`] ?? p.replaceAll('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase());
}
const effectName = (id) => {
	const [ns, p] = id.split(':');
	return lang[`effect.${ns}.${p}`] ?? p;
};
const tagLabel = (tag) => {
	const key = `tag.item.${tag.replace(':', '.').replaceAll('/', '.')}`;
	return lang[key] ?? null;
};

// ------------------------------------------------------------------ tags
const itemTags = runtime.tags.item;
const blockTags = runtime.tags.block;
const usedItemTags = new Set();
const referenced = new Set(); // every item id that needs an icon and a name

// ------------------------------------------------------------------ ingredients
function ingredient(raw) {
	if (raw == null) return null;
	if (typeof raw === 'string') {
		if (raw.startsWith('#')) {
			const tag = raw.slice(1);
			usedItemTags.add(tag);
			const items = itemTags[tag] ?? [];
			items.forEach((i) => referenced.add(i));
			return { tag, items, label: tagLabel(tag) };
		}
		referenced.add(raw);
		return { items: [raw] };
	}
	if (Array.isArray(raw)) {
		const parts = raw.map(ingredient);
		return { items: [...new Set(parts.flatMap((p) => p.items))], any: parts };
	}
	const type = raw['neoforge:ingredient_type'] ?? raw.type;
	if (type === 'neoforge:compound') {
		const parts = raw.children.map(ingredient);
		return { items: [...new Set(parts.flatMap((p) => p.items))], any: parts };
	}
	if (type === 'farmersdelight:item_ability') {
		// Any item that can perform this tool action (e.g. "shears_carve"): resolved from the tools' tags at runtime
		const action = raw.action;
		const byAction = { axe_strip: 'minecraft:axes', shovel_dig: 'minecraft:shovels', shears_carve: 'c:tools/shear', shears_dig: 'c:tools/shear', pickaxe_dig: 'minecraft:pickaxes', hoe_dig: 'minecraft:hoes', sword_dig: 'minecraft:swords' };
		const tag = byAction[action];
		const items = tag ? itemTags[tag] ?? [] : [];
		items.forEach((i) => referenced.add(i));
		return { action, items, tag: tag ?? null, label: null };
	}
	if (raw.items) return ingredient(raw.items);
	if (raw.item) return ingredient(raw.item);
	if (raw.tag) return ingredient('#' + raw.tag);
	return { items: [], unknown: raw };
}
function stack(raw) {
	if (!raw) return null;
	if (typeof raw === 'string') return referenced.add(raw), { id: raw, count: 1 };
	const id = raw.id ?? raw.item;
	referenced.add(id);
	return { id, count: raw.count ?? 1 };
}

// ------------------------------------------------------------------ recipes
const recipes = [];
const loaded = new Set(runtime.loadedRecipes);
for (const root of [GEN, MAIN]) {
	const dir = path.join(root, 'farmersdelight/recipe');
	for (const f of walk(dir)) {
		const json = readFile(path.join(dir, f));
		const id = `farmersdelight:${f.replace(/\.json$/, '')}`;
		const type = json.type.replace(/^minecraft:/, '');
		const r = { id, type, loaded: loaded.has(id), conditions: json['neoforge:conditions'] ?? null };
		if (json.group) r.group = json.group;
		switch (type) {
			case 'crafting_shaped': {
				const key = Object.fromEntries(Object.entries(json.key).map(([k, v]) => [k, ingredient(v)]));
				r.grid = json.pattern.map((row) => [...row].map((c) => (c === ' ' ? null : key[c])));
				r.result = stack(json.result);
				break;
			}
			case 'crafting_shapeless':
				r.ingredients = json.ingredients.map(ingredient);
				r.result = stack(json.result);
				break;
			case 'smelting':
			case 'smoking':
			case 'blasting':
			case 'campfire_cooking':
				r.ingredients = [ingredient(json.ingredient)];
				r.result = stack(json.result);
				r.cookingtime = json.cookingtime ?? (type === 'smelting' ? 200 : type === 'campfire_cooking' ? 100 : 100);
				r.cookingtimeDefaulted = json.cookingtime === undefined;
				r.experience = json.experience ?? 0;
				break;
			case 'farmersdelight:cooking': {
				r.ingredients = json.ingredients.map(ingredient);
				r.result = stack(json.result);
				// Codec defaults: CookingPotRecipe.java (cookingtime 200, experience 0, recipe_book_tab misc)
				r.cookingtime = json.cookingtime ?? 200;
				r.experience = json.experience ?? 0;
				r.tab = json.recipe_book_tab ?? 'misc';
				const remainder = runtime.items[r.result.id]?.craftingRemainder;
				r.container = json.container ? stack(json.container) : remainder ? stack(remainder) : null;
				r.containerOverride = !!json.container;
				break;
			}
			case 'farmersdelight:cutting':
				r.ingredients = json.ingredients.map(ingredient);
				r.tool = ingredient(json.tool);
				r.results = json.result.map((x) => ({ ...stack(x.item), chance: x.chance ?? 1 }));
				r.sound = json.sound ?? null;
				break;
			case 'smithing_transform':
				r.template = ingredient(json.template);
				r.base = ingredient(json.base);
				r.addition = ingredient(json.addition);
				r.result = stack(json.result);
				break;
			default:
				r.special = true;
		}
		recipes.push(r);
	}
}
recipes.sort((a, b) => a.id.localeCompare(b.id));

// ------------------------------------------------------------------ loot tables
function numberText(n) {
	if (n == null) return null;
	if (typeof n === 'number') return { min: n, max: n };
	if (n.type?.endsWith('uniform')) return { min: n.min, max: n.max };
	if (n.type?.endsWith('constant')) return { min: n.value, max: n.value };
	if (n.type?.endsWith('binomial')) return { min: 0, max: n.n, binomial: { n: n.n, p: n.p } };
	return { raw: n };
}
const asList = (x) => (x == null ? [] : Array.isArray(x) ? x : [x]);
function conditionText(c) {
	const t = c.type?.replace(/^minecraft:/, '');
	switch (t) {
		case 'match_block':
			return c.state ? Object.entries(c.state).map(([k, v]) => `${k} = ${v}`).join(', ') : `block ${c.blocks}`;
		case 'block_state_property':
			return Object.entries(c.properties ?? {}).map(([k, v]) => `${k} = ${typeof v === 'object' ? `${v.min ?? ''}–${v.max ?? ''}` : v}`).join(', ');
		case 'match_tool': {
			const p = c.predicate ?? {};
			if (p.predicates?.['minecraft:enchantments']) return 'tool has ' + p.predicates['minecraft:enchantments'].map((e) => short(e.enchantments)).join(', ');
			if (p.items) return 'with ' + (typeof p.items === 'string' ? (p.items.startsWith('#') ? `#${short(p.items.slice(1))}` : itemName(p.items)) : p.items.map(itemName).join('/'));
			return 'with a matching tool';
		}
		case 'inverted':
			return 'not (' + conditionText(c.term) + ')';
		case 'any_of':
			return c.terms.map(conditionText).join(' or ');
		case 'all_of':
			return c.terms.map(conditionText).join(' and ');
		case 'survives_explosion':
			return null;
		case 'random_chance':
			return `${+(c.chance * 100).toFixed(2)}% chance`;
		case 'table_bonus':
			return `fortune chances ${c.chances.map((x) => +(x * 100).toFixed(1) + '%').join(' / ')}`;
		case 'farmersdelight:config_enabled':
		case 'config_enabled':
			return `config ${c.option}`;
		default:
			return t;
	}
}
function lootTable(json) {
	const pools = [];
	// Functions apply at three levels: the table, the pool and the entry (in that order, outermost last)
	const tableMods = asList(json.modifier ?? json.functions);
	for (const pool of json.pools ?? []) {
		const entries = [];
		const poolMods = asList(pool.modifier ?? pool.functions);
		const flat = (e, inherited = []) => {
			const conds = [...inherited, ...asList(e.condition ?? e.conditions)];
			if (e.type?.endsWith('alternatives') || e.type?.endsWith('group') || e.type?.endsWith('sequence')) {
				e.children.forEach((c, i) => flat(c, conds.concat(e.type.endsWith('alternatives') && i > 0 ? [{ type: 'otherwise' }] : [])));
				return;
			}
			if (!e.type?.endsWith('item')) {
				entries.push({ kind: e.type, value: e.value ?? e.name ?? null, weight: e.weight ?? 1 });
				return;
			}
			referenced.add(e.name);
			const mods = [...asList(e.modifier ?? e.functions), ...poolMods, ...tableMods];
			const count = mods.find((m) => m.type?.endsWith('set_count'));
			const bonus = mods.find((m) => m.type?.endsWith('apply_bonus'));
			const damage = mods.find((m) => m.type?.endsWith('set_damage'));
			const enchant = mods.find((m) => m.type?.endsWith('enchant_randomly') || m.type?.endsWith('enchant_with_levels'));
			entries.push({
				item: e.name,
				weight: e.weight ?? 1,
				count: count ? numberText(count.count) : { min: 1, max: 1 },
				fortune: bonus ? { formula: short(bonus.formula), ...bonus.parameters } : null,
				damage: damage ? numberText(damage.damage) : null,
				enchant: enchant ? { type: short(enchant.type), levels: enchant.levels ? numberText(enchant.levels) : null, options: enchant.options ?? null } : null,
				conditions: conds.map((c) => (c.type === 'otherwise' ? 'otherwise' : conditionText(c))).filter(Boolean),
				modifiers: mods.map((m) => short(m.type)).filter((t) => !['set_count', 'apply_bonus', 'set_damage', 'enchant_randomly', 'enchant_with_levels'].includes(t)),
			});
		};
		pool.entries.forEach((e) => flat(e));
		const total = entries.reduce((s, e) => s + (e.weight ?? 1), 0);
		entries.forEach((e) => (e.chance = (e.weight ?? 1) / total));
		pools.push({
			rolls: numberText(pool.rolls),
			bonusRolls: pool.bonus_rolls ?? 0,
			conditions: asList(pool.condition ?? pool.conditions).map(conditionText).filter(Boolean),
			entries,
		});
	}
	return { pools, type: json.type, modifiers: tableMods.map((m) => short(m.type)) };
}
const loot = { chests: {}, blocks: {} };
for (const kind of ['chests', 'blocks']) {
	const dir = path.join(GEN, 'farmersdelight/loot_table', kind);
	for (const f of walk(dir)) loot[kind][`farmersdelight:${kind}/${f.replace(/\.json$/, '')}`] = lootTable(readFile(path.join(dir, f)));
}

// ------------------------------------------------------------------ loot modifiers
const modifiers = [];
{
	const dir = path.join(GEN, 'farmersdelight/loot_modifiers');
	const flatTerms = (c) => (c?.type?.endsWith('all_of') ? c.terms.flatMap(flatTerms) : c ? [c] : []);
	for (const f of walk(dir)) {
		const json = readFile(path.join(dir, f));
		const id = `farmersdelight:${f.replace(/\.json$/, '')}`;
		const terms = flatTerms(json.condition);
		const m = { id, type: json.type, raw: json };
		const tableCond = terms.find((t) => t.type?.endsWith('loot_table_id'));
		if (tableCond) m.target = tableCond.loot_table_id;
		const knife = terms.find((t) => t.type?.endsWith('entity_properties') && t.entity === 'attacker');
		if (knife) m.attackerTool = knife.predicate?.['minecraft:equipment']?.mainhand?.items ?? null;
		// The killed mob(s): may sit directly in the terms or inside an any_of (leather, string)
		const mobs = [];
		const findMobs = (c) => {
			if (!c || typeof c !== 'object') return;
			if (c.type?.endsWith('entity_properties') && c.entity === 'this' && c.predicate?.['minecraft:entity_type']) mobs.push(...asList(c.predicate['minecraft:entity_type']));
			for (const t of asList(c.terms)) findMobs(t);
			if (c.term) findMobs(c.term);
		};
		findMobs(json.condition);
		if (mobs.length) m.entity = mobs.length === 1 ? mobs[0] : mobs;
		m.killedByPlayer = terms.some((t) => t.type?.endsWith('killed_by_player'));
		m.silkTouch = JSON.stringify(json.condition).includes('silk_touch');
		const fire = terms.find((t) => t.predicate?.['minecraft:flags']?.is_on_fire !== undefined);
		if (fire) m.onFire = fire.predicate['minecraft:flags'].is_on_fire;
		const chance = terms.find((t) => t.type?.endsWith('random_chance'));
		if (chance) m.chance = chance.chance;
		const looting = terms.find((t) => t.type?.endsWith('random_chance_with_enchanted_bonus'));
		if (looting) m.lootingChance = { base: looting.unenchanted_chance, enchanted: looting.enchanted_chance };
		const tool = terms.find((t) => t.type?.endsWith('match_tool'));
		if (tool) m.tool = tool.predicate?.items ?? null;
		const block = terms.find((t) => t.type?.endsWith('match_block') || t.type?.endsWith('block_state_property'));
		if (block) m.block = { blocks: block.blocks ?? block.block, state: block.state ?? block.properties ?? null };
		if (json.item) (m.item = json.item), referenced.add(json.item);
		if (json.table) m.table = json.table;
		if (json.count) m.count = json.count;
		if (json.replaced_item ?? json.replace) m.replaces = json.replaced_item ?? json.replace;
		modifiers.push(m);
	}
}

// ------------------------------------------------------------------ villager trades
const trades = [];
{
	const dir = path.join(GEN, 'farmersdelight/villager_trade');
	for (const f of walk(dir)) {
		const json = readFile(path.join(dir, f));
		const parts = f.replace(/\.json$/, '').split('/');
		const trader = parts[0];
		const level = parts.length > 2 ? +parts[1] : null;
		trades.push({
			id: `farmersdelight:${f.replace(/\.json$/, '')}`,
			trader,
			level,
			wants: stack(json.wants),
			gives: stack(json.gives),
			maxUses: json.max_uses,
			xp: json.xp,
			priceMultiplier: json.reputation_discount,
			config: json['neoforge:conditions']?.find((c) => c.type.endsWith('config_enabled'))?.option ?? null,
		});
	}
}

// ------------------------------------------------------------------ worldgen
const worldgen = [];
{
	const bm = path.join(MAIN, 'farmersdelight/neoforge/biome_modifier');
	for (const f of walk(bm)) {
		const mod = readFile(path.join(bm, f));
		const featureIds = asList(mod.features);
		for (const pf of featureIds) {
			const placed = readFile(path.join(MAIN, pf.split(':')[0], 'worldgen/placed_feature', pf.split(':')[1] + '.json'));
			const fid = placed.feature;
			const feature = readFile(path.join(MAIN, fid.split(':')[0], 'worldgen/feature', fid.split(':')[1] + '.json'));
			const placement = placed.placement.map((p) => ({ ...p, type: short(p.type) }));
			const parts = (feature.features ?? [feature]).map((part) => {
				const tp = part.feature?.to_place;
				const blockId = tp?.state?.id ?? tp?.source?.state?.id ?? null;
				const randomProperty = tp?.property ? { property: tp.property, min: tp.values?.min_inclusive, max: tp.values?.max_inclusive } : null;
				const offset = part.placement?.find((p) => p.type.endsWith('offset'));
				const filter = part.placement?.find((p) => p.type.endsWith('block_predicate_filter'));
				const preds = [];
				const flatPred = (p) => (p?.type?.endsWith('all_of') ? p.predicates.forEach(flatPred) : p && preds.push(p));
				flatPred(filter?.predicate);
				const count = part.placement?.find((p) => p.type.endsWith('count'));
				return {
					block: blockId,
					randomProperty,
					spread: offset ? { x: offset.x, y: offset.y, z: offset.z } : null,
					// Every offset step in order (some patches offset twice, e.g. the coarse dirt under wild carrots)
					offsets: (part.placement ?? []).filter((p) => p.type.endsWith('offset')).map((o) => ({ x: o.x, y: o.y, z: o.z })),
					tries: count?.count ?? null,
					predicates: preds.map((p) => ({ type: short(p.type), blocks: p.blocks ?? null, tag: p.tag ?? null, offset: p.offset ?? null, fluids: p.fluids ?? null })),
				};
			});
			const allowed = mod.allowed_biomes ?? mod.biomes;
			const denied = mod.denied_biomes ?? null;
			const resolve = (b) => (b == null ? null : asList(b).flatMap((x) => (x.startsWith('#') ? runtime.tags['worldgen/biome'][x.slice(1)] ?? [] : [x])));
			worldgen.push({
				id: `farmersdelight:${f.replace(/\.json$/, '')}`,
				placed: pf,
				step: mod.step,
				allowed,
				denied,
				// Same filter as AddFeaturesByFilterBiomeModifier: allowed, not denied, base temperature within [min, max]
				biomes: (resolve(allowed) ?? []).filter((b) => {
					const temp = runtime.biomes[b]?.temperature;
					if ((resolve(denied) ?? []).includes(b)) return false;
					if (mod.min_temperature !== undefined && temp < mod.min_temperature) return false;
					if (mod.max_temperature !== undefined && temp > mod.max_temperature) return false;
					return true;
				}),
				candidateBiomes: resolve(allowed) ?? [],
				extra: Object.fromEntries(Object.entries(mod).filter(([k]) => !['type', 'allowed_biomes', 'denied_biomes', 'biomes', 'features', 'step'].includes(k))),
				rarity: placement.find((p) => p.type === 'rarity_filter')?.chance ?? null,
				count: placement.find((p) => p.type === 'count')?.count ?? null,
				placement,
				parts,
				feature: parts.some((p) => p.block) ? null : feature,
			});
		}
	}
}

// ------------------------------------------------------------------ config (Configuration.java)
function parseConfig() {
	const src = fs.readFileSync(path.join(REPO, 'src/main/java/vectorwing/farmersdelight/common/Configuration.java'), 'utf8');
	const body = src.slice(src.indexOf('static {'), src.indexOf('// Backwards compatibility'));
	const consts = Object.fromEntries([...src.matchAll(/String (CATEGORY_\w+) = "(\w+)"/g)].map((m) => [m[1], m[2]]));
	const out = [];
	const stacks = { COMMON_BUILDER: [], CLIENT_BUILDER: [] };
	const strings = (s) => [...s.matchAll(/"((?:[^"\\]|\\.)*)"/g)].map((m) => m[1].replace(/\\"/g, '"'));
	for (const stmt of body.split(';')) {
		const s = stmt.replace(/\s+/g, ' ').trim();
		const push = /(\w+_BUILDER)\.push\((\w+)\)/.exec(s);
		if (push) stacks[push[1]].push(consts[push[2]] ?? push[2]);
		const pop = /(\w+_BUILDER)\.pop\(\)/.exec(s);
		if (pop && !s.includes('.define')) stacks[pop[1]].pop();
		const def = /(\w+) = (\w+_BUILDER)(.*)\.(define|defineInRange|defineList)\((.*)\)$/.exec(s);
		if (!def) continue;
		const [, field, builder, chain, kind, args] = def;
		const comment = [...chain.matchAll(/\.comment\(((?:"(?:[^"\\]|\\.)*"\s*,?\s*)+)\)/g)].flatMap((m) => strings(m[1]));
		const key = strings(args)[0];
		let def0, range = null, type;
		if (kind === 'defineInRange') {
			const nums = args.split(',').slice(1).map((x) => +x.trim());
			[def0, range] = [nums[0], [nums[1], nums[2]]];
			type = Number.isInteger(nums[0]) && !/\./.test(args) ? 'integer' : 'decimal';
		} else if (kind === 'defineList') {
			def0 = strings(/ImmutableList\.of\((.*?)\)/.exec(args)?.[1] ?? '');
			type = 'list';
		} else {
			const v = args.split(',').slice(1).join(',').trim();
			def0 = v === 'true' ? true : v === 'false' ? false : strings(v)[0] ?? v;
			type = typeof def0 === 'boolean' ? 'boolean' : 'string';
		}
		out.push({ file: builder === 'CLIENT_BUILDER' ? 'farmersdelight-client.toml' : 'farmersdelight-common.toml', section: [...stacks[builder]].join('.'), key, field, type, default: def0, range, comment });
	}
	return out;
}
const config = parseConfig();

// ------------------------------------------------------------------ FoodValues.java: vanilla soup effects
function vanillaSoupEffects() {
	const src = fs.readFileSync(path.join(REPO, 'src/main/java/vectorwing/farmersdelight/common/FoodValues.java'), 'utf8');
	const durations = Object.fromEntries([...src.matchAll(/int (\w+_DURATION) = (\d+)/g)].map((m) => [m[1], +m[2]]));
	return [...src.matchAll(/\.put\(Items\.(\w+), new ApplyStatusEffectsConsumeEffect\(nourishment\((\w+)\)\)\)/g)].map((m) => ({
		item: 'minecraft:' + m[1].toLowerCase(),
		effect: 'farmersdelight:nourishment',
		duration: durations[m[2]],
	}));
}
const soupEffects = vanillaSoupEffects();
soupEffects.forEach((s) => referenced.add(s.item));
// Workstation icons the recipe cards show next to vanilla recipe types
['minecraft:crafting_table', 'minecraft:furnace', 'minecraft:smoker', 'minecraft:blast_furnace', 'minecraft:campfire', 'minecraft:smithing_table', 'minecraft:composter'].forEach((i) => referenced.add(i));

// ------------------------------------------------------------------ items
const compostChance = (layers) => {
	if (!layers) return null;
	const p = vanillaData(`data/minecraft/context_int_provider/${layers.replace('minecraft:', '')}.json`);
	if (p === 1 || p?.value === 1) return 1;
	const d = p?.default?.distribution;
	if (!d) return null;
	const total = d.reduce((s, x) => s + x.weight, 0);
	return d.filter((x) => x.data > 0).reduce((s, x) => s + x.weight, 0) / total;
};
function consumable(c) {
	if (!c) return null;
	const effects = [];
	const other = [];
	for (const e of c.on_consume_effects ?? []) {
		const t = short(e.type);
		if (t === 'apply_effects') {
			for (const fx of e.effects) effects.push({ id: fx.id, name: effectName(fx.id), duration: fx.duration ?? 0, amplifier: fx.amplifier ?? 0, probability: e.probability ?? 1 });
		} else other.push({ type: e.type, ...e });
	}
	return {
		seconds: c.consume_seconds ?? 1.6, // Consumable codec default (vanilla Consumable.DEFAULT_CONSUME_SECONDS)
		animation: c.animation ?? 'eat',
		effects,
		other,
	};
}
const attr = (mods, type) => mods?.find((m) => m.type === type)?.amount;
const tagSet = (id) => new Set(runtime.items[id]?.tags ?? []);
const CATEGORY_RULES = [
	['tools', (id, t) => t.has('farmersdelight:tools/knives') || id.endsWith(':skillet')],
	['feasts', (id, t) => t.has('farmersdelight:feasts')],
	['pies', (id, t) => t.has('farmersdelight:pies') || /_pie_slice$|_slice$/.test(id) && /pie|cake|cheesecake/.test(id)],
	['meals', (id, t) => t.has('farmersdelight:meals')],
	['drinks', (id, t) => t.has('farmersdelight:drinks')],
	['sweets', (id, t) => t.has('farmersdelight:sweets')],
	['snacks', (id, t) => t.has('farmersdelight:snacks')],
	['workstations', (id) => /:(cooking_pot|stove|cutting_board|wooden_basket|bamboo_basket)$/.test(id)],
	['crops', (id, t) => [...t].some((x) => /^c:(crops|seeds)(\/|$)/.test(x)) || /:(rice_panicle|rotten_tomato)$/.test(id)],
	['farming', (id, t) => /compost|rich_soil|mushroom_colony|wild_|sandy_shrub|budding/.test(id) || t.has('farmersdelight:wild_crops')],
	['storage', (id, t) => /crate|_bag$|_bale$|cabinet/.test(id) || t.has('farmersdelight:cabinets')],
	['decoration', (id, t) => /sign|tatami|rug|rope|safety_net|canvas$|half_tatami/.test(id)],
	['ingredients', (id) => !!runtime.items[id]?.components?.['minecraft:food']],
	['materials', () => true],
];
const blockOf = Object.fromEntries(Object.entries(runtime.blocks).map(([bid, b]) => [b.item, bid]));
const items = {};
for (const id of runtime.creativeTab) {
	const it = runtime.items[id];
	const c = it.components;
	const t = tagSet(id);
	const blockId = blockOf[id] ?? null;
	const block = blockId ? runtime.blocks[blockId] : null;
	const food = c['minecraft:food'];
	items[id] = {
		id,
		slug: id.split(':')[1],
		name: itemName(id),
		category: CATEGORY_RULES.find(([, f]) => f(id, t))[0],
		class: it.class.split('.').pop(),
		stack: c['minecraft:max_stack_size'],
		rarity: c['minecraft:rarity'],
		food: food ? { nutrition: food.nutrition, saturation: food.saturation, alwaysEdible: !!food.can_always_eat } : null,
		consumable: c['minecraft:consumable'] ? consumable(c['minecraft:consumable']) : null,
		remainder: c['minecraft:use_remainder']?.id ?? it.craftingRemainder ?? null,
		durability: c['minecraft:max_damage'] ?? null,
		attackDamage: attr(c['minecraft:attribute_modifiers'], 'minecraft:attack_damage') ?? null,
		attackSpeed: attr(c['minecraft:attribute_modifiers'], 'minecraft:attack_speed') ?? null,
		attackKnockback: attr(c['minecraft:attribute_modifiers'], 'minecraft:attack_knockback') ?? null,
		tool: c['minecraft:tool'] ?? null,
		weapon: c['minecraft:weapon'] ?? null,
		enchantability: c['minecraft:enchantable']?.value ?? null,
		repairWith: c['minecraft:repairable']?.items ?? null,
		fireResistant: !!c['minecraft:damage_resistant'],
		fuel: c['minecraft:cooking_fuel']?.burn_time ?? null,
		compost: compostChance(c['minecraft:compostable']?.layers),
		villagerFood: c['minecraft:villager_food']?.nutrition ?? null,
		tags: it.tags,
		block: block ? { id: blockId, ...block, class: block.class.split('.').pop() } : null,
		tooltip: Object.entries(fdLang).filter(([k]) => k === `tooltip.farmersdelight.${id.split(':')[1]}`).map(([, v]) => v),
		loot: loot.blocks[`farmersdelight:blocks/${blockId?.split(':')[1]}`] ?? null,
	};
	referenced.add(id);
}

// Where items come from and go to
const produces = (r) => [r.result?.id, ...(r.results ?? []).map((x) => x.id)].filter(Boolean);
const consumes = (r) => [...(r.ingredients ?? []), ...(r.grid ?? []).flat(), r.base, r.addition, r.template].filter(Boolean).flatMap((i) => i.items);
for (const it of Object.values(items)) {
	it.madeBy = recipes.filter((r) => produces(r).includes(it.id)).map((r) => r.id);
	it.usedIn = recipes.filter((r) => consumes(r).includes(it.id) || r.tool?.items.includes(it.id) || r.container?.id === it.id).map((r) => r.id);
	it.chests = Object.entries(loot.chests).filter(([, tb]) => tb.pools.some((p) => p.entries.some((e) => e.item === it.id))).map(([k]) => k);
	it.modifiers = modifiers.filter((m) => m.item === it.id).map((m) => m.id);
	it.trades = trades.filter((t) => t.wants.id === it.id || t.gives.id === it.id).map((t) => t.id);
	it.worldgen = worldgen.filter((w) => w.parts.some((p) => p.block && runtime.blocks[p.block]?.item === it.id)).map((w) => w.id);
}

// Vanilla (and other) items that the site shows: names and food stats for comparison tables
const others = {};
for (const id of referenced) {
	if (items[id]) continue;
	const rt = runtime.items[id];
	const food = rt?.components?.['minecraft:food'];
	others[id] = { id, name: itemName(id), food: food ? { nutrition: food.nutrition, saturation: food.saturation, alwaysEdible: !!food.can_always_eat } : null, stack: rt?.components?.['minecraft:max_stack_size'] ?? null };
}
const vanillaFoods = Object.entries(runtime.items)
	.filter(([id, it]) => !id.startsWith('farmersdelight:') && it.components['minecraft:food'])
	.map(([id, it]) => ({ id, name: itemName(id), nutrition: it.components['minecraft:food'].nutrition, saturation: it.components['minecraft:food'].saturation, consumable: consumable(it.components['minecraft:consumable']), stack: it.components['minecraft:max_stack_size'] }));
vanillaFoods.forEach((f) => referenced.add(f.id));
for (const f of vanillaFoods) if (!items[f.id] && !others[f.id]) others[f.id] = { id: f.id, name: f.name, food: { nutrition: f.nutrition, saturation: f.saturation }, stack: f.stack };

// Effects
const effects = Object.entries(runtime.effects).map(([id, e]) => ({
	id,
	name: effectName(id),
	description: lang[`effect.${id.replace(':', '.')}.description`] ?? null,
	category: e.category,
	color: '#' + e.color.toString(16).padStart(6, '0'),
	grantedBy: Object.values(items).filter((it) => it.consumable?.effects.some((fx) => fx.id === id)).map((it) => it.id),
}));

// Tags (the ones the site talks about): FD's own, any containing an FD entry, and every tag the mod's data or
// code mentions ("#ns:path" in a JSON file, or a "tag" field such as in worldgen predicates)
const mentioned = new Set();
for (const root of [GEN, MAIN]) {
	for (const f of walk(root)) {
		const text = fs.readFileSync(path.join(root, f), 'utf8');
		for (const m of text.matchAll(/"#([a-z0-9_.-]+:[a-z0-9_/.-]+)"/g)) mentioned.add(m[1]);
		for (const m of text.matchAll(/"tag"\s*:\s*"([a-z0-9_.-]+:[a-z0-9_/.-]+)"/g)) mentioned.add(m[1]);
	}
}
const fdTagsAll = {};
for (const [reg, tags] of Object.entries(runtime.tags)) {
	for (const [tag, values] of Object.entries(tags)) {
		const fdHasIt = values.some((v) => v.startsWith('farmersdelight:'));
		if (tag.startsWith('farmersdelight:') || fdHasIt || mentioned.has(tag) || (reg === 'item' && usedItemTags.has(tag))) {
			(fdTagsAll[reg] ??= {})[tag] = values;
			if (reg === 'item') values.forEach((v) => referenced.add(v));
		}
	}
}
// Which of these tags FD itself adds to (from its generated tag files)
const fdTagFiles = [];
for (const ns of fs.readdirSync(GEN)) {
	const dir = path.join(GEN, ns, 'tags');
	for (const f of walk(dir)) {
		const [reg, ...rest] = f.replace(/\.json$/, '').split('/');
		const json = readFile(path.join(dir, f));
		fdTagFiles.push({ registry: reg, tag: `${ns}:${rest.join('/')}`, values: json.values.map((v) => (typeof v === 'string' ? v : v.id + (v.required === false ? '?' : ''))), replace: !!json.replace });
	}
}
for (const ns of fs.readdirSync(MAIN)) {
	const dir = path.join(MAIN, ns, 'tags');
	for (const f of walk(dir)) {
		const [reg, ...rest] = f.replace(/\.json$/, '').split('/');
		const json = readFile(path.join(dir, f));
		fdTagFiles.push({ registry: reg, tag: `${ns}:${rest.join('/')}`, values: json.values.map((v) => (typeof v === 'string' ? v : v.id + (v.required === false ? '?' : ''))), replace: !!json.replace, handWritten: true });
	}
}

// Advancements
const advancements = [];
{
	const dir = path.join(GEN, 'farmersdelight/advancement/main');
	for (const f of walk(dir)) {
		const json = readFile(path.join(dir, f));
		const d = json.display;
		if (!d) continue;
		referenced.add(d.icon.id);
		advancements.push({
			id: `farmersdelight:main/${f.replace(/\.json$/, '')}`,
			parent: json.parent ?? null,
			title: lang[d.title.translate] ?? d.title.translate,
			description: lang[d.description.translate] ?? d.description.translate,
			icon: d.icon.id,
			frame: d.frame ?? 'task',
			hidden: !!d.hidden,
			criteria: Object.keys(json.criteria),
		});
	}
}

// Biome names for the worldgen panel
const biomeName = (b) => lang[`biome.${b.replace(':', '.')}`] ?? b;
const biomes = Object.fromEntries(Object.entries(runtime.biomes).map(([b, info]) => [b, { name: biomeName(b), ...info }]));

// Blocks without an item (crops in the ground, etc.)
const blocks = Object.fromEntries(Object.entries(runtime.blocks).map(([id, b]) => [id, { id, name: lang[`block.${id.replace(':', '.')}`] ?? itemName(b.item), item: b.item === 'minecraft:air' ? null : b.item, properties: b.properties, tags: b.tags, class: b.class.split('.').pop(), hardness: b.hardness, blastResistance: b.blastResistance, maxLight: b.maxLight, loot: loot.blocks[`farmersdelight:blocks/${id.split(':')[1]}`] ?? null }]));

for (const id of referenced) if (!items[id] && !others[id]) others[id] = { id, name: itemName(id), food: null, stack: null };

write('items.json', items);
write('others.json', others);
write('blocks.json', blocks);
write('recipes.json', recipes);
write('loot.json', loot);
write('modifiers.json', modifiers);
write('trades.json', trades);
write('worldgen.json', worldgen);
write('biomes.json', biomes);
write('config.json', config);
write('effects.json', effects);
write('tags.json', { resolved: fdTagsAll, files: fdTagFiles });
write('advancements.json', advancements);
write('vanilla-foods.json', vanillaFoods);
write('soup-effects.json', soupEffects);
// Supported Minecraft versions: one release tag per Minecraft version ("<mc>-<mod_version>")
const versions = [];
try {
	const git = (...a) => execFileSync('git', ['-C', REPO, ...a], { encoding: 'utf8' });
	for (const tag of git('tag', '--list', `*-${props.mod_version}`).split(/\r?\n/).filter(Boolean)) {
		const p = Object.fromEntries(git('show', `${tag}:gradle.properties`).split(/\r?\n/).filter((l) => /^\w+=/.test(l)).map((l) => l.split(/=(.*)/s).slice(0, 2)));
		versions.push({ tag, minecraft: p.minecraft_version, minecraftRange: p.minecraft_version_range, gameVersions: p.release_game_versions, neoforge: p.neo_version, neoforgeRange: p.neo_version_range });
	}
} catch (e) {
	console.log('versions: git not available, keeping the previous list');
	try {
		versions.push(...JSON.parse(fs.readFileSync(path.join(OUT, 'meta.json'), 'utf8')).versions);
	} catch {}
}
versions.sort((a, b) => b.minecraft.localeCompare(a.minecraft, undefined, { numeric: true }));
write('meta.json', {
	versions,
	modVersion: props.mod_version,
	minecraft: props.minecraft_version,
	neoforge: props.neo_version,
	modId: props.mod_id,
	license: props.mod_license,
	counts: {
		items: Object.keys(items).length,
		blocks: Object.keys(blocks).length,
		// Recipes that load in a plain game (integration recipes need their mod and are counted separately)
		recipes: recipes.filter((r) => r.loaded).length,
		integrationRecipes: recipes.filter((r) => !r.loaded).length,
		cooking: recipes.filter((r) => r.loaded && r.type === 'farmersdelight:cooking').length,
		cutting: recipes.filter((r) => r.loaded && r.type === 'farmersdelight:cutting').length,
		foods: Object.values(items).filter((i) => i.food).length,
		lootModifiers: modifiers.length,
		advancements: advancements.length,
	},
});
// NeoForge data maps shipped by the mod (e.g. villager_compostables, block transformables)
const dataMaps = {};
for (const root of [GEN, MAIN]) {
	const dir = path.join(root, 'neoforge/data_maps');
	for (const f of walk(dir)) dataMaps[`neoforge:${f.replace(/\.json$/, '')}`] = { file: path.relative(REPO, path.join(dir, f)).replaceAll('\\', '/'), ...readFile(path.join(dir, f)) };
}
write('data-maps.json', dataMaps);

// Text documents shipped in the repo, for the changelog and differences pages
write('docs-text.json', {
	changelog: fs.readFileSync(path.join(REPO, 'changelog.md'), 'utf8'),
	upstreamIssues: fs.existsSync(path.join(REPO, 'docs/UPSTREAM_ISSUES.md')) ? fs.readFileSync(path.join(REPO, 'docs/UPSTREAM_ISSUES.md'), 'utf8') : '',
	readme: fs.readFileSync(path.join(REPO, 'README.md'), 'utf8'),
});
write('referenced.json', [...referenced].filter(Boolean).sort());
console.log(`items ${Object.keys(items).length}, recipes ${recipes.length}, referenced ${referenced.size}, worldgen ${worldgen.length}, config ${config.length}, advancements ${advancements.length}`);
