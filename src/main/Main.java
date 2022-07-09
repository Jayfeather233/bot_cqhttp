package main;

import function.autoForwardGenerator.AutoForwardGeneratorMain;
import function.getImage621.GetImage621Main;
import function.imageGenerator.ImageGeneratorMain;
import game.deliver.DeliverMain;
import game.guess.GuessGameMain;
import game.uno.UNOMain;
import httpconnect.HttpURLConnectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;


public class Main {
    public static int sendPort;
    public static int receivePort;
    private static final Set<Long> friendSet = new HashSet<>();
    private static final Map<Long, String> userName = new HashMap<>();
    private static final ArrayList<Processable> features = new ArrayList<>();

    public static Set<Long> getFriendSet() {
        return friendSet;
    }

    public synchronized static String getName(long ID) {
        return userName.get(ID);
    }


    public synchronized static void setNextOutput(String input) {//收到传来的EVENT的JSON数据处理
        JSONObject J_input = JSONObject.parseObject(input);
        String post_type = J_input.getString("post_type");
        String message_type = J_input.containsKey("message_type") ? J_input.getString("message_type") : null;
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
            if (group_id !=-1 && J_input.containsKey("sub_type") && J_input.getString("sub_type").equals("poke")) {
                if (J_input.getLong("target_id") == 1573079756 && user_id != 1783241911 && user_id != 1318920100) {
                    System.out.println("poke");
                    setNextSender("group", user_id, group_id, "[CQ:poke,qq=" + user_id + "]");
                    if (new Random().nextInt(3) == 1) setNextSender("group", user_id, group_id, "别戳我TAT");
                }
            }
        }
        if (message == null || message_type == null || user_id == 0) return;//忽略不感兴趣的EVENT
        if (!post_type.equals("message")) return;
        if (!message_type.equals("private") && !message_type.equals("group")) return;

        if (message.equals("cntest")) {
            if (message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message", "中文测试[t]");
                setNextSender("send_group_msg", J);
            }
        } else if (message.contains("蒙德里安")) {
            if (message_type.equals("group")) {
                JSONObject J = new JSONObject();
                J.put("group_id", group_id);
                J.put("message", "啊对对对");
                setNextSender("send_group_msg", J);
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
        } else {
            for (Processable game : features) {
                if (game.check(message_type, message, group_id, user_id)) {
                    game.process(message_type, message, group_id, user_id);
                }
            }
        }
    }

    public synchronized static StringBuffer setNextSender(String msg_type, JSONObject msg) {
        try {
            Thread.sleep(50);//延时。在电脑QQ消息间隔过快收不到
            return HttpURLConnectionUtil.doPost("http://127.0.0.1:" + sendPort + "/" + msg_type, msg);
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

    public static void main(String[] args) throws InterruptedException, IOException {

        System.setProperty("java.net.useSystemProxies", "true");
        features.add(new UNOMain());
        features.add(new AutoForwardGeneratorMain());
        features.add(new DeliverMain());
        features.add(new GuessGameMain());
        features.add(new ImageGeneratorMain());
        features.add(new GetImage621Main());

        File f = new File("./port.txt");
        if(!f.exists()){
            Scanner S = new Scanner(System.in);
            System.out.println("Please input the send_port: ");
            sendPort = S.nextInt();
            System.out.println("Please input the receive_port: ");
            receivePort = S.nextInt();

            FileOutputStream fops = new FileOutputStream(f);
            fops.write(String.valueOf(sendPort).getBytes());
            fops.write(' ');
            fops.write(String.valueOf(receivePort).getBytes());
            fops.close();
            S.close();
            System.out.println("Now you should restart me. Quiting in 5 seconds.");
            Thread.sleep(5000);
            return;
        } else {
            Scanner S = new Scanner(f);
            sendPort = S.nextInt();
            receivePort = S.nextInt();
            S.close();
        }

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
        }
        new Thread(new InputProcess()).start();
    }
}
