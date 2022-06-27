package Game.AutoForwardGenerator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;
import java.util.Scanner;

import static Main.Main.setNextSender;

public class AutoForwardGeneratorMain {
    public static void process(String message, long group_id){
        Scanner S=new Scanner(message);
        JSONArray JA = new JSONArray();
        JSONObject J,J2;
        while(S.hasNext()){
            String s1 = S.next();
            String s2 = S.nextLine();
            int kk=0;
            while(s2.charAt(kk)==' ') kk++;
            s2 = s2.substring(kk);
            String uin;
            s1 = s1.toLowerCase();
            if(s1.startsWith("[cq:at,qq=")){
                s1=s1.substring(10);
                int i=0;
                while(i!=s1.length()&&s1.charAt(i)!=']') i++;
                uin = s1.substring(0,i);
                System.out.println(uin);
                s1=getNameInGroup(group_id,Long.parseLong(uin));
            } else {
                uin = s1;
                s1=getNameInGroup(group_id,Long.parseLong(s1));
            }
            System.out.println(s2);

            J = new JSONObject();
            J2 = new JSONObject();
            J2.put("name",s1);
            J2.put("uin",uin);
            J2.put("content",s2);
            J.put("type","node");
            J.put("data",J2);
            System.out.println(J);
            JA.add(J);
        }

        J = new JSONObject();
        J.put("group_id",group_id);
        J.put("messages",JA);

        setNextSender("send_group_forward_msg",J);
    }

    private static String getNameInGroup(long group_id, long user_id) {
        JSONObject J = new JSONObject();
        J.put("group_id",group_id);
        J.put("user_id",user_id);

        J = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_group_member_info", J)).toString());
        J = J.getJSONObject("data");

        System.out.println("re: "+J);

        String uName;
        if (J.containsKey("card") && !J.getString("card").equals("")) {
            uName = J.getString("card");
        } else if (J.containsKey("nickname")) {
            uName = J.getString("nickname");
        } else uName = J.getString("user_id");
        System.out.println(uName);
        return uName;
    }
}
