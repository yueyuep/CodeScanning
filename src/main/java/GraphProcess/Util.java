package GraphProcess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Util {
    private Util() {
    }

    public static byte[] readFileToByte(String path) throws IOException {
        File file = new File(path);
        InputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }

    public static String readFileToString(String path) throws IOException {
        return new String(readFileToByte(path));
    }

    private static class GsonHolder {
        private static final Gson INSTANCE = new GsonBuilder().disableHtmlEscaping()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public static Gson getGsonInstance() {
        return GsonHolder.INSTANCE;
    }

    public static String getClassLastName(String classPackage) {
        String[] classPackageSplit = classPackage.split("\\.");
        return classPackageSplit[classPackageSplit.length - 1];
    }

    public static String getClassLastName(Object object) {
        String classPackage = object.getClass().toString();
        String[] classPackageSplit = classPackage.split("\\.");
        return classPackageSplit[classPackageSplit.length - 1];
    }

    public static void saveToJsonFile(Object object, String fileName) {
        Gson gson = getGsonInstance();
//        String jsonString = gson.toJson(object).replace("\\","");//去掉转移字符
        String jsonString = gson.toJson(object).replace("\\","");//去掉转移字符
        //saveToFile(jsonString, fileName);

        saveToFile1(jsonString,fileName);
    }
      public static void saveToJsonFile1(Object object, String fileName){
          Gson gson = getGsonInstance();
        String jsonString = gson.toJson(object).replace("\\","");//去掉转移字符
        //saveToFile(jsonString, fileName);

        saveToFile1(jsonString,fileName);
      }

    public static void saveToFile(String text, String fileName) {
        File file = new File(fileName);
        try {
            FileWriter toFileWriter = new FileWriter(file);
            toFileWriter.write(text);
            toFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveToFile1(String text,String fileName){
        File file = new File(fileName);
        if (!file.exists()){
            file.getParentFile().mkdirs();
        }

        BufferedWriter out=null;
        try {
            out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName,true)));
            out.write(text+"\r\n");

        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public static ArrayList<String> readFileToArrayList(String fileName) {
        // 使用ArrayList来存储每行读取到的字符串
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            File file = new File(fileName);
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回数组
        return arrayList;
    }

    //链接url下载文件
    public static void downloadFile(String urlString, String savePath) {
        try {
            URL url = new URL(urlString);
            InputStream inputStream = url.openStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            FileOutputStream fileOutputStream = new FileOutputStream(new File(savePath));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            inputStream.close();
            dataInputStream.close();
            fileOutputStream.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isContain(String master, String sub) {
        return master.contains(sub);
    }

    public static boolean isContain(String master, String[] sub) {
        for (String s : sub) {
            if (isContain(master, s)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileNameOfFilePath(String filePath) {
        String[] paths = filePath.split("/");
        return paths[paths.length - 1];
    }

    public static void mkdirIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
