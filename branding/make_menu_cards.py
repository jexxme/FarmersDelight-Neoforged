"""
Renders the store-page menu cards from the mod's item textures (nearest-neighbour upscale).

  python branding/make_menu_cards.py branding/screenshots/menu-meals.png meals
  python branding/make_menu_cards.py branding/screenshots/menu-pantry.png pantry

Needs Pillow and the Georgia font (C:\\Windows\\Fonts).
"""
import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

TEX = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "farmersdelight", "textures", "item")
OUT = sys.argv[1]
WHICH = sys.argv[2]

SECTIONS = [
    ("Hearty meals", [
        "beef_stew", "chicken_soup", "vegetable_soup", "fish_stew", "baked_cod_stew", "pumpkin_soup", "onion_soup",
        "noodle_soup", "bone_broth", "ratatouille", "fried_rice", "mushroom_rice", "cabbage_rolls", "dumplings",
        "pasta_with_meatballs", "pasta_with_mutton_chop", "squid_ink_pasta", "vegetable_noodles", "steak_and_potatoes",
        "roasted_mutton_chops", "bacon_and_eggs", "grilled_salmon", "stuffed_potato", "mixed_salad", "nether_salad",
        "roast_chicken", "honey_glazed_ham", "shepherds_pie", "stuffed_pumpkin", "fruit_salad",
    ]),
    ("Snacks & handhelds", [
        "hamburger", "bacon_sandwich", "egg_sandwich", "chicken_sandwich", "mutton_wrap", "barbecue_stick",
        "salmon_roll", "cod_roll", "kelp_roll", "kelp_roll_slice", "fried_egg", "cooked_rice", "dog_food", "horse_feed",
    ]),
    ("Sweets & drinks", [
        "apple_pie_slice", "sweet_berry_cheesecake_slice", "chocolate_pie_slice", "cake_slice", "honey_cookie",
        "sweet_berry_cookie", "melon_popsicle", "glow_berry_custard", "hot_cocoa", "apple_cider", "melon_juice",
        "milk_bottle",
    ]),
    ("From the garden & the butcher", [
        "cabbage", "tomato", "onion", "rice", "rice_panicle", "pumpkin_slice", "cabbage_leaf", "tomato_sauce",
        "wheat_dough", "raw_pasta", "pie_crust", "minced_beef", "beef_patty", "bacon", "ham", "chicken_cuts",
        "mutton_chops", "cod_slice", "salmon_slice", "straw", "canvas",
    ]),
    ("Knives", ["flint_knife", "copper_knife", "iron_knife", "golden_knife", "diamond_knife", "netherite_knife"]),
]

SCALE = 6
ICON = 16 * SCALE
CELL = 132
COLS = 10
W = COLS * CELL + 200
MARGIN_X = (W - COLS * CELL) // 2
if WHICH == "meals":
    SECTIONS = SECTIONS[:3]
    TITLE, SUB = "On the menu", "{n} dishes, snacks and drinks"
    SECTIONS[1] = (SECTIONS[1][0], [i for i in SECTIONS[1][1] if i not in ("dog_food", "horse_feed")])
else:
    SECTIONS = [("From the garden & the butcher", SECTIONS[3][1]), ("Knives for every tier", SECTIONS[4][1]),
                ("Something for the animals", ["dog_food", "horse_feed"])]
    TITLE, SUB = "In the pantry", "crops, cuts and ingredients"

BG_TOP = (40, 28, 20)
BG_BOT = (26, 18, 13)
CREAM = (244, 228, 196)
MUTED = (196, 160, 112)
LINE = (120, 88, 56)

serif_b = ImageFont.truetype(r"C:\Windows\Fonts\georgiab.ttf", 64)
serif_i = ImageFont.truetype(r"C:\Windows\Fonts\georgiai.ttf", 30)
head = ImageFont.truetype(r"C:\Windows\Fonts\georgiab.ttf", 30)


def rows(n):
    return (n + COLS - 1) // COLS


def per_row(n):
    r = rows(n)
    return (n + r - 1) // r


title_h = 190
sec_head_h = 70
height = title_h + sum(sec_head_h + rows(len(items)) * CELL + 24 for _, items in SECTIONS) + 40

img = Image.new("RGB", (W, height), BG_TOP)
px = ImageDraw.Draw(img)
for y in range(height):
    t = y / height
    c = tuple(int(BG_TOP[i] * (1 - t) + BG_BOT[i] * t) for i in range(3))
    px.line([(0, y), (W, y)], fill=c)

# soft vignette
vig = Image.new("L", (W, height), 0)
vd = ImageDraw.Draw(vig)
vd.rectangle([60, 60, W - 60, height - 60], fill=255)
vig = vig.filter(ImageFilter.GaussianBlur(120))
dark = Image.new("RGB", (W, height), (12, 8, 6))
img = Image.composite(img, dark, vig)

d = ImageDraw.Draw(img)


def center_text(y, text, font, fill):
    w = d.textlength(text, font=font)
    d.text(((W - w) / 2, y), text, font=font, fill=fill)
    return w


total = sum(len(i) for _, i in SECTIONS)
center_text(52, TITLE, serif_b, CREAM)
center_text(132, SUB.format(n=total), serif_i, MUTED)

layer = Image.new("RGBA", (W, height), (0, 0, 0, 0))
shadow = Image.new("RGBA", (W, height), (0, 0, 0, 0))

y = title_h
for name, items in SECTIONS:
    tw = d.textlength(name, font=head)
    cx = W / 2
    d.text((cx - tw / 2, y + 14), name, font=head, fill=CREAM)
    d.line([(MARGIN_X + 8, y + 34), (cx - tw / 2 - 24, y + 34)], fill=LINE, width=2)
    d.line([(cx + tw / 2 + 24, y + 34), (W - MARGIN_X - 8, y + 34)], fill=LINE, width=2)
    y += sec_head_h
    n = len(items)
    for r in range(rows(n)):
        k = per_row(n)
        row = items[r * k:(r + 1) * k]
        off = MARGIN_X + (COLS - len(row)) * CELL // 2
        for c, item in enumerate(row):
            tex = Image.open(os.path.join(TEX, f"{item}.png")).convert("RGBA")
            tex = tex.crop((0, 0, 16, 16))
            big = tex.resize((ICON, ICON), Image.NEAREST)
            x0 = off + c * CELL + (CELL - ICON) // 2
            y0 = y + r * CELL + (CELL - ICON) // 2
            layer.alpha_composite(big, (x0, y0))
            sh = Image.new("RGBA", big.size, (0, 0, 0, 0))
            sh.putalpha(big.getchannel("A").point(lambda a: 150 if a else 0))
            shadow.alpha_composite(sh, (x0 + 7, y0 + 10))
    y += rows(n) * CELL + 24

shadow = shadow.filter(ImageFilter.GaussianBlur(6))
img = img.convert("RGBA")
img.alpha_composite(shadow)
img.alpha_composite(layer)
img.convert("RGB").save(OUT, quality=95)
print(OUT, img.size, total)
