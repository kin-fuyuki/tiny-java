package tiny.term;

public enum ANSI {
    WHITE("\033[97m"),
    BLACK("\033[90m"),
    RED("\033[91m"),
    GREEN("\033[92m"),
    YELLOW("\033[93m"),
    BLUE("\033[94m"),
    MAGENTA("\033[95m"),
    CYAN("\033[96m"),

    BGRED("\033[101m"),

    BOLDBLACK("\033[1m\033[90m"),
    BOLDRED("\033[1m\033[91m"),
    BOLDGREEN("\033[1m\033[92m"),
    BOLDYELLOW("\033[1m\033[93m"),
    BOLDBLUE("\033[1m\033[94m"),
    BOLDMAGENTA("\033[1m\033[95m"),
    BOLDCYAN("\033[1m\033[96m"),
    BOLDWHITE("\033[1m\033[97m");

    private final String code;

    ANSI(String code) {
        this.code = code;
    }

    public String s() {
        return code;
    }
}
