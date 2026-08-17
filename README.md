# Thanatified Crosshair Tweaks

A client-side Fabric mod for Minecraft 1.21.11 that replaces the vanilla
crosshair entirely, giving you:

1. An in-game GUI (default keybind: **comma `,`**, rebindable in Controls) — and if you have [Mod Menu](https://modrinth.com/mod/modmenu) installed, a "Configure" button on this mod's entry in Mod Menu opens the exact same screen
2. Full control over the normal crosshair: shape, color, size, thickness, gap, outline
3. A separate look for when you're aiming at a player (shape/color/size)
4. A smarter environmental blend that fixes the "disappears on gray blocks" problem

## Why this exists

Vanilla (and most existing crosshair mods) fake the environmental blend with
an inverted color logic op. Inverting a color close to middle gray produces
*another* color close to middle gray, which is exactly why the crosshair
vanishes on stone, deepslate, and concrete. This mod instead **reads the
actual pixels behind the crosshair every frame**, measures how saturated
("colorful") the background is, and:

- If the background is basically gray/desaturated → snaps straight to a
  configurable pure black or white (whichever contrasts harder), instead of
  inverting into another gray.
- If the background has real color → inverts it and pushes the result
  further from middle gray for a sharper, more saturated result.
- Blends that result with your chosen base color by a `Blend Strength` you
  control, so you can go anywhere from "fully static color" to "fully reactive."

## Project layout

```
thanatified-crosshair-tweaks/
├── build.gradle, settings.gradle, gradle.properties
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── thanatifiedcrosshairtweaks.mixins.json
│   └── assets/crosshairtweaks/lang/en_us.json
└── src/main/java/com/crosshairtweaks/
    ├── CrosshairTweaksClient.java      - entrypoint, keybind
    ├── config/CrosshairConfig.java     - all settings + JSON load/save
    ├── config/CrosshairShape.java      - shape enum
    ├── gui/CrosshairConfigScreen.java  - the config GUI
    ├── render/CrosshairRenderer.java   - draws each shape
    ├── render/EnvironmentalBlend.java  - reads the framebuffer, picks a color
    ├── mixin/InGameHudMixin.java       - cancels vanilla crosshair, calls ours
    └── integration/CrosshairTweaksModMenu.java - optional Mod Menu "Configure" button
```

## Building it

You'll need this built on your own machine — I can't reach Mojang's/Fabric's
Maven servers from this sandbox, so **none of this has been compiled or
tested**. It's written against well-established 1.21.x Fabric APIs, but
before you build, go to https://fabricmc.net/develop and confirm the exact
version strings in `gradle.properties` (`yarn_mappings`, `loader_version`,
`fabric_version`) — these change often and a stale one will fail dependency
resolution, not compile. Also check `modmenu_version` against whatever Mod
Menu build currently supports 1.21.11, on Modrinth or CurseForge.

**Mod Menu is optional at runtime.** It's only used to compile against, so
the mod's "Configure" button shows up in Mod Menu's list if it's installed,
but the mod works completely fine (via the comma keybind) without Mod Menu
present at all — it isn't a hard dependency.

```bash
# needs a JDK 21
./gradlew build
```

The built jar lands in `build/libs/thanatifiedcrosshairtweaks-1.0.0.jar`. Drop it in
your `mods` folder alongside a matching Fabric Loader + Fabric API for 1.21.11.

## Things to sanity-check after your first build

- If `renderCrosshair`'s exact method signature has shifted slightly between
  1.21.x patches, the mixin target in `InGameHudMixin` may need its `method`
  string adjusted (Loom's mixin errors at launch will tell you exactly what
  it expected).
- `Framebuffer.fbo` is the field name in current Yarn mappings; if your
  chosen mappings build differs, `EnvironmentalBlend` may need that field
  name updated.
- The GUI is a fixed two-column layout and doesn't scroll — on a very small
  window some bottom rows could clip. Resize the window or shrink `ROW_H` in
  `CrosshairConfigScreen` if that happens to you.

## Setting this up on GitHub

Everything needed to build via `git clone` + push is already here, including
the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) — no
Gradle install required on your end or anyone else's. From inside the
project folder:

```bash
git init
git add .
git commit -m "initial commit"
git remote add origin <your repo url>
git push -u origin main
```

What's already set up for you:

- **`.gitignore`** — keeps `build/`, `.gradle/`, the Loom dev `run/` folder
  (which is a full Minecraft install + saves, easily gigabytes), and IDE
  clutter out of the repo.
- **`.gitattributes`** — forces LF line endings on `gradlew` so it doesn't
  silently break when cloned on Windows.
- **`.github/workflows/build.yml`** — builds the mod on every push/PR, and
  if you push a tag like `v1.0.0` it creates a GitHub Release with the built
  jar attached automatically.

A few things worth doing before your first push:

- Update `"authors": ["you"]` in `fabric.mod.json` to your actual name/GitHub handle.
- Add a `"sources"` and `"homepage"` field to `fabric.mod.json` pointing at your repo, once it exists — this is what shows up as clickable links on Modrinth/CurseForge listings if you publish there later.
- If you ever want CurseForge/Modrinth auto-publishing on tag push, that's an easy future addition to the workflow — just ask when you're ready.



Minecraft's 26.x releases are unobfuscated and use **official Mojang
mappings** instead of Yarn, which changes the whole toolchain, not just the
version number:

- Switch the Loom plugin from the obfuscated `fabric-loom` to
  `net.fabricmc.fabric-loom` (non-obfuscated), and drop the `mappings {}`
  Yarn dependency entirely — Loom resolves Mojang's mappings directly for
  26.1+.
- `modImplementation`/`remapJar` become plain `implementation`/`jar` since
  there's no remapping step anymore.
- Fabric's `HudRenderCallback` was removed in favor of `HudElementRegistry`
  — the crosshair-cancelling approach in `InGameHudMixin` may need to move
  to a `HudElementRegistry`-based hook instead of a mixin, depending on
  whether `InGameHud.renderCrosshair` is still mixin-friendly in 26.2's
  class layout.
- Loom 1.17 + Gradle 9.5.1 are the versions Fabric recommends for 26.2 as of
  this writing.

I'd genuinely recommend getting this fully working and tested on 1.21.11
first, then porting — that keeps the shape/color/GUI/blend logic (which
doesn't touch any Minecraft internals) completely unchanged, and confines
the porting work to just the mixin/HUD hook and build script.
