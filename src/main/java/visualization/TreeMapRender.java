package visualization;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by xiyuan_fengyu on 2019/4/25 16:29.
 */
public class TreeMapRender {

    private static Object getRoot(MyTreeMap treeMap) {
        try {
            Field root = MyTreeMap.class.getDeclaredField("root");
            root.setAccessible(true);
            return root.get(treeMap);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getEntryField(Object entry, String fieldName) {
        if (entry == null) {
            if ("color".equals(fieldName)) {
                return true;
            }
            return null;
        }
        try {
            Field field = entry.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entry);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getEntryKey(Object entry) {
        return getEntryField(entry, "key");
    }

    private static Object getEntryValue(Object entry) {
        return getEntryField(entry, "value");
    }

    private static Object getEntryParent(Object entry) {
        return getEntryField(entry, "parent");
    }

    private static Object getEntryLeft(Object entry) {
        return getEntryField(entry, "left");
    }

    private static Object getEntryRight(Object entry) {
        return getEntryField(entry, "right");
    }

    private static boolean getEntryColor(Object entry) {
        return Boolean.TRUE.equals(getEntryField(entry, "color"));
    }

    private static EntryFormater defaultEntryFormatter =
            (key, value, isBlack) -> String.format(isBlack ? "[%s=%s]" : "%s=%s", key, value);

    public static String toString(MyTreeMap treeMap) {
        return toString(treeMap, 4, defaultEntryFormatter);
    }

    public static String toString(MyTreeMap treeMap, int margin, EntryFormater formater) {
        Object root = getRoot(treeMap);
        char[][] chars = entryToChars(root, margin, formater);
        if (chars != null) {
            StringBuilder builder = new StringBuilder();
            for (char[] charArr : chars) {
                builder.append(charArr).append('\n');
            }
            return builder.toString();
        }
        return "";
    }

    private static char[][] entryToChars(Object entry, int margin, EntryFormater formater) {
        if (entry == null) {
            return null;
        }

        Object key = getEntryKey(entry);
        Object value = getEntryValue(entry);
        Object left = getEntryLeft(entry);
        Object right = getEntryRight(entry);
        boolean color = getEntryColor(entry);

        char[] keyVal = formater.format(key, value, color).toCharArray();

        char[][] leftChars = entryToChars(left, margin, formater);
        char[][] rightChars = entryToChars(right, margin, formater);
        if (leftChars != null && rightChars != null) {
            // 计算每行左右两边的中间的空白距离
            int leftLineNum = leftChars.length;
            int rightLineNum = rightChars.length;
            int disLineNum = Math.min(leftLineNum, rightLineNum);
            int lrLineNums = Math.max(leftLineNum, rightLineNum);

            int[][] disArr = new int[disLineNum][];
            int minCommonBlank = Integer.MAX_VALUE;
            int leftMaxLen = maxLen(leftChars);
            for (int i = 0; i < disLineNum; i++) {
                int blank1 = blankSubfixNum(leftChars[i], leftMaxLen);
                int blank2 = blankPrefixNum(rightChars[i]);
                disArr[i] = new int[]{blank1, blank2};
                minCommonBlank = Math.min(blank1 + blank2, minCommonBlank);
            }

            // 负数，相交距离；正数：远离距离
            int lrDis = margin - minCommonBlank;

            char[][] res = new char[lrLineNums + 2][];
            int firstLineMidBlankLen = disArr[0][0] + disArr[0][1] + lrDis;
            int newFirstLinePreBlankLen;
            if (firstLineMidBlankLen >= keyVal.length + 2) {
                // 第一行的中间空白部分足够放所有行了
                newFirstLinePreBlankLen = (firstLineMidBlankLen - keyVal.length) / 2 + leftChars[0].length;
                res[0] = addBlankPrefix(keyVal, newFirstLinePreBlankLen);
            }
            else {
                newFirstLinePreBlankLen = leftChars[0].length + 1;
                res[0] = addBlankPrefix(keyVal, newFirstLinePreBlankLen);
                lrDis = keyVal.length + 2 - disArr[0][0] - disArr[0][1];
            }

            char[] lineLink = new char[newFirstLinePreBlankLen - 1 + keyVal.length + 2];
            Arrays.fill(lineLink, 0, lineLink.length, ' ');
            lineLink[newFirstLinePreBlankLen - 1] = '/';
            lineLink[lineLink.length - 1] = '\\';
            res[1] = lineLink;

            for (int i = 0; i < lrLineNums; i++) {
                if (i < rightLineNum) {
                    int lineLen = leftMaxLen + rightChars[i].length + lrDis;
                    char[] newLine = new char[lineLen];
                    Arrays.fill(newLine, 0, newLine.length, ' ');
                    if (i < leftLineNum) {
                        char[] leftLine = leftChars[i];
                        System.arraycopy(leftLine, 0, newLine, 0, leftLine.length);
                    }
                    char[] rightLine = rightChars[i];
                    System.arraycopy(rightLine, 0, newLine, newLine.length - rightLine.length, rightLine.length);
                    res[2 + i] = newLine;
                }
                else {
                    res[2 + i] = leftChars[i];
                }
            }

            return res;
        }
        else if (leftChars != null) {
            char[][] res = new char[leftChars.length + 2][];
            res[0] = addBlankPrefix(keyVal, leftChars[0].length + 1);
            res[1] = addBlankPrefix(new char[] {'/'}, leftChars[0].length);
            System.arraycopy(leftChars, 0, res, 2, leftChars.length);
            return res;
        }
        else if (rightChars != null) {
            char[][] res = new char[rightChars.length + 2][];

            int firstLineBlankPrefix = blankPrefixNum(rightChars[0]);
            if (firstLineBlankPrefix <= keyVal.length + 1) {
                res[0] = keyVal;
                res[1] = new char[keyVal.length + 1];
                res[1][keyVal.length] = '\\';
            }
            else {
                res[0] = addBlankPrefix(keyVal, firstLineBlankPrefix - keyVal.length - 1);
                res[1] = new char[firstLineBlankPrefix];
                res[1][firstLineBlankPrefix - 1] = '\\';
            }
            Arrays.fill(res[1], 0, res[1].length - 1, ' ');

            for (int i = 0; i < rightChars.length; i++) {
                char[] rightCharLine = rightChars[i];
                if (firstLineBlankPrefix < keyVal.length + 1) {
                    char[] newLine = addBlankPrefix(rightCharLine, keyVal.length + 1 - firstLineBlankPrefix);
                    res[2 + i] = newLine;
                }
                else {
                    res[2 + i] = rightCharLine;
                }
            }

            return res;
        }
        else {
            char[][] res = new char[1][];
            res[0] = keyVal;
            return res;
        }
    }

    private static int blankPrefixNum(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != ' ') {
                return i;
            }
        }
        return chars.length;
    }

    private static int blankSubfixNum(char[] chars, int len) {
        int res = len - chars.length;
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] != ' ') {
                return res;
            }
            else {
                res++;
            }
        }
        return chars.length;
    }

    private static char[] addBlankPrefix(char[] chars, int num) {
        if (num == 0) {
            return chars;
        }

        char[] newChars = new char[chars.length + num];
        if (num > 0) {
            Arrays.fill(newChars, 0, num, ' ');
        }
        System.arraycopy(chars, 0, newChars, num, chars.length);
        return newChars;
    }

    private static int maxLen(char[][] chars) {
        int max = 0;
        for (char[] charLen : chars) {
            max =Math.max(max, charLen.length);
        }
        return max;
    }

    public interface EntryFormater {

        String format(Object key, Object value, boolean isBlack);

    }

}
