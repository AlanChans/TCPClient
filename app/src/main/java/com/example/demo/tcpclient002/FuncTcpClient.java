package com.example.demo.tcpclient002;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FuncTcpClient extends Activity {

    private String SP_IP = "sp_ip";
    private String SP_PORT = "sp_port";
    public static Context context;
    private Button btnStartClient,btnCloseClient,btnCleanClientRcv,btnClientSend,btnCleanClientSend;
    private TextView editClientSend,editClientIp,editClientPort;
    private TextView txtRcv, txtSend;
    private String TAG = "FuncTcpClient";
    private MyBtnClicker myBtnClicker = new MyBtnClicker();
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    private final MyHandler myHandler = new MyHandler(this);
    //应该是建立一个线程
    ExecutorService exec = Executors.newCachedThreadPool();
    ExecutorService execNum = Executors.newCachedThreadPool();
    private static TcpClient tcpClient = null;
    private TextView btnNumberSet;
    private TextView editNumberSet;
    private TextView btnOnSet;
    private TextView btnOffSet;
    private CheckBox cbRememberData;
    private String TAG_CHECK = "FuncTcpClient";
    private String SP_IS_REMEMBER_DATA = "sp_is_remember_data";
    private SharedPreferences sharedPreferences;
    private boolean misChecked =false;
    private String SP_SET_NUMBER = "sp_set_number";
    private String SP_CLIENT_RECV = "sp_client_recv";

    public class MyBtnClicker implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_tcpClientConn:
                    Log.i(TAG,"onClick:开始");
                    btnStartClient.setEnabled(false);
                    btnCloseClient.setEnabled(true);
//                    btnClientSend.setEnabled(true);
                    btnNumberSet.setEnabled(true);
                    btnOffSet.setEnabled(true);
                    btnOnSet.setEnabled(true);
                    tcpClient = new TcpClient(editClientIp.getText().toString(), getPort(editClientPort.getText().toString()));
                    exec.execute(tcpClient);
                    break;
                case R.id.btn_tcpClientClose:
                    tcpClient.closeSelf();
                    btnStartClient.setEnabled(true);
                    btnCloseClient.setEnabled(false);
//                    btnClientSend.setEnabled(false);
                    btnOnSet.setEnabled(false);
                    btnOffSet.setEnabled(false);
                    btnNumberSet.setEnabled(false);
                    break;
//                case R.id.btn_tcpCleanClientRecv:
//                    txtRcv.setText("");
//                    break;
//                case R.id.btn_tcpCleanClientSend:
//                    txtSend.setText("");
//                    break;
//                case R.id.btn_tcpClientSend:
//                    Message message = Message.obtain();
//                    message.what = 2;
//                    message.obj = editClientSend.getText().toString();
//                    myHandler.sendMessage(message);
//                    exec.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            tcpClient.send(editClientSend.getText().toString());
//                        }
//                    });
//                    break;
                case R.id.btn_setNumber:
                    Message messageNum = Message.obtain();
                    messageNum.what = 3;
                    messageNum.obj = editNumberSet.getText().toString();
                    myHandler.sendMessage(messageNum);
                    execNum.execute(new Runnable() {
                        @Override
                        public void run() {
                            tcpClient.send(("MN"+editNumberSet.getText().toString()));
                        }
                    });
                    break;
                case R.id.btn_setOn:
                    Message messageOn = Message.obtain();
                    messageOn.what = 4;
                    messageOn.obj ="SN";
                    myHandler.sendMessage(messageOn);
                    execNum.execute(new Runnable() {
                        @Override
                        public void run() {
                            tcpClient.send("SN");
                        }
                    });
                    break;
                case R.id.btn_setOff:
                    Message messageOff = Message.obtain();
                    messageOff.what = 5;
                    messageOff.obj ="SF";
                    myHandler.sendMessage(messageOff);
                    execNum.execute(new Runnable() {
                        @Override
                        public void run() {
                            tcpClient.send("SF");
                        }
                    });
                    break;


            }
        }
    }

    private int getPort(String msg) {
        if(msg.equals("")){
            msg = "1234";
        }
        return Integer.parseInt(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_func_tcp_client);
        context = this;
        bindID();
        bindListener();
        bindReceiver();
        Inin();
        //初始化数据回显
        initData();
    }

    private void initData() {
        //实例化sharedPreferences
        if(sharedPreferences == null){
            sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        //回显数据
        editClientIp.setText(sharedPreferences.getString(SP_IP,""));
        editClientPort.setText(sharedPreferences.getString(SP_PORT,""));
        editNumberSet.setText(sharedPreferences.getString(SP_SET_NUMBER,""));
        txtRcv.setText(sharedPreferences.getString(SP_CLIENT_RECV,""));

        misChecked = sharedPreferences.getBoolean(SP_IS_REMEMBER_DATA,false);
        cbRememberData.setChecked(misChecked);
    }

    private void bindReceiver() {
        IntentFilter intentFilter = new IntentFilter("tcpClientReceiver");
        registerReceiver(myBroadcastReceiver,intentFilter);
    }

    private void Inin() {
        btnCloseClient.setEnabled(false);
//        btnClientSend.setEnabled(false);
        btnOffSet.setEnabled(false);
        btnOnSet.setEnabled(false);
        btnNumberSet.setEnabled(false);
    }

    private void bindID(){
        btnStartClient = (Button)findViewById(R.id.btn_tcpClientConn);
        btnCloseClient = (Button)findViewById(R.id.btn_tcpClientClose);
//        btnCleanClientRcv = (Button)findViewById(R.id.btn_tcpCleanClientRecv);
//        btnCleanClientSend = (Button)findViewById(R.id.btn_tcpCleanClientSend);
//        btnClientSend = (Button) findViewById(R.id.btn_tcpClientSend);
        cbRememberData = findViewById(R.id.cb_remember_data);
        btnOnSet = findViewById(R.id.btn_setOn);
        btnOffSet = findViewById(R.id.btn_setOff);
        btnNumberSet = findViewById(R.id.btn_setNumber);
        editNumberSet = findViewById(R.id.edit_setNumber);
        editClientPort =  findViewById(R.id.edit_tcpClientPort);
        editClientIp =  findViewById(R.id.edit_tcpClientIP);
//        editClientSend =  findViewById(R.id.edit_tcpClientSend);
        txtRcv = findViewById(R.id.txt_ClientRcv);
        //       txtSend = findViewById(R.id.txt_ClientSend);



    }
    private void bindListener(){
        btnStartClient.setOnClickListener(myBtnClicker);
        btnCloseClient.setOnClickListener(myBtnClicker);
//        btnCleanClientRcv.setOnClickListener(myBtnClicker);
//        btnCleanClientSend.setOnClickListener(myBtnClicker);
//        btnClientSend.setOnClickListener(myBtnClicker);
        btnNumberSet.setOnClickListener(myBtnClicker);
        btnOnSet.setOnClickListener(myBtnClicker);
        btnOffSet.setOnClickListener(myBtnClicker);


        txtRcv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(misChecked){
                    //实例化SharedPreferences对象
                    if(sharedPreferences == null){
                        sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
                    }
                    //实例化SharedPreferences的编辑者对象
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(SP_CLIENT_RECV,txtRcv.getText().toString());
                    //提交
                    edit.commit();
                }

            }

        });

        editNumberSet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(misChecked){
                    //实例化SharedPreferences对象
                    if(sharedPreferences == null){
                        sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
                    }
                    //实例化SharedPreferences的编辑者对象
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(SP_SET_NUMBER,editNumberSet.getText().toString());
                    //提交
                    edit.commit();
                }
            }
        });

        editClientPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(misChecked){
                    //实例化SharedPreferences对象
                    if(sharedPreferences == null){
                        sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
                    }
                    //实例化SharedPreferences的编辑者对象
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(SP_PORT,editClientPort.getText().toString());
                    //提交
                    edit.commit();
                }
            }

        });

        editClientIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(misChecked){
                    //实例化SharedPreferences对象
                    if(sharedPreferences == null){
                        sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
                    }
                    //实例化SharedPreferences的编辑者对象
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(SP_IP,editClientIp.getText().toString());
                    //提交
                    edit.commit();
                }
            }

        });

        cbRememberData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG_CHECK, "状态为："+ isChecked);
                misChecked = isChecked;
                if(isChecked){
                    //实例化SharedPreferences对象
                    if(sharedPreferences == null){
                        sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
                    }

                    //实例化SharedPreferences的编辑者对象
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    //存储数据
                    edit.putString(SP_IP,editClientIp.getText().toString());
                    edit.putString(SP_PORT,editClientPort.getText().toString());
                    edit.putString(SP_SET_NUMBER,editNumberSet.getText().toString());
                    edit.putString(SP_CLIENT_RECV,txtRcv.getText().toString());
                    edit.putBoolean(SP_IS_REMEMBER_DATA,isChecked);
                    //提交
                    edit.commit();
                }
            }
        });
    }

    private class MyHandler extends android.os.Handler{
        private  WeakReference<FuncTcpClient> mActivity;

        MyHandler(FuncTcpClient activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity != null){
                switch (msg.what){
                    case 1:
                        txtRcv.setText(msg.obj.toString());
                        // txtRcv.append(msg.obj.toString());
                        break;
//                    case 2:
//                        txtSend.append(msg.obj.toString());
//                        break;
                }
            }
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction){
                case "tcpClientReceiver":
                    String msg = intent.getStringExtra("tcpClientReceiver");
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = msg;
                    myHandler.sendMessage(message);
                    break;
            }
        }
    }
}
