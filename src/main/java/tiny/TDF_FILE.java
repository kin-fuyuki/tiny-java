package tiny;

import tiny.tdf.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;

class TDF_FILE {
    private static final String TINYDEFINEHEADER = "0000_TDF_HEADER_DEFINE";

    private HashMap<String, TDF_DATA> data = null;
    private String filepath;
    private Map<Character, Function<String, TDF_DATA>> customFuncs = new HashMap<>();

    public TDF_FILE(String filepath) {
        this.filepath = filepath;
    }

    public void close() {
        // No manual deletion needed in Java due to GC
    }

    public HashMap<String, TDF_DATA> load() throws TDF_ERR, IOException {
        char cchar = ' ';
        int index = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filepath));
            if (data != null) {
                data = null; // Let GC handle
            }
            data = new HashMap<>();
            HashMap<String, TDF_DATA> current = data;
            Stack<HashMap<String, TDF_DATA>> stack = new Stack<>();
            List<String> pathvec = new ArrayList<>();
            boolean textblock = false;
            String block = "";
            String blockkey = "";
            List<String> defines = new ArrayList<>();
            TDF_DATA defdat = new TDF_DATA(TDF_TYPE.TDF_DEFINES, defines);
            current.put(TINYDEFINEHEADER, defdat);
            String line;
            while ((line = reader.readLine()) != null) {
                index++;
                if (line.length() == 0) continue;
                if (textblock) {
                    String blockline = line.replaceFirst("^\\s+", "");
                    if (blockline.equals("\\")) {
                        textblock = false;
                        TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_BLOCK, block);
                        current.put(blockkey, dat);
                    } else {
                        block += line + "\n";
                    }
                } else {
                    String trimmed = line.replaceFirst("^\\s+", "");
                    if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
                    if (trimmed.length() > 1 && trimmed.charAt(1) == ' ') {
                        cchar = trimmed.charAt(0);
                        switch (cchar) {
                            case '"':
                                int lastQuote = trimmed.indexOf(' ', 3);
                                if (lastQuote == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyQuote = trimmed.substring(2, lastQuote);
                                String valueQuote = trimmed.substring(lastQuote + 1);
                                TDF_DATA datQuote = new TDF_DATA(TDF_TYPE.TDF_STR, valueQuote);
                                current.put(keyQuote, datQuote);
                                break;
                            case 'S':
                                int lastS = trimmed.indexOf(' ', 2);
                                String keyS;
                                if (lastS != -1) {
                                    keyS = trimmed.substring(2, lastS);
                                } else {
                                    keyS = trimmed.substring(2);
                                }
                                blockkey = keyS;
                                block = "";
                                textblock = true;
                                break;
                            case '\'':
                                int lastChar = trimmed.indexOf(' ', 3);
                                if (lastChar == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyChar = trimmed.substring(2, lastChar);
                                if (lastChar + 1 >= trimmed.length()) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                char valueChar = trimmed.charAt(lastChar + 1);
                                if (lastChar + 2 < trimmed.length()) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                TDF_DATA datChar = new TDF_DATA(TDF_TYPE.TDF_CHAR, valueChar);
                                current.put(keyChar, datChar);
                                break;
                            case 'i':
                                int lastInt = trimmed.indexOf(' ', 3);
                                if (lastInt == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyInt = trimmed.substring(2, lastInt);
                                String subInt = trimmed.substring(lastInt + 1);
                                Integer valueInt;
                                try {
                                    valueInt = Integer.decode(subInt);
                                } catch (NumberFormatException e) {
                                    throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                }
                                TDF_DATA datInt = new TDF_DATA(TDF_TYPE.TDF_INT, valueInt);
                                current.put(keyInt, datInt);
                                break;
                            case 'h':
                                int lastHex = trimmed.indexOf(' ', 3);
                                if (lastHex == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyHex = trimmed.substring(2, lastHex);
                                String subHex = trimmed.substring(lastHex + 1);
                                Integer valueHex;
                                try {
                                    valueHex = Integer.parseInt(subHex, 16);
                                } catch (NumberFormatException e) {
                                    throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                }
                                TDF_DATA datHex = new TDF_DATA(TDF_TYPE.TDF_HEX, valueHex);
                                current.put(keyHex, datHex);
                                break;
                            case 'b':
                                int lastBin = trimmed.indexOf(' ', 3);
                                if (lastBin == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyBin = trimmed.substring(2, lastBin);
                                String subBin = trimmed.substring(lastBin + 1);
                                Integer valueBin;
                                try {
                                    valueBin = Integer.parseInt(subBin, 2);
                                } catch (NumberFormatException e) {
                                    throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                }
                                TDF_DATA datBin = new TDF_DATA(TDF_TYPE.TDF_BINARY, valueBin);
                                current.put(keyBin, datBin);
                                break;
                            case 'B':
                                int lastBool = trimmed.indexOf(' ', 3);
                                if (lastBool == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyBool = trimmed.substring(2, lastBool);
                                if (lastBool + 1 >= trimmed.length()) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                char c = trimmed.charAt(lastBool + 1);
                                boolean valueBool = c == 'T';
                                if (lastBool + 2 < trimmed.length()) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                TDF_DATA datBool = new TDF_DATA(TDF_TYPE.TDF_BOOL, valueBool);
                                current.put(keyBool, datBool);
                                break;
                            case 'f':
                                int lastFloat = trimmed.indexOf(' ', 3);
                                if (lastFloat == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyFloat = trimmed.substring(2, lastFloat);
                                String subFloat = trimmed.substring(lastFloat + 1);
                                Float valueFloat;
                                try {
                                    valueFloat = Float.parseFloat(subFloat);
                                } catch (NumberFormatException e) {
                                    throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                }
                                TDF_DATA datFloat = new TDF_DATA(TDF_TYPE.TDF_FLOAT, valueFloat);
                                current.put(keyFloat, datFloat);
                                break;
                            case '@':
                                int lastPtr = trimmed.indexOf(' ', 3);
                                if (lastPtr == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                String keyPtr = trimmed.substring(2, lastPtr);
                                String valuePtr = trimmed.substring(lastPtr + 1);
                                List<String> ptr = new ArrayList<>();
                                int pos;
                                while ((pos = valuePtr.indexOf('.')) != -1) {
                                    ptr.add(valuePtr.substring(0, pos));
                                    valuePtr = valuePtr.substring(pos + 1);
                                }
                                ptr.add(valuePtr);
                                TDF_DATA datPtr = new TDF_DATA(TDF_TYPE.TDF_POINTER, ptr);
                                current.put(keyPtr, datPtr);
                                break;
                            case '{':
                                int lastClass = trimmed.indexOf(' ', 3);
                                String keyClass;
                                if (lastClass != -1) {
                                    keyClass = trimmed.substring(2, lastClass);
                                } else {
                                    keyClass = trimmed.substring(2);
                                }
                                HashMap<String, TDF_DATA> newmap = new HashMap<>();
                                TDF_DATA datClass = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                                current.put(keyClass, datClass);
                                List<String> definesNew = new ArrayList<>();
                                TDF_DATA defdatNew = new TDF_DATA(TDF_TYPE.TDF_DEFINES, definesNew);
                                newmap.put(TINYDEFINEHEADER, defdatNew);
                                stack.push(current);
                                pathvec.add(keyClass);
                                current = newmap;
                                break;
                            default:
                                if (customFuncs.containsKey(cchar)) {
                                    TDF_DATA datCustom = customFuncs.get(cchar).apply(trimmed);
                                    if (datCustom.data == null) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                    int lastCustom = trimmed.indexOf(' ', 3);
                                    if (lastCustom == -1) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                                    String keyCustom = trimmed.substring(2, lastCustom);
                                    current.put(keyCustom, datCustom);
                                }
                                break;
                        }
                    } else if (trimmed.equals("}")) {
                        if (stack.isEmpty()) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                        current = stack.pop();
                        if (!pathvec.isEmpty()) pathvec.remove(pathvec.size() - 1);
                    } else {
                        TDF_DATA it = current.get(TINYDEFINEHEADER);
                        if (it == null || it.type != TDF_TYPE.TDF_DEFINES) throw new TDF_ERR(TDF_ERROR.FAILED_TO_PARSE, filepath, Arrays.asList(String.valueOf(index)));
                        @SuppressWarnings("unchecked")
                        List<String> currentDefines = (List<String>) it.data;
                        currentDefines.add(line);
                    }
                }
            }
            return data;
        } catch (FileNotFoundException e) {
            throw new TDF_ERR(TDF_ERROR.FILE_NOT_FOUND, filepath, new ArrayList<>());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void warning(String msg) {
        System.err.println(msg);
    }

    public boolean defined(List<String> path) {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) return false;
            if (i == path.size() - 1) return true;
            if (it.type != TDF_TYPE.TDF_CLASS) return false;
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        return false;
    }

    public boolean defined(List<String> path, boolean defaultval) {
        return defined(path);
    }

    public boolean getbool(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_BOOL) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (Boolean) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public boolean getbool(List<String> path, boolean defaultval) {
        try {
            return getbool(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public int getint(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_INT && it.type != TDF_TYPE.TDF_HEX && it.type != TDF_TYPE.TDF_BINARY)
                    throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (Integer) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public int getint(List<String> path, int defaultval) {
        try {
            return getint(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public String getstring(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_STR && it.type != TDF_TYPE.TDF_BLOCK)
                    throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (String) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public String getstring(List<String> path, String defaultval) {
        try {
            return getstring(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public List<String> getpointer(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_POINTER) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (List<String>) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public List<String> getpointer(List<String> path, List<String> defaultval) {
        try {
            return getpointer(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public char getchar(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_CHAR) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (Character) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public char getchar(List<String> path, char defaultval) {
        try {
            return getchar(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public float getfloat(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_FLOAT) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (Float) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public float getfloat(List<String> path, float defaultval) {
        try {
            return getfloat(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public HashMap<String, TDF_DATA> getclass(List<String> path) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
            if (i == path.size() - 1) {
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
                return (HashMap<String, TDF_DATA>) it.data;
            }
            if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.GET_MISMATCH, filepath, path);
            current = (HashMap<String, TDF_DATA>) it.data;
        }
        throw new TDF_ERR(TDF_ERROR.PATH_NOT_FOUND, filepath, path);
    }

    public HashMap<String, TDF_DATA> getclass(List<String> path, HashMap<String, TDF_DATA> defaultval) {
        try {
            return getclass(path);
        } catch (TDF_ERR e) {
            warning(e.getMessage());
            return defaultval;
        }
    }

    public void setbool(List<String> path, boolean value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_BOOL, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_BOOL) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setint(List<String> path, int value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_INT, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_INT) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setstring(List<String> path, String value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_STR, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_STR) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setblock(List<String> path, String value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_BLOCK, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_BLOCK) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setfloat(List<String> path, float value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_FLOAT, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_FLOAT) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setchar(List<String> path, char value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CHAR, value);
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_CHAR) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = value;
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void setpointer(List<String> path, List<String> value) throws TDF_ERR {
        HashMap<String, TDF_DATA> current = data;
        for (int i = 0; i < path.size(); i++) {
            TDF_DATA it = current.get(path.get(i));
            if (it == null) {
                if (i == path.size() - 1) {
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_POINTER, new ArrayList<>(value));
                    current.put(path.get(i), dat);
                    return;
                } else {
                    HashMap<String, TDF_DATA> newmap = new HashMap<>();
                    TDF_DATA dat = new TDF_DATA(TDF_TYPE.TDF_CLASS, newmap);
                    current.put(path.get(i), dat);
                    current = newmap;
                }
            } else {
                if (i == path.size() - 1) {
                    if (it.type != TDF_TYPE.TDF_POINTER) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                    it.data = new ArrayList<>(value);
                    return;
                }
                if (it.type != TDF_TYPE.TDF_CLASS) throw new TDF_ERR(TDF_ERROR.SET_MISMATCH, filepath, path);
                current = (HashMap<String, TDF_DATA>) it.data;
            }
        }
    }

    public void define(List<String> defines, String name) {
        TDF_DATA it = data.get(TINYDEFINEHEADER);
        if (it == null || it.type != TDF_TYPE.TDF_DEFINES) {
            List<String> newdefines = new ArrayList<>();
            TDF_DATA defdat = new TDF_DATA(TDF_TYPE.TDF_DEFINES, newdefines);
            data.put(TINYDEFINEHEADER, defdat);
            it = data.get(TINYDEFINEHEADER);
        }
        @SuppressWarnings("unchecked")
        List<String> currentdefines = (List<String>) it.data;
        for (String def : defines) {
            currentdefines.add(def);
        }
        currentdefines.add("#define " + name);
    }

    public void save() throws IOException {
        try (FileWriter file = new FileWriter(filepath, false)) {
            StringBuilder dataStr = new StringBuilder();
            crawl(data, dataStr, "");
            file.write(dataStr.toString());
        }
    }

    private void crawl(HashMap<String, TDF_DATA> map, StringBuilder str, String indent) {
        TDF_DATA it = map.get(TINYDEFINEHEADER);
        if (it != null && it.type == TDF_TYPE.TDF_DEFINES) {
            @SuppressWarnings("unchecked")
            List<String> defines = (List<String>) it.data;
            for (String def : defines) {
                str.append(def).append("\n");
            }
        }
        for (Map.Entry<String, TDF_DATA> entry : map.entrySet()) {
            String key = entry.getKey();
            TDF_DATA val = entry.getValue();
            if (key.equals(TINYDEFINEHEADER)) continue;
            switch (val.type) {
                case TDF_STR:
                    str.append("\" ").append(key).append(" ").append((String) val.data).append("\n");
                    break;
                case TDF_CHAR:
                    str.append("' ").append(key).append(" ").append((Character) val.data).append("\n");
                    break;
                case TDF_BLOCK:
                    str.append("S ").append(key).append("\n").append((String) val.data).append("\\\n");
                    break;
                case TDF_BOOL:
                    str.append("B ").append(key).append(" ").append(((Boolean) val.data) ? "T" : "F").append("\n");
                    break;
                case TDF_BINARY:
                    str.append("b ").append(key).append(" ").append(Integer.toBinaryString((Integer) val.data)).append("\n");
                    break;
                case TDF_INT:
                    str.append("i ").append(key).append(" ").append((Integer) val.data).append("\n");
                    break;
                case TDF_FLOAT:
                    str.append("f ").append(key).append(" ").append((Float) val.data).append("\n");
                    break;
                case TDF_HEX:
                    str.append("h ").append(key).append(" ").append(Integer.toHexString((Integer) val.data)).append("\n");
                    break;
                case TDF_POINTER:
                    @SuppressWarnings("unchecked")
                    List<String> ptr = (List<String>) val.data;
                    str.append("@ ").append(key).append(" ");
                    for (int i = 0; i < ptr.size(); i++) {
                        str.append(ptr.get(i));
                        if (i < ptr.size() - 1) str.append(".");
                    }
                    str.append("\n");
                    break;
                case TDF_CLASS:
                    str.append("{ ").append(key).append("\n");
                    crawl((HashMap<String, TDF_DATA>) val.data, str, "");
                    str.append("}\n");
                    break;
            }
        }
    }

    // Method to add custom functions if needed
    public void addCustomFunc(char key, Function<String, TDF_DATA> func) {
        customFuncs.put(key, func);
    }
}
