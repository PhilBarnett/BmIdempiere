# Woo product field setup: collection swatches

The product-field setup process reads the linked Woo product's WAPF fields and creates iDempiere mappings; it does not place an order or edit Woo. Single-choice manufactured curtains continue to create ordinary mappings. The existing roller/outdoor sample branch remains in place. For a swatch field associated with a collection selector, a `multi-image-swatch` field needs an empty-value routing parent and exact child keys, not ordinary per-colour rows. Field IDs come from the product response, never a table of hardcoded IDs.

Before creating a parent or child, the setup checks the entire field. Source choices must be distinct and survive the order format's comma split unchanged. Every choice must resolve uniquely to a colour attribute value on one active Fabric BOM component/slot. Destination matching follows the existing name matcher; the saved key is always the exact source label. Existing active rows must have the expected kind, label and current destinations. A conflict or missing choice leaves that field unchanged and records why. Operator-authored inactive rows and active wrong-kind rows are not converted automatically. A proper null-valued parent takes priority over ordinary rows, so creating it without a safe set of children could make currently working orders fail. The setup still handles other fields normally. For sample orders, the reader binds each accepted exact child value under its own mapping record ID so collection validation can check individual swatches.

## Compatibility and operations

- No new table, Dictionary entries, pack, dependency or toolchain. Follow the existing setup process access and model activation workflow, with one caller-owned transaction (no manual commit). Setup runs on the same product serialize through a product lock acquired after reading Woo. This is not a general lock for ad hoc manual mapping edits; avoid editing this product's field mappings concurrently.
- Existing correct mappings should remain unchanged on repeated setup. For an active ordinary row, duplicate, mismatched active destination or intentional inactive map, export/review the affected product's field records and BOM, then coordinate the exact corrective setup/review on TestERP. Do not delete, reset or rewrite live mappings from this draft. The `Product` selector routing remains outside scope; confirm new Product choices resolve with the existing configuration before claiming end-to-end readiness.
- Precedent: Blindmotion's existing `reviewFabric` and `saveComponent` in [WooCommerceProductSetup](https://github.com/PhilBarnett/BmIdempiere/blob/27e4909db7394d291314690087bd181599afa931/za.co.ntier.woocommerce-idempiere/src/za/co/ntier/processes/WooCommerceProductSetup.java); iDempiere 12 [Query](https://github.com/idempiere/idempiere/blob/release-12/org.adempiere.base/src/org/compiere/model/Query.java#L201-L267) and [PO.saveEx](https://github.com/idempiere/idempiere/blob/release-12/org.adempiere.base/src/org/compiere/model/PO.java#L2697-L2723).

## TestERP release gate

1. Build the exact commit against the actual iDempiere 12/JDK 17 Eclipse PDE target, deploy only to the isolated TestERP database with a synthetic or approved staging Woo fixture. Confirm process/service registration and ordinary-role scope. A fixture Woo product with only blind selectors cannot prove curtain mapping.
2. Run setup on Fabric Sample with all six declared collection fields and two distinct colours in at least one field, then reload parent and child rows with a fresh context. Check exact Woo source values, active Fabric slot, product, attribute and attribute-value IDs. Rerun and verify no duplicate or unintended changes. Repeat after restart. Exercise missing BOM/attribute/choice, ambiguous match, wrong-kind row and concurrent setup. Compare untouched roller/outdoor mappings and ordinary manufactured curtain maps.
3. Verify the existing Product selector choices and both collection selectors on the synthetic fixture. Import a separate mixed/outdoor or mixed-curtain order with same-named colours in different fields, then read back every sample line's exact fabric and colour. Verify selector validation with two colours, single-choice manufactured type and a missing exact mapping that does not silently omit a swatch. Do not widen the named two-swatch smoke test or use a live customer's order.
4. If bundle rollback is needed, stop the affected setups/imports and restore the preceding bundle. That does not undo mappings already committed by setup. Reconcile and restore only verified affected mapping rows through the agreed rollout/backup process.

Standalone Java test for exact labels (not ERP integration), from the Woo bundle directory:
```sh
out=$(mktemp -d)
javac -d "$out" src/za/co/ntier/processes/WooCollectionSwatches.java \
  test/za/co/ntier/processes/WooCollectionSwatchesTest.java
java -cp "$out" za.co.ntier.processes.WooCollectionSwatchesTest
```
