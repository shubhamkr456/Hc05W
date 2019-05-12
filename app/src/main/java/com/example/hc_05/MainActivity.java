package com.example.hc_05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final int R_DISCOVERY_DEVICE = 0xf;
    public static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    private BluetoothDevice mDevice;
    private String mDeviceName = "Unknown Device";
    private static String address = "00:21:13:05:6A:CD";
    private BTHelper mBTHelper;
    private ScrollView mScrollView;
    private TextView tv_log;
    private EditText ev_cmd;
    private Spinner sp_br;
    private Button btn_send;
    private BluetoothAdapter bluetoothAdapter;
    public static final int WHAT_CONNECT = 0;
    public static final int WHAT_ERROR = 1;
    public static final int WHAT_RECV = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CONNECT:
                    boolean suc = (boolean) msg.obj;
                    if (suc) {
                        btn_send.setEnabled(true);
                        msg("Connected to " + mDeviceName + ".\n---------------");
                    } else {
                        msg("Can't connect to " + mDeviceName + ".");
                    }
                    break;
                case WHAT_ERROR:
                    msg("Lost connection.");
                    break;
                case WHAT_RECV:
                    msgData((String) msg.obj);
                    break;
            }
        }
    };

    private void msgData(String msg) {
        if (msg.equals(".")){
            tv_log.append("\n");
        }else{
            tv_log.append(msg);
        }
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }



    ////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
            Log.d("BT message main activity:", "...Switching Bluetooth ON...");
        }
        mDevice = bluetoothAdapter.getRemoteDevice(address);

        initView();
        BThelperInit();//startActivityForResult(new Intent(this, DevicesDiscoveryActivity.class), R_DISCOVERY_DEVICE);

    }

    private void initView() {
        mScrollView = findViewById(R.id.main_scrollview);
        tv_log = findViewById(R.id.main_logview);
        ev_cmd = findViewById(R.id.main_cmdview);
        sp_br =findViewById(R.id.sp_br);
        btn_send = findViewById(R.id.main_send_btn);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = ev_cmd.getText().toString();
                ev_cmd.setText("");
                String br = "\n";
                switch (sp_br.getSelectedItemPosition()) {
                    case 0:
                        br = "\n";
                        break;
                    case 1:
                        br = "";
                        break;
                    case 2:
                        br = "\r\n";
                        break;
                }
                mBTHelper.send((data + br).getBytes());
            }
        });
    }


    protected void BThelperInit(){
        if (mDevice.getName() != null) {
            mDeviceName = mDevice.getName();
        }
        msg("Device Name: " + mDevice.getName() + " Address: " + mDevice.getAddress());
        mBTHelper = new BTHelper(mDevice, new BTHelper.BTListener() {
            @Override
            public void onConnect(boolean success) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_CONNECT;
                msg.obj = success;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onDataReceived(String data) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_RECV;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError() {
                mHandler.sendEmptyMessage(WHAT_ERROR);
            }
        });
        msg("Connecting to " + mDeviceName + ".");
        mBTHelper.connect(DEVICE_UUID);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBTHelper != null) {
            mBTHelper.disconnect();
        }
    }

    public void msg(String msg) {
        tv_log.append(msg + "\n");
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
