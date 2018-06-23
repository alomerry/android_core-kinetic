package mo.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by wu1ji on 2018/3/27.
 */

public class CloudRobot {
    public static String msApi = "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/561f919d-a543-4a65-82cc-6a7faf7bc77c?subscription-key=57fcfc42c2b342dea65fd89bd3f6019f&spellCheck=true&bing-spell-check-subscription-key={YOUR_BING_KEY_HERE}&verbose=true&timezoneOffset=480&q=";
    public static String tulingApi = "http://forrima.azurewebsites.net/api/Talk?luis=";
    private String request = "";
    private String respone = "";
    URL url = null;

    public CloudRobot(String request) {
        try {
            this.request = URLEncoder.encode(request, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getLocationByMSApi() throws IOException, JSONException {
        url = new URL(msApi + request);
        System.out.println("url是：" + url);
        respone = getWebInputStream(url);
        return resolveWithJsonByMSApi(respone);
    }

    public String getWordsByPersonalTuLing() throws IOException, JSONException {
        url = new URL(tulingApi + request);
        respone = getWebInputStream(url);
        System.out.println("respone的内容是：" + respone);
        return resolveWithJsonByPersonalTuLing(respone);
    }

    private String resolveWithJsonByMSApi(String content) throws JSONException {
        JSONObject obj = new JSONObject(content);
        JSONArray entities = obj.getJSONArray("entities");
        JSONObject topScoringIntent = obj.getJSONObject("topScoringIntent");
        JSONObject resolution = null;
        JSONArray temp = null;
        if (topScoringIntent.get("intent").equals("命令导航")) {
            return getEntities(entities, resolution, temp);
        } else if (topScoringIntent.get("intent").equals("命令动画")) {
            return "***" + getEntities(entities, resolution, temp);
        } else {
            return "";
        }
    }

    private String getEntities(JSONArray entities, JSONObject resolution, JSONArray temp) throws JSONException {
        String content = "";
        for (int i = 0; i < entities.length(); i++) {
            content = entities.getJSONObject(i).get("type").toString();
            resolution = new JSONObject(entities.getJSONObject(i).get("resolution").toString());
            temp = resolution.getJSONArray("values");

        }
        if (temp == null) {
            return "";
        }
        for (int i = 0; i < temp.length(); i++) {
            return temp.get(i).toString();
        }
        return null;
    }

    private String resolveWithJsonByPersonalTuLing(String content) throws JSONException {
        //{"info":"不急、还有28天、也就是四个星期！.","model":"rima"}
        JSONObject obj = new JSONObject(content);
        return obj.get("info").toString();
    }

    private String getWebInputStream(URL url) throws IOException {
        HttpURLConnection conn;
        conn = (HttpURLConnection) url.openConnection();
        // 设置允许输出
        //conn.setDoOutput(true);
        // 设置传递方式
        conn.setRequestMethod("GET");
        // 设置文件字符集:
        conn.setRequestProperty("Charset", "UTF-8");
        // 开始连接请求
        conn.connect();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        //创建StringBuffer对象用于存储所有数据
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String content = sb.toString();
        System.out.println(content);
        return content;
    }
}
