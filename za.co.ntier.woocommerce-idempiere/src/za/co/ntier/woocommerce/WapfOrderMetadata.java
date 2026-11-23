package za.co.ntier.woocommerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Decodes the WAPF portion of a WooCommerce order item before ERP mapping. */
final class WapfOrderMetadata {
	static final class Field {
		final String id;
		final String label;
		final String value;

		Field(String id, String label, String value) {
			this.id = id;
			this.label = label;
			this.value = value;
		}
	}

	static List<Field> parse(Object metadata, boolean required) {
		if (!(metadata instanceof List<?>)) {
			throw invalid("meta_data", "expected an array");
		}

		List<?> entries = (List<?>) metadata;
		Map<?, ?> wapf = null;
		for (int i = 0; i < entries.size(); i++) {
			String path = "meta_data[" + i + "]";
			if (!(entries.get(i) instanceof Map<?, ?>)) {
				throw invalid(path, "expected an object with a string key");
			}
			Map<?, ?> entry = (Map<?, ?>) entries.get(i);
			Object key = entry.get("key");
			if (!(key instanceof String)) {
				throw invalid(path + ".key", "expected a string");
			}
			if (!"_wapf_meta".equals(key)) {
				continue;
			}
			if (wapf != null) {
				throw invalid(path, "duplicate _wapf_meta entry");
			}
			wapf = unwrap(entry.get("value"));
		}
		if (wapf == null) {
			if (required) {
				throw invalid("meta_data._wapf_meta", "missing Fabric Sample metadata");
			}
			return List.of();
		}

		String fieldsPath = "meta_data._wapf_meta.fields";
		Object rawFields = wapf.get("fields");
		if (!(rawFields instanceof Map<?, ?>) || ((Map<?, ?>) rawFields).isEmpty()) {
			throw invalid(fieldsPath, "expected a nonempty object");
		}

		Map<?, ?> fields = (Map<?, ?>) rawFields;
		List<Field> result = new ArrayList<>(fields.size());
		for (Map.Entry<?, ?> entry : fields.entrySet()) {
			String path = fieldsPath + "." + entry.getKey();
			if (!(entry.getValue() instanceof Map<?, ?>)) {
				throw invalid(path, "expected a field object");
			}
			Map<?, ?> field = (Map<?, ?>) entry.getValue();
			Object id = field.get("id");
			if (!(id instanceof String) || ((String) id).isBlank()) {
				throw invalid(path + ".id", "expected a nonempty string");
			}
			Object label = field.get("label");
			if (label != null && !(label instanceof String)) {
				throw invalid(path + ".label", "expected a string");
			}
			if (!field.containsKey("value")) {
				throw invalid(path + ".value", "missing field value");
			}
			Object value = field.get("value");
			if (value != null && !(value instanceof String) && !(value instanceof Number)) {
				throw invalid(path + ".value", "expected text, a number or null");
			}
			result.add(new Field((String) id, (String) label, value == null ? null : value.toString()));
		}
		return List.copyOf(result);
	}

	private static Map<?, ?> unwrap(Object value) {
		if (value instanceof List<?>) {
			List<?> groups = (List<?>) value;
			if (groups.size() != 1) {
				throw invalid("meta_data._wapf_meta", "expected an array containing one object");
			}
			value = groups.get(0);
		}
		if (!(value instanceof Map<?, ?>)) {
			throw invalid("meta_data._wapf_meta", "expected a fields/settings object");
		}
		return (Map<?, ?>) value;
	}

	private static IllegalArgumentException invalid(String path, String detail) {
		return new IllegalArgumentException(path + ": " + detail);
	}
}
