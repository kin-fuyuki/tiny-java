package tiny;

import tiny.term.ANSI;

import static tiny.term.ANSI.*;

public class TERM {
    public final String prefix;

    public TERM(String prefix) {
        this.prefix = prefix;
    }

    public static class ErrorLevel {
        private int value;

        public ErrorLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // global level instance
    public static ErrorLevel level = new ErrorLevel(6);

    private static void printColored(ANSI color, String format, Object... args) {
        System.out.printf(color.s() + format + "\033[0m\n", args);
    }

    private static void printColored(ANSI bg,ANSI color, String format, Object... args) {
        System.out.printf(bg.s(),color.s() + format + "\033[0m\n", args);
    }
    public static void echo(String format, Object... args) {
        if (level.getValue() == 4) {
            System.out.printf(format + "\n", args);
        }
    }

    public static void warning(String format, Object... args) {
        if (level.getValue() >= 1) {
            printColored(YELLOW, format, args);
        }
    }

    public static void fatal(String format, Object... args) {
        printColored(BGRED, BOLDWHITE, format, args);
    }

    public static void error(String format, Object... args) {
        if (level.getValue() >= 0) {
            printColored(BOLDRED, format, args);
        }
    }

    public static void success(String format, Object... args) {
        if (level.getValue() >= 2) {
            printColored(GREEN, format, args);
        }
    }

    public static void message(String format, Object... args) {
        if (level.getValue() >= 3) {
            printColored(BOLDWHITE, format, args);
        }
    }

    public static void startup(String game, String version) {
        int lv = level.getValue();
        level.setValue(6);
        echo("%s %s", game, version);
        warning("be warned that");
        error("ERRORS MIGHT OCCUR!!!");
        success("but dont worry");
        message("this is not an error");
        fatal("I SWEAR");
        level.setValue(lv);
    }
    public void echo_(String format, Object... args) {
        echo(prefix + " " + format, args);
    }
    public void warning_(String format, Object... args) {
        warning(prefix + " " + format, args);
    }

    public void fatal_(String format, Object... args) {
        fatal(prefix + " " + format, args);
    }

    public void error_(String format, Object... args) {
        error(prefix + " " + format, args);
    }

    public void success_(String format, Object... args) {
        success(prefix + " " + format, args);
    }

    public void message_(String format, Object... args) {
        message(prefix + " " + format, args);
    }

    public void startup_(String game, String version) {
        int lv = level.getValue();
        level.setValue(6);

        echo_( "%s %s", game, version );
        warning_( "be warned that" );
        error_( "ERRORS MIGHT OCCUR!!!" );
        success_( "but dont worry" );
        message_( "this is not an error" );
        fatal_( "I SWEAR" );

        level.setValue(lv);
    }
}
