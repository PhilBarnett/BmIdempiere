package za.co.ntier.processes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;

import au.blindmot.model.MBLDProductPartType;
import za.co.ntier.model.MzzWoocommerceMap;
import za.co.ntier.model.MzzWoocommerceMapLine;
import za.co.ntier.model.X_zz_woocommerce;

public class WooCommerceProductSetup extends SvrProcess {
	private static final String ATTRIBUTE = "10000003";
	private static final String PRODUCT_ADD = "10000004";
	private static final String PRODUCT_ATTRIBUTE = "10000005";
	private static final String MULTI_SELECT_PARENT = "10000006";
	private static final String MULTI_SELECT_CHILD = "10000007";

	private int productId;
	private int proposed;
	private int needsReview;
	private int existing;

	@Override
	protected void prepare() {
	}

	@Override
	protected String doIt() throws Exception {
		productId = new MzzWoocommerceMap(getCtx(), getProcessInfo().getRecord_ID(),
				get_TrxName()).getM_Product_ID();
		if (productId <= 0) {
			throw new AdempiereUserError("Select a WooCommerce mapping on the product to review.");
		}
		X_zz_woocommerce defaults = new Query(getCtx(), X_zz_woocommerce.Table_Name,
				"IsActive='Y' AND AD_Client_ID=?", get_TrxName())
				.setParameters(Env.getAD_Client_ID(getCtx())).firstOnly();
		if (defaults == null) {
			throw new AdempiereUserError("An active WooCommerce Defaults record is required.");
		}
		int wooProductId = DB.getSQLValueEx(get_TrxName(),
				"SELECT woocommerce_key FROM zz_woocommerce_match"
				+ " WHERE zz_woocommerce_id=? AND m_product_id=? AND IsActive='Y'",
				defaults.get_ID(), productId);
		if (wooProductId <= 0) {
			throw new AdempiereUserError("No active WooCommerce product link for ERP product " + productId);
		}

		log.info("Fetching WooCommerce fields for product " + wooProductId);
		OAuthConfig config = new OAuthConfig((String) defaults.get_Value("url"),
				(String) defaults.get_Value("consumerkey"), (String) defaults.get_Value("consumersecret"));
		Map<?, ?> product = new WooCommerceAPI(config, ApiVersionType.V3)
				.get(EndpointBaseType.PRODUCTS.getValue(), wooProductId);
		List<Map<?, ?>> fields = getFields(product, wooProductId);
		log.info("Reviewing " + fields.size() + " WooCommerce fields for product " + wooProductId);

		MProduct erpProduct = new MProduct(getCtx(), productId, get_TrxName());
		List<MAttribute> attributes = attributesOf(erpProduct);
		List<PartSet> partSets = Arrays.stream(MBLDProductPartType.getMBLDProductPartTypes(
				getCtx(), productId, get_TrxName()))
				.map(type -> new PartSet(type, bomComponents(type.getM_PartTypeID())))
				.filter(set -> !set.products.isEmpty())
				.collect(Collectors.toList());
		for (Map<?, ?> field : fields) {
			reviewField(field, attributes, partSets);
		}
		addLog(productId, null, null, "New proposals: " + proposed + ", needing a destination: "
				+ needsReview + ", existing mappings preserved: " + existing
				+ ". New records are inactive until reviewed.", MProduct.Table_ID, productId);
		return "@OK@";
	}

	private List<MProduct> bomComponents(int partTypeId) {
		return new Query(getCtx(), MProduct.Table_Name,
				"IsActive='Y' AND M_PartType_ID=? AND EXISTS (SELECT 1 FROM PP_Product_BOMLine bl"
				+ " JOIN PP_Product_BOM b ON b.PP_Product_BOM_ID=bl.PP_Product_BOM_ID"
				+ " WHERE bl.M_Product_ID=M_Product.M_Product_ID AND bl.IsActive='Y'"
				+ " AND b.M_Product_ID=? AND b.IsActive='Y' AND b.BOMType='A')", get_TrxName())
				.setParameters(partTypeId, productId).list();
	}

	private static List<Map<?, ?>> getFields(Map<?, ?> product, int wooProductId) {
		Object metadata = product == null ? null : product.get("meta_data");
		if (metadata instanceof List<?>) {
			for (Object entry : (List<?>) metadata) {
				if (entry instanceof Map<?, ?>) {
					Map<?, ?> item = (Map<?, ?>) entry;
					if ("_wapf_fieldgroup".equals(item.get("key")) && item.get("value") instanceof Map<?, ?>) {
						Object fields = ((Map<?, ?>) item.get("value")).get("fields");
						if (fields instanceof List<?> && !((List<?>) fields).isEmpty()) {
							return objects(fields, "WAPF fields");
						}
					}
				}
			}
		}
		throw new AdempiereUserError("WooCommerce product " + wooProductId
				+ " returned no WAPF fields. Check the product and API response.");
	}

	private static List<Map<?, ?>> objects(Object value, String description) {
		if (!(value instanceof List<?>)) {
			throw new AdempiereUserError("Expected a list for " + description);
		}
		return ((List<?>) value).stream().map(entry -> {
			if (!(entry instanceof Map<?, ?>)) {
				throw new AdempiereUserError("Invalid entry in " + description);
			}
			return (Map<?, ?>) entry;
		}).collect(Collectors.toList());
	}

	private static List<MAttribute> attributesOf(MProduct product) {
		MAttributeSet set = product.getAttributeSet();
		return set == null ? List.of() : Arrays.asList(set.getMAttributes(true));
	}

	private void reviewField(Map<?, ?> field, List<MAttribute> attributes, List<PartSet> partSets) {
		String key = (String) field.get("id");
		if (key == null || key.isBlank()) {
			throw new AdempiereUserError("WooCommerce returned a field without an ID.");
		}
		String label = (String) field.get("label");
		Object options = field.get("options");
		Object choicesValue = options instanceof Map<?, ?> ? ((Map<?, ?>) options).get("choices") : null;
		List<Map<?, ?>> choices = choicesValue == null ? List.of() : objects(choicesValue, "choices for " + key);
		List<MAttribute> candidates = suggest(label, attributes, MAttribute::getName);
		if (candidates.size() == 1) {
			MAttribute attribute = candidates.get(0);
			String type = attribute.getAttributeValueType();
			if ("L".equals(type) && !choices.isEmpty()) {
				for (Map<?, ?> choice : choices) {
					String value = choiceLabel(choice, key);
					List<MAttributeValue> matches = attributeValues(attribute, value);
					MzzWoocommerceMap map = proposal(field, value, null, matches, "attribute value");
					if (map != null && matches.size() == 1) {
						saveAttribute(map, attribute, matches.get(0).get_ID());
					}
				}
				return;
			}
			if (("N".equals(type) || "S".equals(type)) && choices.isEmpty()) {
				MzzWoocommerceMap map = proposal(field, null, null, candidates, "attribute");
				if (map != null) {
					saveAttribute(map, attribute, 0);
				}
				return;
			}
		}
		if (choices.isEmpty()) {
			proposal(field, null, null, candidates.size() > 1 ? candidates : List.of(), "attribute");
			return;
		}
		if (candidates.size() > 1) {
			for (Map<?, ?> choice : choices) {
				proposal(field, choiceLabel(choice, key), null, candidates, "attribute");
			}
			return;
		}

		List<PartSet> matchingSets = suggest(label, partSets, set -> set.type.getName());
		if (matchingSets.isEmpty()) {
			matchingSets = partSets.stream()
					.filter(set -> !suggest(label, set.products, MProduct::getName).isEmpty())
					.collect(Collectors.toList());
		}
		if (matchingSets.size() != 1) {
			for (Map<?, ?> choice : choices) {
				proposal(field, choiceLabel(choice, key), null, matchingSets, "BOM part type");
			}
			return;
		}
		PartSet set = matchingSets.get(0);
		if ("Fabric".equalsIgnoreCase(set.type.getName())) {
			reviewFabric(field, choices, set);
			return;
		}
		for (Map<?, ?> choice : choices) {
			String value = choiceLabel(choice, key);
			List<MProduct> products = suggest(value, set.products, MProduct::getName);
			MzzWoocommerceMap map = proposal(field, value, null, products, "BOM component");
			if (map != null && products.size() == 1) {
				saveComponent(map, set, products.get(0), null, null);
			}
		}
	}

	private void reviewFabric(Map<?, ?> field, List<Map<?, ?>> choices, PartSet set) {
		List<MProduct> fabrics = suggest((String) field.get("label"), set.products, MProduct::getName);
		boolean multi = "multi-image-swatch".equals(field.get("type"));
		if (multi) {
			proposal(field, null, MULTI_SELECT_PARENT, List.of(set), "multi-select parent");
		}
		List<MAttribute> colours = fabrics.size() == 1
				? suggest("colour", attributesOf(fabrics.get(0)), MAttribute::getName) : List.of();
		for (Map<?, ?> choice : choices) {
			String value = choiceLabel(choice, (String) field.get("id"));
			String mode = multi ? MULTI_SELECT_CHILD : null;
			if (fabrics.size() != 1) {
				proposal(field, value, mode, fabrics, "BOM fabric");
			} else if (colours.size() != 1) {
				proposal(field, value, mode, colours, "fabric colour attribute");
			} else {
				MAttribute colour = colours.get(0);
				List<MAttributeValue> values = attributeValues(colour, value);
				MzzWoocommerceMap map = proposal(field, value, mode, values, "fabric colour");
				if (map != null && values.size() == 1) {
					saveComponent(map, set, fabrics.get(0), colour, values.get(0));
				}
			}
		}
	}

	private MzzWoocommerceMap proposal(Map<?, ?> field, String value, String mode, List<?> matches, String target) {
		String key = (String) field.get("id");
		String label = (String) field.get("label");
		boolean found = new Query(getCtx(), MzzWoocommerceMap.Table_Name,
				"M_Product_ID=? AND woocommerce_field_key=? AND woocommerce_field_value IS NOT DISTINCT FROM ?",
				get_TrxName()).setParameters(productId, key, value).match();
		if (found) {
			existing++;
			return null;
		}
		MzzWoocommerceMap map = new MzzWoocommerceMap(getCtx(), 0, get_TrxName());
		map.setM_Product_ID(productId);
		map.setwoocommerce_field_key(key);
		map.setwoocommerce_field_label(label);
		map.setwoocommerce_field_value(value);
		map.setzz_woocommerce_m_select_type(mode);
		map.setIsActive(false);
		map.setAdd_To_Duplicate("attribute".equals(target) || "attribute value".equals(target));
		map.setignore_no_child_records(MULTI_SELECT_PARENT.equals(mode));
		if (matches.size() == 1) {
			map.setDescription("Review suggested " + target + " before activating.");
			proposed++;
		} else {
			String reason = (matches.isEmpty() ? "Unmatched " : "Ambiguous ") + target;
			map.setDescription(reason + ". Select a destination or explicitly mark as ignored.");
			addLog(productId, null, null, reason + " for " + label + " [" + key + "]"
					+ (value == null ? "" : ": " + value), MProduct.Table_ID, productId);
			needsReview++;
		}
		map.saveEx();
		return map;
	}

	private void saveAttribute(MzzWoocommerceMap map, MAttribute attribute, int valueId) {
		MzzWoocommerceMapLine line = newLine(map, ATTRIBUTE);
		line.setM_Attribute_ID(attribute.get_ID());
		line.setM_AttributeValue_ID(valueId);
		line.saveEx();
	}

	private void saveComponent(MzzWoocommerceMap map, PartSet set, MProduct product,
			MAttribute colour, MAttributeValue value) {
		MzzWoocommerceMapLine component = newLine(map, PRODUCT_ADD);
		component.setBLD_Product_PartType_ID(set.type.get_ID());
		component.setM_Product_Line_ID(product.get_ID());
		component.saveEx();
		if (colour != null) {
			MzzWoocommerceMapLine line = newLine(map, PRODUCT_ATTRIBUTE);
			line.setBLD_Product_PartType_ID(set.type.get_ID());
			line.setM_Product_Line_ID(product.get_ID());
			line.setM_Attribute_Product_ID(colour.get_ID());
			line.setM_Attributevalue_Product_ID(value.get_ID());
			line.saveEx();
		}
	}

	private MzzWoocommerceMapLine newLine(MzzWoocommerceMap map, String type) {
		MzzWoocommerceMapLine line = new MzzWoocommerceMapLine(getCtx(), 0, get_TrxName());
		line.setZZ_Woocommerce_Map_ID(map.get_ID());
		line.setzz_woocommerce_map_type(type);
		return line;
	}

	private static String choiceLabel(Map<?, ?> choice, String key) {
		Object label = choice.get("label");
		if (!(label instanceof String) || ((String) label).isBlank()) {
			throw new AdempiereUserError("WooCommerce field " + key + " has a choice without a label.");
		}
		return (String) label;
	}

	private static List<MAttributeValue> attributeValues(MAttribute attribute, String label) {
		MAttributeValue[] values = attribute.getMAttributeValues();
		return values == null ? List.of() : WooCommerceNameMatcher.match(label, Arrays.asList(values),
				MAttributeValue::getName);
	}

	private static <T> List<T> suggest(String label, List<T> candidates, Function<T, String> name) {
		return WooCommerceNameMatcher.suggest(label, candidates, name);
	}

	private static final class PartSet {
		private final MBLDProductPartType type;
		private final List<MProduct> products;

		private PartSet(MBLDProductPartType type, List<MProduct> products) {
			this.type = type;
			this.products = products;
		}
	}
}
