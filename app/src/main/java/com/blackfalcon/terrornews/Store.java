package com.blackfalcon.terrornews;

import android.content.Context;
import android.content.SharedPreferences;

public class Store {
    public static final String DEF_ACCOUNTS =
        "OfficialDGISPR,khorasandiary,dawn_com,AJEnglish,BBCWorld,Reuters,LongWarJournal,etribune,BBCUrdu";
    public static final String DEF_KEYWORDS =
        "Bajaur,TTP,IS,ISKP,Taliban,IMU,IMP,Daesh,Islamic State,Khorasan,Khawarij,terror,terrorist,"
        + "terrorism,militant,suicide,blast,bomb,al-Qaeda,Waziristan,"
        + "باجوڑ,ٹی ٹی پی,طالبان,داعش,خوارج,دہشت گرد,دہشتگرد,دھماکہ";

    private final SharedPreferences p;

    public Store(Context c) {
        p = c.getSharedPreferences("terrornews", Context.MODE_PRIVATE);
    }

    public String lang() { return p.getString("lang", "ur"); }
    public void setLang(String l) { p.edit().putString("lang", l).apply(); }
    public boolean dark() { return p.getBoolean("dark", true); }
    public void setDark(boolean d) { p.edit().putBoolean("dark", d).apply(); }
    public String accounts() { return p.getString("acc", DEF_ACCOUNTS); }
    public void setAccounts(String s) { p.edit().putString("acc", s).apply(); }
    public String keywords() { return p.getString("kw", DEF_KEYWORDS); }
    public void setKeywords(String s) { p.edit().putString("kw", s).apply(); }
    public boolean rtl() { return !lang().equals("en"); }

    public String tr(String ur, String en, String ar) {
        String l = lang();
        if (l.equals("en")) return en;
        if (l.equals("ar")) return ar;
        return ur;
    }
}
