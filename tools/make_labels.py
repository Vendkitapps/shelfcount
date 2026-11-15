"""Makes EAN-13 product label images for testing Shelf Count (codes start with 20 = in-store range)."""
import sys
from PIL import Image, ImageDraw, ImageFont

L = ["0001101", "0011001", "0010011", "0111101", "0100011", "0110001", "0101111", "0111011", "0110111", "0001011"]
G = ["0100111", "0110011", "0011011", "0100001", "0011101", "0111001", "0000101", "0010001", "0001001", "0010111"]
R = ["1110010", "1100110", "1101100", "1000010", "1011100", "1001110", "1010000", "1000100", "1001000", "1110100"]
PARITY = ["LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG", "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL"]


def check_digit(d12: str) -> str:
    s = sum(int(c) * (1 if i % 2 == 0 else 3) for i, c in enumerate(d12))
    return str((10 - s % 10) % 10)


def bits(code: str) -> str:
    first, left, right = int(code[0]), code[1:7], code[7:]
    out = "101"
    for p, c in zip(PARITY[first], left):
        out += (L if p == "L" else G)[int(c)]
    out += "01010"
    for c in right:
        out += R[int(c)]
    return out + "101"


def label(name: str, code: str, path: str, module: int = 6):
    b = bits(code)
    quiet = 12 * module
    w = len(b) * module + 2 * quiet
    img = Image.new("RGB", (w + 80, 520), "white")
    d = ImageDraw.Draw(img)
    try:
        big = ImageFont.truetype("arialbd.ttf", 44)
        mono = ImageFont.truetype("consola.ttf", 34)
    except OSError:
        big = mono = ImageFont.load_default()
    d.text((40, 30), name, fill="black", font=big)
    x0, top, h = 40 + quiet, 110, 300
    for i, bit in enumerate(b):
        if bit == "1":
            guard = i < 3 or 45 <= i < 50 or i >= 92
            d.rectangle([x0 + i * module, top, x0 + (i + 1) * module - 1, top + h + (24 if guard else 0)], fill="black")
    ty = top + h + 26
    d.text((x0 - 34, ty), code[0], fill="black", font=mono)
    for group, start, end in ((code[1:7], 3, 45), (code[7:], 50, 92)):
        gw = d.textlength(group, font=mono)
        cx = x0 + (start + end) / 2 * module
        d.text((cx - gw / 2, ty), group, fill="black", font=mono)
    img.save(path)


if __name__ == "__main__":
    out = sys.argv[1]
    products = [("Merino Socks", "200123450001"), ("Wool Beanie", "200123450002"),
                ("Canvas Tote", "200123450003"), ("Linen Shirt", "200123450004")]
    for name, d12 in products:
        code = d12 + check_digit(d12)
        label(name, code, f"{out}/{name.lower().replace(' ', '-')}.png")
        print(name, code)
