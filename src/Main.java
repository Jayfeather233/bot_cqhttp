import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;


public class Main {
    private static final Set<Long> friendSet=new HashSet<>();

    private static final Map<Long,play> map= new HashMap<>();

    private static final Map<Long,UNOGame> unoGameMap=new HashMap<>();
    private static final Map<Long,Long> unoIDMap=new HashMap<>();
    private static final Map<Long,String> userName=new HashMap<>();
    public synchronized static void endThread(long user_id) {
        map.remove(user_id);
    }
    public synchronized static void endUNOGame(long ID){
        unoGameMap.remove(ID);
    }
    public synchronized static boolean UNOJoin(long userID,long gameID){
        if(unoIDMap.containsKey(userID)) return false;
        unoIDMap.put(userID,gameID);
        return true;
    }
    public synchronized static void UNOLeave(long userID){
        unoIDMap.remove(userID);
    }
    public synchronized static String getName(long ID){
        return userName.get(ID);
    }


    public synchronized static void setNextOutput(String input){//收到传来的EVENT的JSON数据处理
        JSONObject J_input=JSONObject.parseObject(input);
        String post_type=J_input.getString("post_type");
        String message_type=J_input.getString("message_type");
        String uName=null;
        long user_id=0,group_id=-1;
        if(J_input.containsKey("user_id")){
            user_id=J_input.getLong("user_id");
        }
        if(J_input.containsKey("group_id")){
            group_id=J_input.getLong("group_id");
        }
        if(J_input.containsKey("sender")){
            if(J_input.getJSONObject("sender").containsKey("card")&&
                    !J_input.getJSONObject("sender").getString("card").equals("")){
                uName=J_input.getJSONObject("sender").getString("card");
            }else if(J_input.getJSONObject("sender").containsKey("nickname")) {
                uName = J_input.getJSONObject("sender").getString("nickname");
            }
            userName.put(user_id, uName);
        }
        String message=J_input.getString("message");
        if(J_input.containsKey("request_type")){
            if(J_input.getString("request_type").equals("friend")){
                JSONObject J=new JSONObject();
                J.put("flag", J_input.getString("flag"));
                J.put("approve", true);
                setNextSender("set_friend_add_request",J);
                friendSet.add(user_id);
                return;
            }
        }
        if(post_type.equals("notice")){
            if(J_input.containsKey("sub_type")&&J_input.getString("sub_type").equals("poke")){
                if(J_input.getLong("target_id")==1573079756&&user_id!=1783241911&&user_id!=1318920100) {
                    System.out.println("poke");
                    if (group_id == -1) {
                        JSONObject J = new JSONObject();
                        J.put("user_id", user_id);
                        J.put("message", "[CQ:poke,qq=" + user_id + "]");
                        Main.setNextSender("send_private_msg", J);
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
        if(post_type==null||message==null||message_type==null||user_id==0) return;//忽略不感兴趣的EVENT
        if(!post_type.equals("message")) return;
        if(!message_type.equals("private")&&!message_type.equals("group")) return;

        message=message.toLowerCase();
        if(map.get(user_id)!=null){
            map.get(user_id).setNextMessage(message,message_type,group_id);
        }else if(message.equals(".play")){
            map.put(user_id,new play(user_id,message_type,group_id));
            System.out.printf("%d started.\n",user_id);
        }else if(message.equals("cntest")){
            if(message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message","中文测试");
                setNextSender("send_group_msg", J);
            }
        }else if(message.contains("蒙德里安")){
            if(message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message","[CQ:at,qq=943771598] 【 CQ:at,qq=1219650440 】");
                setNextSender("send_group_msg", J);
            }
        }
        else if(message.indexOf("621")==0){
            if(message_type.equals("group")){
                if(group_id==1011383394||group_id==118627232||group_id==931369311||group_id==614981678){
                    JSONObject J=new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message",GetImage621.GetImage(message.substring(3)));
                    setNextSender("send_group_msg",J);
                }
            }
        }else if(message.indexOf("mget")==0){
            if(message_type.equals("group")){
                if(group_id==1011383394){
                    JSONObject J=new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message","<来点w fav:jayfeather233"+message.substring(4));
                    setNextSender("send_group_msg",J);
                }
            }
        }else if(message.equals("uno.help")){
            JSONObject J=new JSONObject();
            J.put("message","uno.new 创建一个群UNO游戏\nuno.join 加入当前群游戏\nuno.leave 离开\nuno.start 开始\nuno.order 出牌顺序\nuno.resend 重新发送消息\nuno.help 帮助");
            if(message_type.equals("group")){
                J.put("group_id", group_id);
                setNextSender("send_group_msg",J);
            }else{
                J.put("user_id", user_id);
                setNextSender("send_msg",J);
            }
        }else if(message_type.equals("private")&&unoIDMap.containsKey(user_id)){
            if(message.indexOf("uno.")!=0){
                message="uno.play "+message;
            }
        }
        if(message.indexOf("uno.")==0){
            if(message_type.equals("group")) {
                switch (message) {
                    case "uno.new":
                        if (!unoGameMap.containsKey(group_id)) {
                            unoGameMap.put(group_id, new UNOGame(group_id));
                        } else {
                            JSONObject J = new JSONObject();
                            J.put("group_id", group_id);
                            J.put("message", "本群已存在一个进行中的游戏，使用uno.join来加入");
                            setNextSender("send_group_msg", J);
                        }
                        break;
                    case "uno.join":
                        if (!unoGameMap.containsKey(group_id)) {
                            JSONObject J = new JSONObject();
                            J.put("group_id", group_id);
                            J.put("message", "本群不存在一个进行中的游戏，使用uno.new来新建一个");
                            setNextSender("send_group_msg", J);
                        } else {
                            if (friendSet.contains(user_id)) unoGameMap.get(group_id).join(user_id);
                            else {
                                JSONObject J = new JSONObject();
                                J.put("group_id", group_id);
                                J.put("message", "[CQ:at,qq=" + user_id + "] 请先添加bot好友");
                                setNextSender("send_group_msg", J);
                            }
                        }
                        break;
                    case "uno.start":
                        if (!unoGameMap.containsKey(group_id)) {
                            JSONObject J = new JSONObject();
                            J.put("group_id", group_id);
                            J.put("message", "本群不存在一个进行中的游戏，使用uno.new来新建一个");
                            setNextSender("send_group_msg", J);
                        } else {
                            if (!unoGameMap.get(group_id).isBegin()) new Thread(unoGameMap.get(group_id)).start();
                            else {
                                JSONObject J = new JSONObject();
                                J.put("group_id", group_id);
                                J.put("message", "本群已存在一个进行中的游戏，使用uno.join来加入");
                                setNextSender("send_group_msg", J);
                            }
                        }
                        break;
                    default:
                        if (unoGameMap.containsKey(group_id))
                            unoGameMap.get(group_id).setNextInput(user_id, message.substring(3));
                        break;
                }
            }else{
                if(unoIDMap.containsKey(user_id)) {
                    unoGameMap.get(unoIDMap.get(user_id)).setNextInput(user_id, message.substring(3));
                }
            }
        }
    }
    public synchronized static StringBuffer setNextSender(String msg_type,JSONObject msg) {
        try {
            Thread.sleep(50);//延时。在电脑QQ消息间隔过快收不到
            return HttpURLConnectionUtil.doPost("http://127.0.0.1:5700/" + msg_type, msg);//其中5700是配置文件中的端口
        }catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Go_Listener Listen = new Go_Listener();
        Thread R1 = new Thread(Listen);
        R1.start();
        System.out.println("end.");
        JSONObject J_input=null;
        boolean flg=true;
        JSONArray JA=null;
        while(flg) {
            flg=false;
            try {
                J_input = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_friend_list", null)).toString());
                JA=J_input.getJSONArray("data");
            } catch (NullPointerException e) {
                flg=true;
            }
            Thread.sleep(1000);
        }
        for (Object o : JA) {
            friendSet.add(((JSONObject) o).getLong("user_id"));
            System.out.println("set add" + ((JSONObject) o).getLong("user_id"));
        }

    }
}
