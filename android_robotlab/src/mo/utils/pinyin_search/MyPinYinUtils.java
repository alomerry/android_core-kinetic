package mo.utils.pinyin_search;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

public class MyPinYinUtils {
    private static String[] newNames = {"赵进奇", "吴锦诚", "阿卡贝", "卢中鹤", "张成威"};
    private static String[] namesA = {"zhao,jing,qi,", "wu,jin,chen", "a,ka,bei", "lu,zhon,he", "zhang,chen,wei"};
    private static String[] namesB = {"zhao,jin,qi,", "wu,jin,cheng", "a,ka,bei", "lu,zhong,he", "zhang,cheng,wei"};
    private static String[] namesC = {"zhao,jing,qi,", "wu,jing,chen", "a,ka,bei", "lu,zhon,he", "zhang,chen,wei"};
    private static String[] namesD = {"zhao,jing,qi,", "wu,jing,cheng", "a,ka,bei", "lu,zhon,he", "zhang,chen,wei"};

    public static String changeSimilar(String even) {
        int index = -1, i = 0;
        String pinyin = "";
        try {
            pinyin = PinyinHelper.convertToPinyinString(even, ",", PinyinFormat.WITHOUT_TONE);
            for (i = 0; i < namesA.length; i++) {
                if (pinyin.contains(namesA[i])) {
                    index = pinyin.indexOf(namesA[i]);
                    break;
                } else if (pinyin.contains(namesB[i])) {
                    index = pinyin.indexOf(namesB[i]);
                    break;
                } else if (pinyin.contains(namesC[i])) {
                    index = pinyin.indexOf(namesC[i]);
                    break;
                } else if (pinyin.contains(namesD[i])) {
                    index = pinyin.indexOf(namesD[i]);
                    break;
                }
            }
            if (index == -1) {
                return even;
            }
        } catch (PinyinException e) {
            e.printStackTrace();
        }
        System.out.println(even);
        System.out.println(index + ":" + newNames[i] + ":" + i);
        //计算逗号个数
        index = countDouHou(pinyin.substring(0, index));
        System.out.println(index);
        if (index >= even.length() - 1 - newNames[i].length()) {
            even = even.substring(0, index) + newNames[i];
        } else {
            even = even.substring(0, index) + newNames[i] + even.substring(index + newNames[i].length(), even.length() - 1);
        }
        return even;
    }

    /**
     * 计算string中"，"的个数
     *
     * @param string
     * @return
     */
    private static int countDouHou(String string) {
        int count = 0;
        char[] chs = string.toCharArray();
        for (char ch : chs) {
            System.out.print(ch);
            if (ch == ',') {
                count++;
            }
        }
        System.out.println();
        return count;
    }
}
