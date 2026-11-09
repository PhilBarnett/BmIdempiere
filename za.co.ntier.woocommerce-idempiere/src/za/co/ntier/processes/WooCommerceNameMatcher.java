package za.co.ntier.processes;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

final class WooCommerceNameMatcher {
	private WooCommerceNameMatcher() {
	}

	static <T> List<T> match(String label, List<T> candidates, Function<T, String> name) {
		String exact = fold(label);
		if (exact.isEmpty()) {
			return List.of();
		}
		List<T> matches = candidates.stream()
				.filter(candidate -> exact.equals(fold(name.apply(candidate))))
				.collect(Collectors.toList());
		if (!matches.isEmpty()) {
			return matches;
		}
		String compact = compact(exact);
		if (compact.isEmpty()) {
			return List.of();
		}
		return candidates.stream()
				.filter(candidate -> compact.equals(compact(fold(name.apply(candidate)))))
				.collect(Collectors.toList());
	}

	// Weaker, whole-token matches are suggestions only. Retain every top-scoring
	// candidate so a tie is visible instead of being resolved by BOM row order.
	static <T> List<T> suggest(String label, List<T> candidates, Function<T, String> name) {
		List<T> exact = match(label, candidates, name);
		if (!exact.isEmpty()) {
			return exact;
		}
		List<String> source = tokens(label);
		int best = 0;
		List<T> matches = new java.util.ArrayList<>();
		for (T candidate : candidates) {
			List<String> target = tokens(name.apply(candidate));
			if (source.isEmpty() || target.isEmpty()
					|| (!source.containsAll(target) && !target.containsAll(source))) {
				continue;
			}
			int score = Math.min(source.size(), target.size());
			if (score > best) {
				best = score;
				matches.clear();
			}
			if (score == best) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	private static List<String> tokens(String value) {
		return Arrays.stream(fold(value).split("[^\\p{L}\\p{N}]+"))
				.filter(token -> !token.isEmpty())
				.distinct()
				.collect(Collectors.toList());
	}

	private static String fold(String value) {
		return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFKC)
				.strip().toLowerCase(Locale.ROOT);
	}

	private static String compact(String value) {
		return value.replaceAll("[\\p{P}\\p{Z}\\s]+", "");
	}
}
