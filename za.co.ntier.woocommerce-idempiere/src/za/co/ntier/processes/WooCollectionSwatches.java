package za.co.ntier.processes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exact labels shared by WAPF field setup and the comma-separated order format. */
final class WooCollectionSwatches {
	private WooCollectionSwatches() {
	}

	static String collection(Object label) {
		if (!(label instanceof String text)) {
			return null;
		}
		String name = text.strip().replaceFirst("(?i)\\s+colou?rs?$", "");
		return name.equals(text.strip()) ? null : name;
	}

	static boolean validChoiceLabels(List<String> values) {
		if (values.isEmpty()) {
			return false;
		}
		Set<String> exactLabels = new HashSet<>();
		for (String value : values) {
			// The order importer splits on comma and trims each token.
			// Such a choice cannot have the same key before and after splitting.
			if (value == null || value.isBlank() || !value.equals(value.strip())
					|| value.indexOf(',') >= 0 || !exactLabels.add(value)) {
				return false;
			}
		}
		return true;
	}
}
