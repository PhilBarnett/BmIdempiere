import au.blindmot.utils.MtmBarcode;

/** Standalone tests: no running ERP, credentials or database required. */
public class MtmBarcodeTest {
    private static int checks;

    private static void valid(String raw, MtmBarcode.Target target, int id) {
        MtmBarcode parsed = MtmBarcode.parse(raw);
        if (parsed.getTarget() != target || parsed.getRecordId() != id)
            throw new AssertionError("Incorrect barcode routing");
        checks++;
    }

    private static void invalid(String raw) {
        try {
            MtmBarcode.parse(raw);
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Invalid barcode accepted");
    }

    public static void main(String[] args) {
        valid("021026423", MtmBarcode.Target.ITEM, 1026423);
        valid("011003521", MtmBarcode.Target.PRODUCTION, 1003521);
        valid("031000001", MtmBarcode.Target.CUT, 1000001);
        valid(" 021026423\r\n", MtmBarcode.Target.ITEM, 1026423);
        valid("0210000000", MtmBarcode.Target.ITEM, 10000000);
        valid("022147483647", MtmBarcode.Target.ITEM, Integer.MAX_VALUE);
        valid("021", MtmBarcode.Target.ITEM, 1);
        for (String raw : new String[] {null, "", " ", "0", "02", "020", "02-1",
                "02+1", "021.0", "021e3", "021 2", "021\n2", "991026423",
                "021026423 OR 1=1", "022147483648", "029999999999",
                "02001026423", "02\u0661", "0".repeat(65)}) invalid(raw);
        System.out.println("Passed " + checks + " barcode validation checks.");
    }
}
