# Hush individual imagegen icon package

70 unique UI glyphs, each generated separately with the built-in imagegen tool. No bulk generation or sprite sheet. Each master has a matching `.prompt.txt`; `prompts.json` consolidates the exact prompts. Original PNG pixels and alpha are preserved.

- [Final redesign plan](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/UI_REDESIGN_PLAN.md)
- [Per-page placement guide](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/ICON_PLACEMENT_GUIDE.md)
- [Mockup and real asset placement preview](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-icon-system/icon-placement-preview.html)
- [Manifest, completion and alpha metrics](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-icon-system/manifest.json)
- [Page mapping](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-icon-system/page-map.json)

The HTML preview uses the original PNG alpha masks, uniform optical fitting and semantic tint. It demonstrates placement and 24/32px glyph samples; it does not implement playback or Android routes. Serve the repository locally and open `/PipePipeClient/assets/hush-icon-system/icon-placement-preview.html`. Rebuild its inventory with `python3 PipePipeClient/assets/hush-icon-system/build_package.py`; this reads alpha metrics without altering images.

Use individual PNGs rather than treating the old drawn symbols within mockup raster images as the final icon source. Bottom navigation uses the shared 22dp margin. Watch removes search; video collapse restores the preceding screen with a floating player. Integration and runtime QA remain a later implementation phase.
