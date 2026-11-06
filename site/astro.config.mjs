// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import preact from '@astrojs/preact';
import { visit } from 'unist-util-visit';

// GitHub Pages project site: https://jexxme.github.io/FarmersDelight-Neoforged/
const site = process.env.SITE_URL ?? 'https://jexxme.github.io';
const base = process.env.BASE_PATH ?? '/FarmersDelight-Neoforged';

/** Markdown links written as "/mechanics/..." get the base path, so pages can link with plain site paths. */
function rehypeBaseLinks() {
	return (tree) => {
		visit(tree, 'element', (node) => {
			for (const attr of ['href', 'src']) {
				const v = node.properties?.[attr];
				if (typeof v === 'string' && v.startsWith('/') && !v.startsWith('//') && !v.startsWith(base + '/')) {
					node.properties[attr] = base + v;
				}
			}
		});
	};
}

export default defineConfig({
	site,
	base,
	trailingSlash: 'always',
	markdown: { rehypePlugins: [rehypeBaseLinks] },
	integrations: [
		preact(),
		starlight({
			title: "Farmer's Delight Neoforged",
			description: "The complete, data-driven guide to Farmer's Delight Neoforged for Minecraft 26.x: every item, recipe, mechanic and number, generated from the mod itself.",
			logo: { src: './src/assets/logo.png', alt: '' },
			favicon: '/favicon.png',
			social: [
				{ icon: 'github', label: 'GitHub', href: 'https://github.com/jexxme/FarmersDelight-Neoforged' },
			],
			editLink: { baseUrl: 'https://github.com/jexxme/FarmersDelight-Neoforged/edit/26.3/site/' },
			customCss: [
				'@fontsource-variable/literata',
				'@fontsource-variable/literata/wght-italic.css',
				'@fontsource/young-serif',
				'@fontsource/ibm-plex-mono/400.css',
				'@fontsource/ibm-plex-mono/500.css',
				'@fontsource/pixelify-sans/400.css',
				'./src/styles/theme.css',
				'./src/styles/mc.css',
			],
			components: {
				Head: './src/components/overrides/Head.astro',
				Footer: './src/components/overrides/Footer.astro',
			},
			lastUpdated: false,
			tableOfContents: { minHeadingLevel: 2, maxHeadingLevel: 3 },
			sidebar: [
				{
					label: 'Start here',
					items: [
						{ label: 'Getting started', slug: 'getting-started' },
						{ label: 'Your first meal', slug: 'guides/first-meal' },
						{ label: 'Farming progression', slug: 'guides/farming-progression' },
					],
				},
				{
					label: 'Cooking',
					items: [
						{ label: 'Cooking Pot', slug: 'mechanics/cooking-pot' },
						{ label: 'Heat sources', slug: 'mechanics/heat-sources' },
						{ label: 'Stove', slug: 'mechanics/stove' },
						{ label: 'Skillet', slug: 'mechanics/skillet' },
						{ label: 'Cutting Board', slug: 'mechanics/cutting-board' },
						{ label: 'Feasts', slug: 'mechanics/feasts' },
						{ label: 'Pies & cakes', slug: 'mechanics/pies' },
						{ label: 'Drinks', slug: 'mechanics/drinks' },
					],
				},
				{
					label: 'Farming',
					items: [
						{ label: 'Crops', slug: 'mechanics/crops' },
						{ label: 'Wild crops & worldgen', slug: 'mechanics/wild-crops' },
						{ label: 'Rich Soil', slug: 'mechanics/rich-soil' },
						{ label: 'Organic Compost', slug: 'mechanics/organic-compost' },
						{ label: 'Mushroom Colonies', slug: 'mechanics/mushroom-colonies' },
						{ label: 'Straw, rope & nets', slug: 'mechanics/straw-and-rope' },
					],
				},
				{
					label: 'Tools & effects',
					items: [
						{ label: 'Knives', slug: 'mechanics/knives' },
						{ label: 'Nourishment & Comfort', slug: 'mechanics/effects' },
						{ label: 'Animal feed', slug: 'mechanics/animal-feed' },
					],
				},
				{
					label: 'World',
					items: [
						{ label: 'Storage & decoration', slug: 'mechanics/storage-and-decoration' },
						{ label: 'Villagers & villages', slug: 'mechanics/villagers' },
						{ label: 'Loot', slug: 'mechanics/loot' },
					],
				},
				{
					label: 'Reference',
					items: [
						{ label: 'All items & blocks', link: '/items/' },
						{ label: 'Food reference', slug: 'reference/food' },
						{ label: 'Recipe planner', slug: 'reference/recipe-planner' },
						{ label: 'All recipes', slug: 'reference/recipes' },
						{ label: 'Configuration', slug: 'reference/config' },
						{ label: 'Advancements', slug: 'reference/advancements' },
					],
				},
				{
					label: 'For pack makers',
					items: [
						{ label: 'Overview', slug: 'pack-makers' },
						{ label: 'Recipe formats', slug: 'pack-makers/recipes' },
						{ label: 'Tags', slug: 'pack-makers/tags' },
						{ label: 'Loot modifiers', slug: 'pack-makers/loot-modifiers' },
						{ label: 'Worldgen', slug: 'pack-makers/worldgen' },
					],
				},
				{
					label: 'About',
					items: [
						{ label: 'Compatibility', slug: 'compatibility' },
						{ label: 'Differences from upstream', slug: 'differences' },
						{ label: 'Changelog', slug: 'changelog' },
						{ label: 'About these docs', slug: 'about' },
					],
				},
			],
		}),
	],
});
