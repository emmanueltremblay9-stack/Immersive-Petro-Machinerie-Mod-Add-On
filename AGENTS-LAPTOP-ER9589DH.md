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
- Do not implement 5x5/7x7/9x9 mining until explicitly approved.
- Do not touch Tunnel Digger mining behavior before the 11x13x10 garage
  foundation is validated.
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

Use relevant local Codex skills as procedural aids only. Skills do not override
the Hard Rules, phase order, or user-provided task boundaries in this file.

- Use `minecraft-mod-dev` for NeoForge/Minecraft implementation work,
  dependency/API checks, registry patterns, data/resource changes, and
  migration planning. For NeoForge 1.21.1 API details, prefer official
  NeoForge documentation or local dependency inspection before assuming method
  names or registry behavior.
- Use `doc-maintenance` when changing versions, dependencies, public docs,
  manual text, release notes, or packaging notes. Adapt its checklist to this
  repository's actual files: `gradle.properties`, `build.gradle`,
  `src/main/resources/META-INF/neoforge.mods.toml`, `docs/`,
  `src/main/resources/assets/immersive_petro_machinery/manual/`, and lang
  files. Ignore examples that refer to unrelated projects.
- Use Notion skills/tools when a task requires project-result pages, task
  status updates, roadmap notes, or validation checklist updates.
- Use `redstone-basics` only for in-game redstone automation or player-facing
  redstone examples. Do not apply it to Java mod architecture, mining logic, or
  machine behavior unless explicitly requested.
- Use `skill-creator` only when creating or revising Codex skills themselves.

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
