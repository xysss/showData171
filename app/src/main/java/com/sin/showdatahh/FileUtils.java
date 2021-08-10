package com.sin.showdatahh;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bysd-2 on 2018/6/5.
 */

public class FileUtils {

    public static void JsonWrite(Context mc, String fileName, String json) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        MediaScannerConnection.scanFile(mc, new String[] { fileName }, null, null);
    }


    /**
     * string写入file中，已存在得file将不再写入；
     *
     * @param fileName
     * @param write_str
     * @throws IOException
     */
    public static void stringWrite2file(String fileName, String write_str) throws IOException {
        try {
            boolean file = FileUtils.createFile(fileName);

            if (file) {
                FileOutputStream fout = new FileOutputStream(fileName);
                byte[] bytes = write_str.getBytes();
                fout.write(bytes);
                fout.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建单个文件   已存在得文件不继续
     *
     * @param descFileName 文件名，包含路径
     * @return 如果创建成功，则返回true，否则返回false
     */
    public static boolean createFile(String descFileName) {
        File file = new File(descFileName);
        if (file.exists()) {
            return false;
        }
        if (descFileName.endsWith(File.separator)) {
            return false;
        }
        if (!file.getParentFile().exists()) {
            // 如果文件所在的目录不存在，则创建目录
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        // 创建文件
        try {
            if (file.createNewFile()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 获取文件中每行的内容；
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static ArrayList<String> readTxtLine(String fileName) throws Exception {
        ArrayList<String> strList = new ArrayList<>();

        File file = new File(fileName);
        if (file.isDirectory()) {
            return null;
        } else {
            FileInputStream fileInputStream = new FileInputStream(file);
            if (fileInputStream != null) {

                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                // 分行读
                while ((line = bufferedReader.readLine()) != null) {
                    strList.add(line);
                }
                inputStreamReader.close();
            }
            return strList;
        }
    }

    //判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static String readTxtContent(String fileName) throws Exception {

        ArrayList<String> strList = new ArrayList<>();

        File file = new File(fileName);
        if (file.isDirectory()) {
            return "";
        } else {
            FileInputStream fileInputStream = new FileInputStream(file);
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                // 分行读
                while ((line = bufferedReader.readLine()) != null) {
                    strList.add(line);
                }
                inputStreamReader.close();
            }

            JSONObject jsonObject = new JSONObject();
            // 得到strList
            for (int i = 0; i < strList.size(); i++) {
                String s = strList.get(i);
                String[] split = s.split(":");
                jsonObject.put(split[0], split[1]);
            }

            return jsonObject.toString();

        }

    }


    /**
     * 判断文件编码
     *
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public static String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 判断SD卡是否可用
     *
     * @return SD卡可用返回true
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(status);
    }

    //写入自定义Log
    public static String saveLog(Context context, String s, String name, String number, String countId,String timeStr) {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) + "\r\n";
        String sb = countId + "       " + s + "       "+timeStr+ "       "+format+ "\n";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String time = formatter.format(new Date());
        String fileName = name + "-" + number + "-" + time + ".txt";
        if (FileUtils.hasSdcard()) {
           /* String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "171DataSave" + File.separator;*/
            String path = context.getExternalFilesDir(null).getPath() + File.separator + "171DataSave" + File.separator;
            //创建文件路径
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path + fileName, true);
                fos.write(sb.getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    //写入自定义Log
    public static String saveInfo(Context context, String content, String name) {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) + "\r\n";
        String saveContent = format + "         " + content + "\n";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String time = formatter.format(new Date());
        String fileName = name + ".txt";
        if (FileUtils.hasSdcard()) {
            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "171DataSave" + File.separator;*/
            String path = context.getExternalFilesDir(null).getPath() + File.separator + "171DataSave" + File.separator;

            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path + fileName, true);
                fos.write(saveContent.getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }


    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param path 文件或目录的绝对路径
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        return deleteFile(new File(path));
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param file
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    public static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }

        File[] files = file.listFiles();
        if (files == null)
            return true;
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

}
