// Minecraft-style tooltips for anything with data-tip-items, and JEI-like cycling of tag slots.
const tip = document.createElement('div');
tip.id = 'mc-tooltip';
tip.setAttribute('role', 'tooltip');
document.body.appendChild(tip);
let current: HTMLElement | null = null;

function show(el: HTMLElement, x: number, y: number) {
	const entries = (el.dataset.tipItems ?? '').split('|').filter(Boolean);
	if (!entries.length) return;
	const cyc = el.querySelector('.mc-cycle');
	const idx = cyc ? Math.max(0, [...cyc.querySelectorAll('img')].findIndex((i) => i.classList.contains('on'))) : 0;
	const [name, id] = (entries[idx] ?? entries[0]).split('~');
	tip.replaceChildren();
	const add = (cls: string, text: string) => {
		const d = document.createElement('div');
		d.className = cls;
		d.textContent = text;
		tip.appendChild(d);
	};
	add('t-name', name);
	for (const line of (el.dataset.tipExtra ?? '').split('\n').filter(Boolean)) add('t-blue', line);
	if (el.dataset.tipSub) add('t-gold', el.dataset.tipSub);
	if (id) add('t-id', id);
	place(x, y);
	tip.classList.add('on');
}
function place(x: number, y: number) {
	const r = tip.getBoundingClientRect();
	let left = x + 14;
	let top = y - r.height - 10;
	if (left + r.width > innerWidth - 8) left = x - r.width - 14;
	if (top < 8) top = y + 18;
	tip.style.left = `${Math.max(8, left)}px`;
	tip.style.top = `${top}px`;
}
document.addEventListener('pointerover', (e) => {
	const el = (e.target as HTMLElement).closest<HTMLElement>('[data-tip-items]');
	if (el && el !== current) {
		current = el;
		show(el, e.clientX, e.clientY);
	}
	if (!el && current) {
		current = null;
		tip.classList.remove('on');
	}
});
document.addEventListener('pointermove', (e) => current && place(e.clientX, e.clientY));
document.addEventListener('focusin', (e) => {
	const el = (e.target as HTMLElement).closest<HTMLElement>('[data-tip-items]');
	if (!el) return;
	const r = el.getBoundingClientRect();
	current = el;
	show(el, r.right, r.top);
});
document.addEventListener('focusout', () => {
	current = null;
	tip.classList.remove('on');
});

// Site search: put the item's icon on item results (item slugs match their icon files) and drop the sub-result
// that only repeats the page title. Pagefind re-renders as you type, so this just re-syncs after every change.
const search = document.getElementById('starlight__search');
const iconBase = `${import.meta.env.BASE_URL.replace(/\/$/, '')}/icons/farmersdelight/`;
const syncSearch = () => {
	for (const r of search!.querySelectorAll<HTMLElement>('.pagefind-ui__result')) {
		const title = r.querySelector<HTMLElement>('.pagefind-ui__result-title');
		const link = title?.querySelector<HTMLAnchorElement>('.pagefind-ui__result-link');
		if (!title || !link) continue;
		const slug = link.pathname.match(/\/items\/([^/]+)\/$/)?.[1];
		const want = slug ? iconBase + slug + '.png' : null;
		let img = title.querySelector<HTMLImageElement>('img.fd-search-icon');
		if (want && img?.getAttribute('src') !== want) {
			img?.remove();
			img = Object.assign(new Image(32, 32), { className: 'fd-search-icon', alt: '', src: want, onerror: () => img?.remove() });
			title.prepend(img);
		} else if (!want) img?.remove();
		const name = link.textContent?.trim();
		for (const n of r.querySelectorAll<HTMLElement>('.pagefind-ui__result-nested')) {
			n.classList.toggle('fd-dup', n.querySelector('.pagefind-ui__result-link')?.textContent?.trim() === name);
		}
	}
};
if (search) new MutationObserver(syncSearch).observe(search, { childList: true, subtree: true });

// Cycle tag slots once a second, all in step (like JEI). A hovered slot holds still so its tooltip stays right.
// Only slots on screen are cycled: long pages have hundreds, and touching them all every second costs real time.
const visible = new Set<HTMLElement>();
const io = new IntersectionObserver(
	(entries) => {
		for (const e of entries) e.isIntersecting ? visible.add(e.target as HTMLElement) : visible.delete(e.target as HTMLElement);
	},
	{ rootMargin: '100px' },
);
for (const c of document.querySelectorAll<HTMLElement>('.mc-cycle')) io.observe(c);
setInterval(() => {
	if (document.hidden) return;
	for (const c of visible) {
		if (current && current.contains(c)) continue;
		const on = c.querySelector('img.on') ?? c.firstElementChild;
		const next = on?.nextElementSibling?.tagName === 'IMG' ? on.nextElementSibling : c.querySelector('img');
		on?.classList.remove('on');
		next?.classList.add('on');
	}
}, 1000);
