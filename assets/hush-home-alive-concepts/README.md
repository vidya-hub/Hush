# Hush: a home with more character

Built-in imagegen design proposals, 2026-09-30. No Android code changes. These build on the previously inspected emulator UI: Search, Games, Breathe bottom navigation, Hush header, search history and video playback.

The previous proposals made minimalism mostly empty space. These use a composed hierarchy: the existing tagline and breathing-ring motif give the upper page identity; search stays straightforward; History, Saved and Downloads expose existing destinations; active playback gives the lower page meaningful context.

## Concepts

- `01-paper-composed.png`: warm paper home with a sage identity panel, recent search chips, compact library tiles and floating video.
- `02-pine-editorial.png`: fewer containers, editorial typography directly on the pine surface, small library links and contextual listening.

Recommended direction: the second concept's lighter use of containers, available in both existing light and dark palettes. The first tests a stronger visual anchor; its library tiles and navigation elevation can be reduced during implementation.

The floating video stays above the three navigation buttons; no bottom player strip. It should share the existing playback session. The tagline is the existing app string. Ring decoration echoes the implemented breathing tool. Search chips and video imagery are illustrative; production must use actual profile history and media. Hide history in incognito. Now-playing title and playback motif appear only with active playback; no fake live state or decorative audio meter while idle.

The home should adapt to actual state: keep library shortcuts available when idle, omit empty recent history, and keep the identity composition compact on short windows. A future breathing-ring animation can be very slow and optional, respect reduced-motion settings, and never imply an active breathing session.

These are raster layout proposals, not implemented behavior. Exact prompts are in `prompts.json`; precise sizing, contrast, safe areas, floating-player dragging, keyboard and orientation behavior require implementation checks. The installed emulator UI differs from this source checkout, as recorded in the preceding `hush-home-pip-concepts/README.md`.
