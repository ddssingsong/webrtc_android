package com.dds.net.urlconn;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/11/28.
 * android_shuai@163.com
 */
public class UrlConnUtils {
    private static final String TAG = "dds_UrlConnUtils";

    public static String sendPost(String serverUrl, String formBody) throws Exception {
        String result;
        DataOutputStream out;
        URL url = new URL(serverUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        if (serverUrl.startsWith("https")) {
            trustAllHosts(connection);
            connection.setHostnameVerifier(DO_NOT_VERIFY);
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.connect();
        out = new DataOutputStream(connection.getOutputStream());
        if (formBody != null && !"".equals(formBody)) {
            out.writeBytes(formBody);
        }
        out.flush();
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            InputStream inputStream = connection.getInputStream();
            result = inputStream2String(inputStream);
        } else {
            throw new Exception(String.format("response code:%d, error msg:%s", responseCode, connection.getResponseMessage()));
        }
        connection.disconnect();
        out.close();
        return result;
    }

    public static String sendGet(String serverUrl, String param) throws Exception {
        String result;
        String reqUrl = serverUrl + (param == null ? "" : ("?" + param));
        URL url = new URL(reqUrl);

        // ---------------------------------https--------------------------
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//        if (serverUrl.startsWith("https")) {
//            trustAllHosts(connection);
//            connection.setHostnameVerifier(DO_NOT_VERIFY);
//        }
        // http
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            InputStream inputStream = connection.getInputStream();
            result = inputStream2String(inputStream);
        } else {
            throw new Exception(String.format("response code:%d, error msg:%s", responseCode, connection.getResponseMessage()));
        }
        connection.disconnect();
        return result;
    }

    public static boolean download(String u, String path) {
        try {
            URL url = new URL(u);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            trustAllHosts(connection);
            connection.setHostnameVerifier(DO_NOT_VERIFY);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            //可设置请求头
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            connection.setRequestProperty("Charset", "UTF-8");
            connection.connect();
            byte[] file = input2byte(connection.getInputStream());
            File file1 = writeBytesToFile(file, path);
            if (file1.exists()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 构建json参数
     */
    public static String builderJsonParams(Map<String, Object> params) {
        JSONObject jsonObject;
        try {
            Set<String> keySet = params.keySet();
            List<String> keyList = new ArrayList<>(keySet);
            Collections.sort(keyList);
            jsonObject = new JSONObject();
            for (String key : keyList) {
                Object value = params.get(key);
                if (value == null || "".equals(value)) {
                    continue;
                }
                jsonObject.put(key, String.valueOf(params.get(key)));
            }
        } catch (JSONException e) {
            return null;
        }
        return jsonObject.toString();
    }

    /**
     * 构建post参数
     */
    public static String builderUrlParams(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = params.keySet();
        List<String> keyList = new ArrayList<>(keySet);
        Collections.sort(keyList);
        for (String key : keyList) {
            Object value = params.get(key);
            if (value == null || "".equals(value)) {
                continue;
            }
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

    private static byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    private static File writeBytesToFile(byte[] b, String outputFile) {
        File file = null;
        FileOutputStream os = null;
        try {
            file = new File(outputFile);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            os = new FileOutputStream(file);
            os.write(b);
        } catch (Exception var13) {
            var13.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException var12) {
                var12.printStackTrace();
            }
        }
        return file;
    }

    private static String inputStream2String(InputStream inputStream) {
        ByteArrayOutputStream bos = null;
        byte[] bytes = new byte[1024];
        int len = 0;
        try {
            bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }
            return new String(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
    }};

    private static final HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;
}
