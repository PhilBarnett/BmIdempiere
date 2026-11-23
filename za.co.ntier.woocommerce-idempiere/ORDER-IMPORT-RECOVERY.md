# WooCommerce order import recovery

This change requires the PostgreSQL script `migration/postgresql/20261123_woo_import_receipts.sql` before installing the bundle. The table exists per ERP database and receipts are scoped to the configured Woo store and Woo order ID. Keep each Woo Defaults record bound to the same store; do not repoint an existing Defaults record to another store with pending receipts.

The ERP order, its dependent records, and its import receipt are committed together. A committed receipt is the only evidence permitting automatic Woo acknowledgement. A legacy/manual order matched by Document No. or PO reference is deliberately blocked for reconciliation if it has no receipt. An order deleted in ERP leaves a tombstone, not permission to import it again.

A separate acknowledgement transaction only updates Woo after a committed receipt exists. It verifies the order's current status, changes Processing to Completed if necessary, then updates and verifies the sync flag. Outstanding receipts are checked independently of Woo's Processing list, including after a lost response. Do not bulk-reset Completed orders or backfill receipts from Woo status. Unexpected Woo status, duplicate sync metadata, or a legacy/manual duplicate requires operator review. Do not use live customers for testing.

## TestERP release gate

1. Take a TestERP snapshot, point Woo defaults at staging, install the migration and build/deploy this OSGi bundle against the actual iDempiere 12 target. Disable fulfilment and customer mail for synthetic tests.
2. Run a staged good → bad → good Processing batch. Use one unmapped regular line returning false and, in a second run, an unmapped or malformed curtain swatch payload that throws. Assert exactly the two good orders and receipts committed per run, no ERP header/lines/payment/installation/receipt for either bad order, both good Woo statuses Completed and sync flags yes, both bad orders still Processing.
3. Fail a shipping line save and simulate failed/unconfirmed ERP commit. Assert no Woo writes, no committed partial ERP order, and a subsequent healthy order still imports when the connection remains healthy. Confirm complete DB outage stops rather than silently skipping all work.
4. After ERP commit, fail or lose the status update response, then separately fail or lose the sync metadata response. Run again. Assert one ERP order and receipt, no duplicate remote status change or payment, and eventual Completed/yes with the receipt acknowledged. Test two concurrent importer runs for the same order.
5. Verify targeted import, QA drafts with no Woo writes, manual/legacy duplicate refusal, and no mutation for hold, cancelled, refunded, or wrong-ID Woo responses. For an acknowledgement review use the positive WooCommerce Order ID parameter, not manual re-creation.
6. Run curtain-only, mixed curtain/outdoor, and conventional paid fixtures. Confirm all selected ERP fabric/colour components, shipping, payment and installation records as applicable. The six-range catalogue mapping verification is separate from this importer change.

The standalone Java test in `test/za/co/ntier/processes/WooOrderAcknowledgementTest.java` covers Woo acknowledgement ordering, error recovery, idempotency and refusal cases without an ERP dependency. It does not replace the database/iDempiere test above.
