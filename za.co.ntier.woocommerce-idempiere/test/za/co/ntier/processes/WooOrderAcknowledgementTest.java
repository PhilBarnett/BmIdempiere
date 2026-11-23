package za.co.ntier.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Standalone Java regression for acknowledgement ordering and uncertainty. */
public class WooOrderAcknowledgementTest {
	private static final String KEY = "syncedToIdempiere";

	private static final class Woo {
		String status = "processing";
		String synced = "no";
		boolean duplicates;
		int applyAttempts;
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
				synced = "no"; // The Woo status hook resets import metadata.
			} else {
				writes.add("meta");
				Map<?, ?> meta = (Map<?, ?>) ((List<?>) body.get("meta_data")).get(0);
				require(Integer.valueOf(7).equals(meta.get("id")), "must update the existing meta row");
				synced = (String) meta.get("value");
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
		require(normal.writes.equals(List.of("status", "meta")), "normal acknowledgement and no extra writes");

		Woo lostStatusResponse = new Woo();
		lostStatusResponse.apply(body -> {
			lostStatusResponse.put(body);
			if (body.containsKey("status")) throw new IllegalStateException("simulated lost status response");
		});
		require(lostStatusResponse.writes.equals(List.of("status", "meta")), "read status back rather than resending");

		Woo lostMetaResponse = new Woo();
		lostMetaResponse.apply(body -> {
			lostMetaResponse.put(body);
			if (body.containsKey("meta_data")) throw new IllegalStateException("simulated lost metadata response");
		});
		require(lostMetaResponse.writes.equals(List.of("status", "meta")), "read metadata back rather than resending");

		Woo rejectedMeta = new Woo();
		expectFailure(() -> rejectedMeta.apply(body -> {
			if (body.containsKey("meta_data")) {
				rejectedMeta.writes.add("meta-attempt");
				throw new IllegalStateException("simulated rejected metadata write");
			}
			rejectedMeta.put(body);
		}));
		require(rejectedMeta.writes.equals(List.of("status", "meta-attempt")), "unconfirmed metadata must not be resent");

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
		expectFailure(() -> silentFailure.apply(body -> { silentFailure.applyAttempts++; }));
		require(silentFailure.applyAttempts == 1 && "processing".equals(silentFailure.status),
				"unverified status change must stop without a second attempt");

		System.out.println("8 Woo acknowledgement regression scenarios passed.");
	}

	private static void expectFailure(Runnable action) {
		try {
			action.run();
		} catch (IllegalStateException expected) {
			return;
		}
		throw new AssertionError("Expected an unconfirmed acknowledgement");
	}

	private static void require(boolean success, String description) {
		if (!success) throw new AssertionError(description);
	}
}
