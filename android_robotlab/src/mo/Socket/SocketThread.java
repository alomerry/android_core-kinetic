package mo.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by wu1ji on 2018/3/27.
 */

public class SocketThread implements Runnable {
    Socket st;
    public String content = "";
    public Object lock = new Object();
    PrintWriter pw;
    InputStream is;

    public SocketThread(Socket st) throws IOException {
        super();
        this.st = st;
        is = st.getInputStream();
    }

    @Override
    public void run() {

    }

    public void send() {
        try {
            pw = new PrintWriter(st.getOutputStream());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (true) {
            synchronized (lock) {
                try {
                    System.out.println("waiting for user to notify...");
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("notify by user.");
            }
            pw.write(content);
            pw.flush();
        }
    }

    public void listen() {
        byte[] buf = new byte[1024];
        try {
            is = st.getInputStream();
            int readlength = 0;
            while (true) {
                readlength = is.read(buf);
                System.out.println(readlength);
                while (readlength <= 1024 && readlength > 0) {
                    content = "";
                    System.out.println("listening Serve...");
                    content += new String(buf) + "";
                    System.out.println(content);
                    if (readlength < 1024) {
                        break;
                    } else {
                        readlength = is.read(buf);
                    }
                }
                readlength = -1;
                System.out.println("Get the message:" + content);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void Servelisten() {
        byte[] buf = new byte[1024];
        try {
            is = st.getInputStream();
            PrintWriter pw = new PrintWriter(st.getOutputStream());
            int readlength = 0;
            while (true) {
                readlength = is.read(buf);
                while (readlength <= 1024 && readlength > 0) {
                    content = "";
                    System.out.println("listening Serve...");
                    content += new String(buf) + "";
                    System.out.println(content);
                    if (readlength < 1024) {
                        break;
                    } else {
                        readlength = is.read(buf);
                    }
                }
                readlength = -1;
                pw.write("123");
                pw.flush();
                System.out.println("Get the message:" + content);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            HomeServe.sfs = null;
            System.out.println("客户机中断");
        }
    }

    public void close() {
        try {
            if (is != null)
                is.close();
            if (pw != null)
                pw.close();
            if (st != null)
                st.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
