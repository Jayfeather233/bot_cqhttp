public class NextOutputThread implements Runnable{
    String s;
    public NextOutputThread(String input){
        s=input;
    }
    @Override
    public void run() {
        Main.setNextOutput(s);
    }
}
