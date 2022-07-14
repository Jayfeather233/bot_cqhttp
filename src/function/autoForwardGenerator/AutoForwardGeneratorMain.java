package function.autoForwardGenerator;

import main.Main;
import main.Processable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Scanner;

public class AutoForwardGeneratorMain implements Processable {
    @Override
    public void process(String message_type, String message, long group_id, long user_id) {
        message = message.substring(2);
        Scanner S = new Scanner(message);
        JSONArray JA = new JSONArray();
        JSONObject J, J2;
        while (S.hasNext()) {
            String s1 = S.next();
            String s2 = S.nextLine();
            int kk = 0;
            while (s2.charAt(kk) == ' ') kk++;
            s2 = s2.substring(kk);
            String uin;
            if (s1.startsWith("[CQ:at,qq=")) {
                s1 = s1.substring(10);
                int i = 0;
                while (i != s1.length() && s1.charAt(i) != ']') i++;
                uin = s1.substring(0, i);
                s1 = Main.getUserName(group_id, Long.parseLong(uin));
            } else {
                uin = s1;
                s1 = Main.getUserName(group_id, Long.parseLong(s1));
            }
            int pos = 0;
            do {
                pos = s2.indexOf("[CQ:at,qq=", pos);
                if (pos == -1) break;
                int po1 = pos + 10;
                while (s2.charAt(pos) != ']') pos++;
                s2 = s2.substring(0, pos) + ",name=" + Main.getUserName(group_id, Long.parseLong(s2.substring(po1, pos))) + s2.substring(pos);
            } while (true);

            J = new JSONObject();
            J2 = new JSONObject();
            J2.put("name", s1);
            J2.put("uin", uin);
            J2.put("content", s2);
            J.put("type", "node");
            J.put("data", J2);
            JA.add(J);
        }

        J = new JSONObject();
        J.put("messages", JA);

        if(message_type.equals("group")){
            J.put("group_id", group_id);
            Main.setNextSender("send_group_forward_msg", J);
        }
        else{
            J.put("user_id", user_id);
            Main.setNextSender("send_private_forward_msg", J);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("转发");
    }

}
