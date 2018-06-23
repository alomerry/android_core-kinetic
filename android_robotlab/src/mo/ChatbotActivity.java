/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mo;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ros.android.robotlab.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import mo.Socket.HomeServe;
import mo.control.InitConfig;
import mo.control.MySyntherizer;
import mo.control.NonBlockSyntherizer;
import mo.listener.MySpeechListener;
import mo.utils.CloudRobot;
import mo.utils.MyHandler;
import mo.utils.OfflineResource;
import mo.utils.ToastTool;

import static java.lang.Thread.sleep;


public class ChatbotActivity extends Activity {
    /*数据库*/
    private static SQLiteDatabase mainDB;
    /*应用界面可接收发送回调Textview控件*/
    private TextView show_textview;
    /*发送输入指令button控件*/
    private Button send_button;
    /*用户输入命令的editText控件*/
    private EditText input_editText;
    private ImageView voice_imgeView, start_wakeup;//闪光小灯泡
    /**/
    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;
    /*消息处理Handler*/
    private MyHandler myHandler;
    /*发送节点*/
    /*语音识别、语音唤醒管理器*/
    private EventManager asr, wp;
    private MySpeechListener speechListener;
    /*语音监听器*/
    private EventListener wpListener;
    /*语音输出结果*/
    private String result = "";
    /*head结果*/
    public static short headData = -1;
    protected String offlineVoice = OfflineResource.VOICE_MALE;
    /*发送节点是否发送标记*/
    public static boolean sendstringIndex = false, headIntIndex = false;
    /*唤醒声音*/
    private MediaPlayer mp;

    public static String getInput_content() {
        return input_content;
    }

    /*存放键盘输入字符串*/
    public static String input_content = "";
    /*更新ui线程和检测唤醒线程*/
    private Thread checkWakeUp = null, runningUIupdate = null;
    /*灯泡闪烁标记*/
    private boolean runningUIupdateIndex = false;
    /*检测唤醒线程锁*/
    public Object checkWakeUpLock = new Object();
    /*语音结束唤醒*/
    private Object listenCloseLock = new Object();
    /*主控制类，所有合成控制方法从这个类开始*/
    protected MySyntherizer synthesizer;
    private HomeServe serve;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonemain);
        /*初始化控件*/
        initView();

        myHandler = new MyHandler(this);
        /*创建唤醒管理器实例*/
        wp = EventManagerFactory.create(this, "wp");
        /*创建语音识别管理器实例*/
        asr = EventManagerFactory.create(this, "asr");
        /*为唤醒设置监听*/
        wpListener = new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d("morizunzhu_wakeup", String.format("event: name=%s, params=%s", name, params));

                /*唤醒成功*/
                if (name.equals("wp.data")) {
                    try {
                        /*解析唤醒结果*/
                        JSONObject json = new JSONObject(params);
                        /*获取解析错误代码*/
                        int errorCode = json.getInt("errorCode");
                        /*唤醒成功*/
                        if (errorCode == 0) {
                            ToastTool.showToast(ChatbotActivity.this, "唤醒成功", 300);

                            /*播放唤醒音*/
                            mp = MediaPlayer.create(ChatbotActivity.this, R.raw.speech_on);
                            mp.start();
//                            SoundPool sp =new SoundPool(10240, AudioManager.STREAM_MUSIC,8);
//                            int id = sp.load(ChatbotActivity.this,R.raw.speech_on, 1);
//                            sp.play(id, 1F, 1F, 1, 0, 1F);
                            /*启动语音识别*/
                            EventListener myListener = new EventListener() {
                                @Override
                                public void onEvent(String name, String params, byte[] data, int offset, int length) {
                                    String logTxt = "name: " + name;
                                    if (params != null && !params.isEmpty()) {
                                        logTxt += ";params :" + params;
                                        Log.d("VoiceEvent", logTxt);
                                    }
                                    if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                                        /* 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了*/
                                        System.out.println("语音识别准备就绪");
                                        /*唤醒发送线程发送消息给客户端做动作*/

                                    }
                                    /*回调函数返回识别结果*/
                                    if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                                        /*解析最佳识别*/
                                        System.out.println(params);
                                        try {
                                            JSONObject father = new JSONObject(params);
                                            result = (String) father.get("best_result");
                                            System.out.println("识别结束，最佳识别内容是:" + result);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    /*识别结束*/
                                    if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                                        synchronized (listenCloseLock) {
                                            listenCloseLock.notify();
                                        }
                                        System.out.println("出现错误：错误参数" + params);
                                        if (params.contains("VAD detect no speech")) {
                                            System.out.println("用户没说话");
                                            result = "";
                                            input_content = "";
                                        }
                                    }
                                }
                            };
                            /*将此监听注册到语音识别管理器中*/
                            asr.registerListener(myListener);
                            /*释放线程锁，是检测唤醒词线程开始监听唤醒词*/
                            synchronized (checkWakeUpLock) {
                                checkWakeUpLock.notify();
                            }
                        } else {
                            /*唤醒失败*/
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                /*唤醒结束*/
                else if ("wp.exit".equals(name)) {
                    System.out.println("唤醒结束");
                }
            }
        };
        /*将唤醒监听注册进唤醒管理器中*/
        wp.registerListener(wpListener);


        /*初始化引擎*/
        initialTts();
        //StartServer();
    }

    /**
     * 初始化控件和对象
     */
    private void initView() {
        //voice_imgeView = (ImageView) this.findViewById(R.id.voice_imgeView);
        /*获取启动语音识别图片按钮控件*/
        start_wakeup = (ImageView) findViewById(R.id.start_wakeup);
        /*为启动按钮设置监听*/
        start_wakeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*如果灯泡闪烁标记是关闭并且灯泡线程不存在则创建*/
                if (runningUIupdate == null && !runningUIupdateIndex) {
                    runningUIupdateIndex = true;
                    runningUIupdate = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                /**/
                                while (runningUIupdateIndex) {
                                    myHandler.sendEmptyMessage(0x01);
                                    sleep(700);
                                    myHandler.sendEmptyMessage(0x02);
                                    sleep(700);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    runningUIupdate.start();
                } else {
                    runningUIupdateIndex = false;
                }
                if (checkWakeUp == null && runningUIupdateIndex) {
                    checkWakeUp = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (runningUIupdateIndex) {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");//"assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

                                //params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
                                //params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,true);
                                //params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
                                //params里 "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
                                String json = new JSONObject(params).toString();
                                //ToastTool.showToast(ChatbotActivity.this,"INFO" +  "wakeup params(反馈请带上此行日志):" + json,500);
                                Log.i("INFO" + ".Debug", "wakeup params(反馈请带上此行日志):" + json);
                                wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
                                synchronized (checkWakeUpLock) {
                                    try {
                                        checkWakeUpLock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                start();
                                synchronized (listenCloseLock) {
                                    try {
                                        listenCloseLock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                stop();
                            }
                        }
                    });
                    checkWakeUp.start();
                } else {
                    checkWakeUp = null;
                    runningUIupdateIndex = false;
                }
                /*隐藏Activity至后台运行*/
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        });

        //键盘输入指令
        input_editText = (EditText) this.findViewById(R.id.input_editText);
        /*文字指令发送按钮*/
        send_button = (Button) this.findViewById(R.id.send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_content = input_editText.getText().toString();
                input_editText.setText("");
                sendstringIndex = true;
            }
        });


        String sdlocation = Environment.getExternalStorageDirectory() + getResources().getString(R.string.sdLocation) +
                getResources().getString(R.string.DBName);
        System.out.println("sdlocation=" + sdlocation);
        mainDB = SQLiteDatabase.openOrCreateDatabase(sdlocation, null);
    }

    /**
     * 该方法用于启动语音识别
     */
    private void start() {
        myHandler.sendEmptyMessage(R.integer.listen);
        ToastTool.showToast(ChatbotActivity.this, "开始语音识别", 1000);
        if (!mp.isPlaying()) {
            mp.release();
        }
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        //  params.put(SpeechConstant.NLU, "enable");
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 800);
        //params.put(SpeechConstant.SOUND_END,R.raw.speech_on);
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        //  params.put(SpeechConstant.PROP ,20000);
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        String json = null; //可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);

    }

    /**
     * 该方法用于关闭语音识别
     */
    private void stop() {
        myHandler.sendEmptyMessage(R.integer.stand);


        ToastTool.showToast(ChatbotActivity.this, "语音识别结束，识别到：" + result, 500);
        System.out.println("语音识别结束，识别到：" + result);
        //将识别到的语句发送给图灵机器人

        callTuLing(result);
//        personalTuLing(result);
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
//        input_content = result;
//        sendstringIndex = true;
    }

    /**
     * @param result 语音识别到的文字
     */
    private void callTuLing(String result) {
        //按照api设置json字符串
        JSONObject obj = new JSONObject(), perception = new JSONObject(), inputText = new JSONObject(),
                userInfo = new JSONObject();
        try {
            inputText.put("text", result);
            perception.put("inputText", inputText);
            userInfo.put("apiKey", "e07da9808b6a4dea83c8e26a8d31664a");
            userInfo.put("userId", "ff00253d88d22a07");

            obj.put("perception", perception);
            obj.put("userInfo", userInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(obj);
        String content = "";
        // 创建url资源
        URL url = null;
        //向web-api post请求
        try {
            url = new URL("http://openapi.tuling123.com/openapi/api/v2");
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            // 转换为字节数组
            byte[] data = (obj.toString()).getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "json");
            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write((obj.toString()).getBytes());
            out.flush();
            out.close();
            System.out.println(conn.getResponseCode());
            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                System.out.println("连接图灵成功");
                // 请求返回的数据
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String strRead = null;
                StringBuffer sbf = new StringBuffer();
                while ((strRead = reader.readLine()) != null) {
                    sbf.append(strRead);
                    sbf.append("\r\n");
                }
                reader.close();
                strRead = sbf.toString();
                JSONObject sb = null;
                try {
                    sb = new JSONObject(strRead);//读取返回的json
                    JSONArray results = sb.getJSONArray("results");//获取所有结果
                    JSONObject temp = new JSONObject();
                    for (int i = 0; i < results.length(); i++) {
                        strRead = results.getJSONObject(i).get("resultType").toString();
                        System.out.println(strRead);
                        temp = results.getJSONObject(i).getJSONObject("values");
                        content = (strRead.endsWith("text")) ? temp.get("text").toString() : "";
                        System.out.println(content);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastTool.showToast(ChatbotActivity.this, "图灵机器人返回数据：" + content, 2000);
        speak(content);

    }

    /**
     * 本方法为调用私人封装机器人(有问题)
     */
    private void personalTuLing(String result) {
        CloudRobot cr = new CloudRobot(result);
        String tempstring = null;
        try {
            //后门 程宇健 家
            tempstring = cr.getLocationByMSApi();
            if (!tempstring.equals("")) {
                if (tempstring.startsWith("***")) {
                    if (HomeServe.sfs != null) {
                        System.out.println("开始动作");
                        synchronized (HomeServe.sfs.getSd().lock) {
                            HomeServe.sfs.getSd().content = tempstring.substring(3);//unity开始动作
                            HomeServe.sfs.getSd().lock.notify();
                        }
                        System.out.println("开始动作");
                    }
                    //speak("好的");
                } else {
                    input_content = WhatCommand(tempstring);
                    sendstringIndex = true;
                    speak("好的");
                }
            } else {
                String tem = findIfhasHYITTeacher(result);
                if ((result.contains("在哪里") || result.contains("在哪")) && tem != null) {
                    speak(tem);
                } else {
                    //myHandler.sendEmptyMessage(R.integer.speark);
                    System.out.println("连接图灵");
                    //System.out.println("图灵应该说"+cr.getWordsByPersonalTuLing());
                    //speak(cr.getWordsByPersonalTuLing());
                    callTuLing(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String findIfhasHYITTeacher(String result) {
//        String stu_table="create table teachers(" +
//                0"teacherId INTEGER  PRIMARY KEY autoincrement NOT NULL," +
//                1"teacherName_CN String NOT NULL," +
//                2"teacherRoom int NOT NULL," +
//                3"job String NOT NULL," +
//                4"teacherName_EN String NOT NULL," +
//                5"department String NOT NULL" +
//                ")";
        Cursor rs = mainDB.query("teachers", null, null, null, null, null, null);
        int i = 0;
        String names[] = new String[rs.getCount()];
        while (rs.moveToNext()) {
            names[i] = rs.getString(1);//teacherName_CN
            System.out.println("teacherName_CN:" + names[i]);
            if (result.contains(names[i])) {
                return names[i] + "的办公室在" + rs.getInt(2);
            }
        }
        ;
        return null;
    }

    private String WhatCommand(String tempstring) {
        if (tempstring.equals("家")) {
            ChatbotActivity.headIntIndex = true;
            ChatbotActivity.headData = 3;
            return "go home";
        } else if (tempstring.equals("程宇健")) {
            return "chen";
        } else if (tempstring.equals("后门")) {
            ChatbotActivity.headIntIndex = true;
            ChatbotActivity.headData = 3;
            return "back door";
        }
        return "";
    }

    /**
     * @param content 图灵机器人返回的话在本方法进行播放
     */
    private void speak(String content) {
        synthesizer.speak(content);
    }

    /*启动服务器给python客服端发送消息让unity展示动画*/
    private void StartServer() {
        serve = new HomeServe();
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对/**handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        //SpeechSynthesizerListener listener = new UiMessageListener(myHandler);
        speechListener = new MySpeechListener(myHandler);
        Map<String, String> params = getParams();
        InitConfig initConfig = new InitConfig("10517583", "EyRCOnfrM15Z0xP3k2N4wgvG",
                "ea3ecaf8b44e1791cf783a8fbf2285e4", ttsMode, params, speechListener);
        synthesizer = new NonBlockSyntherizer(this, initConfig, myHandler);
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "4");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synthesizer.release();
        wp.unregisterListener(wpListener);
        System.out.println("destory");
    }

    /**
     * 语音离线资源
     *
     * @param voiceType
     * @return
     */
    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            //ToastTool.showToast(ChatbotActivity.this, "【error】:copy files from assets failed." + e.getMessage(), 1000);
            System.out.println("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
}
