package com.github.t1.yaml.tools;

public class Tools {
    // 1000 should be enough
    private static final String SPACES = "" +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    " +
            "                                                                                                    ";

    public static String spaces(int indent) {
        assert SPACES.length() == 1000 : "actually " + SPACES.length();
        assert indent <= SPACES.length();
        return SPACES.substring(0, indent);
    }

    public static String decodeHex(String text) {
        return new String(new int[]{Integer.decode("0x" + text)}, 0, 1);
    }
}
