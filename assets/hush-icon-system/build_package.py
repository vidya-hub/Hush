"""Build design documentation/HTML from individual imagegen assets. Reads PNGs only; never edits pixels."""
import json
import hashlib
from pathlib import Path
from PIL import Image
HERE = Path(__file__).resolve().parent
manifest = json.loads((HERE / "manifest.json").read_text())
pages = json.loads((HERE / "page-map.json").read_text())["pages"]
for item in manifest["icons"]:
    path = HERE / (item["id"] + ".png")
    item["status"] = "generated" if path.exists() else "pending"
    if path.exists():
        item["sha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
    prompt_path = HERE / (item["id"] + ".prompt.txt")
    item["prompt_file"] = str(prompt_path)
    if path.exists():
        with Image.open(path) as im:
            assert "A" in im.getbands(), path
            alpha = im.getchannel("A")
            low, high = alpha.getextrema()
            assert low == 0 and high >= 240, (path, low, high)
            bounds = alpha.point(lambda value: 255 if value >= 128 else 0).getbbox()
            assert bounds is not None, path
            item["metrics"] = {"width": im.width, "height": im.height, "bounds": list(bounds), "alpha_extrema": list(alpha.getextrema())}
manifest["generated_count"] = sum(i["status"] == "generated" for i in manifest["icons"])
manifest["total_count"] = len(manifest["icons"])
(HERE / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
prompts = {i["id"]: Path(i["prompt_file"]).read_text() for i in manifest["icons"] if Path(i["prompt_file"]).exists()}
(HERE / "prompts.json").write_text(json.dumps({"mode": manifest["mode"], "execution": manifest["execution"], "prompts": prompts}, indent=2) + "\n")
html = (HERE / "preview-template.html").read_text().replace("__DATA__", json.dumps({"icons": manifest["icons"], "pages": pages}).replace("</", "<\\/"))
(HERE / "icon-placement-preview.html").write_text(html)
print(f'{manifest["generated_count"]}/{manifest["total_count"]} individual icons; {len(pages)} page mappings; originals preserved')
