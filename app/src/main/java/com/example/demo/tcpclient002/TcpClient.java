package com.example.demo.tcpclient002;

import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
public class TcpClient implements Runnable{


    private String serverIP = "192.168.10.2";
    private Socket socket = null;
    private int serverPort = 1234;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = true;
    private byte[] buff = new byte[4096];
    private int rcvLen;
    private String TAG = "TcpClient";
    private String rcvMsg;

    public TcpClient(String ip, int port){
        this.serverIP = ip;
        this.serverPort = port;

    }
    public void closeSelf(){
        isRun = false;
    }
    public void send(String msg){
        pw.println(msg);
        pw.flush();
    }
    @Override
    public void run() {
        try {
            socket = new Socket(serverIP, serverPort);
            socket.setSoTimeout(5000);
            pw = new PrintWriter(socket.getOutputStream(), true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        }catch (IOException e){
            e.printStackTrace();
        }
        while (isRun){
            try{
                rcvLen = dis.read(buff);
                rcvMsg = new String(buff,0,rcvLen,"utf-8");
                Log.i(TAG, "run:收到消息："+ rcvMsg);
                Intent intent = new Intent();
                //建立一个消息键值对
                intent.setAction("tcpClientReceiver");
                intent.putExtra("tcpClientReceiver",rcvMsg);
                //将消息发给主页面
                FuncTcpClient.context.sendBroadcast(intent);
                if(rcvMsg.equals("QuitClient")){//服务器要求客户端结束
                    isRun = false;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try{
            pw.close();
            is.close();
            dis.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
