package za.co.ntier.processes;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Runs only after ERP has a committed receipt for this exact store and order. */
final class WooOrderAcknowledgement {
	private static final String SYNC_KEY = "syncedToIdempiere";

	private WooOrderAcknowledgement() {
	}

	static void apply(int id, Supplier<Map<?, ?>> read, Consumer<Map<String, Object>> update) {
		Map<?, ?> current = read.get();
		requireResponse(current, id, null);
		if ("processing".equals(current.get("status"))) {
			// The Woo status hook resets the flag. Read back and keep the
			// writes ordered; replay only writes not already confirmed.
			update.accept(Map.of("status", "completed"));
			current = read.get();
		}
		requireResponse(current, id, "completed");
		List<?> flags = flags(current);
		if (flags.size() > 1) {
			throw new IllegalStateException("WooCommerce has duplicate sync flags; reconcile manually.");
		}
		if (!isSynced(flags)) {
			Map<?, ?> existing = flags.isEmpty() ? Map.of() : (Map<?, ?>) flags.get(0);
			Map<String, Object> desired = existing.get("id") instanceof Number metaId
					? Map.of("id", metaId, "key", SYNC_KEY, "value", "yes")
					: Map.of("key", SYNC_KEY, "value", "yes");
			update.accept(Map.of("meta_data", List.of(desired)));
			current = read.get();
			requireResponse(current, id, "completed");
			if (!isSynced(flags(current))) {
				throw new IllegalStateException("WooCommerce did not confirm the synced flag.");
			}
		}
	}

	private static void requireResponse(Map<?, ?> response, int id, String status) {
		if (response == null || !Integer.valueOf(id).equals(response.get("id"))
				|| (status != null && !status.equals(response.get("status")))) {
			throw new IllegalStateException("WooCommerce did not confirm the expected order and status.");
		}
	}

	private static List<?> flags(Map<?, ?> order) {
		if (!(order.get("meta_data") instanceof List<?> metadata)) {
			throw new IllegalStateException("WooCommerce did not return order metadata.");
		}
		return metadata.stream().filter(entry -> entry instanceof Map<?, ?> meta
				&& SYNC_KEY.equals(meta.get("key"))).toList();
	}

	private static boolean isSynced(List<?> flags) {
		return flags.size() == 1 && "yes".equals(((Map<?, ?>) flags.get(0)).get("value"));
	}
}
