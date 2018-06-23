package mo.Socket;

import java.net.Socket;

/**
 * Created by wu1ji on 2018/3/27.
 */

public class ServeModel {
    Socket st;
    SocketThread sd;

    public ServeModel(Socket st, SocketThread sd) {
        super();
        this.st = st;
        this.sd = sd;
    }

    public Socket getSt() {
        return st;
    }

    public void setSt(Socket st) {
        this.st = st;
    }

    public SocketThread getSd() {
        return sd;
    }

    public void setSd(SocketThread sd) {
        this.sd = sd;
    }
}
