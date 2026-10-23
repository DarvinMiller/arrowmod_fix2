# Arrow Friends — Fabric 1.19.4

A starter implementation of a consent-based friend locator for Minecraft Java 1.19.4.

## Important: this is a source project, not a prebuilt `.jar`

A Gradle installation and internet access are required to resolve Fabric/Minecraft dependencies and build the mod. This environment did not have Gradle installed, so the project could not be compiled or runtime-tested here.

## Build

- Java 17
- Gradle 8.6 (Fabric Loom 1.6.12)

From this folder run:

```sh
gradle --no-daemon clean build
```

The output jar should appear in `build/libs/`.

## Install

Install the same built jar on **every player's client and on the server**. This starter uses custom server networking; merely installing it on two clients while joining an unmodded vanilla server will not provide tracking. Fabric API is required.

## Included in this starter

- Friend request, accept and remove actions in the in-game menu.
- Tracking is mutual and opt-in; removal is reciprocal.
- Server only relays coordinates for accepted friends in the same dimension, and only while both tracking toggles are enabled.
- HUD arrow/name and distance, distance color bands, selectable top/center/corner layouts.
- Individual deterministic colors per player.
- Temporary 5-second ping marker sent to accepted friends in the same dimension.
- Keybindings exposed in Minecraft's Controls menu; `K` toggles arrows, `H` opens settings, middle mouse sends a ping by default.
- Configuration stored in `.minecraft/config/arrowmod.json`.

## Current limitations

This is an initial scaffold, not a polished release. The radial ping selector, camera-locking while the ping wheel is open, accurate activity detection for chest/crafting/anvil screens, AFK detection, persistent UUID-based offline friend removal, and a fully tested/compiled release build are not implemented yet. Activity indicators currently cover chat and a generic open-screen state only. Do not treat the project as a finished production mod until it has been built and tested against Fabric 1.19.4.


## GitHub Actions build

The repository workflow builds the project from `arrowmod/` with Java 17 and Gradle 8.6, then uploads the JAR as the `arrowmod-fabric-1.19.4` artifact. If your repository has a different layout, update the workflow `working-directory` accordingly.
