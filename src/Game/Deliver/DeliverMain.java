package Game.Deliver;

import java.io.*;
import java.util.*;

import static Main.Main.setNextSender;

class DeliverItemInfo {
    String name;
    int possibility;
    int color; //0:green 1:yellow 2:purple
}

public class DeliverMain {
    private final DeliverItemInfo[] diiArray = new DeliverItemInfo[100];
    private final Random R = new Random();
    private int totalPoss = 0;

    public DeliverMain() {
        try {
            FileReader f = new FileReader("DeliverPossibility.txt");
            Scanner S = new Scanner(f);
            int n = S.nextInt();
            for (int i = 0; i < n; i++) {
                diiArray[i] = new DeliverItemInfo();
                diiArray[i].name = S.next();
                diiArray[i].possibility = S.nextInt();
                diiArray[i].color = S.nextInt();
                totalPoss += diiArray[i].possibility;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DeliverItemInfo getRandomDeliverItem() {
        int u = R.nextInt(totalPoss);
        for (DeliverItemInfo deliverU : diiArray) {
            if (u <= deliverU.possibility) {
                return deliverU;
            }
            u -= deliverU.possibility;
        }
        return null;
    }

    private ArrayList<DeliverItemInfo> getDeliverItem(int t) {
        ArrayList<DeliverItemInfo> re = new ArrayList<>();
        for (int i = 0; i < t; i++) re.add(getRandomDeliverItem());
        return re;
    }

    public void process(String message, String message_type, long user_id, long group_id) {
        if (message_type.equals("private")) return;
        int t;
        if (message.equals("")) {
            t = 1;
        } else t = 11;

        ArrayList<DeliverItemInfo> deliverItemArray = getDeliverItem(t);

        StringBuilder output = new StringBuilder("[CQ:at,qq=" + user_id + "]大嘴鸥回来啦\n");
        for (DeliverItemInfo u : deliverItemArray) {
            switch (u.color) {
                case 0 -> output.append("绿色 ");
                case 1 -> output.append("黄色 ");
                case 2 -> output.append("紫色 ");
            }
            output.append(u.name).append('\n');
        }
        output.append("以后会加图片");
        setNextSender(message_type, user_id, group_id, String.valueOf(output));
    }
}
