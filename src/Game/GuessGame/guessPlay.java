package Game.GuessGame;

public class guessPlay {
    public static String play(String msg, int stdAns) {
        int u = 0;
        u = play.getU(msg, u);
        if (u == -1) return null;
        System.out.printf("Get Int %d\n", u);

        if (stdAns == u) {
            return "Correct!";
        }
        if (Math.abs(stdAns - u) <= 10) {
            return "Very close to ans!";
        } else if (stdAns > u) {
            return "Too small.";
        } else {
            return "Too big.";
        }
    }

    public static String play2(String msg, int stdAns) {
        int u = 0;
        u = play.getU(msg, u);
        if (u == -1) return null;
        System.out.printf("Get Int %d\n", u);

        if (stdAns == u) {
            return "Correct!";
        }
        if (Math.abs(stdAns - u) <= 10) {
            return "Very close to ans!";
        } else if (Math.abs(stdAns - u) <= 100) {
            return "close to ans";
        } else {
            return "quite difference to ans";
        }
    }
}
