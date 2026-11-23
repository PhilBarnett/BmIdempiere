-- Apply to PostgreSQL with the normal iDempiere schema owner before deploying
-- the associated WooCommerce bundle. Do not backfill from Woo status alone.
BEGIN;
CREATE TABLE adempiere.bm_woo_import_receipt (
    zz_woocommerce_id numeric(10,0) NOT NULL REFERENCES adempiere.zz_woocommerce(zz_woocommerce_id),
    woo_order_id integer NOT NULL CHECK (woo_order_id > 0),
    c_order_id numeric(10,0) REFERENCES adempiere.c_order(c_order_id) ON DELETE SET NULL,
    acknowledged_at timestamp with time zone,
    PRIMARY KEY (zz_woocommerce_id, woo_order_id)
);
CREATE INDEX bm_woo_import_receipt_pending
    ON adempiere.bm_woo_import_receipt (zz_woocommerce_id, woo_order_id)
    WHERE acknowledged_at IS NULL AND c_order_id IS NOT NULL;
COMMIT;
