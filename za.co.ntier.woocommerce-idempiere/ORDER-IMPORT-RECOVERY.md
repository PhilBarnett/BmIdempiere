# WooCommerce import: order isolation and recovery

## Scope and precedents

Preserve the Processing batch and exact-ID entry points, the existing product and payment behaviour, and the named QA draft guards. This changes the rollback unit from the batch to an order, treats a shipping line save failure as a failed order, and prevents Woo from moving ahead of ERP. No new Dictionary objects, tables, roles, service registrations or deployment toolchain are required. This is independent of curtain sample catalogue configuration and does not replay historic orders.

Blindmotion's [existing process](https://github.com/PhilBarnett/BmIdempiere/blob/27e4909db7394d291314690087bd181599afa931/za.co.ntier.woocommerce-idempiere/src/za/co/ntier/processes/WooCommerce.java) and upstream iDempiere 12 [ImportProduct](https://github.com/idempiere/idempiere/blob/release-12/org.adempiere.base.process/src/org/compiere/process/ImportProduct.java#L524-L652) are the starting points. Upstream rolls back failed rows, records an error and continues; it doesn't call Woo. We use private named transactions rather than committing the process/caller's transaction. The process's configured active Woo Defaults record is the mutex for jobs using that store. Every dependent ERP model gets the same transaction name. Stage the rollout with no old copy/scheduler running: older binaries don't take the lock. Verify there is one active Defaults record for the client and it points to the intended store/organisation. The lock is not held during a Woo request.

## Failure and replay

- Known order errors: roll back that one order; log its Woo ID and continue in a batch. Infrastructure or uncertain rollback: stop; do not acknowledge any unconfirmed order. Earlier committed orders stay committed.
- Only this invocation's confirmed ERP commit followed by a fresh scoped reload of its exact new sales order ID/document and nonzero lines allows Woo acknowledgement. Readback is a persistence gate, not proof that every mapped component is correct. Woo is reread before mutation; if already on hold, cancelled or otherwise unexpected, do not overwrite it. A competing status change between the read and write remains a Woo API race; this is not a compare-and-set API.
- Woo completion can reset its sync flag. Read status back before updating the flag, then read the flag back separately. After an ambiguous write response, reread authoritative state instead of resending the mutation.
- No automatic replay after process interruption or a still-unconfirmed acknowledgement. A matching existing ERP document number or PO reference blocks re-import; neither is proof of a complete import. If interrupted after ERP may have committed, an operator must compare exact Woo ID/store with ERP client, organisation, document/reference, actual product, options, charges, payment/installation and current Woo status. Correct the affected side only after verifying the records, approval and customer-email effects. Do not rerun or bulk-reset orders to repair status. For Completed orders with flag issues, use an approved read-only reconciliation to locate them, because the importer continues to list Processing only.
- A future unattended retry would need an approved immutable store/order receipt persisted with the ERP transaction, visibility for operations, a retention policy, and either a Dictionary-managed/2Pack install or a justified exception. Do not repurpose PO reference or description for this release.

## Release gate (iDempiere 12)

1. Confirm the exact TestERP target, PostgreSQL, JDK 17 and OSGi/PDE setup and build the exact candidate commit. No schema change. Keep synthetic Woo credentials/endpoint only in the test configuration. Silence fulfilment, customer mail and other external effects.
2. In real TestERP, import synthetic good → generic false → good and separately good → typed mapping exception → good. Assert that the two good orders commit and acknowledge, that each rejected order leaves no ERP header, lines, installation, payment or Woo status change, and that targeted QA retains its draft/no-Woo behaviour. Include zero-line, shipping-save and commit/rollback failure. Check database and Woo state using a fresh context.
3. Verify paid MTM line components, options, totals, charges, discount, installation and POS bookkeeping; some pre-existing MTM mapping branches have legacy unchecked saves and are outside this transaction-control rewrite. Verify each swatch in a separate mixed curtain/outdoor fixture; the narrow two-swatch staging safeguard is not a mixed-order validation.
4. Run concurrent batch/exact-ID passes for one store; assert one ERP order, no Woo write from the duplicate invocation. Interrupt after commit and before status, and after status and before metadata. Run again and assert fail-closed detection, no duplicate creation and useful reconciliation logging. Simulate rejected and lost Woo status/metadata responses, and operator On hold.
5. Confirm clean bundle install/upgrade, existing process/roles, expected version/logs, and service resolution. Before promotion, agree operator reconciliation and approval. If rollback is needed, stop the importer before reverting the bundle and reconcile already committed ERP/Woo records; reverting the bundle does not undo writes.

The standalone Java acknowledgement test checks ordering and Woo response handling, not database transactions. To run it in a development checkout from the bundle directory:
```sh
out=$(mktemp -d)
javac -d "$out" src/za/co/ntier/processes/WooOrderAcknowledgement.java \
  test/za/co/ntier/processes/WooOrderAcknowledgementTest.java
java -cp "$out" za.co.ntier.processes.WooOrderAcknowledgementTest
```
