package com.picturecapture;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by 明白 on 2016/5/5.
 */
public class ParseHtml {

    public static Vector<String> getImagePaths(String htmlPath) {
        if(!htmlPath.contains("http"))
            htmlPath="http://"+htmlPath;
        if(!htmlPath.contains("/"))
            htmlPath+="/";
        final Vector<String> vecPath = new Vector<>();
        final String rootPath = htmlPath;
        String html = null;
        try {
            html = getHtmlCode(htmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (html != null) {
            Html.fromHtml(html, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    Log.e("soure", source);
                    if(!source.contains("http"))
                    {
                        source = rootPath+source;
                    }
                    vecPath.add(source);
                    return null;
                }
            }, null);
        } else {
            Log.e("error", "html=null");
        }
        return vecPath;
    }

    //根据网址下载html代码
    private static String getHtmlCode(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inStream = conn.getInputStream();

        byte[] data = readFromInput(inStream);
        String html = new String(data, "gbk");
        return html;
    }

    //从输入流读取数据
    private static byte[] readFromInput(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
}
