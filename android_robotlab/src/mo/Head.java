package mo;

import org.ros.concurrent.CancellableLoop;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_tutorial_pubsub.Talker;

import std_msgs.String;

/**
 * Created by wu1ji on 2018/3/28.
 */

public class Head extends Talker {
    private java.lang.String topicName;

    public Head(java.lang.String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Publisher<std_msgs.UInt16> publisher = connectedNode.newPublisher(this.topicName, "std_msgs/UInt16");
        connectedNode.executeCancellableLoop(new CancellableLoop() {

            protected void setup() {
            }

            protected void loop() throws InterruptedException {
                std_msgs.UInt16 str = (std_msgs.UInt16) publisher.newMessage();
                if (MainActivity.headIntIndex) {
                    str.setData(MainActivity.headData);
                    publisher.publish(str);
                    MainActivity.headIntIndex = false;
                }
                Thread.sleep(300L);
            }
        });

    }
}
