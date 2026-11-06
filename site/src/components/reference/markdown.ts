// Renders repository markdown (changelog, UPSTREAM_ISSUES) to HTML at build time with Astro's own processor.
import { createMarkdownProcessor } from '@astrojs/markdown-remark';

let processor: Awaited<ReturnType<typeof createMarkdownProcessor>> | null = null;
const REPO = 'https://github.com/jexxme/FarmersDelight-Neoforged/blob/26.3/';
const UPSTREAM = 'https://github.com/vectorwing/FarmersDelight/issues/';

export async function renderMarkdown(md: string, opts: { upstreamRefs?: boolean } = {}) {
	processor ??= await createMarkdownProcessor({ syntaxHighlight: false });
	let src = md;
	// Bare "#1412" references point at vectorwing's tracker
	if (opts.upstreamRefs) src = src.replace(/(^|[\s(,])#(1[0-9]{3})\b/g, (_, pre, n) => `${pre}[#${n}](${UPSTREAM}${n})`);
	const { code } = await processor.render(src);
	// Relative repo links (docs/UPSTREAM_ISSUES.md, LICENSE...) go to GitHub
	return code.replace(/href="(?!https?:|#|\/)([^"]+)"/g, (_, p) => `href="${REPO}${p}"`);
}

/** Split markdown into "## " sections: { title, body }. Text before the first heading is dropped. */
export function sections(md: string) {
	return md
		.split(/^## /m)
		.slice(1)
		.map((s) => {
			const nl = s.indexOf('\n');
			return { title: s.slice(0, nl).trim(), body: s.slice(nl + 1).replace(/^# .*$/m, '').trim() };
		});
}
export const slug = (s: string) =>
	s
		.toLowerCase()
		.replace(/[^a-z0-9.+]+/g, '-')
		.replace(/^-|-$/g, '');
