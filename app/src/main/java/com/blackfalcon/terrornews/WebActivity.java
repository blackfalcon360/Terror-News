package com.blackfalcon.terrornews;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebActivity extends Activity {
    WebView wv;
    ProgressBar pb;
    String alt;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Store st = new Store(this);
        boolean dark = st.dark();
        int bg = dark ? 0xFF000000 : 0xFFF2F2F2;
        int fg = dark ? 0xFFFFFFFF : 0xFF111111;
        String url = getIntent().getStringExtra("url");
        alt = getIntent().getStringExtra("alt");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(8), dp(8), dp(8));
        bar.addView(barBtn("\u2715", fg, v -> finish()));
        bar.addView(barBtn("\u2190", fg, v -> { if (wv.canGoBack()) wv.goBack(); }));
        bar.addView(barBtn("\u27F3", fg, v -> wv.reload()));
        TextView x = barBtn("x.com", fg, v -> { if (alt != null && alt.length() > 0) wv.loadUrl(alt); });
        bar.addView(x);
        root.addView(bar);

        pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        root.addView(pb, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        wv = new WebView(this);
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                pb.setProgress(p);
                pb.setVisibility(p >= 100 ? android.view.View.GONE : android.view.View.VISIBLE);
            }
        });
        root.addView(wv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = new TextView(this);
        credit.setText("By: Black Falcon \uD83E\uDD85");
        credit.setTextColor(dark ? 0xFFAAAAAA : 0xFF555555);
        credit.setTextSize(12);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(6), 0, dp(8));
        root.addView(credit);

        setContentView(root);
        if (url != null) wv.loadUrl(url);
    }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    TextView barBtn(String s, int color, android.view.View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(18);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(14), dp(6), dp(14), dp(6));
        t.setOnClickListener(l);
        return t;
    }

    @Override
    public void onBackPressed() {
        if (wv.canGoBack()) wv.goBack();
        else finish();
    }

    @Override
    protected void onDestroy() {
        if (wv != null) wv.destroy();
        super.onDestroy();
    }
}
