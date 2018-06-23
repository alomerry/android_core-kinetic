package mo.listener;

import org.ros.android.robotlab.R;

import mo.Socket.HomeServe;
import mo.utils.MyHandler;
import mo.utils.ToastTool;

/**
 * Created by wu1ji on 2018/3/27.
 */

public class MySpeechListener extends MessageListener {
    MyHandler myHandler;

    public MySpeechListener(MyHandler myHandler) {
        this.myHandler = myHandler;
    }

    @Override
    public void onSpeechStart(String utteranceId) {
        super.onSpeechStart(utteranceId);
        System.out.println("在MySpeechListener接受到语音播放开始的回调");
//        if(HomeServe.sfs!=null){
//            synchronized (HomeServe.sfs.getSd().lock){
//                HomeServe.sfs.getSd().content="2";//"谈话";//unity开始倾听
//                HomeServe.sfs.getSd().lock.notify();
//            }
//        }
        myHandler.sendEmptyMessage(R.integer.speark);
    }

    @Override
    public void onSpeechFinish(String utteranceId) {
        super.onSpeechFinish(utteranceId);
        System.out.println("在MySpeechListener接受到语音播放结束的回调");
//        if(HomeServe.sfs!=null){
//            synchronized (HomeServe.sfs.getSd().lock){
//                HomeServe.sfs.getSd().content="3";//"站立";//unity开始倾听
//                HomeServe.sfs.getSd().lock.notify();
//            }
//        }
        myHandler.sendEmptyMessage(R.integer.stand);
    }
}
