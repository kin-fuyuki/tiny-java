package tiny.tdf;

public enum TDF_TYPE {
    TDF_DEFINES(10),
    TDF_BOOL(3),
    TDF_CHAR(1),
    TDF_BINARY(4),
    TDF_INT(5),
    TDF_HEX(7),
    TDF_STR(0),
    TDF_BLOCK(200),
    TDF_FLOAT(6),
    TDF_POINTER(8),
    TDF_CLASS(9);

    private final int value;

    TDF_TYPE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
