package tiny.tdf;

public enum TDF_ERROR {
    SET_MISMATCH(1),
    GET_MISMATCH(2),
    PATH_NOT_FOUND(3),
    UNKNOWN_ERROR(-1),
    FILE_NOT_FOUND(4),
    FAILED_TO_PARSE(5),
    NULL_POINTER(6);

    private final int value;

    TDF_ERROR(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}