// Build-time access to files of the mod's repository (the site lives in <repo>/site).
import fs from 'node:fs';
import path from 'node:path';

function findRepo() {
	let dir = process.cwd();
	for (let i = 0; i < 4; i++) {
		if (fs.existsSync(path.join(dir, 'gradle.properties')) && fs.existsSync(path.join(dir, 'src/main/java'))) return dir;
		dir = path.dirname(dir);
	}
	throw new Error('Could not find the mod repository above ' + process.cwd());
}
export const REPO = findRepo();
export const repoPath = (p: string) => path.join(REPO, p);
export const readRepo = (p: string) => fs.readFileSync(repoPath(p), 'utf8').replace(/\r\n/g, '\n');
export const readRepoJson = (p: string) => JSON.parse(readRepo(p));
export const listRepo = (dir: string) =>
	fs.existsSync(repoPath(dir))
		? fs
				.readdirSync(repoPath(dir), { recursive: true })
				.map((f) => String(f).replaceAll('\\', '/'))
				.filter((f) => f.endsWith('.json'))
				.sort()
		: [];
export const JAVA = 'src/main/java/vectorwing/farmersdelight/';
