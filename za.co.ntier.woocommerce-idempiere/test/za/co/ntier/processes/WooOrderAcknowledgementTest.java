package za.co.ntier.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Standalone regression fixture (no ERP, database, or credentials required). */
public class WooOrderAcknowledgementTest {
	private static final String KEY = "syncedToIdempiere";

	private static final class Woo {
		String status = "processing";
		String synced = "no";
		boolean duplicates;
		final List<String> writes = new ArrayList<>();

		Map<?, ?> get() {
			Map<String, Object> flag = Map.of("id", 7, "key", KEY, "value", synced);
			return Map.of("id", 42, "status", status, "meta_data",
					duplicates ? List.of(flag, flag) : List.of(flag));
		}

		void put(Map<String, Object> body) {
			if (body.containsKey("status")) {
				writes.add("status");
				status = (String) body.get("status");
				synced = "no";
			} else {
				writes.add("meta");
				Map<?, ?> metadata = (Map<?, ?>) ((List<?>) body.get("meta_data")).get(0);
				require(Integer.valueOf(7).equals(metadata.get("id")), "update existing meta ID");
				synced = (String) metadata.get("value");
			}
		}

		void apply(Consumer<Map<String, Object>> writer) {
			WooOrderAcknowledgement.apply(42, this::get, writer);
		}
	}

	public static void main(String[] args) {
		Woo normal = new Woo();
		normal.apply(normal::put);
		normal.apply(normal::put);
		require(normal.writes.equals(List.of("status", "meta")), "normal and idempotent replay");

		Woo statusResponseLost = new Woo();
		expectFailure(() -> statusResponseLost.apply(body -> {
			statusResponseLost.put(body);
			throw new IllegalStateException("simulated lost status response");
		}));
		statusResponseLost.apply(statusResponseLost::put);
		require(statusResponseLost.writes.equals(List.of("status", "meta")), "lost status response must not repeat completion");

		Woo metaWriteFailed = new Woo();
		expectFailure(() -> metaWriteFailed.apply(body -> {
			if (body.containsKey("meta_data")) throw new IllegalStateException("simulated rejected metadata write");
			metaWriteFailed.put(body);
		}));
		metaWriteFailed.apply(metaWriteFailed::put);
		require(metaWriteFailed.writes.equals(List.of("status", "meta")), "recover metadata without repeating status");

		Woo metaResponseLost = new Woo();
		expectFailure(() -> metaResponseLost.apply(body -> {
			metaResponseLost.put(body);
			if (body.containsKey("meta_data")) throw new IllegalStateException("simulated lost metadata response");
		}));
		metaResponseLost.apply(metaResponseLost::put);
		require(metaResponseLost.writes.equals(List.of("status", "meta")), "lost metadata response must not repeat writes");

		Woo onHold = new Woo();
		onHold.status = "on-hold";
		expectFailure(() -> onHold.apply(onHold::put));
		require(onHold.writes.isEmpty(), "do not override operator status");

		Woo wrongOrder = new Woo();
		expectFailure(() -> WooOrderAcknowledgement.apply(99, wrongOrder::get, wrongOrder::put));
		require(wrongOrder.writes.isEmpty(), "do not mutate wrong order");

		Woo duplicate = new Woo();
		duplicate.status = "completed";
		duplicate.duplicates = true;
		expectFailure(() -> duplicate.apply(duplicate::put));
		require(duplicate.writes.isEmpty(), "do not add a third sync flag");

		Woo silentFailure = new Woo();
		expectFailure(() -> silentFailure.apply(body -> { }));
		require("processing".equals(silentFailure.status), "unverified status change cannot proceed");

		System.out.println("8 Woo acknowledgement regression scenarios passed");
	}

	private static void expectFailure(Runnable action) {
		try {
			action.run();
		} catch (IllegalStateException expected) {
			return;
		}
		throw new AssertionError("Expected a recoverable acknowledgement failure");
	}

	private static void require(boolean success, String description) {
		if (!success) throw new AssertionError(description);
	}
}
