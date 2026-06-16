---
name: ie-formed-multiblock-dev
description: Use only for Immersive Petro-Machinery true Immersive Engineering-style formed Industrial Driller Garage migration work, formed controller/part routing decisions, formed state handoff, capability-position planning, disassembly policy, and safety gates. Do not use for generic Minecraft modding, Phase 8 mining, assets, or GameTest harness implementation.
---

# IE Formed Multiblock Dev

Use this project-local skill for IPM-specific true formed multiblock migration decisions. This skill narrows the generic `minecraft-ie-style-multiblocks` domain to Immersive Petro-Machinery's 11x13x10 Industrial Driller Garage, current real-block fallback, formed-shell spike, and safety gates.

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

- True IE formed Industrial Driller Garage migration.
- `IETemplateMultiblock` or `TemplateMultiblock` planning.
- `MultiblockRegistration` alignment.
- Formed part block architecture for IPM only.
- Master/dummy block entity decisions.
- `IMultiblockLogic` or `IMultiblockState` planning.
- `GantryState` design.
- Capability position planning.
- Controller-relative click routing.
- Formed GUI routing.
- Formed Survey Console routing.
- Fuel/lubricant capability routing through formed positions.
- Disassembly policy.
- Manual/projector/hammer template alignment.
- Formed shell collision and access validation.
- Hybrid real-block fallback decisions.

## When Not To Use

- Do not use for general NeoForge work unless formed multiblock architecture is involved.
- Do not use for GameTest harness implementation; use `neoforge-gametest-diagnostics`.
- Do not use for Phase 8 mining changes.
- Do not use for asset texture work.
- Do not use to copy code, assets, names, or implementation details from IE, IP, IM, or IA.
- Do not use to rewrite the whole mod.

## Current IPM Status

- Real-block 11x13x10 Industrial Driller Garage is the stable gameplay path.
- Formed shell creation is disabled by default.
- Experimental formed shell creation requires:
  `-Dimmersive_petro_machinery.enableFormedShellCreation=true`
- Protected controller state must block experimental formation.
- True IE formed migration remains `Needs Review`.

## Hard Rules

- Never enable formed shell by default.
- Never bypass protected-state formation guard.
- Never mark true formed Gate 1 passed without explicit formed-shell vehicle access/collision testing.
- Never start Phase 8.
- Never change Tunnel Digger mining.
- Never add 5x5, 7x7, or 9x9 digging.
- Never add mixins.
- Never modify the native Tunnel Digger GUI for IPM upgrades.
- Never transfer fluids into the Tunnel Digger.
- Never re-enable Survey Console final output generation.
- Never implement final Core Sampling Scan activation.

## Danger Zones

- Replacing `DockingControllerBlockEntity` before migrating state.
- Losing fuel/lubricant tanks on formation or disassembly.
- Losing Garage upgrade inventory.
- Breaking GUI `stillValid` after formation.
- Breaking Survey Console containment.
- Filling the bay or ramp collision so the Tunnel Digger cannot enter.
- Letting manual/projector/hammer templates diverge.

## Migration Checklist

1. Keep real-block fallback.
2. Define `GantryState`.
3. Migrate only minimal formed state first.
4. Route controller-relative clicks.
5. Rewrite GUI access against formed state.
6. Move or bridge fuel/lubricant state safely.
7. Add capability positions.
8. Add disassembly policy.
9. Validate manual/projector/hammer same template.
10. Validate collision/access.
11. Only then consider later gameplay expansion, and only with explicit approval.

## Output Expectations

- State whether the task is formed-multiblock-specific.
- Identify any hard-rule or phase-order risk before proposing changes.
- Keep the real-block fallback explicit.
- List files changed, build/test result, and remaining blockers.
- Confirm Phase 8 was not started.
- Confirm no Tunnel Digger mining changes were made.
