package Game;

public interface Playable {
    void process(String message_type, String message, long group_id, long user_id);
    boolean check(String message_type, String message, long group_id, long user_id);
}
