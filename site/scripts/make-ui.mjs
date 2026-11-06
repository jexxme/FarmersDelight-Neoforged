// UI textures for the site, drawn the same way branding/make_branding.py draws the logo's signs:
// checkered sign paper, a 1 px frame with a light inner edge, cut corners and an opaque 1 px drop shadow.
// Also copies the trimmed in-game banner and a few vanilla textures (buttons, planks).
// Output: public/ui/*.png
import fs from 'node:fs';
import path from 'node:path';
import sharp from 'sharp';
import { REPO, readAsset } from './lib/assets.mjs';

const SITE = path.resolve(import.meta.dirname, '..');
const OUT = path.join(SITE, 'public/ui');
fs.mkdirSync(OUT, { recursive: true });

// The original sign palette (make_branding.py comments) and its orange "Neoforged" variant
const PALETTES = {
	sign: { a: [206, 181, 151], b: [212, 190, 164], edge: [195, 168, 136], light: [177, 142, 101], frame: [72, 48, 21] },
	orange: { a: [242, 186, 132], b: [247, 198, 150], edge: [226, 160, 102], light: [196, 118, 62], frame: [110, 50, 20] },
	dark: { a: [52, 36, 22], b: [58, 41, 26], edge: [70, 50, 31], light: [96, 68, 40], frame: [20, 13, 7] },
};

/** A 9-slice sign: w x h including the 1 px shadow on the right and bottom. */
async function sign(name, pal, { w = 10, h = 10, shadow = true } = {}) {
	const W = w + (shadow ? 1 : 0);
	const H = h + (shadow ? 1 : 0);
	const buf = Buffer.alloc(W * H * 4);
	const set = (x, y, c, a = 255) => {
		const i = (y * W + x) * 4;
		buf[i] = c[0], buf[i + 1] = c[1], buf[i + 2] = c[2], buf[i + 3] = a;
	};
	if (shadow) {
		for (let x = 1; x <= w; x++) for (let y = 1; y <= h; y++) if (!((x === 1 || x === w) && (y === 1 || y === h))) set(x, y, [0, 0, 0]);
	}
	for (let x = 0; x < w; x++) {
		for (let y = 0; y < h; y++) {
			const corner = (x === 0 || x === w - 1) && (y === 0 || y === h - 1);
			if (corner) continue;
			if (x === 0 || x === w - 1 || y === 0 || y === h - 1) set(x, y, pal.frame);
			else if (x === 1 || x === w - 2 || y === 1 || y === h - 2) set(x, y, (x + y) % 2 ? pal.light : pal.edge);
			else set(x, y, (x + y) % 2 ? pal.a : pal.b);
		}
	}
	await sharp(buf, { raw: { width: W, height: H, channels: 4 } }).png().toFile(path.join(OUT, `${name}.png`));
}
/** A 2x2 checker tile of the sign paper, for large paper backgrounds. */
async function checker(name, pal) {
	const c = [pal.a, pal.b, pal.b, pal.a].flatMap((x) => [...x, 255]);
	await sharp(Buffer.from(c), { raw: { width: 2, height: 2, channels: 4 } }).png().toFile(path.join(OUT, `${name}.png`));
}

for (const [k, pal] of Object.entries(PALETTES)) {
	await sign(k === 'sign' ? 'sign' : `sign-${k}`, pal);
	await sign(k === 'sign' ? 'sign-flat' : `sign-${k}-flat`, pal, { shadow: false });
	await checker(k === 'sign' ? 'paper' : `paper-${k}`, pal);
}

// The in-game banner (src/main/resources/banner.png), trimmed to its content
await sharp(path.join(REPO, 'src/main/resources/banner.png')).trim({ threshold: 0 }).png().toFile(path.join(OUT, 'banner.png'));
await sharp(path.join(REPO, 'src/main/resources/icon.png')).trim({ threshold: 0 }).png().toFile(path.join(OUT, 'icon.png'));

// Vanilla textures: buttons (9-slice, 200x20) and planks for wooden surfaces
const copy = {
	'button.png': 'assets/minecraft/textures/gui/sprites/widget/button.png',
	'button-hover.png': 'assets/minecraft/textures/gui/sprites/widget/button_highlighted.png',
	'oak-planks.png': 'assets/minecraft/textures/block/oak_planks.png',
	'spruce-planks.png': 'assets/minecraft/textures/block/spruce_planks.png',
	'dark-oak-planks.png': 'assets/minecraft/textures/block/dark_oak_planks.png',
	'stripped-oak.png': 'assets/minecraft/textures/block/stripped_oak_log.png',
};
for (const [to, from] of Object.entries(copy)) {
	const b = readAsset(from);
	if (b) fs.writeFileSync(path.join(OUT, to), b);
	else console.log('missing', from);
}
// Farmer's Delight textures used as surfaces
for (const t of ['canvas_rug', 'oak_cabinet_front', 'oak_cabinet_side', 'tatami_half', 'straw_bale_side']) {
	const b = readAsset(`assets/farmersdelight/textures/block/${t}.png`);
	if (b) fs.writeFileSync(path.join(OUT, `${t.replaceAll('_', '-')}.png`), b);
}
console.log('ui:', fs.readdirSync(OUT).join(' '));
