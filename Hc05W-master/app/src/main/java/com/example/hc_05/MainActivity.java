package com.example.hc_05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
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
    public EditText ev_cmd;
    private Spinner sp_br;
    private Button btn_send;
    private BluetoothAdapter bluetoothAdapter;
    public static final int WHAT_CONNECT = 0;
    public static final int WHAT_ERROR = 1;
    public static final int WHAT_RECV = 2;
    String finalData = "";
    Button bt;

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
                    String data = String.valueOf(msg.obj);
                    String[] dataArr = data.split("-", 6);
                    JSONObject dataObj = new JSONObject();
                    try {
                        dataObj.put("time", dataArr[0]);
                        dataObj.put("rawData", dataArr[1]);
                        dataObj.put("sensorTime", dataArr[3]);
                        dataObj.put("glucoseLevel", dataArr[2]);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
                    String json = appSharedPrefs.getString("MyObject", "");
                    JSONArray jsonArray=new JSONArray();
                    try {
                        jsonArray = new JSONArray(json);
                    }
                    catch (JSONException e){
                        Log.e("log_tag", "Error parsing data " + e.toString());

                    }
                    //JSONArray jsonArray = new JSONArray();
                    jsonArray.put(dataObj);
                    JSONObject finalobject = new JSONObject();
                    try {
                        finalobject.put("data", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    saveToFileSystem(getApplicationContext(), data, "data");
                    //writeToFile(data,getApplicationContext());
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    prefsEditor.putString("MyObject", jsonArray.toString());
                    prefsEditor.commit();
                    Toast.makeText(getApplicationContext(), "Object stored in SharedPreferences", Toast.LENGTH_LONG);




                    msg((String) msg.obj);

                    break;
            }
        }
    };


    ////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        sp_br = findViewById(R.id.sp_br);
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
        bt = findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // JSONObject show=readFromFileSystem(getApplicationContext(),"data");
                //Log.d("meeeeeeeeeeeee",String.valueOf(show));
                 }
        });

    }


    protected void BThelperInit() {
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
                String str = Character.toString((char) Integer.parseInt(data));
                finalData += str;
                if (data.equals("10")) {
                    msg.obj = finalData;
                    finalData = "";
                    mHandler.sendMessage(msg);
                }
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

    public static synchronized void saveToFileSystem(Context context, String object, String binFileName) {
        try {
            String tempPath = Environment.getExternalStorageDirectory() + "/" + binFileName + ".txt";
            File file = new File(tempPath);
            OutputStreamWriter oos = new OutputStreamWriter(new FileOutputStream(file));
            oos.write(object);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized JSONObject readFromFileSystem(Context context, String binFileName) {
        JSONObject obj = new JSONObject();
        try {
            String tempPath = Environment.getExternalStorageDirectory() + "/" + binFileName + ".bin";
            File file = new File(tempPath);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                obj = (JSONObject) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


}


