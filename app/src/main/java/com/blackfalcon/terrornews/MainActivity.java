package com.blackfalcon.terrornews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends Activity {

    static final String KW_FATA = "Bajaur,Waziristan,Khyber,Kurram,Tirah,Mohmand,Orakzai,Bannu,Tribal,FATA,Swat,"
        + "Pakhtunkhwa,KP,باجوڑ,وزیرستان,خیبر,کرم,قبائلی,فاٹا";
    static final String KW_TTP = "TTP,Tehreek-e-Taliban,Tehrik-i-Taliban,Khawarij,Fitna al,ٹی ٹی پی,خوارج,فتنہ الخوارج";
    static final String KW_IS = "ISKP,ISIS,IS,Daesh,Islamic State,Khorasan,داعش,خراسان";
    static final String KW_TALIBAN = "Taliban,Taleban,Haqqani,Islamic Emirate,طالبان,حقانی";
    static final int ACCENT = 0xFFD32F2F;

    Store st;
    int tab = 0;
    boolean loading = false;
    final ArrayList<Item> items = new ArrayList<Item>();
    LinearLayout listBox, setBox;
    ScrollView newsScroll, setScroll;
    TextView status;
    TextView[] tabViews;
    int cBg, cCard, cText, cSub;
    RadioGroup rgLang, rgTheme;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];
    EditText etAcc, etKw;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        buildUI();
        showTab(0);
        load();
    }

    String tr(String ur, String en, String ar) { return st.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    String[] tabNames() {
        return new String[] {
            tr("سب", "All", "الكل"),
            tr("فاٹا / باجوڑ", "FATA / Bajaur", "فاتا / باجور"),
            tr("ٹی ٹی پی", "TTP", "طالبان باكستان"),
            tr("داعش / ISKP", "IS / ISKP", "داعش / ISKP"),
            tr("طالبان", "Taliban", "طالبان"),
            tr("سیٹنگز", "Settings", "الإعدادات")
        };
    }

    void buildUI() {
        boolean dark = st.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(st.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        // header
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14), dp(14), dp(14), dp(6));
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("Terror News");
        title.setTextColor(cText);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        status = new TextView(this);
        status.setTextColor(cSub);
        status.setTextSize(12);
        titleBox.addView(title);
        titleBox.addView(status);
        head.addView(titleBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView refresh = new TextView(this);
        refresh.setText("\u27F3");
        refresh.setTextColor(Color.WHITE);
        refresh.setTextSize(24);
        refresh.setGravity(Gravity.CENTER);
        refresh.setBackground(round(ACCENT, 22));
        refresh.setOnClickListener(v -> load());
        head.addView(refresh, new LinearLayout.LayoutParams(dp(44), dp(44)));
        root.addView(head);

        // tabs
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(8), dp(4), dp(8), dp(8));
        String[] names = tabNames();
        tabViews = new TextView[names.length];
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            TextView tv = new TextView(this);
            tv.setText(names[i]);
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(14), dp(8), dp(14), dp(8));
            tv.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(4), 0, dp(4), 0);
            tabs.addView(tv, lp);
            tabViews[i] = tv;
        }
        hs.addView(tabs);
        root.addView(hs, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // content
        FrameLayout content = new FrameLayout(this);
        newsScroll = new ScrollView(this);
        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setPadding(0, dp(4), 0, dp(8));
        newsScroll.addView(listBox);
        setScroll = new ScrollView(this);
        setBox = new LinearLayout(this);
        setBox.setOrientation(LinearLayout.VERTICAL);
        setBox.setPadding(dp(14), dp(8), dp(14), dp(16));
        setScroll.addView(setBox);
        content.addView(newsScroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(setScroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // credit
        TextView credit = new TextView(this);
        credit.setText("By: Black Falcon \uD83E\uDD85");
        credit.setTextColor(cSub);
        credit.setTextSize(13);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(8), 0, dp(10));
        root.addView(credit);

        buildSettings();
        setContentView(root);
    }

    boolean pass(Item it) {
        switch (tab) {
            case 0: return it.web || Kw.any(it.text, st.keywords());
            case 1: return Kw.any(it.text, KW_FATA);
            case 2: return Kw.any(it.text, KW_TTP);
            case 3: return Kw.any(it.text, KW_IS);
            case 4: return Kw.any(it.text, KW_TALIBAN);
            default: return false;
        }
    }

    void showTab(int i) {
        tab = i;
        for (int k = 0; k < tabViews.length; k++) {
            boolean sel = (k == i);
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? ACCENT : cCard, 20));
        }
        newsScroll.setVisibility(i == 5 ? View.GONE : View.VISIBLE);
        setScroll.setVisibility(i == 5 ? View.VISIBLE : View.GONE);
        if (i != 5) render();
    }

    void render() {
        listBox.removeAllViews();
        int n = 0;
        for (Item it : items) {
            if (!pass(it)) continue;
            listBox.addView(card(it));
            if (++n >= 80) break;
        }
        if (n == 0) {
            TextView e = new TextView(this);
            e.setTextColor(cSub);
            e.setTextSize(15);
            e.setGravity(Gravity.CENTER);
            e.setPadding(dp(20), dp(40), dp(20), dp(40));
            e.setText(loading ? "" : tr("کوئی خبر نہیں ملی۔ ریفریش (⟳) دبائیں۔",
                    "No news found. Tap refresh (\u27F3).", "لا توجد أخبار. اضغط تحديث (\u27F3)."));
            listBox.addView(e);
        }
    }

    View card(final Item it) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 12));
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(5), dp(10), dp(5));
        c.setLayoutParams(lp);

        TextView src = new TextView(this);
        src.setText(it.src);
        src.setTextColor(ACCENT);
        src.setTextSize(13);
        src.setTypeface(Typeface.DEFAULT_BOLD);
        TextView body = new TextView(this);
        body.setText(it.text);
        body.setTextColor(cText);
        body.setTextSize(15);
        body.setPadding(0, dp(4), 0, dp(4));
        TextView date = new TextView(this);
        date.setText(it.date);
        date.setTextColor(cSub);
        date.setTextSize(12);
        c.addView(src);
        c.addView(body);
        c.addView(date);
        c.setOnClickListener(v -> open(it));
        return c;
    }

    void open(Item it) {
        String u = it.url;
        if (it.id.length() > 0) {
            u = "https://platform.twitter.com/embed/Tweet.html?id=" + it.id
                    + "&theme=" + (st.dark() ? "dark" : "light");
        }
        Intent i = new Intent(this, WebActivity.class);
        i.putExtra("url", u);
        i.putExtra("alt", it.url);
        startActivity(i);
    }

    void load() {
        if (loading) return;
        loading = true;
        status.setText(tr("خبریں لوڈ ہو رہی ہیں…", "Loading news…", "جارٍ تحميل الأخبار…"));
        final String accCsv = st.accounts();
        new Thread(() -> {
            final ArrayList<Item> res = new ArrayList<Item>();
            ArrayList<Thread> ts = new ArrayList<Thread>();
            for (String a : accCsv.split("[,\u060C\\s]+")) {
                final String acc = a.trim().replace("@", "");
                if (acc.length() == 0) continue;
                Thread t = new Thread(() -> {
                    ArrayList<Item> l = Fetcher.account(acc);
                    synchronized (res) { res.addAll(l); }
                });
                ts.add(t);
                t.start();
            }
            for (Thread t : ts) {
                try { t.join(); } catch (InterruptedException e) { }
            }
            final boolean usedWeb = res.size() < 5;
            if (usedWeb) res.addAll(Fetcher.news());
            Collections.sort(res, (x, y) -> Long.compare(y.time, x.time));
            final ArrayList<Item> out = new ArrayList<Item>();
            HashSet<String> seen = new HashSet<String>();
            for (Item it : res) {
                String k = it.text.length() > 60 ? it.text.substring(0, 60) : it.text;
                if (seen.add(k)) out.add(it);
            }
            runOnUiThread(() -> {
                items.clear();
                items.addAll(out);
                loading = false;
                String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date());
                status.setText(tr("تازہ ترین: ", "Updated: ", "آخر تحديث: ") + time
                        + "  \u2022  " + out.size() + (usedWeb ? "  (Web)" : "  (X)"));
                render();
            });
        }).start();
    }

    // ---------- Settings ----------
    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(cText);
        t.setTextSize(16);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(14), 0, dp(6));
        return t;
    }

    RadioGroup radios(String[] labels, int checked, int[] ids) {
        RadioGroup g = new RadioGroup(this);
        g.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    EditText edit(String val, String hint) {
        EditText e = new EditText(this);
        e.setText(val);
        e.setHint(hint);
        e.setTextColor(Color.BLACK);
        e.setHintTextColor(0xFF777777);
        e.setTextSize(14);
        e.setBackground(round(0xFFFFFFFF, 8));
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        e.setMinLines(2);
        e.setGravity(Gravity.TOP);
        return e;
    }

    Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(ACCENT, 10));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(12), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    void buildSettings() {
        setBox.removeAllViews();

        setBox.addView(label(tr("زبان", "Language", "اللغة")));
        String l = st.lang();
        int li = l.equals("en") ? 1 : (l.equals("ar") ? 2 : 0);
        rgLang = radios(new String[] {"اردو", "English", "العربية"}, li, langIds);
        setBox.addView(rgLang);

        setBox.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {
            tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")
        }, st.dark() ? 0 : 1, themeIds);
        setBox.addView(rgTheme);

        setBox.addView(label(tr("X اکاؤنٹس (کومہ سے الگ، @ کے بغیر)",
                "X accounts (comma separated, no @)", "حسابات X (مفصولة بفاصلة، بدون @)")));
        etAcc = edit(st.accounts(), "OfficialDGISPR,dawn_com");
        setBox.addView(etAcc);

        setBox.addView(label(tr("کی ورڈز (کومہ سے الگ)", "Keywords (comma separated)",
                "الكلمات المفتاحية (مفصولة بفاصلة)")));
        etKw = edit(st.keywords(), "Bajaur,TTP,ISKP");
        setBox.addView(etKw);

        setBox.addView(btn(tr("محفوظ کریں", "Save", "حفظ"), v -> saveSettings()));
        setBox.addView(btn(tr("ڈیفالٹ بحال کریں", "Restore defaults", "استعادة الافتراضي"), v -> {
            st.setAccounts(Store.DEF_ACCOUNTS);
            st.setKeywords(Store.DEF_KEYWORDS);
            etAcc.setText(Store.DEF_ACCOUNTS);
            etKw.setText(Store.DEF_KEYWORDS);
            Toast.makeText(this, tr("بحال ہو گیا", "Restored", "تمت الاستعادة"), Toast.LENGTH_SHORT).show();
        }));

        TextView note = new TextView(this);
        note.setText(tr(
            "نوٹ: X کی عوامی ٹائم لائنز بغیر لاگ اِن دکھائی جاتی ہیں۔ X کبھی بھی رسائی روک یا محدود کر سکتا ہے؛ ایسی صورت میں ایپ خود بخود ویب نیوز (Google News) دکھاتی ہے۔ خبروں کی درستی ذریعہ کی ذمہ داری ہے۔",
            "Note: public X timelines are shown without login. X may limit or block access at any time; the app then falls back to web news (Google News) automatically. Accuracy of each post is the source's responsibility.",
            "ملاحظة: تُعرض الخطوط الزمنية العامة في X دون تسجيل دخول. قد تقيّد X الوصول في أي وقت، وعندها يعرض التطبيق أخبار الويب (Google News) تلقائيًا. مسؤولية صحة المنشورات على مصدرها."));
        note.setTextColor(cSub);
        note.setTextSize(12);
        note.setPadding(0, dp(18), 0, 0);
        setBox.addView(note);
    }

    void saveSettings() {
        String[] langs = {"ur", "en", "ar"};
        int cur = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < langIds.length; i++) {
            if (langIds[i] == cur) st.setLang(langs[i]);
        }
        st.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        String a = etAcc.getText().toString().trim();
        if (a.length() > 0) st.setAccounts(a);
        String k = etKw.getText().toString().trim();
        if (k.length() > 0) st.setKeywords(k);
        buildUI();
        showTab(0);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
        load();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟", "Do you really want to exit?",
                    "هل تريد الخروج فعلًا؟"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (d, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}
