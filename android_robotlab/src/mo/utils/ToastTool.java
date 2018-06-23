package mo.utils;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by wu1ji on 2017/12/6.
 */

public class ToastTool {
    public static String checkInput(String content) {
        int contentLength = content.length();
        if (contentLength <= 0) {
            return "没有输入";
        }
        char[] items = content.toCharArray();
        for (char item : items) {
            if (item < 33 || item > 126)
                return "信息有误";
        }
        return null;
    }

    public static final long toastShowTime = 700;

    public static void showToast(final Activity activity, final String word, final long time) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, time);
            }
        });
    }
}
