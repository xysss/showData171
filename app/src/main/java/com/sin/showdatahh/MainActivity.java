package com.sin.showdatahh;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button1, button2, button3, button4, button_define;
    private TextView testView1;
    private ConstraintLayout ConstraintLayout1;
    private final String testCode1 = "C0010200150000000000000000000101F00100000000C0";  //第一个按键
    private final String testCode2 = "C0010200150000000000000000000101F00200000000C0";  //第二个按键
    private final String testCode3 = "C0010200150000000000000000000101F00300000000C0";  //第三个按键
    private final String testCode4 = "C001020016000000000000000000010180020001020000C0";  //开始原始数据上传
    private final String testCode5 = "C001020016000000000000000000010180020001000000C0";  //停止原始数据上传
    private final String testCode6 = "C0 01 02 00 15 00 00 00 00 00 00 00 00 00 01 01 80 00 00 00 00 00 C0";  //读取设备信息
    private final String testCode7 = "C0 01 02 00 15 00 00 00 00 00 00 00 00 00 01 01 08 01 00 00 00 00 C0";  //重启
    private final String testCode8 = "C0 01 02 00 19 00 00 00 00 00 00 00 00 00 01 01 80 03 00 04 00 00 00 00 00 00 C0";  //导出报警记录
    private final String testCode9 = "C0 01 02 00 15 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00 00 C0";  //握手命令
    public static final int NIU_BI_PASSWORD = 12345;  // 密码；
    private DatagramPacket dp, packet;
    private DatagramSocket ds; //指明发送端的端口号
    private EditText FileName_EditText, Alarm_description_editText;
    private String fileName = "admin", description = "空";
    private byte[] bRec = null;
    private byte[] dealAfterRec = null;
    private List<Byte> recList = new ArrayList();
    private boolean isopen = true;
    private BufferedReader reader;
    private String[] realData = new String[6];
    private String recycleData = "";//接收数据
    private int count = 1;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String fileNameKey="fileName";
    private String descriptionKey="description";
    private String dataKey="data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);  //隐藏标题栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //保持屏幕常亮


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //去掉状态栏
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button_define = findViewById(R.id.button_define);
        testView1 = findViewById(R.id.testView1);
        ConstraintLayout1 = findViewById(R.id.ConstraintLayout1);
        FileName_EditText = findViewById(R.id.FileName_EditText);
        Alarm_description_editText = findViewById(R.id.Alarm_description_editText);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button_define.setOnClickListener(this);

        if (ds == null) {
            try {
                ds = new DatagramSocket(6677); //指明本地发送端的端口号
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        //申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
        }
        new ReceiveThread().start();
        //myHandler.postDelayed(myRunnable, 100);//延时100毫秒
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ds != null) {
            ds.close();
        }
        isopen = false;

        //myHandler.removeCallbacks(myRunnable); //取消执行
    }

    private void show(final String response, final int aa) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (aa == 171) {
                    if (response != null) {
                        testView1.setText(response);
                    }
                }
            }
        });
    }

    private void sendUdp(final String testCode) {
        new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //byte[] buf = new String(testCode).getBytes();
                byte[] buf = MyFunc.HexToByteArr(testCode);
                //UDP是无连接的，所以要在发送的数据包裹中指定要发送到的ip：port
                dp = new DatagramPacket(buf, buf.length, new InetSocketAddress("192.168.1.1", 7788));
                try {
                    ds.send(dp);
                    show("发送成功", 171);
                } catch (IOException e) {
                    show("发送失败", 171);
                    e.printStackTrace();
                }
                //ds.close();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor = getSharedPreferences(dataKey, MODE_PRIVATE).edit();
        editor.putString(fileNameKey, fileName);
        editor.putString(descriptionKey, description);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = getSharedPreferences(dataKey, MODE_PRIVATE);
        fileName = preferences.getString(fileNameKey, "");
        if (!fileName.equals("")) {
            FileName_EditText.setText(fileName);
        }
        description = preferences.getString(descriptionKey, "");
        if (!description.equals("")) {
            Alarm_description_editText.setText(description);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:  //开始接收
                show("", 171);
                sendUdp(testCode4);
                isopen = true;
                break;
            case R.id.button2:  //停止接收
                show("", 171);
                sendUdp(testCode5);
                break;
            case R.id.button3:
                show("", 171);
                fileName = FileName_EditText.getText().toString();
                if (fileName.equals("")) {
                    Toast.makeText(MainActivity.this, "设备id不能为空！", Toast.LENGTH_SHORT).show();
                    fileName = "admin";
                } else {
                    Toast.makeText(MainActivity.this, "设置成功！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button4:
                showVerify();
                break;
            case R.id.button_define:
                description = Alarm_description_editText.getText().toString();
                if (description.equals("")) {
                    Toast.makeText(MainActivity.this, "描述内容不能为空！", Toast.LENGTH_SHORT).show();
                    description = "空";
                } else {
                    Toast.makeText(MainActivity.this, "设置成功！", Toast.LENGTH_SHORT).show();
                    FileUtils.saveInfo(MainActivity.this,description, "报警描述");
                }
                break;
            default:
                break;
        }
    }

    private void showVerify() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("验证密码");
        View view = View.inflate(MainActivity.this, R.layout.dialog_verify_password, null);
        final EditText dialog_verify_password_et = (EditText) view.findViewById(R.id.dialog_verify_password_et);
        builder.setView(view);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputNumStr = dialog_verify_password_et.getText().toString();
                if (TextUtils.isEmpty(inputNumStr)) {
                    Toast.makeText(MainActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    int inputNum = 0;
                    try {
                        inputNum = Integer.parseInt(inputNumStr);
                        if (NIU_BI_PASSWORD == inputNum) {
                            Intent intent = new Intent(MainActivity.this, DeleteActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.show();

    }


    public class ReceiveThread extends Thread {

        private final String TAG = "UdpReceiveThread";

        @Override
        public void run() {
            while (isopen) {//循环接收，isAlive() 判断防止无法预知的错误
                try {
                    //sleep(50);
                    recList.clear();
                    byte buffer[] = new byte[1024];
                    packet = new DatagramPacket(buffer, buffer.length);
                    ds.receive(packet); //阻塞式，接收发送方的 packet
                    bRec = new byte[packet.getLength()];
                    //InetAddress address = packet.getAddress();
                    for (int i = 0; i < packet.getLength(); i++) {
                        bRec[i] = packet.getData()[i];
                    }

                    for (int w = 0; w < bRec.length; w++) {
                        if (w == bRec.length - 1) {
                            recList.add(bRec[w]);
                            break;
                        }
                        if ((bRec[w] & 0xff) != 0xDB) {
                            recList.add(bRec[w]);
                        } else {
                            if ((bRec[w + 1] & 0xff) == 0xDC) {
                                recList.add(MyFunc.HexToByte("C0"));
                                w++;
                            } else if ((bRec[w + 1] & 0xff) == 0xDD) {
                                recList.add(MyFunc.HexToByte("DB"));
                                w++;
                            } else {
                                recList.add(bRec[w]);
                            }
                        }
                    }

                    dealAfterRec = new byte[recList.size()];
                    for (int e = 0; e < recList.size(); e++) {
                        dealAfterRec[e] = recList.get(e);
                    }
                    String bRecStr = MyFunc.ByteArrToHex(bRec);
                    String bRecStr1 = MyFunc.ByteArrToHex(dealAfterRec);
                    if ((dealAfterRec[16] & 0xff) == 0x80 && (dealAfterRec[17] & 0xff) == 0x01) {  //8001
                        byte[] timeByte = new byte[4];
                        byte[] timeByte2 = new byte[4];
                        String timeStr2="";
                        timeByte[1]=dealAfterRec[13];
                        timeByte[0]=dealAfterRec[14];
                        timeByte[2]=0;
                        timeByte[3]=0;
                        //int timeInt = MyFunc.bytesToIntLittle(timeByte, 0);
                        //String timeStr=String.valueOf(timeInt);
                        //Log.e("saved", "time: "+timeStr);
                        byte[] dealRec20 = new byte[240];
                        byte[] dealRec1 = new byte[40];
                        byte[] dealRec2 = new byte[40];
                        byte[] dealRec3 = new byte[40];
                        byte[] dealRec4 = new byte[40];
                        byte[] dealRec5 = new byte[40];
                        byte[] dealRec6 = new byte[40];
                        //byte[] dealRec7=new byte[1];  //当前状态
                        byte dealRec8 = 0;  //当前状态
                        for (int t = 0; t < 240; t++) {
                            dealRec20[t] = dealAfterRec[t + 20];
                        }
                        if (dealAfterRec.length > 264) {
                            //dealRec7[0]=dealAfterRec[264];
                            dealRec8 = dealAfterRec[264];
                            timeByte2[0]=dealAfterRec[260];
                            timeByte2[1]=dealAfterRec[261];
                            timeByte2[2]=dealAfterRec[262];
                            timeByte2[3]=dealAfterRec[263];
                            int timeInt2 = MyFunc.bytesToIntLittle(timeByte2, 0);
                            timeStr2=String.valueOf(timeInt2);
                            Log.e("saved", "time2: "+timeStr2);
                        }

                       /* String dd=MyFunc.bytesToHexString(dealRec20);
                        dealRec20=MyFunc.hexStringToBytes(dd);*/

                        for (int p = 0; p < 40; p++) {
                            dealRec1[p] = dealRec20[p];
                            dealRec2[p] = dealRec20[p + 40];
                            dealRec3[p] = dealRec20[p + 80];
                            dealRec4[p] = dealRec20[p + 120];
                            dealRec5[p] = dealRec20[p + 160];
                            dealRec6[p] = dealRec20[p + 200];
                        }

                        String[] resultStrings1 = new String[10];
                        String[] resultStrings2 = new String[10];
                        String[] resultStrings3 = new String[10];
                        String[] resultStrings4 = new String[10];
                        String[] resultStrings5 = new String[10];
                        String[] resultStrings6 = new String[10];

                       /* StringBuilder stringBuilder1 =new StringBuilder();
                        StringBuilder stringBuilder2 =new StringBuilder();
                        StringBuilder stringBuilder3 =new StringBuilder();
                        StringBuilder stringBuilder4 =new StringBuilder();
                        StringBuilder stringBuilder5 =new StringBuilder();
                        StringBuilder stringBuilder6 =new StringBuilder();
                        String spe1,spe2,spe3,spe4,spe5,spe6;*/
                        byte[] Rec1spe1 = new byte[4];
                        byte[] Rec1spe2 = new byte[4];
                        byte[] Rec1spe3 = new byte[4];
                        byte[] Rec1spe4 = new byte[4];
                        byte[] Rec1spe5 = new byte[4];
                        byte[] Rec1spe6 = new byte[4];
                        Log.e("saved","8001 :"+count);
                        for (int k = 0; k < 10; k++) {
                            for (int j = 0; j < 4; j++) {
                                Rec1spe1[j] = dealRec1[k * 4 + j];
                                Rec1spe2[j] = dealRec2[k * 4 + j];
                                Rec1spe3[j] = dealRec3[k * 4 + j];
                                Rec1spe4[j] = dealRec4[k * 4 + j];
                                Rec1spe5[j] = dealRec5[k * 4 + j];
                                Rec1spe6[j] = dealRec6[k * 4 + j];
                            }
                            int dealtest1 = MyFunc.bytesToIntLittle(Rec1spe1, 0);
                            int dealtest2 = MyFunc.bytesToIntLittle(Rec1spe2, 0);
                            int dealtest3 = MyFunc.bytesToIntLittle(Rec1spe3, 0);
                            int dealtest4 = MyFunc.bytesToIntLittle(Rec1spe4, 0);
                            int dealtest5 = MyFunc.bytesToIntLittle(Rec1spe5, 0);
                            int dealtest6 = MyFunc.bytesToIntLittle(Rec1spe6, 0);

                            //int dealtest7=MyFunc.bytesToIntLittle(dealRec7,0);
                            //int dealtest7=0;
                            int dealtest8 = (int) dealRec8;

                            resultStrings1[k] = "" + dealtest1 + "  ：  " + dealtest8;
                            resultStrings2[k] = "" + dealtest2 + "  ：  " + dealtest8;
                            resultStrings3[k] = "" + dealtest3 + "  ：  " + dealtest8;
                            resultStrings4[k] = "" + dealtest4 + "  ：  " + dealtest8;
                            resultStrings5[k] = "" + dealtest5 + "  ：  " + dealtest8;
                            resultStrings6[k] = "" + dealtest6 + "  ：  " + dealtest8;
                            Log.e("saved","begin"+count);
                            FileUtils.saveLog(MainActivity.this,resultStrings1[k], fileName, "1", String.valueOf(count),timeStr2);
                            FileUtils.saveLog(MainActivity.this,resultStrings2[k], fileName, "2", String.valueOf(count),timeStr2);
                            FileUtils.saveLog(MainActivity.this,resultStrings3[k], fileName, "3", String.valueOf(count),timeStr2);
                            FileUtils.saveLog(MainActivity.this,resultStrings4[k], fileName, "4", String.valueOf(count),timeStr2);
                            FileUtils.saveLog(MainActivity.this,resultStrings5[k], fileName, "5", String.valueOf(count),timeStr2);
                            FileUtils.saveLog(MainActivity.this,resultStrings6[k], fileName, "6", String.valueOf(count),timeStr2);
                            count++;


                           /* stringBuilder1.append(""+dealtest1+",");
                            stringBuilder2.append(""+dealtest2+",");
                            stringBuilder3.append(""+dealtest3+",");
                            stringBuilder4.append(""+dealtest4+",");
                            stringBuilder5.append(""+dealtest5+",");
                            stringBuilder6.append(""+dealtest6+",");*/
                        }
                       /* spe1=stringBuilder1.toString();
                        spe2=stringBuilder2.toString();
                        spe3=stringBuilder3.toString();
                        spe4=stringBuilder4.toString();
                        spe5=stringBuilder5.toString();
                        spe6=stringBuilder6.toString();*/

                        show("收到数据8001,存储完成！", 171);

                        //ds.close(); //必须及时关闭 socket，否则会出现 error


                    } else if ((dealAfterRec[16] & 0xff) == 0x08 && (dealAfterRec[17] & 0xff) == 0x00) {  //0800
                        show("收到数据", 171);
                        Log.e("saved", "0800");

                    } else {
                        show("0000", 171);
                        Log.e("saved", "未知");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break; //当 catch 到错误时，跳出循环
                }
            }
        }

    }

}