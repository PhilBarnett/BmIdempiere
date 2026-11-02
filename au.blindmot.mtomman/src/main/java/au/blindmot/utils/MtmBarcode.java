package au.blindmot.utils;

/** Validates MTM labels without database access or manufacturing side effects. */
public final class MtmBarcode {
    public enum Target { PRODUCTION, ITEM, CUT }

    private final Target target;
    private final int recordId;

    private MtmBarcode(Target target, int recordId) {
        this.target = target;
        this.recordId = recordId;
    }

    public Target getTarget() { return target; }
    public int getRecordId() { return recordId; }

    public static MtmBarcode parse(String input) {
        if (input == null || input.length() > 64)
            throw new IllegalArgumentException("Invalid made-to-measure barcode.");
        String value = input.trim();
        // Two prefix digits plus a positive integer ID. Do not assume IDs stay seven digits.
        if (!value.matches("0[123][1-9][0-9]{0,9}"))
            throw new IllegalArgumentException("Expected an MTM barcode with prefix 01, 02 or 03 and a positive numeric ID.");
        int id;
        try {
            id = Integer.parseInt(value.substring(2));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("MTM barcode record ID exceeds the supported range.");
        }
        Target target = value.startsWith("01") ? Target.PRODUCTION
                : value.startsWith("02") ? Target.ITEM : Target.CUT;
        return new MtmBarcode(target, id);
    }
}
