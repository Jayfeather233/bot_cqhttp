package HTTPConnect;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * @author riemann
 * date 2019/05/24 23:42
 */
public class HttpURLConnectionUtil {

    /**
     * Http get请求
     *
     * @param httpUrl 连接
     * @return 响应数据
     */
    public static String doGet(String httpUrl) {
        //链接

        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            //创建连接
            URL url = new URL(httpUrl);

            connection = (HttpURLConnection) url.openConnection();

            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setReadTimeout(15000);
            //开始连接
            connection.connect();
            //获取响应数据
            if (connection.getResponseCode() == 200) {
                //获取返回的数据
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String temp;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result.toString();
    }

    /**
     * Http post请求
     *
     * @param ADD_URL 连接
     * @param obj     参数
     */
    public static StringBuffer doPost(String ADD_URL, JSONObject obj) {

        try {
            //创建连接
            URL url = new URL(ADD_URL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            if (obj != null) connection.setRequestProperty("Content-Type", "application/json;charset:utf-8;");

            connection.connect();

            //POST请求
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());

            if (obj != null) out.write((obj + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();

            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer();
            while ((lines = reader.readLine()) != null) {
                //lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                sb.append(lines);
            }
            //System.out.println(sb);
            reader.close();
            // 断开连接
            connection.disconnect();
            return sb;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

}