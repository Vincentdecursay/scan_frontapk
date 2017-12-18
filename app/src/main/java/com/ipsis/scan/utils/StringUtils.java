package com.ipsis.scan.utils;

/**
 * Created by pobouteau on 10/5/16.
 */

public class StringUtils {
    public static int indexAfter(String string, String search) {
        int index = string.indexOf(search);
        if (index == -1) {
            return -1;
        } else {
            return index + search.length();
        }
    }

    private static String capitalizeAfterChar(String symbol, String string) {
        String[] tokens = string.split(symbol);
        String result = "";

        for (String token : tokens) {
            if (!token.equals("RER")) {
                if (token.length() > 1) {
                    result += token.substring(0, 1).toUpperCase();
                }

                if (token.length() > 1) {
                    result += token.substring(1, token.length());
                }

                result += symbol;
            }
        }

        if (result.length() > 1) {
            return result.substring(0, result.length() - 1);
        } else {
            return result;
        }
    }

    public static String capitalizeLineName(String string) {
        String[] tokens = string.split(" ");
        String result = "";

        for (String token : tokens) {
            if (!token.equals("RER")) {
                String lowercase = token.toLowerCase();
                if (lowercase.length() > 0) {
                    result += lowercase.substring(0, 1).toUpperCase();
                }

                if (lowercase.length() > 1) {
                    result += lowercase.substring(1, lowercase.length());
                }

                result += " ";
            } else {
                result += token + " ";
            }
        }

        if (result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }

        return capitalizeAfterChar("'", capitalizeAfterChar("-", result));
    }
}
