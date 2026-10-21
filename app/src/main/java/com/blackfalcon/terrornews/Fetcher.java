package com.blackfalcon.terrornews;

import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Fetcher {
    static final String UA =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) "
        + "Chrome/120.0.0.0 Mobile Safari/537.36";

    static String http(String u) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setRequestProperty("User-Agent", UA);
            c.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            c.setInstanceFollowRedirects(true);
            if (c.getResponseCode() != 200) return null;
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
            r.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    static String clean(String s) {
        s = s.replaceAll("https?://t\\.co/\\S+", "");
        s = s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
             .replace("&quot;", "\"").replace("&#39;", "'");
        return s.trim();
    }

    static String fmt(long t) {
        if (t <= 0) return "";
        return new SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH).format(new Date(t));
    }

    // Public X (Twitter) profile timeline, no login (syndication endpoint).
    public static ArrayList<Item> account(String acc) {
        ArrayList<Item> out = new ArrayList<Item>();
        try {
            String html = http("https://syndication.twitter.com/srv/timeline-profile/screen-name/" + acc);
            if (html == null) return out;
            int a = html.indexOf("__NEXT_DATA__");
            if (a < 0) return out;
            int s = html.indexOf('>', a) + 1;
            int e = html.indexOf("</script>", s);
            if (s <= 0 || e < 0) return out;
            JSONObject root = new JSONObject(html.substring(s, e));
            JSONArray en = root.getJSONObject("props").getJSONObject("pageProps")
                    .getJSONObject("timeline").getJSONArray("entries");
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
            for (int i = 0; i < en.length(); i++) {
                JSONObject o = en.optJSONObject(i);
                if (o == null) continue;
                JSONObject c = o.optJSONObject("content");
                if (c == null) continue;
                JSONObject t = c.optJSONObject("tweet");
                if (t == null) continue;
                String text = clean(t.optString("full_text", t.optString("text", "")));
                if (text.length() == 0) continue;
                String id = t.optString("id_str", "");
                JSONObject u = t.optJSONObject("user");
                String sn = u != null ? u.optString("screen_name", acc) : acc;
                String name = u != null ? u.optString("name", sn) : sn;
                Item it = new Item();
                it.src = name + "  @" + sn;
                it.text = text;
                it.id = id;
                it.url = "https://x.com/" + sn + "/status/" + id;
                try {
                    Date d = df.parse(t.optString("created_at", ""));
                    if (d != null) it.time = d.getTime();
                } catch (Exception ex) { }
                it.date = fmt(it.time);
                out.add(it);
            }
        } catch (Exception e) { }
        return out;
    }

    // Backup: Google News RSS (used only when X returns too little).
    public static ArrayList<Item> news() {
        ArrayList<Item> out = new ArrayList<Item>();
        try {
            String q = "terrorism OR terrorist OR TTP OR ISKP OR Bajaur OR Taliban OR Waziristan when:7d";
            String xml = http("https://news.google.com/rss/search?q=" + URLEncoder.encode(q, "UTF-8")
                    + "&hl=en-US&gl=US&ceid=US:en");
            if (xml == null) return out;
            XmlPullParser xp = Xml.newPullParser();
            xp.setInput(new StringReader(xml));
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            int ev = xp.getEventType();
            boolean inItem = false;
            String title = null, link = null, date = null;
            while (ev != XmlPullParser.END_DOCUMENT) {
                if (ev == XmlPullParser.START_TAG) {
                    String tag = xp.getName();
                    if ("item".equals(tag)) {
                        inItem = true;
                        title = null; link = null; date = null;
                    } else if (inItem) {
                        if ("title".equals(tag)) title = xp.nextText();
                        else if ("link".equals(tag)) link = xp.nextText();
                        else if ("pubDate".equals(tag)) date = xp.nextText();
                    }
                } else if (ev == XmlPullParser.END_TAG) {
                    if ("item".equals(xp.getName()) && inItem) {
                        inItem = false;
                        if (title != null && link != null) {
                            Item it = new Item();
                            int k = title.lastIndexOf(" - ");
                            if (k > 0) {
                                it.src = title.substring(k + 3);
                                it.text = title.substring(0, k);
                            } else {
                                it.src = "News";
                                it.text = title;
                            }
                            it.url = link;
                            it.web = true;
                            try {
                                Date d = df.parse(date == null ? "" : date);
                                if (d != null) it.time = d.getTime();
                            } catch (Exception ex) { }
                            it.date = fmt(it.time);
                            out.add(it);
                        }
                    }
                }
                ev = xp.next();
            }
        } catch (Exception e) { }
        return out;
    }
}
