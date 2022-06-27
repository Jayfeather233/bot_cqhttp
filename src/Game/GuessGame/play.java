package Game.GuessGame;

import Main.Main;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;
import java.util.Random;

public class play extends Thread {
    private boolean hasNextMessage = false;
    private String nextMessage;
    private String nextMessageType;
    private int stdAns = -1;
    private long user_id;
    private long group_id;
    private int game_type = -1;

    public play(long user_id, String type, long group_id) {
        nextMessageType = type;
        this.group_id = group_id;
        try {
            this.user_id = user_id;
            sendMsg("Which game do you want to play?\n1.Guess game\n2.Hard guess game");
            Thread.sleep(500);
            start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static int getU(String msg, int u) {
        for (int i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                u = -1;
                break;
            }
            u = u * 10 + msg.charAt(i) - '0';
            if (u < 0) break;
        }
        return u;
    }

    private void sendMsg(String msg) {
        JSONObject J = new JSONObject();
        if (nextMessageType.equals("private")) {
            J.put("message_type", "private");
            J.put("user_id", user_id);
            J.put("message", msg);
            Main.setNextSender("send_msg", J);
        } else if (nextMessageType.equals("group")) {
            J.put("group_id", group_id);
            J.put("message", "[CQ:at,qq=" + user_id + "]" + msg);
            Main.setNextSender("send_group_msg", J);
        }
    }

    private void end() {
        Main.endThread(user_id);
    }

    public void setNextMessage(String msg, String type, long group_id) {
        nextMessage = msg;
        nextMessageType = type;
        this.group_id = group_id;
        hasNextMessage = true;
    }

    public String getNextInput() {
        int tle = 0;
        while (!hasNextMessage) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                sendMsg("Sleep error. Force stop.");
                end();
                return null;
            }
            tle++;
            if (tle > 10 * 60 * 10) {
                sendMsg("TLE. Force stop.");
                end();
                return null;
            }
        }
        hasNextMessage = false;
        System.out.println("Get msg " + nextMessage);
        return nextMessage;
    }

    @Override
    public void run() {
        while (true) {
            String msg = getNextInput();
            if (msg == null) break;
            //处理输入
            if (msg.equals("end") || msg.equals(".end")) {
                break;
            }

            int u = 0;
            if (game_type == -1 || stdAns == -1) {
                u = getU(msg, u);
                if (u == -1) continue;
            }
            if (game_type == -1) {
                if (1 <= u && u <= 2) {
                    game_type = u;
                    sendMsg("Game ID Get.");
                    sendMsg("Please set upper bounder:");
                } else {
                    sendMsg("Unrecognizable game ID.");
                }
            } else if (stdAns == -1) {
                stdAns = (new Random()).nextInt(u);
                sendMsg("Upper Bounder has been set. Play Now.");
            } else {
                String ret = switch (game_type) {
                    case 1 -> guessPlay.play(msg, stdAns);
                    case 2 -> guessPlay.play2(msg, stdAns);
                    default -> null;
                };
                if (ret != null) sendMsg(ret);
                if (Objects.equals(ret, "Correct!")) {
                    break;
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendMsg("Game ended.");
        end();
    }
}
