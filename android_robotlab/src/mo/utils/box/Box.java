package mo.utils.box;

import org.json.JSONException;
import org.json.JSONObject;

public class Box {
    private static Box box;
    public static Object objectLock = new Object();
    //    private static String[] send = {"给","递给","送给","送到"};
    private static String[] send = {"给", "送到"};
    private static String[] name = {"张成威", "吴锦诚", "赵进奇", "卢中鹤", "阿卡贝"};
    public static Thread thread;
    public static BoxRightWay boxRightWay;

    public void setText_1(String text_1) {
        this.text_1 = text_1;
    }

    private String text_1 = "";

    private Box() {

    }

    public static Box newInstence() {
        if (box != null) {
//            throw new RuntimeException("Box已经存在对象，请勿多次创建");//线程锁
//            synchronized (objectLock){
//                try {
//                    objectLock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            box = new Box();
            return box;
        } else {
            box = new Box();
            return box;
        }
    }

    public String getResult() throws JSONException {
        String url = "https://aip.baidubce.com/rpc/2.0/nlp/v2/simnet";
        double high = 0, score = 0;
        /*定位*/
        String location = "";
        int index = 0;
        int a1 = send.length;
        int b1 = name.length;
        for (int i = 0; i < a1; i++) {
            for (int k = 0; k < b1; k++) {
                JSONObject jsonText = new JSONObject();
                jsonText.put("text_1", text_1);
                jsonText.put("text_2", "把它" + send[i] + name[k]);
                HttpUtils httpUtils = new HttpUtils();
                try {
                    String result = httpUtils.post(url, GetTok.getAuth(), jsonText.toString());
                    //System.out.println(result);
                    jsonText = new JSONObject(result);
                    score = (double) jsonText.get("score");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                index = (high < score) ? k : index;
                high = (high < score) ? score : high;
            }
        }
//        if(high>0.6 && text_1.contains("张成威")){
//            return "张成威";
//        }
        if (high > 0.7) {
            System.out.println(name[index]);
            return name[index];
        }
        return "";
    }
}
