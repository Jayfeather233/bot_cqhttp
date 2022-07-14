package main;

import event.friendadd.friendAddMain;
import event.groupmemberchange.MemberChangeMain;
import event.poke.pokeMain;
import function.auto114514.Auto114514Main;
import function.autoForwardGenerator.AutoForwardGeneratorMain;
import function.autoreply.AutoReplyMain;
import function.getImage621.GetImage621Main;
import function.getimage2d.GetImage2DMain;
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
    public static long botQQ;
    private static final Set<Long> friendSet = new HashSet<>();
    private static final Map<Long, String> userName = new HashMap<>();
    private static final ArrayList<Processable> features = new ArrayList<>();
    private static final ArrayList<EventProcessable> events = new ArrayList<>();

    public static Set<Long> getFriendSet() {
        return friendSet;
    }

    public synchronized static String getName(long ID) {
        return userName.get(ID);
    }


    public synchronized static void setNextOutput(String input) {//收到传来的EVENT的JSON数据处理
        JSONObject J_input = JSONObject.parseObject(input);
        String post_type = J_input.getString("post_type");
        String uName = null;

        if (post_type.equals("request") || post_type.equals("notice")) {
            for (EventProcessable eve : events) {
                if (eve.check(J_input)) {
                    eve.process(J_input);
                }
            }
        } else if (post_type.equals("message")) {
            String message = J_input.getString("message");
            String message_type = J_input.containsKey("message_type") ? J_input.getString("message_type") : null;
            if (message == null || message_type == null) return;
            if (!message_type.equals("private") && !message_type.equals("group")) return;
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
        } else if (msg_type.equals("private")) {
            J.put("user_id", user_id);
            return setNextSender("send_msg", J);
        } else return null;
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        System.setProperty("java.net.useSystemProxies", "true");
        features.add(new UNOMain());
        features.add(new AutoForwardGeneratorMain());
        features.add(new DeliverMain());
        features.add(new GuessGameMain());
        features.add(new ImageGeneratorMain());
        features.add(new GetImage621Main());
        features.add(new Auto114514Main());
        features.add(new GetImage2DMain());
        features.add(new AutoReplyMain());

        events.add(new friendAddMain());
        events.add(new MemberChangeMain());
        events.add(new pokeMain());

        File f = new File("./port.txt");
        if (!f.exists()) {
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
        J_input = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_login_info", null)).toString());
        botQQ = J_input.getJSONObject("data").getLong("user_id");
        System.out.println("QQ:" + botQQ);
        for (Object o : JA) {
            friendSet.add(((JSONObject) o).getLong("user_id"));
        }
        new Thread(new InputProcess()).start();
    }

    public static String getUserName(long group_id, long user_id) {
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
