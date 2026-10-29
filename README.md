# Jenny Mod — Fabric port for Minecraft 26.3

**English** · [Русский](README.ru.md)

> **18+ only.** This is a port of an adult (pornographic) Minecraft mod. Do not use, build or share it if you are under 18.

An unofficial port of **Jenny Mod (Fapcraft) 1.1.0** by Schnurri (trolmastercard) from Minecraft 1.12.2 / Forge to **Minecraft 26.3 / Fabric**. The original mod has no release for current Minecraft versions; this project brings its characters and scenes over and is open for anyone to continue.

The repository includes the port code, the converted runtime assets and a ready-to-use `jenny-mod-1.1.0+26.3.jar`. The original mod's models, animations, textures and sounds remain the property of their author.

## Status

Playable, not feature-complete.

**Works**

- 9 characters with spawn eggs: Jenny, Ellie, Bia, Bee, Allie, Luna, Slime, Galath, Manglelie.
- Original Bedrock models and animations, rendered with GeckoLib 5; dressed/undressed models for Jenny, Ellie, Bia and Slime.
- Right-click action menu with the original item prices (Jenny: emeralds, ender pearls, diamonds; Luna: fish).
- Scenes as chains of the original clips: `G` moves to the next stage, `Shift` ends the scene.
- The player takes part through the character model's built-in player rig, drawn with **your own skin** (slim and wide arms).
- First-person scene camera from the rig's eyes (`F5` for a side view).
- 193 original sound cues tied to the animation keyframes.
- Adult-content warning on first launch; English and Russian translations.

**Not ported (yet)**

Natural spawning in biomes, per-character AI and combat, dialogue, the girls' inventory, structures, Allie's lamp, Galath's coin and flight, multiplayer sex requests, armor on characters.

**Removed on purpose**

- **Goblin and Kobold** — their models are child-height (0.99-block hitbox). They are excluded from the import and the code.
- **Galath's non-consensual clips** (`rape_*`) and their sound cues.

Please keep it that way in forks you publish.

## Requirements

- Minecraft **26.3**, Java **25**
- [Fabric Loader](https://fabricmc.net/use/) 0.20.1+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.165.0+26.3
- [GeckoLib](https://modrinth.com/mod/geckolib) 5.5.7 for Fabric 26.3

For multiplayer the mod must be installed on the server and on every client.

## Building

1. Install a Java 25 JDK (for example [Temurin](https://adoptium.net/)).
2. Build:

   ```bash
   ./gradlew build
   ```

   The mod is `build/libs/jenny-mod-1.1.0+26.3.jar`. Put it into `mods` together with Fabric API and GeckoLib.

To try it in a development client: `./gradlew runClient`.

### What the import changes

The original files were made for GeckoLib 3 on 1.12.2. The import (`gradle/original-assets.gradle`) converts them:

- Moves models and animations to GeckoLib 5's `geckolib/models` and `geckolib/animations` folders.
- Repairs exporter leftovers GeckoLib 5 rejects: easing names inside values (`"-97.03+easeInOutSine"`), bare `easeInOutBack` / `yaw` / `pitch`, `"50+."`, `NaN`.
- Sorts keyframes by time and **holds a channel's first keyframe from time 0**. Without this, GeckoLib 5 interpolates face-expression bones to huge values (giant mouths, flying eyes).
- Adds the first-person camera bone `boyCam` to models that lack it.
- Drops Goblin, Kobold and the `rape_*` clips; copies only the sounds and textures that are used.

## Controls

| Key | Action |
| --- | --- |
| Right click on a character | Action menu |
| `G` (configurable) | Next scene stage |
| `Shift` | End the scene |
| `F5` | Switch between first-person and side view |

## Project layout

```
src/main/java/.../sexmod/      common code: entity, actions, scene logic, networking
src/client/java/.../client/    rendering, partner skin layer, scene camera, menus, sounds
src/main/resources/            the port's own files: translations, spawn eggs, metadata
gradle/original-assets.gradle  import and conversion of the original assets
original-assets/               converted runtime assets included in the repository
```

Main entry points: `GirlEntity` (scene state machine), `GirlActions` (menus and prices), `GirlRenderer` / `PartnerSkinLayer` (rendering and the player rig), `SceneCamera` (first-person camera), `CueSounds` (sound cues).

### Scripted scene check

For development, a flag-gated test spawns a character next to you in a copy of a world, starts an action and takes screenshots:

```bash
./gradlew runClient -Pautotest=jenny:blowjob -Pworld=<world folder in run/saves> -Pshots=8
```

Optional: `-Pcamera=THIRD_PERSON_BACK`. Without `-Pautotest` none of this code runs.

## Contributing

Pull requests and forks are welcome — especially the missing features listed above.

## Credits

- **Schnurri (trolmastercard)** — the original Jenny Mod, its models, animations, textures and sounds, and the voice actresses credited in the original mod.
- [palkaline/jenny-mod-re](https://github.com/palkaline/jenny-mod-re) and the 1.7.10 port attempt — reverse-engineered 1.12.2 reference.
- [GeckoLib](https://github.com/bernie-g/geckolib), [Fabric](https://fabricmc.net/).

## License

The port's own code and files are released under the [MIT License](LICENSE). This does **not** cover the original mod's assets or design, which remain the property of their author. This project is not affiliated with or endorsed by the original author or Mojang.
