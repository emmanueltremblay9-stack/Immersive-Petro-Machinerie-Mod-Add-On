# Industrial Driller Garage Layout Contract

This document supersedes both the early 7x3x3 Industrial Docking Platform and
the first 9x9x9 garage draft. The active design is now a larger IE-style service
garage with room for a Tunnel Digger bay, an entry ramp, two real rear 3x3x3
tank volumes, and an added hempcrete foundation layer.

This document is the active structure contract for validation, manual preview,
Immersive Petroleum Projector projection, Tunnel Digger detection bounds, Survey
Console physical lookup, and fuel/lubricant port lookup. It does not define
mining changes, hard movement control, repair, upgrade activation, final scan
output, or Tunnel Digger inventory/fuel modification.

The Immersive Petroleum Projector is supported as a preview and creative
real-block placement helper. It does not replace Docking Controller validation
and it does not clear unrelated blocks from the hollow bay. If the Projector has
already locked a projection position, sneak-use clears that projection instead
of direct-building the structure; direct-build testing must start from projection
mode with no locked position. After placement, validate with the Docking
Controller and follow the exact local/world coordinate reported by the chat
message for any missing block. In normal builds, Engineer's Hammer validation
only validates the real-block garage and does not replace the structure with
the experimental formed shell.

## Structure Contract

- Size: `11x13x10` including the lower hempcrete foundation layer.
- Above-grade garage frame: `11x13x9`.
- Width: 11 blocks, `x = -5..5`.
- Height: 10 blocks, `y = -2..7`.
- Length: 13 blocks, `z = -12..0`.
- Controller origin: `docking_controller` at `(0, 0, 0)`.
- Front: negative Z. This is where the Tunnel Digger enters.
- Back: `z = 0`. This is the rear control and tank wall.
- Lower hempcrete foundation: `y = -2`.
- Anchor/foundation layer: `y = -1`.
- Raised service floor: `y = 0`.
- Top armature: primarily `y = 6..7`.
- Validation remains controller-centered and orientation-aware.

The Tunnel Digger is an entity/vehicle, not part of the multiblock volume. The
garage is a sparse industrial service structure, not a solid cube.

## Design Intent

The garage should read closer to an Immersive Engineering multiblock:

- broad steel scaffolding frame;
- side support pillars;
- straight steel-scaffolding border pillars that are not interrupted by tank
  blocks or Heavy Engineering anchors;
- open center bay;
- hempcrete foundation slab and filled base gaps;
- stronger rear service wall with visible tank interfaces, an open center
  service window, and a pipe header;
- stepped service ramp into the bay;
- raised guide lane/service floor;
- two rear 3x3x3 sheetmetal tank bodies;
- top armature and overhead crane/service arm;
- fluid pipe and pump runs from rear tanks into the service area.

IPM custom blocks are limited to IPM-specific function points.

## Coordinate System

Coordinates are local to the Docking Controller:

- `x < 0`: left side when looking from the controller toward the front.
- `x > 0`: right side.
- `z < 0`: forward into the garage and ramp.
- `z = 0`: rear service wall.
- `x = -5` and `x = 5`: outer side border support columns.

World conversion is handled by the validator using the controller's horizontal
facing.

## Hollow Center And Entry Clearance

The main bay clearance is validated as air:

- `x = -3..3`
- `y = 1..5`
- `z = -9..-3`

The ramp entry clearance is also validated as air:

- `x = -2..2`
- `y = 1..4`
- `z = -12..-10`

The only intentional non-air exceptions in the main bay are high service pipe
endpoints:

- fuel arm: `(-3, 4, -7)` and `(-2, 4, -7)`;
- lubricant arm: `(3, 4, -7)` and `(2, 4, -7)`.

## Ramp And Raised Service Floor

The ramp is a validated stepped IE-style entry ramp:

- lower hempcrete foundation: `x = -5..5`, `y = -2`, `z = -12..0`;
- lower approach deck: `x = -2..2`, `y = -1`, `z = -12..-11`, steel scaffolding;
- raised entry: `x = -2..2`, `y = 0`, `z = -10..-9`;
- guide lane: `x = -1..1`, `y = 0`, `z = -10..-1`, IPM Locking Rail;
- service deck edges: `x = -2` and `x = 2`, `y = 0`, `z = -10..-1`, steel scaffolding;
- ramp guard rails: `x = -3` and `x = 3`, `y = 0`, `z = -12..-9`, steel fence.

The approach space directly above the lower ramp deck remains clear:

- `x = -2..2`
- `y = 0`
- `z = -12..-11`

Do not fill this space; it is the approach clearance before the raised entry
blocks at `z = -10..-9`.

The three base layers are filled with IE hempcrete wherever there is no required
functional/structural block and no ramp approach clearance:

- lower foundation fill: `y = -2`;
- underfloor/foundation fill around anchors and ramp base: `y = -1`;
- raised service-floor gap fill around rails, ports, walkways, and service
  blocks: `y = 0`.

Side service walkways run along:

- left: `x = -5` and `x = -4`, `y = 0`, `z = -12..0`;
- right: `x = 4` and `x = 5`, `y = 0`, `z = -12..0`;
- functional blocks replace walkway blocks where required.

## Rear Service Wall

The rear wall is intentionally stronger than a flat scaffold grid. It should
read as an Immersive Engineering service wall with two tank masses, accessible
IPM control blocks, structural corner anchors, and visible upper pipe headers.

Validated functional positions:

| Local position | Role | Block |
| --- | --- | --- |
| `(-4, 0, 0)` | Fuel tank interface | IPM Fuel Port |
| `(-2, 0, 0)` | Repair station | IPM Repair Bay |
| `(-1, 0, 0)` | Service block | IE Light Engineering Block |
| `(0, 0, 0)` | Controller | IPM Docking Controller |
| `(0, 1, 0)` | Survey station | IPM Survey Console |
| `(1, 0, 0)` | Control block | IE Redstone Engineering Block |
| `(2, 0, 0)` | Upgrade station | IPM Upgrade Bay |
| `(4, 0, 0)` | Lubricant tank interface | IPM Lubricant Port |
| `(5, 0, -1)` | Side output interface | IPM Output Port |

Additional rear wall service details:

- structural tank footings: `(-3, 0, 0)` and `(3, 0, 0)`, IE Heavy
  Engineering Blocks;
- outer rear border pillars at `(-5, y, 0)` and `(5, y, 0)` remain straight IE
  steel scaffolding from `y = 0..7`;
- center rear service window above the controller remains open except for the
  Survey Console and pipe header;
- rear pipe header: `x = -4..4`, `y = 4`, `z = 0`;
- pump/header nodes at `(-4, 4, 0)` and `(4, 4, 0)`.

## Rear 3x3x3 Tanks

Fuel tank:

- bounds: `x = -4..-2`, `y = 1..3`, `z = -2..0`;
- body block: IE steel sheetmetal;
- interface: IPM Fuel Port at `(-4, 0, 0)`;
- pump outlet: IE fluid pump at `(-4, 4, -1)`.

Lubricant tank:

- bounds: `x = 2..4`, `y = 1..3`, `z = -2..0`;
- body block: IE steel sheetmetal;
- interface: IPM Lubricant Port at `(4, 0, 0)`;
- pump outlet: IE fluid pump at `(4, 4, -1)`.

These are real 3x3x3 structure volumes, not flat facade tanks. Each tank is
set one block inward from the outer side border so the straight steel
scaffolding border pillars remain visible.

## Fuel And Lubricant Arms

Fuel service arm:

- pump: `(-4, 4, -1)`;
- rear header node: `(-4, 4, 0)`;
- side pipe run: `(-4, 4, -2..-9)`;
- inward service endpoints: `(-3, 4, -7)` and `(-2, 4, -7)`.

Lubricant service arm:

- pump: `(4, 4, -1)`;
- rear header node: `(4, 4, 0)`;
- side pipe run: `(4, 4, -2..-9)`;
- inward service endpoints: `(3, 4, -7)` and `(2, 4, -7)`.

These arms are visual/validated garage service lines only. They do not transfer
fluid into the Tunnel Digger in the current phase.

## Survey Console Containment

The Survey Console remains part of the physical rear service wall, but final
scan output is contained during the Phase 7.8A0 safety gate. Interacting with a
valid linked Survey Console reports that the Core Sampling Scan flow is under
review and will require the Garage-installed IPM upgrade flow later.

In this contained state, the Survey Console does not:

- generate IE Core Sample items;
- generate Immersive Petroleum survey result items;
- drain diesel;
- run final scan targeting rules.

The existing survey service code remains available for later implementation
planning, but it is not called by the active Survey Console interaction.

## Side Supports And Top Armature

Primary support pillars:

- left/right: `x = -5` and `x = 5`;
- anchor z positions: `z = -12`, `-8`, `-4`, `0`;
- steel scaffolding support bases at `y = -1`;
- hempcrete footing below all base gaps and below the steel scaffolding bases
  at `y = -2`;
- steel scaffolding continues up to `y = 7`, except where rear tank bodies
  would only occupy non-border positions.
- side border pillars at `x = -5` and `x = 5`, `z = -12`, `-8`, `-4`, and `0`
  remain straight IE steel scaffolding from `y = -1..7`.

Top armature:

- side top rails: `x = -5` and `x = 5`, `y = 7`, `z = -12..0`;
- crossbeams: `z = -12`, `-8`, `-4`, `0`, `y = 7`, `x = -5..5`;
- lower crane beam: `x = 0`, `y = 6`, `z = -10..-2`;
- crane trolley crossbar: `x = -2..2`, `y = 6`, `z = -6`.
- inner side crane/service rails: `x = -4` and `x = 4`, `y = 6`,
  `z = -10..-2`.

The side profile should show a readable sequence from rear to front: rear tank
mass, forward service pipe arm, tall support pillars, upper crane/service rail,
and an open middle bay. The garage must not become a fully enclosed scaffold
cage.

## IE Block IDs Used By Validation

The validator uses these verified Immersive Engineering block ids:

- `immersiveengineering:steel_scaffolding_standard`
- `immersiveengineering:fluid_pipe`
- `immersiveengineering:fluid_pump`
- `immersiveengineering:heavy_engineering`
- `immersiveengineering:light_engineering`
- `immersiveengineering:rs_engineering`
- `immersiveengineering:sheetmetal_steel`
- `immersiveengineering:steel_fence`
- `immersiveengineering:hempcrete`

## IPM Custom Blocks Used By Validation

- Docking Controller: `(0, 0, 0)`
- Survey Console: `(0, 1, 0)`
- Fuel Port: `(-4, 0, 0)`
- Lubricant Port: `(4, 0, 0)`
- Repair Bay: `(-2, 0, 0)`
- Upgrade Bay: `(2, 0, 0)`
- Output Port: `(5, 0, -1)`
- Locking Rail: center guide lane, `x = -1..1`, `y = 0`, `z = -10..-1`

## Required Symbols

- `DC`: IPM Docking Controller
- `SC`: IPM Survey Console
- `RB`: IPM Repair Bay
- `UB`: IPM Upgrade Bay
- `OP`: IPM Output Port
- `FP`: IPM Fuel Port
- `LP`: IPM Lubricant Port
- `LR`: IPM Locking Rail
- `SS`: IE Steel Scaffolding
- `HE`: IE Heavy Engineering Block
- `LE`: IE Light Engineering Block
- `RE`: IE Redstone Engineering Block
- `SM`: IE Steel Sheetmetal
- `PI`: IE Fluid Pipe
- `PU`: IE Fluid Pump
- `SF`: IE Steel Fence
- `HC`: IE Hempcrete

## Phase Boundaries

This redesign must not implement:

- Tunnel Digger behavior changes;
- hard movement control;
- item or fluid transfer into the Tunnel Digger;
- fuel or lubricant consumption;
- repair logic;
- upgrade swapping;
- survey behavior changes;
- digging-size changes;
- mixins;
- custom rendering.

The IE-style formed multiblock path remains an experimental spike and is
disabled by default for normal runtime testing. Engineer's Hammer interaction on
the Docking Controller is validation-only in normal mode: it marks the real
garage valid and preserves the real Docking Controller block entity that owns
soft lock, dock fuel/lubricant tanks, and Garage upgrade inventory.

Experimental formed shell creation is gated behind the Java system property
`immersive_petro_machinery.enableFormedShellCreation=true`. Even when that
explicit test gate is enabled, formation must reject if the Docking Controller
has any protected state:

- locked Tunnel Digger UUID;
- fuel tank contents;
- lubricant tank contents;
- any Garage upgrade inventory item.

The formed path must stay test-only until the controller GUI, fluid storage,
survey, soft lock, and Tunnel Digger detection have been migrated or explicitly
preserved on the formed master.
