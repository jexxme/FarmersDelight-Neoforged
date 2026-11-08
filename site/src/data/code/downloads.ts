// Code facts: where each release can be downloaded, and how its version and file are named. Citations are for
// maintainers; pages never show them.
//
// Naming (.github/workflows/release.yml:3-4, 44-50): the tag is "<minecraft_version>-<mod_version>", mod_version is
// "<Farmer's Delight version>-<n>", the stores and GitHub show "MC 26.3 · FD 1.3.4 (2)", and the jar is
// "FarmersDelight-Neoforged-MC<first game version>[-<last>]-FD<mod_version>.jar". Releases made before that naming were
// tagged "<mc>-<FD version>+neoforged.<n>", with the jar "FarmersDelight-Neoforged-<tag>.jar" (still on GitHub).
import { meta } from '../../lib/data';

export const REPO = 'https://github.com/jexxme/FarmersDelight-Neoforged';
const CURSEFORGE = 'https://www.curseforge.com/minecraft/mc-mods/farmers-delight-neoforged';
const MODRINTH = 'https://modrinth.com/mod/farmers-delight-neoforged';
// Only stores whose page is public get a link. Modrinth's project page isn't public yet (its API answers 404):
// set this to true once it is, and every Modrinth link on the site turns on.
const MODRINTH_LIVE = false;
// CurseForge's mod loader type id for NeoForge
const CF_NEOFORGE = 6;

export type Link = { label: string; href: string };
type Version = (typeof meta.versions)[number];

const LEGACY = /^(.*)\+neoforged\.(\d+)$/;
/** "1.3.4-2" or "1.3.4+neoforged.2" -> { fd: "1.3.4", n: "2" } */
export function splitVersion(modVersion: string) {
	const m = modVersion.match(LEGACY) ?? modVersion.match(/^(.*)-(\d+)$/);
	return m ? { fd: m[1], n: m[2] } : { fd: modVersion, n: null };
}
/** The Farmer's Delight content version, e.g. "1.3.4" */
export const fdVersion = (modVersion = meta.modVersion) => splitVersion(modVersion).fd;
/** The version as the stores show it, e.g. "FD 1.3.4 (2)" */
export function versionLabel(modVersion = meta.modVersion) {
	const { fd, n } = splitVersion(modVersion);
	return n ? `FD ${fd} (${n})` : `FD ${fd}`;
}

const games = (v: Version) => v.gameVersions.split(',');
/** "26.3", or "26.1–26.1.1" for a file that runs on several versions */
export const minecraftLabel = (v: Version) => {
	const g = games(v);
	return g.length > 1 ? `${g[0]}–${g[g.length - 1]}` : g[0];
};
/** The release's jar on GitHub, named the way release.yml names it (old-style tags keep their old file name) */
export function jarUrl(v: Version) {
	const tag = v.tag;
	const mod = tag.slice(tag.indexOf('-') + 1);
	const g = games(v);
	const file = LEGACY.test(tag) ? `FarmersDelight-Neoforged-${tag}.jar` : `FarmersDelight-Neoforged-MC${g.length > 1 ? `${g[0]}-${g[g.length - 1]}` : g[0]}-FD${mod}.jar`;
	return `${REPO}/releases/download/${encodeURIComponent(tag)}/${encodeURIComponent(file)}`;
}
export const releaseUrl = (v: Version) => `${REPO}/releases/tag/${encodeURIComponent(v.tag)}`;

/** The store pages, filtered to this Minecraft version and NeoForge */
export const storeLinks = (v: Version): Link[] => [
	...(MODRINTH_LIVE ? [{ label: 'Modrinth', href: `${MODRINTH}/versions?l=neoforge&${games(v).map((g) => `g=${g}`).join('&')}` }] : []),
	{ label: 'CurseForge', href: `${CURSEFORGE}/files/all?version=${games(v)[0]}&gameVersionTypeId=${CF_NEOFORGE}` },
];
/** The store front pages, for prose ("get it from ...") */
export const stores: Link[] = [
	...(MODRINTH_LIVE ? [{ label: 'Modrinth', href: MODRINTH }] : []),
	{ label: 'CurseForge', href: CURSEFORGE },
	{ label: 'GitHub releases', href: `${REPO}/releases` },
];
