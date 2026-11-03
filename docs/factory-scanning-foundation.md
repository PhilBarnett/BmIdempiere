# Factory scanning foundation

## Scope of this branch

Correct the ResultSet overload of BLDMtomModelFactory so production documents
use MBLDMtomProduction, consistently with the ID overload and getClass.
Add a dependency-free MTM barcode parser and use it in the existing lookup form.
This form still only opens a record; it does not record a production event.

The parser retains the existing 01/document, 02/item and 03/cut routing, accepts
surrounding scanner whitespace, validates ASCII digits and positive integer IDs,
and supports IDs beyond the old seven-digit limit. Leading zeroes in the ID
are rejected; MtmUtils generates unpadded integer IDs. Database existence,
business scope and user permissions must be checked separately by any future
scan endpoint. A parsed barcode is not authorisation.

Run `sh scripts/test-mtm-barcode.sh` with Java 17 including jdk.compiler.
These tests exercise parsing only. They do not certify the full OSGi build,
the UI integration or model construction against a running ERP.

## Verified baseline on 2 November 2026

Production bundle 426 was Active at version 12.0.4.202601021647; its manifest
aligned with the repository's 12.0.4.qualifier. Exact binary/source equivalence
was not established. Scoped database counts were 3,378 production documents,
25,639 items, 66,823 cuts, zero scans and zero scanpoints for client and org
1000000. Shared-org records, scanpoint types and production-log records remain
outside the current reader's verified scope.

The reviewed production completeIt path processes stock transactions and sets
item iscomplete without setting the physical completion timestamp. The scan model
has no custom processing; the reviewed registered event handler does not
subscribe to scans and the process factory has no scan processor. These findings
do not rule out logic in other installed bundles or dictionary configuration.

## Next implementation boundary

Use a dedicated authenticated ERP operation to receive station events; do not
give station clients the database reader's credentials or direct write access.
Validate client/org, operator, configured station, record existence and eligible
production status server-side. Record event UUID, immutable payload, item/cut
reference, station/device, operation, UTC occurrence time and server receipt time.
Commit the event and its acceptance atomically. A retry with the same UUID and
payload returns the original acceptance; the same UUID with a changed payload
is rejected. Separately flag accidental repeated scans with distinct UUIDs;
explicit rework must remain distinguishable from duplicates.

The native Debian station needs a durable local outbox and visible queued,
accepted and rejected states. Offline events are provisional until ERP validation.
Reject or flag clock anomalies; use occurrence time only when trustworthy.
Production events must not invoke production document completion or stock
transactions again. Stocktake and movements need their own explicit workflows.

Start with cutting completion per blind, then welding, assembly and packing.
Confirm item versus cut labels at the physical station before a pilot. Completion
scans measure output; operation duration needs explicit start/finish events.
Dashboard totals count accepted distinct item completions, exclude duplicates,
and report rework separately, grouped by Australia/Sydney production date.

Before rollout: complete the scoped schema/dictionary audit, review other bundle
handlers, design the migration and endpoint, test duplicate/retry/offline and
cross-tenant cases in a non-production ERP, and verify no stock side effects.
The database's previously observed unauthorised authenticated traccar session
also needs containment and investigation before adding production write access.

No migration, station endpoint, dashboard, scan writer or deployment is included
in this branch. Do not install a replacement production bundle solely on the
strength of the standalone parser tests. The existing repository mixes PDE and
Maven configuration and requires a verified iDempiere 12 build/test environment.
