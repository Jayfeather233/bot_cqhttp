import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/*
    test code
    never used
*/
public class ImageDownloader {
    public static void main(String[] args) throws IOException {

        URL url1 = new URL("http://pic38.nipic.com/20140225/2531170_214014788000_2.jpg");
        URLConnection uc = url1.openConnection();
        InputStream inputStream = uc.getInputStream();

        FileOutputStream out = new FileOutputStream("\"D:\\QQ bot\\tmp.jpg\"");
        int j = 0;
        while ((j = inputStream.read()) != -1) {
            out.write(j);
        }
        inputStream.close();


    }
}
