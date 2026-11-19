-- Run on TestERP first, with dictionary maintenance access.
-- Changes only the existing WooCommerce component lookup rule, not product/BOM data.
-- The stored old code below is the rollback value observed on TestERP on 20 November 2026.
BEGIN;
DO $migration$
DECLARE
    current_code text;
    old_code text := $old$m_product.m_product_Id IN (SELECT pbl.m_product_id FROM PP_Product_BOMLine pbl JOIN PP_Product_BOM ppb ON ppb.PP_Product_BOM_id = pbl.PP_Product_BOM_id AND ppb.bomtype = 'A' AND ppb.m_product_id=@M_Product_ID@ JOIN m_product mp ON mp.m_product_id = pbl.m_product_id JOIN bld_product_parttype bld ON bld.m_parttype_id = mp.m_parttype_id WHERE bld.bld_product_parttype_id = @BLD_Product_PartType_ID@ AND pbl.isactive = 'Y')$old$;
    new_code text := $new$M_Product.M_Product_ID IN (SELECT DISTINCT component.M_Product_ID FROM BLD_Product_PartType selector JOIN M_Product owner ON owner.M_Product_ID=selector.M_Product_ID JOIN M_Product component ON component.M_PartType_ID=selector.M_PartType_ID JOIN PP_Product_BOMLine componentLine ON componentLine.M_Product_ID=component.M_Product_ID JOIN PP_Product_BOM componentBom ON componentBom.PP_Product_BOM_ID=componentLine.PP_Product_BOM_ID WHERE selector.M_Product_ID=@M_Product_ID@ AND selector.BLD_Product_PartType_ID=@BLD_Product_PartType_ID@ AND owner.AD_Client_ID=@#AD_Client_ID@ AND selector.AD_Client_ID=owner.AD_Client_ID AND selector.IsActive='Y' AND owner.IsActive='Y' AND component.IsActive='Y' AND componentLine.IsActive='Y' AND componentBom.IsActive='Y' AND componentBom.BOMType='A' AND component.AD_Client_ID IN (0,owner.AD_Client_ID) AND componentLine.AD_Client_ID=owner.AD_Client_ID AND componentBom.AD_Client_ID=owner.AD_Client_ID AND ((COALESCE(selector.is_select_other_bld_parttype,'N')='N' AND componentBom.M_Product_ID=owner.M_Product_ID) OR (selector.is_select_other_bld_parttype='Y' AND EXISTS (SELECT 1 FROM PP_Product_BOM parentBom JOIN PP_Product_BOMLine parentLine ON parentLine.PP_Product_BOM_ID=parentBom.PP_Product_BOM_ID JOIN M_Product source ON source.M_Product_ID=parentLine.M_Product_ID WHERE parentBom.M_Product_ID=owner.M_Product_ID AND parentBom.IsActive='Y' AND parentBom.BOMType='A' AND parentLine.IsActive='Y' AND parentBom.AD_Client_ID=owner.AD_Client_ID AND parentLine.AD_Client_ID=owner.AD_Client_ID AND source.AD_Client_ID IN (0,owner.AD_Client_ID) AND source.IsActive='Y' AND source.M_PartType_ID=selector.Otherbom_M_Parttype_ID AND source.M_Product_ID=componentBom.M_Product_ID))))$new$;
BEGIN
    SELECT Code INTO current_code FROM AD_Val_Rule
    WHERE AD_Val_Rule_UU='cda281b4-c729-4900-b386-7c9ebe2a88ca'
      AND Name='ZZ_Woocommerce_Map_MProductOnBom' AND Type='S'
    FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Expected WooCommerce component validation rule not found';
    END IF;
    IF current_code = new_code THEN
        RETURN;
    END IF;
    IF lower(regexp_replace(current_code, '\s+', '', 'g'))
        IS DISTINCT FROM lower(regexp_replace(old_code, '\s+', '', 'g')) THEN
        RAISE EXCEPTION 'Component rule has changed; review before applying this migration';
    END IF;
    UPDATE AD_Val_Rule SET Code=new_code, Updated=now()
    WHERE AD_Val_Rule_UU='cda281b4-c729-4900-b386-7c9ebe2a88ca';
END;
$migration$;
COMMIT;
-- Reset the application cache and reopen the mapping window after this update.
