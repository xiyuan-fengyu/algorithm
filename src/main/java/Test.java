import visualization.MyTreeMap;
import visualization.TreeMapRender;

/**
 * Created by xiyuan_fengyu on 2019/4/22 15:36.
 */
public class Test {

    public static void main(String[] args) {
        MyTreeMap<Integer, Object> map = new MyTreeMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);
        }
        System.out.println(TreeMapRender.toString(map));
    }

}
