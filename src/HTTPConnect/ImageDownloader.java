package HTTPConnect;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/*
    test code
    never used
*/
public class ImageDownloader {
    public static void main(String[] args) throws IOException {
        String fileAddr;
        Scanner S = new Scanner(System.in);
        fileAddr = S.next();

        URL url1 = new URL(fileAddr);
        URLConnection uc = url1.openConnection();
        InputStream inputStream = uc.getInputStream();

        FileOutputStream out = new FileOutputStream("./tmp."+fileAddr.substring(fileAddr.length()-3));
        int j;
        while ((j = inputStream.read()) != -1) {
            out.write(j);
        }
        inputStream.close();
    }
}
