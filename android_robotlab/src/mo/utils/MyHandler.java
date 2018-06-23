package mo.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.widget.ImageView;

import org.ros.android.robotlab.R;

import mo.MainActivity;
import mo.Socket.HomeServe;
import mo.database.model.Teacher;

/**
 * Created by wu1ji on 2018/1/8.
 */

public class MyHandler extends Handler {
    private Activity activity;
    private ImageView voice_imgeView, mainpic;

    public MyHandler(Activity activity) {
        this.activity = activity;
        voice_imgeView = (ImageView) activity.findViewById(R.id.voice_imgeView);
        mainpic = (ImageView) activity.findViewById(R.id.mainpic);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case R.integer.listen: {
                MainActivity.headIntIndex = true;
                MainActivity.headData = 2;
                if (HomeServe.sfs != null) {
                    synchronized (HomeServe.sfs.getSd().lock) {
                        HomeServe.sfs.getSd().content = "聆听";//unity开始倾听
                        System.out.println("发送站立");
                        HomeServe.sfs.getSd().lock.notify();
                    }
                }
                break;
            }
            case R.integer.speark: {
                if (HomeServe.sfs != null) {
                    synchronized (HomeServe.sfs.getSd().lock) {
                        HomeServe.sfs.getSd().content = "谈话";//unity开始倾听
                        System.out.println("发送站立");
                        HomeServe.sfs.getSd().lock.notify();
                    }
                }
                break;
            }
            case R.integer.stand: {
                MainActivity.headIntIndex = true;
                MainActivity.headData = 2;
                if (HomeServe.sfs != null) {
                    synchronized (HomeServe.sfs.getSd().lock) {
                        HomeServe.sfs.getSd().content = "立正";//unity开始倾听
                        System.out.println("发送站立");
                        HomeServe.sfs.getSd().lock.notify();
                    }
                }
                break;
            }

        }
    }

}
	
