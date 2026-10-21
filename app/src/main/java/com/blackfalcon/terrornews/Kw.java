package com.blackfalcon.terrornews;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class Kw {
    private static final HashMap<String, Pattern> CACHE = new HashMap<String, Pattern>();

    public static boolean any(String text, String csv) {
        if (text == null || csv == null) return false;
        String[] parts = csv.split("[,\u060C]");
        for (String k : parts) {
            if (one(text, k.trim())) return true;
        }
        return false;
    }

    // Short words (<=4 chars) match as whole words; ALL-CAPS ones are case-sensitive (IS, TTP).
    // Longer words match anywhere, ignoring case.
    static boolean one(String text, String kw) {
        if (kw.length() == 0) return false;
        if (kw.length() <= 4) {
            Pattern pt = CACHE.get(kw);
            if (pt == null) {
                boolean upper = kw.equals(kw.toUpperCase(Locale.ROOT))
                        && !kw.equals(kw.toLowerCase(Locale.ROOT));
                String pat = "(?<![\\p{L}\\p{N}])" + Pattern.quote(kw) + "(?![\\p{L}\\p{N}])";
                int flags = upper ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                pt = Pattern.compile(pat, flags);
                CACHE.put(kw, pt);
            }
            return pt.matcher(text).find();
        }
        return text.toLowerCase(Locale.ROOT).contains(kw.toLowerCase(Locale.ROOT));
    }
}
