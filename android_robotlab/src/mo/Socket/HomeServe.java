package mo.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wu1ji on 2018/3/27.
 */

public class HomeServe {
    private int port = 8140;
    private ServerSocket sst;
    public String content = "";
    private Socket st;
    /*包含socket线程和客户端的model*/
    public static ServeModel sfs;

    public HomeServe() {
        try {
            sst = new ServerSocket(port);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Accept();
                }

            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Accept() {
        while (true) {
            try {
                if (sfs == null) {
                    st = sst.accept();
                    System.out.println("检测到客服端连接");

                    SocketThread sd = new SocketThread(st) {
                        @Override
                        public void run() {
                            /*x此线程被锁定在此知道有消息发送才被唤醒*/
                            send();
                            close();
                        }
                    };
                    /*用于接受消息*/
//                    SocketThread testGet = new SocketThread(st) {
//                        @Override
//                        public void run() {
//                            Servelisten();
//                            close();
//                        };
//                    };
//                    new Thread(testGet).start();
                    sfs = new ServeModel(st, sd);
                    new Thread(sd).start();
                } else {
//                    try {
//                        OutputStream outputStream = sfs.getSt().getOutputStream();
//                        outputStream.write(null);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        System.out.println("666");
//                    }
//                    System.out.println("已有客户机连接");
                    Thread.yield();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
