# AGENTS.md

## Project Summary

Immersive Petro-Machinery is an unofficial NeoForge 1.21.1 addon centered on
the Immersive Machinery Tunnel Digger.

It integrates:

- Immersive Machinery
- Immersive Petroleum
- Immersive Engineering

Project constants:

- Mod id: `immersive_petro_machinery`
- Minecraft: `1.21.1`
- Loader: NeoForge
- Java: `21`

## Hard Rules

- Do not copy code or assets from Immersive Petroleum, Immersive Machinery,
  Immersive Engineering, or Immersive Aircraft.
- Do not invent registry IDs.
- Do not implement mixins unless the task explicitly allows it.
- Do not start Phase 8 unless the user explicitly approves Phase 8 work.
- Do not implement 5x5/7x7/9x9 mining until explicitly approved.
- Do not change Tunnel Digger mining behavior unless explicitly approved.
- Do not modify the native Tunnel Digger GUI for IPM upgrades.
- Do not transfer fluids into the Tunnel Digger.
- Do not re-enable Survey Console final output generation.
- Do not implement final Core Sampling Scan activation.
- Do not rewrite the whole mod.
- Do not implement custom rendering before gameplay foundation is validated.
- Always use Java 21.
- Always run build after changes.

## Current Phase Order

- Phase 1: Skeleton and dependency stack verification - Done.
- Phase 2A: IPM dock block set + deprecated 7x3x3 layout - Done.
- Phase 2B: Deprecated 7x3x3 multiblock validation - Done.
- Phase 3: Tunnel Digger detection - Done.
- Phase 4: Soft lock/unlock - Done.
- Phase 5: Maintenance GUI - Done.
- Phase 6: Fuel/lubricant ports - Done.
- Phase 7: IE + IP survey - Done.
- Garage refactor: 9x9x9 Industrial Driller Garage validation/manual/port
  alignment - Done.
- Garage redesign: 11x13x10 Industrial Driller Garage with lower hempcrete
  foundation, ramp, and rear 3x3x3 tanks - Done, pending manual runtime
  validation.
- Manual validation before Phase 8 - Next.
- Phase 8: 5x5/7x7/9x9 mining research/implementation - Later, only after
  explicit approval.

## Build Commands

Use Java 21 for all builds. The Gradle build is configured with a Java 21
toolchain, Java 21 source/target compatibility, and `--release 21` compilation.

The Gradle wrapper still needs a valid JVM to start. If `JAVA_HOME` is set to an
invalid location, such as a `bin` folder instead of a JDK root, Gradle fails
before it can read the project toolchain settings. In that case, either unset
`JAVA_HOME` so Gradle can start from `PATH`, or set `JAVA_HOME` to a Java 21 JDK
root.

Unix-like systems:

```sh
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

Windows PowerShell with explicit Java 21:

```powershell
$env:JAVA_HOME = "C:\Path\To\Java21" # JDK root, not the bin folder
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat build
```

## Dependency Stack

The verified Phase 1 dependency stack is:

- Immersive Engineering `1.21.1-12.4.2-194`
- Immersive Petroleum `4.4.1-37`
- Immersive Aircraft `1.2.2+1.21.1+neoforge`
- Immersive Machinery `0.2.0+1.21.1+neoforge`

## Codex Skill Routing

Use local Codex skills as procedural aids only. Skills never override the Hard
Rules, phase order, or user-provided task boundaries in this file. Skills never
authorize Phase 8, Tunnel Digger mining changes, mixins, native Tunnel Digger GUI
modification, or Survey Console final output reactivation.

Project-local skills live under `.agents/skills`. Installed global skills under
the user Codex skills directory may be used as general helpers, but this file is
the source of truth for IPM-specific scope boundaries.

If a task touches multiple skills, choose the narrowest/highest-risk specialist
first. Use this priority order:

1. `skill-creator` - only when creating, revising, or reviewing Codex skills.
2. `neoforge-gametest-diagnostics` - GameTest/dev diagnostics and automated
   safety tests.
3. `ie-formed-multiblock-dev` - true IE formed Industrial Driller Garage
   migration and formed-state safety gates.
4. `tunnel-digger-integration-research` - no project-local skill currently
   exists; dormant/future-only; do not create or use until explicit Phase 8
   approval.
5. `ie-style-minecraft-assets` - no project-local skill currently exists; do not
   create now. For explicit asset-only work, use existing visual/model skills
   such as `minecraft-multiblock-model-master` or text-only asset planning via
   `minecraft-asset-autogenerator`.
6. `doc-maintenance` - docs, manual, changelog, routing, release text, and
   validation-result documentation.
7. `minecraft-mod-dev` - default general NeoForge/IPM code work unless a
   narrower specialist applies.
8. `redstone-basics` - in-game redstone examples only.

### Skill Ownership Matrix

| Work type | Final owner | Boundary |
| --- | --- | --- |
| Creating or revising Codex skills | `skill-creator` | Does not authorize gameplay work or Phase 8. |
| GameTest/dev diagnostics | `neoforge-gametest-diagnostics` | Dev/test-only; does not implement normal gameplay. |
| True IE formed multiblock migration | `ie-formed-multiblock-dev` | IPM-specific formed migration only; generic formed architecture may reference `minecraft-ie-style-multiblocks`. |
| Future Tunnel Digger mining research | `tunnel-digger-integration-research` | Dormant until explicit Phase 8 approval; no current skill file. |
| IPM visual assets/textures/GUI visuals | Existing visual/model skills | Do not create `ie-style-minecraft-assets` now; use existing asset skills only when explicitly asked for art/resources. |
| Docs/manual/changelog/routing | `doc-maintenance` | Does not own gameplay Java implementation. |
| General NeoForge/IPM code | `minecraft-mod-dev` | Use for general Minecraft mod development unless a narrower specialist skill applies. |
| In-game redstone examples | `redstone-basics` | Unrelated to IPM Java architecture unless the user asks for player-facing redstone. |

### Proposed Skill Pack Decisions

| Proposed skill | Existing overlap | Decision | Reason | Final owner |
| --- | --- | --- | --- | --- |
| `minecraft-mod-dev` | Existing general Minecraft/NeoForge skill | Keep existing unchanged | Broad owner remains useful but must yield to specialists. | `minecraft-mod-dev` |
| `doc-maintenance` | Existing documentation skill | Keep existing unchanged | Covers docs/manual/changelog/routing without gameplay code. | `doc-maintenance` |
| `ie-formed-multiblock-dev` | Existing project-local skill, plus partial overlap with `minecraft-ie-style-multiblocks` | Update existing skill | Keep as narrow IPM-specific owner for formed Garage status, danger zones, and phase gates. | `.agents/skills/ie-formed-multiblock-dev` |
| `neoforge-gametest-diagnostics` | Existing project-local skill; broad `minecraft-mod-dev` mentions GameTest tooling | Update existing skill | Keep as narrow dev-only safety diagnostics owner without becoming generic Java testing. | `.agents/skills/neoforge-gametest-diagnostics` |
| `tunnel-digger-integration-research` | Overlaps future Phase 8 mining risk | Do not create now; dormant note only | Creating it now could encourage blocked mining work. | Future only after Phase 8 approval |
| `ie-style-minecraft-assets` | Overlaps existing visual/model/asset skills | Reject as duplicate | Existing asset/model skills cover this until a concrete IPM-only asset workflow is needed. | Existing visual/model skills |
| `redstone-basics` | Existing redstone skill | Keep existing unchanged | Only owns in-game redstone examples. | `redstone-basics` |
| `skill-creator` | Existing skill creation skill | Keep existing unchanged | Owns skill creation/revision only. | `skill-creator` |

See `docs/skill-routing-overlap-review.md` for the full overlap review.

## Notion Workflow

After each phase:

- Update the relevant `IPM Tasks` item.
- Create or update a phase result page.
- Document build result.
- Document files changed.
- Document blockers.
- Do not mark a task Done unless build/test passed or the user explicitly
  accepts partial completion.

## Reporting Format

Every Codex result must include:

- Summary
- Build result
- Files changed
- Tests or run checks
- Notion updates made
- Blockers
- Recommended next step
