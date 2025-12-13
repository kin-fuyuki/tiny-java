package tiny.tdf;

import java.util.List;

public class TDF_ERR extends Exception {
    private String message;

    public TDF_ERR(TDF_ERROR err, String file, List<String> path) {
        message = "TDF ERROR: ";
        switch (err) {
            case SET_MISMATCH:
                message += "SET TYPE MISMATCH ";
                break;
            case GET_MISMATCH:
                message += "GET TYPE MISMATCH ";
                break;
            case PATH_NOT_FOUND:
                message += "PATH NOT FOUND ";
                break;
            case FILE_NOT_FOUND:
                message += "FILE NOT FOUND " + file;
                return;
            case FAILED_TO_PARSE:
                message += "FAILED TO PARSE " + file + " AT LINE :" + path.get(0);
                return;
            case NULL_POINTER:
                message += "NULL POINTER of selector " + path.get(0);
                message += " FAILED TO PARSE " + file + " AT LINE :" + path.get(1);
                return;
            default:
                message += "UNKNOWN ERROR ";
                break;
        }
        message += " ON FILE: " + file + " AT PATH: ";
        for (int i = 0; i < path.size(); i++) {
            message += path.get(i);
            if (i != path.size() - 1) {
                message += " . ";
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}