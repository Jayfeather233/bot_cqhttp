package function.auto114514;

import main.Processable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

class info{
    int num;
    String ans;

    public info(int a, String b){
        num = a;
        ans = b;
    }
}

public class Auto114514Main implements Processable {
    ArrayList<info> ai = new ArrayList<>();
    final String __1 = "11-4-5+1-4";

    public Auto114514Main(){
        try{
            FileReader f = new FileReader("homodata.txt");
            Scanner S = new Scanner(f);
            while(S.hasNext()){
                ai.add(new info(S.nextInt(),S.next()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("似乎缺失了文件 homodata.txt");
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id) {
        try {
            long num = Long.parseLong(message.substring(4).trim());
            StringBuilder ans = new StringBuilder("" + num + "=");
            ans.append(getAns(num));
            main.Main.setNextSender(message_type,user_id,group_id,ans.toString());
        } catch(NumberFormatException e){
            main.Main.setNextSender(message_type,user_id,group_id,"需要一个数字，这事数字吗（恼）");
        }
    }

    private StringBuilder getAns(long num) {
        StringBuilder re = new StringBuilder();
        if(num < 0){
            return re.append("(").append(__1).append(")").append(getAns((-1)*num));
        }
        info x = getMinNum(num);
        assert x != null;
        if(x.num == num) return new StringBuilder(x.ans);
        if(num/x.num == 1) return re.append(x.ans).append("+(").append(getAns(num%x.num)).append(")");
        else return re.append("(").append(x.ans).append(")*(").append(getAns(num/x.num)).append(")+(").append(getAns(num%x.num)).append(")");
    }

    private info getMinNum(long u){
        for(info s : ai){
            if(s.num<=u) return s;
        }
        return null;
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("恶臭数字");
    }
}
