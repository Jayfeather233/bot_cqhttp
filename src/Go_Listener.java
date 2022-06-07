import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class Proc extends Thread{
    private Socket socket;
    public Proc (Socket socket){
        this.socket=socket;
        start();
    }
    @Override
    public void run() {
        try {
            BufferedReader in;
            PrintWriter out;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new DataOutputStream(socket.getOutputStream()), true);
            String str;
            while (true) {
                str = in.readLine();
                //System.out.println("Receive: " + str);
                if (str.length()>=1&&str.charAt(0)=='{') {
                    NextOutputThread not=new NextOutputThread(str);
                    new Thread(not).start();
                    break;
                }
            }
            //System.out.println(socket + ", seesion closing....");

            out.write("HTTP/1.1 200 OK" +'\n');
            out.write("Content-Type: application/json" + '\n');
            out.write("Content-Length: 0" +'\n'+'\n');
            //默认不进行快速操作，返回空的body

            out.flush();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class Go_Listener implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSock = new ServerSocket(9808);
            System.out.println("start.");
            while (true) {//端口监听，多线程操作
                Socket socket = serverSock.accept();
                //System.out.println("Accepted.");
                new Proc(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}