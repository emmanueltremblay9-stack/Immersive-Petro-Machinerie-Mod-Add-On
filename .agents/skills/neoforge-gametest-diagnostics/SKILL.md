---
name: neoforge-gametest-diagnostics
description: Use only for Immersive Petro-Machinery dev-only NeoForge GameTest and diagnostic harness work, including runGameTestServer setup, persistence checks, protected-state guards, formed-shell disabled-path tests, and clear pass/fail diagnostics. Do not use for normal gameplay implementation, Phase 8 mining, docs-only work, or assets.
---

# NeoForge GameTest Diagnostics

Use this project-local skill for safe dev-only NeoForge GameTest and diagnostic harness work in Immersive Petro-Machinery.

Skills never override `AGENTS.md`, project hard rules, phase order, or explicit user scope.

## Hard Rule Inheritance

- Never override project hard rules.
- Never override phase order.
- Never authorize Phase 8.
- Never authorize Tunnel Digger mining changes.
- Never authorize mixins.
- Never authorize native Tunnel Digger GUI modification.
- Never authorize Survey Console final output reactivation.

## When To Use

- GameTest harness creation.
- `runGameTestServer` setup.
- NBT save/load validation.
- `DockingControllerBlockEntity` state persistence tests.
- Garage upgrade inventory persistence tests.
- Fuel/lubricant persistence tests.
- Formed shell disabled-path tests.
- Protected-state formation guard tests.
- Survey Console containment tests.
- Small diagnostic helpers.

## When Not To Use

- Do not use for normal gameplay implementation.
- Do not use for Phase 8 mining.
- Do not use for texture or artwork.
- Do not use for Notion-only or docs-only updates.
- Do not use to weaken runtime behavior for test convenience.

## Coverage

- `@GameTest`.
- `@GameTestHolder`.
- `RegisterGameTestsEvent`.
- `GameTestHelper`.
- `data/<namespace>/structure` templates.
- `/test` command usage.
- `runGameTestServer`.
- Small template or no-template diagnostic design.
- Clear pass/fail/skipped reporting.
- Dev/test-only safety.

## Preferred Diagnostic Helpers

- `hasProtectedStateForFormation()`
- `getGarageUpgradeItemCountForDiagnostics()`
- `getFuelAmountForDiagnostics()`
- `getLubricantAmountForDiagnostics()`
- `hasSoftLockedDiggerForDiagnostics()`

## Hard Rules

- Diagnostics must not weaken normal gameplay.
- No tick-spam logs.
- No final Survey Console output.
- No Phase 8.
- No Tunnel Digger mining changes.
- No 5x5, 7x7, or 9x9 digging.
- No mixins.
- No native Tunnel Digger GUI modification.
- No fluid transfer into the Tunnel Digger.
- No final Core Sampling Scan activation.

## Output Expectations

- State the diagnostic purpose and whether it is dev/test-only.
- Prefer the smallest harness that proves the requested safety point.
- Keep skipped diagnostics explicit when a dependency or template is unavailable.
- Report command used, pass/fail result, and changed files.
- Confirm no gameplay code changed unless the user explicitly requested a diagnostic helper.
- Confirm Phase 8 was not started.
