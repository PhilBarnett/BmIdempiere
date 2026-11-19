package za.co.ntier.model;

import java.util.List;
import java.util.Properties;

import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.util.Env;

/** Resolve components using the product selector's configured BOM source. */
public final class WooCommerceComponentLookup {
    private WooCommerceComponentLookup() {
    }

    // Parameter order: parent product, its selectable part row, current tenant.
    static final String PRODUCT_IDS_SQL =
            "SELECT DISTINCT component.M_Product_ID"
            + " FROM BLD_Product_PartType selector"
            + " JOIN M_Product owner ON owner.M_Product_ID=selector.M_Product_ID"
            + " JOIN M_Product component ON component.M_PartType_ID=selector.M_PartType_ID"
            + " JOIN PP_Product_BOMLine componentLine ON componentLine.M_Product_ID=component.M_Product_ID"
            + " JOIN PP_Product_BOM componentBom ON componentBom.PP_Product_BOM_ID=componentLine.PP_Product_BOM_ID"
            + " WHERE selector.M_Product_ID=? AND selector.BLD_Product_PartType_ID=?"
            + " AND owner.AD_Client_ID=? AND selector.AD_Client_ID=owner.AD_Client_ID"
            + " AND selector.IsActive='Y' AND owner.IsActive='Y'"
            + " AND component.IsActive='Y' AND componentLine.IsActive='Y'"
            + " AND componentBom.IsActive='Y' AND componentBom.BOMType='A'"
            + " AND component.AD_Client_ID IN (0,owner.AD_Client_ID)"
            + " AND componentLine.AD_Client_ID=owner.AD_Client_ID"
            + " AND componentBom.AD_Client_ID=owner.AD_Client_ID"
            + " AND ((COALESCE(selector.is_select_other_bld_parttype,'N')='N'"
            + " AND componentBom.M_Product_ID=owner.M_Product_ID)"
            + " OR (selector.is_select_other_bld_parttype='Y'"
            + " AND EXISTS (SELECT 1 FROM PP_Product_BOM parentBom"
            + " JOIN PP_Product_BOMLine parentLine ON parentLine.PP_Product_BOM_ID=parentBom.PP_Product_BOM_ID"
            + " JOIN M_Product source ON source.M_Product_ID=parentLine.M_Product_ID"
            + " WHERE parentBom.M_Product_ID=owner.M_Product_ID"
            + " AND parentBom.IsActive='Y' AND parentBom.BOMType='A' AND parentLine.IsActive='Y'"
            + " AND parentBom.AD_Client_ID=owner.AD_Client_ID AND parentLine.AD_Client_ID=owner.AD_Client_ID"
            + " AND source.AD_Client_ID IN (0,owner.AD_Client_ID) AND source.IsActive='Y'"
            + " AND source.M_PartType_ID=selector.Otherbom_M_Parttype_ID"
            + " AND source.M_Product_ID=componentBom.M_Product_ID)))";

    public static List<MProduct> products(Properties ctx, int parentProductId,
            int selectablePartId, String trxName) {
        return new Query(ctx, MProduct.Table_Name,
                "M_Product_ID IN (" + PRODUCT_IDS_SQL + ")", trxName)
                .setParameters(parentProductId, selectablePartId, Env.getAD_Client_ID(ctx))
                .setOrderBy("Name,M_Product_ID").list();
    }

    public static boolean contains(Properties ctx, int parentProductId, int selectablePartId,
            int componentProductId, String trxName) {
        return products(ctx, parentProductId, selectablePartId, trxName).stream()
                .anyMatch(product -> product.get_ID() == componentProductId);
    }
}

