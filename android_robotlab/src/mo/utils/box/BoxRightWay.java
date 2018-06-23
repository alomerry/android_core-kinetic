package mo.utils.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mo.utils.static_interface.StaticValues;

public class BoxRightWay {//a:张成威 b:吴锦诚 c:赵进奇 d:卢中鹤 e:阿卡贝
    private char rightCode;
    private List<String> nameMap = new ArrayList<>();
    private List<String> location = new ArrayList<>();

    public BoxRightWay(char rightCode) {
        this.rightCode = rightCode;
        char[] temp = {'a', 'b', 'c'};//,'d','e'};
        for (char t : temp) {
            location.add("" + t);
            if (t != rightCode) {
                nameMap.add("" + t);
            }
        }
    }

    /**
     * @return 获取目的地的人名的拼音
     */
    public String getRightPinyinName() {
        return StaticValues.namesMapping[rightCode - 97];
    }

    /**
     * @return 获取目的地的人的中文名
     */
    public String getRightTrueName() {
        return StaticValues.name[rightCode - 97];
    }

    /**
     * @param s 将含有s的识别标记删除，返回下一个未识别的面部
     * @return 返回下一个目标脸
     */
    public String deleteThisFaceAndMoveAgain(String s) {
        char temp = 0;
        for (int i = 0; i < StaticValues.namesMapping.length; i++) {
            if (s.equals(StaticValues.namesMapping[i])) {
                temp = (char) (97 + i);
                break;
            }
        }
        nameMap.remove(temp + "");
        int size = nameMap.size();
        if (size <= 0) {
            return null;
        } else {
            Random rand = new Random(2);
            size = rand.nextInt(size);
//            System.out.println("返回一个新的检测点："+nameMap.get(size));
//            return nameMap.get(size);
            return location.get(0);
        }
    }

    /**
     * 删除上次已发送过的地址
     */
    public void removeLocation(String locate) {
        location.remove(locate);
    }

    public String getNextFaceAndMoveAgain() {
        int size = nameMap.size();
        if (nameMap.size() <= 0) {
            return null;
        } else {
            Random rand = new Random(2);
            size = rand.nextInt(size);
//        return nameMap.get(size);
            return location.get(0);
        }
    }
}
