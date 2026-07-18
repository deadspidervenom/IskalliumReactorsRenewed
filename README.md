# Iskallium Reactors Renewed

A NeoForge 1.21.1 mod that adds a custom-shape steel and Iskallium power reactor.

## What it does

Mine Iskallium ore (regular or deepslate) to get Iskallium Essence, smelt Steel Ingots,
and build a hollow box out of Steel Casing/Glass with a Controller and a Power Tap
somewhere on its walls. Fill the inside with Iskallium blocks and the reactor comes
online, generating FE based on how many Iskallium blocks are inside.

- **Custom reactor size** - any box big enough (see the config for the minimum
  dimension) works, so bigger reactors with more Iskallium blocks generate more power.
- **Power Tap** - the reactor's FE output point; other mods can pull energy from it
  through the standard `IEnergyStorage` capability.
- **Create compatibility** (optional) - if Create is installed, an alternate
  rotational Power Tap is available that feeds the reactor's output into Create's
  kinetic network as Stress Units instead of FE.
- **Reactor Status / Power Tap screens** - right-click the Controller or Power Tap to
  see whether the reactor is running, its size and core count, current output, and
  what's wrong with it if it isn't valid.

## Building

Standard NeoForge Gradle workflow:

```
./gradlew build
```

Run `./gradlew runClient` or `./gradlew runServer` to test locally.

## License

MIT - see [LICENSE](LICENSE).
