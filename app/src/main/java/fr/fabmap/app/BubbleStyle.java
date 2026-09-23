package fr.fabmap.app;

import android.graphics.Color;
import org.json.JSONObject;
import java.util.Locale;

/** User-defined cognitive meaning never overwrites the descriptive meaning. */
final class BubbleStyle {
    private BubbleStyle(){}
    static String hex(String raw){
        if(raw==null)return null;
        String s=raw.trim().toUpperCase(Locale.ROOT);
        if(s.matches("#[0-9A-F]{6}"))return s;
        if(s.matches("[0-9A-F]{6}"))return "#"+s;
        return null;
    }
    static int userColor(JSONObject node,int fallback){
        if(node==null)return fallback;
        String s=hex(node.optString("cognitiveColor",""));
        return s==null?fallback:Color.parseColor(s);
    }
    static int blend(int a,int b,float amount){
        float t=Math.max(0,Math.min(1,amount)),q=1-t;
        return Color.rgb(Math.round(Color.red(a)*q+Color.red(b)*t),
            Math.round(Color.green(a)*q+Color.green(b)*t),
            Math.round(Color.blue(a)*q+Color.blue(b)*t));
    }
    static String preview(JSONObject node){
        if(node==null)return "";
        String emotion=node.optString("cognitiveFeelings","").trim();
        String evocation=node.optString("cognitiveEvocation","").trim();
        StringBuilder text=new StringBuilder(node.optString("description",""));
        if(!emotion.isEmpty())text.append("\nRessenti personnel : ").append(emotion);
        if(!evocation.isEmpty())text.append("\nÉvocation : ").append(evocation);
        return text.toString().trim();
    }
}
