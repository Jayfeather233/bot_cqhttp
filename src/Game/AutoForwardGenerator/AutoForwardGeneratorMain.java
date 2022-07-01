package Game.AutoForwardGenerator;

import Game.Playable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;
import java.util.Scanner;

import static Main.Main.setNextSender;

public class AutoForwardGeneratorMain implements Playable {
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
                s1 = getNameInGroup(group_id, Long.parseLong(uin));
            } else {
                uin = s1;
                s1 = getNameInGroup(group_id, Long.parseLong(s1));
            }
            int pos = 0;
            do {
                pos = s2.indexOf("[CQ:at,qq=", pos);
                if (pos == -1) break;
                int po1 = pos + 10;
                while (s2.charAt(pos) != ']') pos++;
                s2 = s2.substring(0, pos) + ",name=" + getNameInGroup(group_id, Long.parseLong(s2.substring(po1, pos))) + s2.substring(pos);
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
            setNextSender("send_group_forward_msg", J);
        }
        else{
            J.put("user_id", user_id);
            setNextSender("send_private_forward_msg", J);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("转发");
    }

    private static String getNameInGroup(long group_id, long user_id) {
        JSONObject J = new JSONObject();
        J.put("group_id", group_id);
        J.put("user_id", user_id);

        J = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_group_member_info", J)).toString());
        if (J.getString("status").equals("failed")) {
            J = new JSONObject();
            J.put("user_id", user_id);
            J = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_stranger_info", J)).toString());
        }
        J = J.getJSONObject("data");


        String uName;
        if (J.containsKey("card") && !J.getString("card").equals("")) {
            uName = J.getString("card");
        } else if (J.containsKey("nickname")) {
            uName = J.getString("nickname");
        } else uName = J.getString("user_id");
        return uName;
    }
}
