package mo;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_tutorial_pubsub.Talker;

import std_msgs.String;

/**
 * Created by wu1ji on 2017/12/12.
 */

public class Send extends Talker {
    private java.lang.String topicName;

    public Send(java.lang.String topicName) {
        this.topicName = topicName;
    }


    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_mo/mobileControl");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {

        final Publisher<String> publisher = connectedNode.newPublisher(this.topicName, "std_msgs/String");
        connectedNode.executeCancellableLoop(new CancellableLoop() {

            protected void setup() {
            }

            protected void loop() throws InterruptedException {
                String str = (String) publisher.newMessage();
                if (MainActivity.sendstringIndex) {
                    System.out.println("Message发送");
                    str.setData(MainActivity.getInput_content());
                    System.out.println(MainActivity.input_content);
                    publisher.publish(str);
                    MainActivity.sendstringIndex = false;
                }
                Thread.sleep(300L);
            }
        });

    }
}
