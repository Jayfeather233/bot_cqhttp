package function.getimage2d;

import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import main.Processable;

public class GetImage2DMain implements Processable {

    @Override
    public void process(String message_type, String message, long group_id, long user_id) {
        JSONObject J = JSONObject.parseObject(HttpURLConnectionUtil.doGet("https://www.dmoe.cc/random.php?return=json"));
        main.Main.setNextSender(message_type,user_id,group_id,"[CQ:image,file=" + J.getString("imgurl") + ",id=40000]");
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.equals("来点二次元");
    }
}
