package function.getImage621;

import httpconnect.HttpURLConnectionUtil;
import main.Main;
import main.Processable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class GetImage621Main implements Processable {

    private JSONArray JGroup, JPrivate, JAdmin;
    private long lastMsg = 0;

    private int retry = 0;


    public GetImage621Main() {
        try {
            FileReader f = new FileReader("621Level.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            JSONObject J = JSONObject.parseObject(String.valueOf(sb));
            JGroup = J.getJSONArray("group");
            JPrivate = J.getJSONArray("private");
            JAdmin = J.getJSONArray("admin");
            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    level:
        0: safe feral pokemon
        1: safe feral
        2: safe
        3: question
        4: explicit feral or s/q
        5: explicit
     */
    private StringBuilder dealInput(String input, int level) {

        StringBuilder sb = new StringBuilder(input);

        if (level <= 0) sb.append(" score:>0");
        if (level <= 2) sb.append(" rating:s");
        else if (level <= 3) {
            sb.append(" -rating:e");
        }

        if (level == 0) sb.append(" feral pokemon");
        if (level == 1) sb.append(" feral");
        if (level == 4 && sb.indexOf("rating:q") == -1 && sb.indexOf("rating:s") == -1) sb.append(" feral");

        String[] qs = String.valueOf(sb).split(" ");
        StringBuilder quest = new StringBuilder();
        for (String q : qs) {
            if (q.startsWith("score:>") && level <= 2) continue;
            if (q.startsWith("rating:q") && level <= 2) continue;
            if (q.startsWith("rating:e") && level <= 3) continue;

            if (q.length() > 0) {
                quest.append('+').append(q);
            }
        }
        if (quest.length() == 0) quest.append("eeveelution");
        if (!input.contains("id:")) {
            if (!input.contains("favcount") && !input.contains("score") && level <= 2)
                quest.append("+favcount:>10").append("+score:>10");
            else if (!input.contains("favcount") && !input.contains("score"))
                quest.append("+favcount:>400").append("+score:>200");
            if (!input.contains("order")) quest.append("+order:random");
            if (!input.contains("gore") && level <= 4) quest.append("+-gore");
            if (!input.contains("human") && level <= 2) quest.append("+-human");
        }
        return quest;
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id) {

        message = message.toLowerCase();
        if (message.equals("621.recall") && lastMsg != 0) {
            JSONObject J = new JSONObject();
            J.put("message_id", lastMsg);
            Main.setNextSender("delete_msg", J);
            return;
        }
        if (message.startsWith("621.set")) {
            adminProcessSet(message_type, message.substring(8), group_id, user_id);
            return;
        }
        if (message.startsWith("621.del")) {
            adminProcessDel(message_type, message.substring(8), group_id, user_id);
            return;
        }
        if (message.equals("621.level")) {
            Main.setNextSender(message_type, user_id, group_id,
                    """
                            level:
                                0: safe feral pokemon
                                1: safe feral
                                2: safe
                                3: question
                                4: explicit feral or safe/q
                                5: explicit""");
            return;
        }

        int level = -1;
        if (message_type.equals("group")) {
            for (int i = 0; i < JGroup.size(); i++) {
                JSONObject J = JGroup.getJSONObject(i);
                if (J.getLong("id") == group_id) level = J.getInteger("level");
            }
        } else {
            for (int i = 0; i < JPrivate.size(); i++) {
                JSONObject J = JPrivate.getJSONObject(i);
                if (J.getLong("id") == user_id) level = J.getInteger("level");
            }
        }
        if (level < 0) return;

        message = message.substring(3);
        if (message.equals(".default")) {
            Main.setNextSender(message_type, user_id, group_id, "????????????tag???????????????eeveelution\n????????????favcount???score???????????????favcount:>400 score:>200\n??????????????????tags??????????????????gore,anthro,human");
            return;
        }
        StringBuilder quest = dealInput(message, level);
        System.out.println(quest);

        String answer = null;
        try {
            answer = HttpURLConnectionUtil.doGet("https://e621.net/posts?tags=" + quest);
        } catch (SocketTimeoutException e) {
            Main.setNextSender(message_type, user_id, group_id, "??????????????????");
        }
        if (answer == null) {
            Main.setNextSender(message_type, user_id, group_id, "??????????????????");
            return;
        }
        int pos = answer.indexOf("data-id");
        pos = answer.indexOf("data-id", pos + 1);
        int id;
        try {
            id = Integer.parseInt(answer.substring(pos + 9, answer.indexOf(' ', pos + 1) - 1));
        } catch (NumberFormatException e) {
            Main.setNextSender(message_type, user_id, group_id, "None has been found.");
            return;
        }
        pos = answer.indexOf("data-fav-count");
        int fav_count = Integer.parseInt(answer.substring(pos + 16, answer.indexOf(' ', pos) - 1));

        try {
            answer = HttpURLConnectionUtil.doGet("https://e621.net/posts/" + id);
        } catch (SocketTimeoutException e) {
            Main.setNextSender(message_type, user_id, group_id, "??????????????????");
        }
        if (answer == null) {
            Main.setNextSender(message_type, user_id, group_id, "??????????????????");
            return;
        }

        pos = answer.indexOf("data-score");

        int score = Integer.parseInt(answer.substring(pos + 12, answer.indexOf(' ', pos + 1) - 1));
        pos = answer.indexOf("contentUrl");
        pos = answer.indexOf("src", pos);
        String imageUrl = answer.substring(pos + 5, answer.indexOf(' ', pos + 1) - 1);
        if (imageUrl.contains(".webm") || !answer.contains("contentUrl")) {
            retry++;
            if (retry > 3) {
                retry = 0;
                Main.setNextSender(message_type, user_id, group_id, "Get .webm.\nid: " + id);
                return;
            }
            System.out.println("retry");
            process(message_type, "621" + message, user_id, group_id);
            return;
        }

        quest = new StringBuilder();
        quest.append("[CQ:image,file=").append(imageUrl).append(",id=40000]\n");
        quest.append("Fav:").append(fav_count).append("  ");
        quest.append("Score:").append(score).append("\n");
        quest.append("id:").append(id);

        retry = 0;
        JSONObject J = JSONObject.parseObject(String.valueOf(Main.setNextSender(message_type, user_id, group_id, String.valueOf(quest))));
        if(J.getString("status").equals("failed")){
            Main.setNextSender(message_type, user_id, group_id, "??????????????????");
        } else {
            lastMsg = J.getJSONObject("data").getLong("message_id");
        }
    }

    private void adminProcessSet(String message_type, String message, long group_id, long user_id) {

        int level = getLevel(user_id);
        if (level <= 0) return;

        String[] sp = message.split(" ");
        if (sp.length <= 1) {
            Main.setNextSender(message_type, user_id, group_id, "????????????621.set (type id)/this level");
            return;
        }
        try {
            long id;
            int setLevel;
            if (sp[0].equals("this")) {
                if (sp.length != 2) {
                    Main.setNextSender(message_type, user_id, group_id, "????????????621.set (type id)/this level");
                    return;
                }
                sp[0] = message_type;
                if (message_type.equals("group")) {
                    id = group_id;
                } else {
                    id = user_id;
                }
                setLevel = Integer.parseInt(sp[1]);
            } else {
                if (sp.length != 3) {
                    Main.setNextSender(message_type, user_id, group_id, "????????????621.set (type id)/this level");
                    return;
                }
                id = Long.parseLong(sp[1]);
                setLevel = Integer.parseInt(sp[2]);
            }
            if (setLevel >= level) {
                Main.setNextSender(message_type, user_id, group_id, "????????????");
                return;
            }
            switch (sp[0]) {
                case "group" -> {
                    boolean flg = true;
                    for (int i = 0; i < JGroup.size(); i++) {
                        JSONObject J = JGroup.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            J.put("level", setLevel);
                            JGroup.set(i, J);
                            Main.setNextSender(message_type, user_id, group_id, "????????????");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JGroup.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "????????????");
                    }
                }
                case "private" -> {
                    boolean flg = true;
                    for (int i = 0; i < JPrivate.size(); i++) {
                        JSONObject J = JPrivate.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            JPrivate.set(i, JPrivate.getJSONObject(i).put("level", setLevel));
                            Main.setNextSender(message_type, user_id, group_id, "????????????");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JPrivate.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "????????????");
                    }
                }
                case "admin" -> {
                    boolean flg = true;
                    for (int i = 0; i < JAdmin.size(); i++) {
                        JSONObject J = JAdmin.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            JAdmin.set(i, JAdmin.getJSONObject(i).put("level", setLevel));
                            Main.setNextSender(message_type, user_id, group_id, "????????????");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JAdmin.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "????????????");
                    }
                }
                default -> Main.setNextSender(message_type, user_id, group_id, "type: group/private/admin or this");
            }

            saveLevel();
        } catch (NumberFormatException | IOException e) {
            Main.setNextSender(message_type, user_id, group_id, e.getMessage());
        }
    }

    private void adminProcessDel(String message_type, String message, long group_id, long user_id) {
        int level = getLevel(user_id);
        if (level <= 0) return;

        String[] sp = message.split(" ");
        if (sp.length != 2 && sp.length != 1) {
            Main.setNextSender(message_type, user_id, group_id, "????????????621.del (type id)/this");
            return;
        }
        try {
            long id;
            if (sp[0].equals("this")) {
                sp[0] = message_type;
                if (message_type.equals("group")) {
                    id = group_id;
                } else {
                    id = user_id;
                }
            } else {
                id = Long.parseLong(sp[1]);
            }

            switch (sp[0]) {
                case "group" -> {
                    boolean flg = true;
                    for (int i = 0; i < JGroup.size(); i++) {
                        JSONObject J = JGroup.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                            } else {
                                JGroup.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "?????????");
                    }
                }
                case "private" -> {
                    boolean flg = true;
                    for (int i = 0; i < JPrivate.size(); i++) {
                        JSONObject J = JPrivate.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                            } else {
                                JPrivate.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "?????????");
                    }
                }
                case "admin" -> {
                    boolean flg = true;
                    for (int i = 0; i < JAdmin.size(); i++) {
                        JSONObject J = JAdmin.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                            } else {
                                JAdmin.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "????????????");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "?????????");
                    }
                }
                default -> Main.setNextSender(message_type, user_id, group_id, "type: group/private/admin or this");
            }

            saveLevel();
        } catch (NumberFormatException | IOException e) {
            Main.setNextSender(message_type, user_id, group_id, e.toString());
        }
    }

    private int getLevel(long user_id) {
        int level = 0;
        for (int i = 0; i < JAdmin.size(); i++) {
            JSONObject J = JAdmin.getJSONObject(i);
            if (J.getLong("id") == user_id) level = J.getInteger("level");
        }
        return level;
    }

    private void saveLevel() throws IOException {
        FileWriter fw = new FileWriter("621Level.json", false);
        BufferedWriter bw = new BufferedWriter(fw);
        JSONObject J = new JSONObject();
        J.put("group", JGroup);
        J.put("private", JPrivate);
        J.put("admin", JAdmin);
        bw.write(J.toString());
        bw.close();
        fw.close();
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        if (!message.startsWith("621")) return false;
        if (message_type.equals("group")) {
            for (int i = 0; i < JGroup.size(); i++) {
                JSONObject J = JGroup.getJSONObject(i);
                if (J.getLong("id") == group_id) return true;
            }
        } else {
            for (int i = 0; i < JPrivate.size(); i++) {
                JSONObject J = JPrivate.getJSONObject(i);
                if (J.getLong("id") == user_id) return true;
            }
        }
        return false;
    }
}