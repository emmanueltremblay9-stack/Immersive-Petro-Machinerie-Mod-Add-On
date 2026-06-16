# Codex Skill Pack Overlap Review - Immersive Petro-Machinery

Date: 2026-05-15

## Existing Skill System

Project-local skill directory: `.agents/skills`

Project-local skills found:

| Skill | File | Purpose | Boundary |
| --- | --- | --- | --- |
| `ie-formed-multiblock-dev` | `.agents/skills/ie-formed-multiblock-dev/SKILL.md` | IPM-specific true IE formed Industrial Driller Garage migration and formed-state safety gates. | Not for general NeoForge work, GameTest harnesses, assets, or Phase 8 mining. |
| `neoforge-gametest-diagnostics` | `.agents/skills/neoforge-gametest-diagnostics/SKILL.md` | Dev-only GameTest and diagnostic harness work for IPM state safety. | Not for normal gameplay implementation, docs-only work, assets, or Phase 8 mining. |

Relevant installed global skills inspected for overlap:

| Skill | Purpose | Review note |
| --- | --- | --- |
| `skill-creator` | Creating or updating Codex skills. | Keep as the owner for skill creation/revision/review. |
| `minecraft-mod-dev` | General Minecraft mod development and integration. | Keep as the default general NeoForge/IPM code owner, but it yields to narrower IPM specialists. |
| `doc-maintenance` | Documentation updates and release/documentation workflow. | Keep for docs/manual/changelog/routing updates; AGENTS.md remains the IPM-specific source of truth. |
| `minecraft-ie-style-multiblocks` | Generic IE-style multiblock architecture. | Too broad for IPM safety gates; use only as a reference helper behind `ie-formed-multiblock-dev`. |
| `minecraft-asset-autogenerator` | Text-only Minecraft asset plans and placeholder descriptors. | Covers proposed IPM asset planning without a new project-local skill. |
| `minecraft-multiblock-model-master` | Visual design, Blockbench planning, and aesthetic handoff. | Covers IE-style visual planning without runtime ownership. |
| `minecraft-doc-sync` | Text-only Minecraft documentation sync payloads. | Optional helper for wiki/Notion payloads, not the routing owner. |
| `minecraft-changelog-generator` | Text-only Minecraft changelogs and patch notes. | Optional helper for release notes, not the routing owner. |
| `redstone-basics` | In-game redstone basics/examples. | Keep only for player-facing redstone examples, not IPM Java architecture. |

## Overlap Matrix

| Proposed Skill | Existing overlap | Decision | Reason | Final owner |
| --- | --- | --- | --- | --- |
| `minecraft-mod-dev` | Installed global general Minecraft/NeoForge skill. | Keep existing unchanged | It remains useful for general NeoForge/IPM code work but must yield to project specialists. | `minecraft-mod-dev` |
| `doc-maintenance` | Installed global documentation skill, plus optional `minecraft-doc-sync` and `minecraft-changelog-generator`. | Keep existing unchanged | It covers docs/manual/changelog/routing without owning gameplay Java implementation. | `doc-maintenance` |
| `ie-formed-multiblock-dev` | Existing project-local skill; partial overlap with global `minecraft-ie-style-multiblocks`. | Update existing skill | The project-local skill is narrower and carries IPM-specific formed-shell safety gates. | `.agents/skills/ie-formed-multiblock-dev` |
| `neoforge-gametest-diagnostics` | Existing project-local skill; broad `minecraft-mod-dev` mentions `runGameTestServer`. | Update existing skill | IPM needs a dev-only diagnostic owner for state safety and formation guards. | `.agents/skills/neoforge-gametest-diagnostics` |
| `tunnel-digger-integration-research` | No current project-local skill; overlaps blocked Phase 8 mining research. | Mark dormant/future-only | Creating it now risks enabling Phase 8 before explicit approval. | No owner until explicit Phase 8 approval |
| `ie-style-minecraft-assets` | Existing `minecraft-asset-autogenerator` and `minecraft-multiblock-model-master`. | Reject as duplicate | Existing asset/model skills cover visual planning; no IPM-specific asset skill is needed now. | Existing visual/model skills |
| `redstone-basics` | Installed global redstone skill. | Keep existing unchanged | It owns in-game redstone examples only and must not cover IPM Java architecture. | `redstone-basics` |
| `skill-creator` | Installed/system skill creation skill. | Keep existing unchanged | It owns creating, revising, and reviewing skills only. | `skill-creator` |

## Final Non-Overlapping Ownership

| Work type | Final owner | Routing boundary |
| --- | --- | --- |
| Creating or revising Codex skills | `skill-creator` | Only skill creation/revision/review; no gameplay authority. |
| GameTest/dev diagnostics and automated safety checks | `neoforge-gametest-diagnostics` | Dev/test-only validation, persistence checks, and safety harnesses. |
| True IE formed Industrial Driller Garage migration | `ie-formed-multiblock-dev` | Formed state, master/dummy BE, controller-relative routing, disassembly, and Gate checks only. |
| Future Tunnel Digger mining research | Dormant/future-only | Do not create or use a skill until Phase 8 is explicitly approved. |
| IPM visual assets and IE-style visual planning | Existing visual/model skills | Use only for explicit asset/model tasks; never copy IE/IP/IM/IA assets. |
| Docs/manual/changelog/routing | `doc-maintenance` | Documentation and release text only; no Java gameplay ownership. |
| General NeoForge/IPM code | `minecraft-mod-dev` | Default general code work unless a narrower specialist applies. |
| In-game redstone examples | `redstone-basics` | Player-facing redstone examples only. |

## Hard Rule Propagation

All IPM-relevant skills inherit these rules from `AGENTS.md`:

- Skills never override project hard rules.
- Skills never override phase order.
- Skills never authorize Phase 8.
- Skills never authorize Tunnel Digger mining changes.
- Skills never authorize mixins.
- Skills never authorize native Tunnel Digger GUI modification.
- Skills never authorize Survey Console final output reactivation.

## Review Result

- Skills created: none.
- Skills updated: `ie-formed-multiblock-dev`, `neoforge-gametest-diagnostics`.
- Skills rejected as duplicate: `ie-style-minecraft-assets`.
- Skills marked dormant/future-only: `tunnel-digger-integration-research`.
- Gameplay code changed: no.
- Phase 8 started: no.
