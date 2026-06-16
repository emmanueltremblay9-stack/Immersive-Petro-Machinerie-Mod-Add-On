# Dev Diagnostics

## Garage State Safety GameTests

The Phase 7.8A3 diagnostic harness is dev-only and runs through NeoForge's
GameTest server. It does not enable Phase 8, mining changes, mixins, custom
rendering, final survey output, or Tunnel Digger fluid transfer.

Run from PowerShell with Java 21:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\graalvm-jdk-21.0.4+8.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat runGameTestServer --no-daemon --console=plain
```

The run config enables only the `immersive_petro_machinery` GameTest namespace.
The current ModDevGradle `RunModel` in this project does not expose the
`setForceExit(false)` method shown in some NeoForge GameTest docs, so if the
runner reports a confusing forced-exit failure, check the GameTest log summary
before treating it as an IPM test failure.

The harness validates:

- normal-mode formed shell creation is disabled by default;
- protected controller state blocks the experimental formed-shell branch;
- Survey Console containment does not create item outputs;
- Garage upgrade inventory persists through block entity NBT;
- dock fuel and lubricant tanks persist through block entity NBT when the
  Immersive Petroleum fluids are available.

The harness does not validate true formed-shell vehicle access/collision. True
formed Gate 1 remains Needs Review until the formed shell itself is tested with
the Tunnel Digger path.
