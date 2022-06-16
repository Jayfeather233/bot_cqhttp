package notice;

import com.alibaba.fastjson.JSONObject;
import java.util.Random;
import static Main.Main.setNextSender;

public class NoticeMain {
    static public void process(JSONObject J_input, long user_id, long group_id){
        if(J_input.containsKey("sub_type")&&J_input.getString("sub_type").equals("poke")){
            if(J_input.getLong("target_id")==1573079756&&user_id!=1783241911&&user_id!=1318920100) {
                System.out.println("poke");
                if (group_id == -1) {
                    JSONObject J = new JSONObject();
                    J.put("user_id", user_id);
                    J.put("message", "[CQ:poke,qq=" + user_id + "]");
                    setNextSender("send_private_msg", J);
                } else {
                    JSONObject J = new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message", "[CQ:poke,qq=" + user_id + "]");
                    setNextSender("send_group_msg", J);
                    if(new Random().nextInt(3)==1){
                        J = new JSONObject();
                        J.put("group_id", group_id);
                        J.put("message", "别戳我TAT");
                        setNextSender("send_group_msg", J);
                    }
                }
            }
        }
    }
}
