package za.co.ntier.processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MProduct;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.model.X_M_PartType;
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
import za.co.ntier.woocommerce.WcOrder;

public class WooCommerceProductSetup extends SvrProcess {
	private static final String ATTRIBUTE = "10000003";
	private static final String PRODUCT_ADD = "10000004";
	private static final String PRODUCT_ATTRIBUTE = "10000005";
	private static final String MULTI_SELECT_PARENT = "10000006";
	private static final String MULTI_SELECT_CHILD = "10000007";

	private int productId;
	private int activated;
	private int needsReview;
	private int existing;
	private final Map<String, List<String>> exceptions = new LinkedHashMap<>();

	@Override
	protected void prepare() {
	}

	@Override
	protected String doIt() throws Exception {
		productId = getProcessInfo().getTable_ID() == MProduct.Table_ID
				? getProcessInfo().getRecord_ID()
				: new MzzWoocommerceMap(getCtx(), getProcessInfo().getRecord_ID(), get_TrxName()).getM_Product_ID();
		if (productId <= 0) {
			throw new AdempiereUserError("Select a product or a WooCommerce mapping on the product to review.");
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
		if (product == null || !(product.get("id") instanceof Number sourceId) || sourceId.intValue() != wooProductId) {
			throw new AdempiereUserError("WooCommerce did not return the linked product " + wooProductId + ".");
		}
		List<Map<?, ?>> fields = getFields(product, wooProductId);
		log.info("Reviewing " + fields.size() + " WooCommerce fields for product " + wooProductId);

		MProduct erpProduct = new MProduct(getCtx(), productId, get_TrxName());
		List<MAttribute> attributes = attributesOf(erpProduct);
		List<PartSet> partSets = Arrays.stream(MBLDProductPartType.getMBLDProductPartTypes(
				getCtx(), productId, get_TrxName()))
				.map(type -> new PartSet(type, bomComponents(type.getM_PartTypeID())))
				.filter(set -> !set.products.isEmpty())
				.collect(Collectors.toList());
		List<String> collections = fields.stream().filter(field -> isFabricCollection(field.get("label")))
				.flatMap(field -> fieldChoices(field).stream()
						.map(choice -> choiceLabel(choice, (String) field.get("id"))))
				.distinct().collect(Collectors.toList());
		List<PartSet> fabricSets = collections.isEmpty()
				? List.of() : partSets.stream().filter(this::isFabricPartSet).collect(Collectors.toList());
		// Setup uses the caller's transaction. A product lock serialises
		// preflight and activation for concurrent setup runs on this product.
		if (fields.stream().anyMatch(field -> isMultiselectCollection(field, collections))) {
			MProduct lockedProduct = new Query(getCtx(), MProduct.Table_Name,
					MProduct.COLUMNNAME_M_Product_ID + "=?", get_TrxName())
					.setParameters(productId).setClient_ID().setOnlyActiveRecords(true).setForUpdate(true).firstOnly();
			if (lockedProduct == null) {
				throw new AdempiereUserError("The linked ERP product is not active in this client.");
			}
		}
		for (Map<?, ?> field : fields) {
			reviewField(wooProductId, field, attributes, partSets, collections, fabricSets);
		}
		exceptions.forEach((message, values) -> addLog(productId, null, null,
				values.isEmpty() ? message : message + ": " + String.join(", ", values),
				MProduct.Table_ID, productId));
		addLog(productId, null, null, "Mappings activated: " + activated + ", exceptions: "
				+ needsReview + ", existing mappings preserved: " + existing
				+ ". Exceptions were left unchanged.", MProduct.Table_ID, productId);
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

	private void reviewField(int wooProductId, Map<?, ?> field, List<MAttribute> attributes, List<PartSet> partSets,
			List<String> collections, List<PartSet> fabricSets) {
		String key = (String) field.get("id");
		if (key == null || key.isBlank()) {
			throw new AdempiereUserError("WooCommerce returned a field without an ID.");
		}
		String label = (String) field.get("label");
		List<Map<?, ?>> choices = fieldChoices(field);
		if (WcOrder.isCurtainSelector(wooProductId, key)) {
			if (choices.isEmpty()) {
				reportException(key, label, null, "Curtain selector has no choices");
			}
			for (Map<?, ?> choice : choices) {
				String value = choiceLabel(choice, key);
				if (!WcOrder.isSupportedCurtainSelection(key, value)) {
					reportException(key, label, value, "Unsupported curtain selection");
				}
			}
			return;
		}

		if (isMultiselectCollection(field, collections)) {
			reviewMultiselectCollection(wooProductId, field, choices, WooCollectionSwatches.collection(label),
					collections, fabricSets);
			return;
		}

		if (!collections.isEmpty() && label != null && !choices.isEmpty()) {
			if (isFabricCollection(label)) {
				for (Map<?, ?> choice : choices) {
					String value = choiceLabel(choice, key);
					MzzWoocommerceMap map = proposal(field, value, null, List.of(value), "fabric collection selector");
					if (map != null) {
						map.setignore_no_child_records(true);
						map.setDescription(WcOrder.FABRIC_COLLECTION_SELECTOR_DESCRIPTION);
						activate(map);
					}
				}
				return;
			}
			String collection = label.strip().replaceFirst("(?i)\\s+colou?rs?$", "");
			if (!collection.equals(label.strip())
					&& !WooCommerceNameMatcher.match(collection, collections, Function.identity()).isEmpty()) {
				reviewCollectionColours(field, choices, collection, fabricSets);
				return;
			}
		}
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
			List<MProduct> products = WooCommerceNameMatcher.match(value, set.products, MProduct::getName);
			MzzWoocommerceMap map = proposal(field, value, null, products, "BOM component");
			if (map != null && products.size() == 1) {
				saveComponent(map, set, products.get(0), null, null);
			}
		}
	}

	private static List<Map<?, ?>> fieldChoices(Map<?, ?> field) {
		Object options = field.get("options");
		Object choices = options instanceof Map<?, ?> ? ((Map<?, ?>) options).get("choices") : null;
		return choices == null ? List.of() : objects(choices, "choices for " + field.get("id"));
	}

	private static boolean isFabricCollection(Object label) {
		return label instanceof String && ((String) label).strip()
				.matches("(?i)(?:sheer|blockout) fabric collection");
	}

	private boolean isFabricPartSet(PartSet set) {
		String partType = new X_M_PartType(getCtx(), set.type.getM_PartTypeID(), get_TrxName()).getName();
		return "Fabric".equalsIgnoreCase(partType) || "Lining".equalsIgnoreCase(partType);
	}


	private static boolean isMultiselectCollection(Map<?, ?> field, List<String> collections) {
		String collection = WooCollectionSwatches.collection(field.get("label"));
		return collection != null && "multi-image-swatch".equals(field.get("type"))
				&& !WooCommerceNameMatcher.match(collection, collections, Function.identity()).isEmpty();
	}

	/**
	 * A null-valued parent takes precedence in the order reader. Do not add it
	 * unless this field's full palette and existing mapping shape are usable.
	 * An existing ordinary row is an operator decision, not permission to edit it.
	 */
	private void reviewMultiselectCollection(int wooProductId, Map<?, ?> field, List<Map<?, ?>> choices,
			String collection, List<String> collections, List<PartSet> fabricSets) {
		String key = (String) field.get("id");
		String label = (String) field.get("label");
		List<String> values = choices.stream().map(choice -> choiceLabel(choice, key)).collect(Collectors.toList());
		if (!WooCollectionSwatches.validChoiceLabels(values)
				|| collections.stream().filter(collection::equalsIgnoreCase).count() != 1) {
			reportException(key, label, null, "No distinct exact swatches or matching collection selector");
			return;
		}

		// A Fabric Sample line must carry one Fabric (not Lining) plus an
		// instance colour. Reuse the active BOM; do not auto-create fabric SKUs.
		List<PartSet> sampleSets = fabricSets.stream().filter(set -> set.type.isActive())
				.filter(set -> "Fabric".equalsIgnoreCase(
						new X_M_PartType(getCtx(), set.type.getM_PartTypeID(), get_TrxName()).getName()))
				.collect(Collectors.toList());
		List<MProduct> components = sampleSets.stream().flatMap(set -> set.products.stream())
				.distinct().collect(Collectors.toList());
		String binding = MSysConfig.getValue("BM_WOO_SWATCH_" + wooProductId + "_" + key, "",
				Env.getAD_Client_ID(getCtx()));
		if (!binding.isBlank() && !binding.matches("[1-9][0-9]*:[1-9][0-9]*(?:,[A-Za-z0-9_-]+=[1-9][0-9]*)+")) {
			reportException(key, label, null, "Invalid configured swatch binding");
			return;
		}
		String[] entries = binding.split(",");
		String[] destination = entries[0].split(":");
		List<MProduct> fabrics = binding.isBlank() ? collectionFabrics(collection, components)
				: components.stream().filter(product -> Integer.toString(product.get_ID()).equals(destination[0]))
						.collect(Collectors.toList());
		if (fabrics.size() != 1) {
			reportChoiceReview(key, label, null, "sample BOM fabric", fabrics);
			return;
		}
		MProduct fabric = fabrics.get(0);
		List<PartSet> slots = sampleSets.stream()
				.filter(set -> set.products.stream().anyMatch(product -> product.get_ID() == fabric.get_ID()))
				.collect(Collectors.toList());
		List<MAttribute> colours = binding.isBlank() ? collectionColourAttributes(fabric)
				: attributesOf(fabric).stream().filter(MAttribute::isActive)
						.filter(attribute -> attribute.isInstanceAttribute() && "L".equals(attribute.getAttributeValueType()))
						.filter(attribute -> Integer.toString(attribute.get_ID()).equals(destination[1]))
						.collect(Collectors.toList());
		if (slots.size() != 1 || colours.size() != 1) {
			reportChoiceReview(key, label, null, slots.size() != 1 ? "sample Fabric slot" : "sample colour attribute",
					slots.size() != 1 ? slots : colours);
			return;
		}
		PartSet slot = slots.get(0);
		MAttribute colour = colours.get(0);
		Map<String, MAttributeValue> targets = new LinkedHashMap<>();
		Map<String, String> colourIds = new LinkedHashMap<>();
		for (int i = 1; i < entries.length; i++) {
			String[] choice = entries[i].split("=");
			if (colourIds.putIfAbsent(choice[0], choice[1]) != null) {
				reportException(key, label, choice[0], "Duplicate configured Woo choice slug");
				return;
			}
		}
		// Durable bindings live outside generated mappings. Retain current Woo labels
		// as order lookup keys because older order payloads may carry labels only.
		MAttributeValue[] available = colour.getMAttributeValues();
		for (int i = 0; i < choices.size(); i++) {
			String value = values.get(i);
			String colourId = colourIds.remove(choices.get(i).get("slug"));
			List<MAttributeValue> matches = binding.isBlank() ? attributeValues(colour, value)
					: available == null || colourId == null ? List.of()
					: Arrays.stream(available).filter(MAttributeValue::isActive)
							.filter(candidate -> Integer.toString(candidate.get_ID()).equals(colourId))
							.collect(Collectors.toList());
			if (matches.size() == 1) {
				targets.put(value, matches.get(0));
			} else {
				reportChoiceReview(key, label, value, "sample colour", matches);
			}
		}
		if (!colourIds.isEmpty()) {
			reportException(key, label, null, "Configured Woo slugs absent from the field: " + colourIds.keySet());
			return;
		}
		if (targets.size() != values.size()) {
			return;
		}
		if (targets.values().stream().map(MAttributeValue::get_ID).distinct().count() != targets.size()) {
			reportException(key, label, null, "Multiple swatches resolve to the same ERP colour");
			return;
		}
		if (!existingCollectionMappingsFit(key, label, slot, fabric, colour, targets)) {
			return;
		}

		// The exact labels from Woo become stored child keys, even where
		// destination matching normalises names. Use the same checked model
		// activation as the established roller/outdoor multi-select path.
		MzzWoocommerceMap parent = proposal(field, null, MULTI_SELECT_PARENT, List.of(slot), "multi-select parent");
		if (parent != null) {
			activate(parent);
		}
		targets.forEach((value, target) -> {
			MzzWoocommerceMap child = proposal(field, value, MULTI_SELECT_CHILD, List.of(target), "fabric colour");
			if (child != null) {
				saveComponent(child, slot, fabric, colour, target);
			}
		});
	}

	private void reportChoiceReview(String key, String label, String value, String target, List<?> matches) {
		reportException(key, label, value, (matches.isEmpty() ? "Unmatched " : "Ambiguous ") + target);
	}

	/** Review all live rows, even an obsolete ordinary value the new parent would shadow. */
	private boolean existingCollectionMappingsFit(String key, String label, PartSet slot, MProduct fabric,
			MAttribute attribute, Map<String, MAttributeValue> targets) {
		List<MzzWoocommerceMap> mappings = new Query(getCtx(), MzzWoocommerceMap.Table_Name,
				"M_Product_ID=? AND LOWER(woocommerce_field_key)=LOWER(?)", get_TrxName())
				.setParameters(productId, key).setClient_ID().list();
		Map<String, Integer> exactKeys = new LinkedHashMap<>();
		boolean valid = true;
		for (MzzWoocommerceMap map : mappings) {
			String value = map.getwoocommerce_field_value();
			String mode = value == null ? MULTI_SELECT_PARENT : MULTI_SELECT_CHILD;
			if (!key.equals(map.getwoocommerce_field_key()) || exactKeys.merge(value, 1, Integer::sum) != 1) {
				reportException(key, label, value, "Duplicate or non-exact field mapping");
				valid = false;
				continue;
			}
			if (!map.isActive()) {
				if ((value == null || targets.containsKey(value)) && !isEmptyGeneratedProposal(map)) {
					reportException(key, label, value, "Existing inactive mapping preserved");
					valid = false;
				}
				continue;
			}
			if (!mode.equals(map.getzz_woocommerce_m_select_type())
					|| !Objects.equals(label, map.getwoocommerce_field_label())) {
				reportException(key, label, value, "Existing active mapping kind or label differs; review before activating");
				valid = false;
				continue;
			}
			MzzWoocommerceMapLine[] lines = map.getMzzWoocommerceMapLines(map.get_ID(), getCtx(), "", "");
			if (value == null && (!map.isignore_no_child_records() || lines.length != 0)) {
				reportException(key, label, null, "Existing parent is not a routing-only mapping");
				valid = false;
			}
			if (value != null && targets.containsKey(value)
					&& !matchesSampleComponent(lines, slot, fabric, attribute, targets.get(value))) {
				reportException(key, label, value, "Existing child destination differs; review before activating");
				valid = false;
			}
		}
		return valid;
	}

	private static boolean matchesSampleComponent(MzzWoocommerceMapLine[] lines, PartSet slot,
			MProduct fabric, MAttribute attribute, MAttributeValue value) {
		if (lines.length != 2) {
			return false;
		}
		boolean component = false;
		boolean colour = false;
		for (MzzWoocommerceMapLine line : lines) {
			if (line.getBLD_Product_PartType_ID() != slot.type.get_ID() || line.getM_Product_Line_ID() != fabric.get_ID()) {
				return false;
			}
			component |= PRODUCT_ADD.equals(line.getzz_woocommerce_map_type());
			colour |= PRODUCT_ATTRIBUTE.equals(line.getzz_woocommerce_map_type())
					&& line.getM_Attribute_Product_ID() == attribute.get_ID()
					&& line.getM_Attributevalue_Product_ID() == value.get_ID();
		}
		return component && colour;
	}

	private List<MProduct> collectionFabrics(String collection, List<MProduct> components) {
		List<MProduct> fabrics = WooCommerceNameMatcher.match(collection, components,
				product -> product.getName().strip().replaceFirst("(?i)^hoads?\\s+", "")
						.replaceFirst("(?i)\\s+-\\s+hoads?$", ""));
		List<MProduct> withColour = fabrics.stream()
				.filter(product -> collectionColourAttributes(product).size() == 1)
				.collect(Collectors.toList());
		return withColour.size() == 1 ? withColour : fabrics;
	}

	private void reviewCollectionColours(Map<?, ?> field, List<Map<?, ?>> choices, String collection,
			List<PartSet> fabricSets) {
		List<MProduct> components = fabricSets.stream().flatMap(set -> set.products.stream())
				.distinct().collect(Collectors.toList());
		List<MProduct> fabrics = collectionFabrics(collection, components);
		for (Map<?, ?> choice : choices) {
			String value = choiceLabel(choice, (String) field.get("id"));
			if (fabrics.isEmpty()) {
				// Some collections have a separate BOM product for each colour.
				List<MProduct> variants = WooCommerceNameMatcher.match(collection + " " + value,
						components, MProduct::getName);
				reviewCollectionComponent(field, value, variants, fabricSets, null);
			} else {
				reviewCollectionComponent(field, value, fabrics, fabricSets, collection);
			}
		}
	}

	private void reviewCollectionComponent(Map<?, ?> field, String value, List<MProduct> fabrics,
			List<PartSet> fabricSets, String collection) {
		if (fabrics.size() != 1) {
			proposal(field, value, null, fabrics, "BOM fabric");
			return;
		}
		MProduct fabric = fabrics.get(0);
		List<PartSet> slots = fabricSets.stream()
				.filter(set -> set.products.stream().anyMatch(product -> product.get_ID() == fabric.get_ID()))
				.collect(Collectors.toList());
		if (slots.size() != 1 || collection == null) {
			MzzWoocommerceMap map = proposal(field, value, null, slots, "BOM fabric part type");
			if (map != null && slots.size() == 1) {
				saveComponent(map, slots.get(0), fabric, null, null);
			}
			return;
		}
		List<MAttribute> colours = collectionColourAttributes(fabric);
		if (colours.size() != 1) {
			proposal(field, value, null, colours, "fabric colour attribute");
			return;
		}
		MAttribute colour = colours.get(0);
		List<MAttributeValue> values = attributeValues(colour, value);
		MzzWoocommerceMap map = proposal(field, value, null, values, "fabric colour");
		if (map != null && values.size() == 1) {
			saveComponent(map, slots.get(0), fabric, colour, values.get(0));
		}
	}

	private List<MAttribute> collectionColourAttributes(MProduct fabric) {
		if (fabric.getM_AttributeSet_ID() <= 0) {
			return List.of();
		}
		MAttributeSet set = new MAttributeSet(getCtx(), fabric.getM_AttributeSet_ID(), get_TrxName());
		return Arrays.stream(set.getMAttributes(true))
				.filter(attribute -> "L".equals(attribute.getAttributeValueType()))
				.filter(attribute -> attribute.getName().strip().matches("(?i)(?:.*\\s)?colou?rs?")
						|| "Colour".equalsIgnoreCase(attribute.getDescription()))
				.collect(Collectors.toList());
	}

	private void reviewFabric(Map<?, ?> field, List<Map<?, ?>> choices, PartSet set) {
		List<MProduct> fabrics = suggest((String) field.get("label"), set.products, MProduct::getName);
		boolean multi = "multi-image-swatch".equals(field.get("type"));
		if (multi) {
			// A parent changes how the order reader interprets every choice on this
			// field. Inspect duplicates before activating any parent or children.
			if (hasDuplicateFabricMappings(field, choices)) {
				return;
			}
			MzzWoocommerceMap parent = proposal(field, null, MULTI_SELECT_PARENT,
					List.of(set), "multi-select parent");
			if (parent != null) {
				activate(parent);
			}
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

	private boolean hasDuplicateFabricMappings(Map<?, ?> field, List<Map<?, ?>> choices) {
		String key = (String) field.get("id");
		String label = (String) field.get("label");
		List<MzzWoocommerceMap> rows = new Query(getCtx(), MzzWoocommerceMap.Table_Name,
				"M_Product_ID=? AND woocommerce_field_key=?", get_TrxName())
				.setParameters(productId, key).setClient_ID().list();
		Map<String, List<MzzWoocommerceMap>> byValue = new LinkedHashMap<>();
		for (MzzWoocommerceMap row : rows) {
			byValue.computeIfAbsent(row.getwoocommerce_field_value(), ignored -> new ArrayList<>()).add(row);
		}
		boolean conflict = false;
		List<String> keys = new ArrayList<>();
		keys.add(null); // Null is the routing parent, not an empty-string child.
		for (Map<?, ?> choice : choices) {
			keys.add(choiceLabel(choice, key));
		}
		for (String value : keys.stream().distinct().collect(Collectors.toList())) {
			List<MzzWoocommerceMap> candidates = byValue.getOrDefault(value, List.of());
			if (candidates.size() > 1) {
				reportDuplicateMapping(key, label, value, candidates);
				conflict = true;
			}
		}
		return conflict;
	}

	private void reportDuplicateMapping(String key, String label, String value, List<MzzWoocommerceMap> rows) {
		String records = rows.stream().map(row -> row.get_ID() + (row.isActive() ? " active" : " inactive"))
				.sorted().collect(Collectors.joining(", "));
		reportException(key, label, value,
				"Duplicate ERP " + (value == null ? "null-valued routing parent" : "field value")
				+ " mappings (records " + records + "); no row selected");
	}

	private MzzWoocommerceMap proposal(Map<?, ?> field, String value, String mode, List<?> matches, String target) {
		String key = (String) field.get("id");
		String label = (String) field.get("label");
		List<MzzWoocommerceMap> rows = new Query(getCtx(), MzzWoocommerceMap.Table_Name,
				"M_Product_ID=? AND woocommerce_field_key=? AND woocommerce_field_value IS NOT DISTINCT FROM ?",
				get_TrxName()).setParameters(productId, key, value).setClient_ID().list();
		// Both active and inactive rows matter here. Don't select an arbitrary
		// winner or hide a conflict by adding another record of the same key.
		if (rows.size() > 1) {
			reportDuplicateMapping(key, label, value, rows);
			return null;
		}
		MzzWoocommerceMap map = rows.isEmpty() ? null : rows.get(0);
		if (map != null && map.isActive()) {
			existing++;
			return null;
		}
		if (matches.size() != 1) {
			String reason = (matches.isEmpty() ? "Unmatched " : "Ambiguous ") + target;
			reportException(key, label, value, reason);
			return null;
		}
		if (map == null) {
			map = new MzzWoocommerceMap(getCtx(), 0, get_TrxName());
		} else if (!isEmptyGeneratedProposal(map)) {
			existing++;
			reportException(key, label, value, "Existing inactive mapping preserved");
			return null;
		}
		map.setM_Product_ID(productId);
		map.setwoocommerce_field_key(key);
		map.setwoocommerce_field_label(label);
		map.setwoocommerce_field_value(value);
		map.setzz_woocommerce_m_select_type(mode);
		map.setIsActive(false);
		map.setAdd_To_Duplicate("attribute".equals(target) || "attribute value".equals(target));
		map.setignore_no_child_records(MULTI_SELECT_PARENT.equals(mode));
		map.setDescription("Automatically mapped " + target + ".");
		map.saveEx();
		return map;
	}

	private boolean isEmptyGeneratedProposal(MzzWoocommerceMap map) {
		String description = map.getDescription();
		return description != null && (description.startsWith("Unmatched ") || description.startsWith("Ambiguous "))
				&& description.endsWith(". Select a destination or explicitly mark as ignored.")
				&& !map.isignore_no_child_records()
				&& !new Query(getCtx(), MzzWoocommerceMapLine.Table_Name, "ZZ_Woocommerce_Map_ID=?",
						get_TrxName()).setParameters(map.get_ID()).match();
	}

	private void reportException(String key, String label, String value, String reason) {
		List<String> values = exceptions.computeIfAbsent(
				reason + " for " + label + " [" + key + "]", ignored -> new ArrayList<>());
		if (value != null) {
			values.add(value);
		}
		needsReview++;
	}

	private void activate(MzzWoocommerceMap map) {
		map.setIsActive(true);
		map.saveEx();
		activated++;
	}

	private void saveAttribute(MzzWoocommerceMap map, MAttribute attribute, int valueId) {
		MzzWoocommerceMapLine line = newLine(map, ATTRIBUTE);
		line.setM_Attribute_ID(attribute.get_ID());
		line.setM_AttributeValue_ID(valueId);
		line.saveEx();
		activate(map);
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
		activate(map);
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
		return values == null ? List.of() : WooCommerceNameMatcher.match(label,
				Arrays.stream(values).filter(MAttributeValue::isActive).collect(Collectors.toList()),
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
