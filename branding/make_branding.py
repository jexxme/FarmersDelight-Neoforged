"""
Builds the Farmer's Delight Neoforged branding from vectorwing's original logo (src/main/resources/logo.png,
MIT, Farmer's Delight) plus an own "Neoforged" label, the same way Farmer's Delight Refabricated
marks its edition.

The label copies the look of the original sign (checkered paper, thin frame, 7px lettering) in orange.
Letters that exist on the original sign (e, r, g) use the original glyph shapes; N, o, f, d are drawn
in the same style.

Outputs:
  src/main/resources/banner.png  in-game mod list banner (mods.toml bannerFile)
  src/main/resources/icon.png    in-game mod list icon (mods.toml iconFile)
  branding/banner.png            banner scaled x4 for project pages
  branding/icon.png              512x512 project icon (CurseForge / Modrinth)

Usage (from the repository root, needs Pillow):  python branding/make_branding.py
"""
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
LOGO = ROOT / "src/main/resources/logo.png"

# Original sign palette, shifted to orange
PAPER_A = (242, 186, 132, 255)    # sign paper (206, 181, 151)
PAPER_B = (247, 198, 150, 255)    # sign paper (212, 190, 164)
PAPER_EDGE = (226, 160, 102, 255) # sign paper edge (195, 168, 136)
FRAME_LIGHT = (196, 118, 62, 255) # sign frame highlight (177, 142, 101)
FRAME = (110, 50, 20, 255)        # sign frame (72, 48, 21)
INK = (104, 44, 16, 255)          # sign lettering (85, 58, 31)
INK_SOFT = (176, 98, 52, 255)     # sign soft lettering pixels (120, 88, 54)
SHADOW = (0, 0, 0, 255)           # the original logo's drop shadow is opaque black

# 9 rows: cap height 7 (rows 0-6), x-height 5 (rows 2-6), descender rows 7-8.
# '#' = ink, '.' = soft ink, ' ' = paper. e, r, g are taken from the original "Farmer's Delight" sign.
GLYPHS = {
    "N": ["#   #", "##  #", "# . #", "# # #", "# . #", "#  ##", "#   #", "     ", "     "],
    "e": ["     ", "     ", ".### ", "#   #", "####.", "#    ", " ###.", "     ", "     "],
    "o": ["     ", "     ", ".###.", "#   #", "#   #", "#   #", ".###.", "     ", "     "],
    "f": [" .##", " #  ", "### ", " #  ", " #  ", " #  ", " #  ", "    ", "    "],
    "r": ["     ", "     ", "..## ", "##  .", "#    ", "#    ", "#    ", "     ", "     "],
    "g": ["     ", "     ", ".###.", "#   #", "#   #", ".####", "    #", ".### ", "     "],
    "d": ["    #", "    #", ".####", "#   #", "#   #", "#   #", ".####", "     ", "     "],
}


def text_width(text):
    return sum(len(GLYPHS[c][0]) for c in text) + len(text) - 1


def label(text, pad_x=3):
    """A small sign: 1px frame, light inner edge, checkered paper, lettering, opaque drop shadow."""
    inner_w = text_width(text) + 2 * pad_x
    inner_h = 8 + 2  # glyph rows used (cap + 1 descender row) + 1px padding top/bottom
    w, h = inner_w + 4, inner_h + 4
    img = Image.new("RGBA", (w + 1, h + 1), (0, 0, 0, 0))
    px = img.load()
    # drop shadow (offset 1,1), like the original logo
    for x in range(1, w + 1):
        for y in range(1, h + 1):
            if not ((x in (1, w) and y in (1, h))):
                px[x, y] = SHADOW
    for x in range(w):
        for y in range(h):
            corner = (x in (0, w - 1)) and (y in (0, h - 1))
            if corner:
                px[x, y] = (0, 0, 0, 0)
            elif x in (0, w - 1) or y in (0, h - 1):
                px[x, y] = FRAME
            elif x in (1, w - 2) or y in (1, h - 2):
                px[x, y] = FRAME_LIGHT if (x + y) % 2 else PAPER_EDGE
            else:
                px[x, y] = PAPER_A if (x + y) % 2 else PAPER_B
    # restore the shadow under the transparent corners' neighbours
    x0, y0 = 2 + pad_x, 2 + 1
    for char in text:
        glyph = GLYPHS[char]
        for row, line in enumerate(glyph[:8]):
            for col, cell in enumerate(line):
                if cell == "#":
                    px[x0 + col, y0 + row] = INK
                elif cell == ".":
                    px[x0 + col, y0 + row] = INK_SOFT
        x0 += len(glyph[0]) + 1
    return img


def scaled(img, factor):
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def sign_bounds(logo):
    """Bounding box of the "Farmer's Delight" sign (everything right of the crate)."""
    right = logo.crop((41, 0, logo.width, logo.height))
    l, t, r, b = right.getbbox()
    return l + 41, t, r + 41, b


def build_banner(logo):
    tag = label("Neoforged")
    sl, st, sr, sb = sign_bounds(logo)
    # Hang the label from the sign: right-aligned with the sign's frame, top overlapping its bottom rail.
    x = sr - tag.width + 3
    y = sb - 8
    height = max(logo.height, y + tag.height)
    banner = Image.new("RGBA", (logo.width, height), (0, 0, 0, 0))
    banner.alpha_composite(logo)
    banner.alpha_composite(tag, (x, y))
    return banner


def build_icon(logo):
    crate = logo.crop((0, 0, 40, logo.height))
    crate = crate.crop(crate.getbbox())
    icon = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    cx, cy = (48 - crate.width) // 2, (48 - crate.height) // 2
    icon.alpha_composite(crate, (cx, cy))
    tag = label("N", pad_x=2)
    # Pinned to the crate's bottom-right corner, half on the crate.
    icon.alpha_composite(tag, (cx + crate.width - tag.width + 3, cy + crate.height - tag.height + 4))
    return icon


def main():
    logo = Image.open(LOGO).convert("RGBA")
    banner = build_banner(logo)
    icon = build_icon(logo)
    banner.save(ROOT / "src/main/resources/banner.png")
    icon.save(ROOT / "src/main/resources/icon.png")
    out = ROOT / "branding"
    out.mkdir(exist_ok=True)
    scaled(banner, 4).save(out / "banner.png")
    icon_512 = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    icon_512.alpha_composite(scaled(icon, 10), (16, 16))  # 480x480 centered
    icon_512.save(out / "icon.png")
    print("written", out)


if __name__ == "__main__":
    main()
