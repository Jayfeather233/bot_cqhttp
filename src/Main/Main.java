package Main;

import Game.Deliver.DeliverMain;
import Game.UNO.UNOGame;
import Game.UNO.UNOMain;
import Game.play;
import HTTPConnect.GetImage621;
import HTTPConnect.HttpURLConnectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;


public class Main {
    private static final Set<Long> friendSet = new HashSet<>();
    private static final Map<Long, play> map = new HashMap<>();
    private static final Map<Long, UNOGame> unoGameMap = new HashMap<>();
    private static final Map<Long, Long> unoIDMap = new HashMap<>();
    private static final DeliverMain deliver = new DeliverMain();
    private static final Map<Long, String> userName = new HashMap<>();

    public static Set<Long> getFriendSet() {
        return friendSet;
    }

    public static Map<Long, UNOGame> getUnoGameMap() {
        return unoGameMap;
    }

    public static Map<Long, Long> getUnoIDMap() {
        return unoIDMap;
    }

    public synchronized static void endThread(long user_id) {
        map.remove(user_id);
    }

    public synchronized static void endUNOGame(long ID) {
        unoGameMap.remove(ID);
    }

    public synchronized static boolean UNOJoin(long userID, long gameID) {
        if (unoIDMap.containsKey(userID)) return false;
        unoIDMap.put(userID, gameID);
        return true;
    }

    public synchronized static void UNOLeave(long userID) {
        unoIDMap.remove(userID);
    }

    public synchronized static String getName(long ID) {
        return userName.get(ID);
    }


    public synchronized static void setNextOutput(String input) {//收到传来的EVENT的JSON数据处理
        JSONObject J_input = JSONObject.parseObject(input);
        String post_type = J_input.getString("post_type");
        String message_type = J_input.getString("message_type");
        String uName = null;
        long user_id = 0, group_id = -1;
        if (J_input.containsKey("user_id")) {
            user_id = J_input.getLong("user_id");
        }
        if (J_input.containsKey("group_id")) {
            group_id = J_input.getLong("group_id");
        }
        if (J_input.containsKey("sender")) {
            if (J_input.getJSONObject("sender").containsKey("card") &&
                    !J_input.getJSONObject("sender").getString("card").equals("")) {
                uName = J_input.getJSONObject("sender").getString("card");
            } else if (J_input.getJSONObject("sender").containsKey("nickname")) {
                uName = J_input.getJSONObject("sender").getString("nickname");
            }
            userName.put(user_id, uName);
        }
        String message = J_input.getString("message");
        if (J_input.containsKey("request_type")) {
            if (J_input.getString("request_type").equals("friend")) {
                JSONObject J = new JSONObject();
                J.put("flag", J_input.getString("flag"));
                J.put("approve", true);
                setNextSender("set_friend_add_request", J);
                friendSet.add(user_id);
                return;
            }
        }
        if (post_type.equals("notice")) {
            if (J_input.containsKey("sub_type") && J_input.getString("sub_type").equals("poke")) {
                if (J_input.getLong("target_id") == 1573079756 && user_id != 1783241911 && user_id != 1318920100) {
                    System.out.println("poke");
                    setNextSender(message_type, user_id, group_id, "[CQ:poke,qq=" + user_id + "]");
                    if (new Random().nextInt(3) == 1) setNextSender(message_type, user_id, group_id, "别戳我TAT");
                }
            }
        }
        if (message == null || message_type == null || user_id == 0) return;//忽略不感兴趣的EVENT
        if (!post_type.equals("message")) return;
        if (!message_type.equals("private") && !message_type.equals("group")) return;

        message = message.toLowerCase();
        if (map.get(user_id) != null) {
            map.get(user_id).setNextMessage(message, message_type, group_id);
        } else if (message.equals(".play")) {
            map.put(user_id, new play(user_id, message_type, group_id));
            System.out.printf("%d started.\n", user_id);
        } else if (message.equals("cntest")) {
            if (message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message", "中文测试");
                setNextSender("send_group_msg", J);
            }
        } else if (message.contains("蒙德里安")) {
            if (message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message", "啊对对对");
                setNextSender("send_group_msg", J);
            }
        } else if (message.indexOf("621") == 0) {
            if (message_type.equals("group")) {
                if (group_id == 1011383394 || group_id == 118627232 || group_id == 931369311 || group_id == 614981678) {
                    JSONObject J = new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message", GetImage621.GetImage(message.substring(3)));
                    setNextSender("send_group_msg", J);
                }
            }
        } else if (message.indexOf("mget") == 0) {
            if (message_type.equals("group")) {
                if (group_id == 1011383394) {
                    JSONObject J = new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message", "<来点w fav:jayfeather233" + message.substring(4));
                    setNextSender("send_group_msg", J);
                }
            }
        } else if (message.startsWith("uno.")) {
            UNOMain.process(message.substring(4), message_type, user_id, group_id);
        } else if (message_type.equals("private") && getUnoIDMap().containsKey(user_id)) {
            if (message.startsWith("uno.")) UNOMain.process(message.substring(4), message_type, user_id, group_id);
            else UNOMain.process(message, message_type, user_id, group_id);
        } else if (message.startsWith("外送")) {
            if (message.equals("外送")) deliver.process("", message_type, user_id, group_id);
            else if (message.equals("外送10次")) deliver.process("11", message_type, user_id, group_id);
        } else if (message.startsWith("deliver")) {
            if (message.equals("deliver")) deliver.process("", message_type, user_id, group_id);
            else if (message.equals("deliver 10 times")) deliver.process("11", message_type, user_id, group_id);
        }
    }

    public synchronized static StringBuffer setNextSender(String msg_type, JSONObject msg) {
        try {
            Thread.sleep(50);//延时。在电脑QQ消息间隔过快收不到
            return HttpURLConnectionUtil.doPost("http://127.0.0.1:5700/" + msg_type, msg);//其中5700是配置文件中的端口
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static StringBuffer setNextSender(String msg_type, long user_id, long group_id, String msg) {
        JSONObject J = new JSONObject();
        J.put("message", msg);
        if (msg_type.equals("group")) {
            J.put("group_id", group_id);
            return setNextSender("send_group_msg", J);
        } else {
            J.put("user_id", user_id);
            return setNextSender("send_msg", J);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Go_Listener Listen = new Go_Listener();
        Thread R1 = new Thread(Listen);
        R1.start();
        System.out.println("end.");
        JSONObject J_input;
        boolean flg = true;
        JSONArray JA = null;
        while (flg) {
            flg = false;
            try {
                J_input = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_friend_list", null)).toString());
                JA = J_input.getJSONArray("data");
            } catch (NullPointerException e) {
                flg = true;
            }
            Thread.sleep(10000);
        }
        for (Object o : JA) {
            friendSet.add(((JSONObject) o).getLong("user_id"));
            System.out.println("set add" + ((JSONObject) o).getLong("user_id"));
        }

    }
}
