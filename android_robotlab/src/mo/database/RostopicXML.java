package mo.database;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.ros.android.robotlab.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class RostopicXML {
    private String content;
    private String topicName;
    private String msgType;
    private HashMap<String, String> messages;

    public RostopicXML(Activity activity) {
        content = "";
        try {
            //读取文件
            FileInputStream fis = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() +
                            activity.getResources().getString(R.string.sdLocation) + activity.getResources().getString(R.string.rostopicXMLNmae)));
            byte[] buf = new byte[1024];
            int readIndex = 0;
            while ((readIndex = fis.read(buf)) != -1) {
                content += new String(buf);
            }
            System.out.println(content);
            Log.i("read_XML_tag","content:------\n"+content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        content = content.trim();
        try {
            //处理xml
            Document dc = DocumentHelper.parseText(content);
            //root节点
            Element root = dc.getRootElement();

            topicName = root.element("topic_name").getText();
            msgType = root.element("message_type").getText();

            Element xml_messages = root.element("messages");
            List<Element> msgs = xml_messages.elements();
            if (messages == null) {
                messages = new HashMap<>();
            } else {
                messages.clear();
            }
            for (Element msg : msgs) {
                messages.put(msg.attributeValue("key"), msg.getText());
            }
            System.out.println(msgs);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, String> messages) {
        this.messages = messages;
    }
}
