# Dependency Resolution

Build environment setup is documented in `docs/build.md`. Keep dependency
changes separate from Java runtime setup unless a dependency requires a
different toolchain.

Phase 1 uses these verified dependency sources:

- Immersive Engineering `1.21.1-12.4.2-194`: `blusunrize.immersiveengineering:ImmersiveEngineering` from BlameJared Maven.
- Immersive Petroleum `4.4.1-37`: `maven.modrinth:immersivepetroleum` from Modrinth Maven.
- Immersive Aircraft `1.2.2+1.21.1+neoforge`: `net.conczin:immersive_aircraft` from Conczin Maven.
- Immersive Machinery `0.2.0+1.21.1+neoforge`: `net.conczin:immersive_machinery` from Conczin Maven.
- DualCodecs `0.1.2`: `malte0811:DualCodecs`, required by the IE/IP stack.

If Modrinth Maven is unavailable, manually download `ImmersivePetroleum-1.21.1-4.4.1-37.jar` from Modrinth and place it in `libs/`, then change the Immersive Petroleum dependency to:

```gradle
implementation name: "ImmersivePetroleum-1.21.1-4.4.1-37"
```
