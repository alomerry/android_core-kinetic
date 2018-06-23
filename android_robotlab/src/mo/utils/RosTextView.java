package mo.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.TextView;

import org.ros.android.MessageCallable;
import org.ros.android.robotlab.R;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import mo.MainActivity;
import mo.utils.box.Box;


/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RosTextView<T> extends android.support.v7.widget.AppCompatTextView implements NodeMain {
    public static String data = new String();

    public String getTopicName() {
        return topicName;
    }

    public void setMyHandler(MyHandler myHandler) {
        this.myHandler = myHandler;
    }

    private MyHandler myHandler;
    private String topicName;
    private String messageType;
    private MessageCallable<String, T> callable;

    public RosTextView(Context context) {
        super(context);
    }

    public RosTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RosTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessageToStringCallable(MessageCallable<String, T> callable) {
        this.callable = callable;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_gingerbread/ros_text_view");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {


        Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(new MessageListener<T>() {
            @Override
            public void onNewMessage(final T message) {
                System.out.println("进入回调函数！！！！！");
                if (callable != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            data = callable.call(message);
                            System.out.println("接受到face的消息：" + data);
                            synchronized (Box.objectLock) {
                                Box.objectLock.notify();
                            }
                            //append(callable.call(message)+"\n");

                        }
                    });
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            append(message.toString() + "\n");
                        }
                    });
                }
                postInvalidate();
            }
        });
        System.out.println("设置完毕！！！！");
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }
}
