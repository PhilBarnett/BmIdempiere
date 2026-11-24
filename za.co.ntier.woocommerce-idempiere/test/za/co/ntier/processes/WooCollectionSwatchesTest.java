package za.co.ntier.processes;

import java.util.Arrays;
import java.util.List;

/** Catalogue parsing tests with synthetic labels; no ERP or store required. */
public class WooCollectionSwatchesTest {
	public static void main(String[] args) {
		for (String family : List.of("First", "Second", "Third", "Fourth", "Fifth", "Sixth")) {
			require(family.equals(WooCollectionSwatches.collection(family + " Colours")));
		}
		require("Sample Family".equals(WooCollectionSwatches.collection(" Sample Family COLOR ")));
		require(WooCollectionSwatches.collection("Fabric Collection") == null);
		require(WooCollectionSwatches.collection(null) == null);
		require(WooCollectionSwatches.validChoiceLabels(List.of("Warm White", "Cool White")));
		require(WooCollectionSwatches.validChoiceLabels(List.of("Warm White")));
		require(!WooCollectionSwatches.validChoiceLabels(List.of()));
		require(!WooCollectionSwatches.validChoiceLabels(List.of("White", "White")));
		require(!WooCollectionSwatches.validChoiceLabels(List.of("White, Warm")));
		require(!WooCollectionSwatches.validChoiceLabels(List.of(" White")));
		require(!WooCollectionSwatches.validChoiceLabels(List.of("White ")));
		require(!WooCollectionSwatches.validChoiceLabels(List.of(" ")));
		require(!WooCollectionSwatches.validChoiceLabels(Arrays.asList("White", null)));
		System.out.println("18 Java swatch-label regressions passed.");
	}

	private static void require(boolean ok) {
		if (!ok) throw new AssertionError("Unexpected collection swatch parsing result.");
	}
}
