package com.gxwtech.roundtrip2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gxwtech.roundtrip2.HistoryActivity.HistoryPageListActivity;
import com.gxwtech.roundtrip2.ServiceData.BasalProfile;
import com.gxwtech.roundtrip2.ServiceData.BolusWizardCarbProfile;
import com.gxwtech.roundtrip2.ServiceData.ISFProfile;
import com.gxwtech.roundtrip2.ServiceData.PumpModelResult;
import com.gxwtech.roundtrip2.ServiceData.ReadPumpClockResult;
import com.gxwtech.roundtrip2.ServiceMessageViewActivity.ServiceMessageViewListActivity;
import com.gxwtech.roundtrip2.util.tools;

import info.nightscout.androidaps.interfaces.PumpDescription;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceNotification;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.medtronic.driver.MedtronicPumpStatus;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicConst;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.androidaps.utils.SP;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private BroadcastReceiver mBroadcastReceiver;

    private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    BroadcastReceiver apsAppConnected;
    Bundle storeForHistoryViewer;

    // UI items
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLinear;
    private Toolbar toolbar;

    public static Context mContext; // TODO: 09/07/2016 @TIM this should not be needed
    BroadcastReceiver btReceiver;
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
                    msg((String) msg.obj);
//                    ShowAAPS2Activity hi=new ShowAAPS2Activity(dataArr[2]);
//                    hi.startAction();
                    saveToFileSystem(getApplicationContext(), data, "data");
                    //writeToFile(data,getApplicationContext());
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    prefsEditor.putString("MyObject", jsonArray.toString());
                    prefsEditor.commit();
                    Toast.makeText(getApplicationContext(), "Object stored in SharedPreferences", Toast.LENGTH_LONG);




//                    msg((String) msg.obj);

                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMenuAndToolbar();

        mContext = this; // TODO: 09/07/2016 @TIM this should not be needed
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
            Log.d("BT messagemainactivity:", "...Switching Bluetooth ON...");
        }
        mDevice = bluetoothAdapter.getRemoteDevice(address);

        initView();


        // Sets default Preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_pump, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_rileylink, false);

        setBroadcastReceiver();

        setBTReceiver();

        // Temporary AAPS
        MedtronicUtil.setPumpStatus(new MedtronicPumpStatus(new PumpDescription()));

        /* start the RileyLinkMedtronicService */
        /*
         * using startService() will keep the service running until it is explicitly stopped
         * with stopService() or by RileyLinkMedtronicService calling stopSelf().
         * Note that calling startService repeatedly has no ill effects on RileyLinkMedtronicService
         */
        // explicitly call startService to keep it running even when the GUI goes away.
        Intent bindIntent = new Intent(this, RileyLinkMedtronicService.class);
        startService(bindIntent);


        linearProgressBar = (ProgressBar)findViewById(R.id.progressBarCommandActivity);
        spinnyProgressBar = (ProgressBar)findViewById(R.id.progressBarSpinny);
        BThelperInit();//startActivityForResult(new Intent(this, DevicesDiscoveryActivity.class), R_DISCOVERY_DEVICE);
    }


    private void setBTReceiver() {
        btReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            LOG.trace("Bluetooth off");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            // LOG.trace("Turning Bluetooth off...");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            LOG.trace("Bluetooth on");
                            RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.Intents.BluetoothReconnected);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            // LOG.trace("Turning Bluetooth on...");
                            break;
                    }
                }
            }
        };

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        // LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(btReceiver, filter);
        registerReceiver(btReceiver, filter);

    }
    private void initView() {
        mScrollView = findViewById(R.id.main_scrollview);
        tv_log = findViewById(R.id.main_logview);
        ev_cmd = findViewById(R.id.main_cmdview);
        sp_br = findViewById(R.id.sp_br);
        btn_send=findViewById(R.id.button);
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
    private void writeToFile(String data, Context context) {
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



    @Override
    protected void onResume() {
        super.onResume();

        setBroadcastReceiver();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (apsAppConnected != null) {
            LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(apsAppConnected);
        }
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(mBroadcastReceiver);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (btReceiver != null) {
            unregisterReceiver(btReceiver);
        }
        if (mBTHelper != null) {
            mBTHelper.disconnect();
        }

    }


    public void setBroadcastReceiver() {
        // Register this receiver for UI Updates
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent receivedIntent) {

                if (receivedIntent == null) {
                    Log.e(TAG, "onReceive: received null intent");
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.instance());
                    ServiceTransport transport;

                    switch (receivedIntent.getAction()) {
                        case RT2Const.local.INTENT_serviceConnected:
                        case RT2Const.local.INTENT_NEW_rileylinkAddressKey:
                            showIdle();
                            /**
                             * Client MUST send a "UseThisRileylink" message because it asserts that
                             * the user has given explicit permission to use bluetooth.
                             *
                             * We can change the format so that it is a simple "bluetooth OK" message,
                             * rather than an explicit address of a Rileylink, and the Service can
                             * use the last known good value. But the kick-off of bluetooth ops must
                             * come from an Activity.
                             */
                            String RileylinkBLEAddress = SP.getString(RileyLinkConst.Prefs.RileyLinkAddress, "");
                            if (RileylinkBLEAddress.equals("")) {
                                // TODO: 11/07/2016 @TIM UI message for user
                                Log.e(TAG, "No Rileylink BLE Address saved in app");
                            } else {
                                showBusy("Configuring Service", 50);
                                MainApp.getServiceClientConnection().setThisRileylink(RileylinkBLEAddress);
                            }
                            break;

                        case RT2Const.local.INTENT_NEW_disconnectRileyLink: {
                            showBusy("Configuring Service", 50);
                            MainApp.getServiceClientConnection().setThisRileylink(null);
                        }
                            break;

                        case RT2Const.local.INTENT_NEW_pumpIDKey:
                            MainApp.getServiceClientConnection().sendPUMP_useThisDevice(
                                SP.getString(MedtronicConst.Prefs.PumpSerial, ""));
                            break;
                        case RT2Const.local.INTENT_historyPageViewerReady:
                            Intent sendHistoryIntent = new Intent(RT2Const.local.INTENT_historyPageBundleIncoming);
                            sendHistoryIntent.putExtra(RT2Const.IPC.MSG_PUMP_history_key, storeForHistoryViewer);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendHistoryIntent);
                            break;
                        case RT2Const.IPC.MSG_ServiceResult:
                            Log.i(TAG, "Received ServiceResult");

                            Bundle bundle = receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey);
                            transport = new ServiceTransport(bundle);
                            if (transport.commandDidCompleteOK()) {
                                String originalCommandName = transport.getOriginalCommandName();
                                switch (originalCommandName) {
                                    case "ReadPumpModel":
                                        PumpModelResult modelResult = new PumpModelResult();
                                        modelResult.initFromServiceResult(transport.getServiceResult());
                                        String pumpModelString = modelResult.getPumpModel();
                                        // GGW Tue Jul 12 02:29:54 UTC 2016: ok, now what do we do with the pump model?
                                        showIdle();
                                        break;
                                    case "ReadPumpClock":
                                        ReadPumpClockResult clockResult = new ReadPumpClockResult();
                                        clockResult.initFromServiceResult(transport.getServiceResult());
                                        TextView pumpTimeTextView = (TextView)findViewById(R.id.textViewPumpClockTime);
                                        pumpTimeTextView.setText(clockResult.getTimeString());
                                        showIdle();
                                        break;
                                    case "FetchPumpHistory":
                                        storeForHistoryViewer = receivedIntent.getExtras().getBundle(
                                            RT2Const.IPC.bundleKey);
                                        startActivity(new Intent(context, HistoryPageListActivity.class));
                                        // wait for history viewer to announce "ready"
                                        showIdle();
                                        break;
                                    case "RetrieveHistoryPage":
                                        storeForHistoryViewer = receivedIntent.getExtras().getBundle(
                                            RT2Const.IPC.bundleKey);
                                        startActivity(new Intent(context, HistoryPageListActivity.class));
                                        // wait for history viewer to announce "ready"
                                        showIdle();
                                        break;
                                    case "ISFProfile":
                                        ISFProfile isfProfile = new ISFProfile();
                                        isfProfile.initFromServiceResult(transport.getServiceResult());
                                        // TODO: do something with isfProfile
                                        showIdle();
                                        break;
                                    case "BasalProfile":
                                        BasalProfile basalProfile = new BasalProfile();
                                        basalProfile.initFromServiceResult(transport.getServiceResult());
                                        // TODO: do something with basal profile
                                        showIdle();
                                        break;
                                    case "BolusWizardCarbProfile":
                                        BolusWizardCarbProfile carbProfile = new BolusWizardCarbProfile();
                                        carbProfile.initFromServiceResult(transport.getServiceResult());
                                        // TODO: do something with carb profile
                                        showIdle();
                                        break;
                                    case "UpdatePumpStatus":
                                        // rebroadcast for HAPP

                                        break;
                                    default:
                                        Log.e(
                                            TAG,
                                            "Dunno what to do with this command completion: "
                                                + transport.getOriginalCommandName());
                                }
                            } else {
                                Log.e(TAG, "Command failed? " + transport.getOriginalCommandName());
                            }
                            break;
                        case RT2Const.IPC.MSG_ServiceNotification:
                            transport = new ServiceTransport(receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey));
                            ServiceNotification notification = transport.getServiceNotification();
                            String note = notification.getNotificationType();
                            switch (note) {
                                case RT2Const.IPC.MSG_BLE_RileyLinkReady:
                                    setRileylinkStatusMessage("OK");
                                    break;
                                case RT2Const.IPC.MSG_PUMP_pumpFound:
                                    setPumpStatusMessage("OK");
                                    break;
                                case RT2Const.IPC.MSG_PUMP_pumpLost:
                                    setPumpStatusMessage("Lost");
                                    break;
                                case RT2Const.IPC.MSG_note_WakingPump:
                                    showBusy("Waking Pump", 99);
                                    break;
                                case RT2Const.IPC.MSG_note_FindingRileyLink:
                                    showBusy("Finding RileyLink", 99);
                                    break;
                                case RT2Const.IPC.MSG_note_Idle:
                                    showIdle();
                                    break;
                                default:
                                    Log.e(TAG, "Unrecognized Notification: '" + note + "'");
                            }
                            break;
                        default:
                            Log.e(TAG, "Unrecognized intent action: " + receivedIntent.getAction());
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.local.INTENT_serviceConnected);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceResult);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceNotification);
        intentFilter.addAction(RT2Const.local.INTENT_historyPageViewerReady);
        intentFilter.addAction(RT2Const.local.INTENT_NEW_disconnectRileyLink);

        linearProgressBar = (ProgressBar)findViewById(R.id.progressBarCommandActivity);
        spinnyProgressBar = (ProgressBar)findViewById(R.id.progressBarSpinny);
        LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     * GUI element functions
     */

    private int mProgress = 0;
    private int mSpinnyProgress = 0;
    private ProgressBar linearProgressBar;
    private ProgressBar spinnyProgressBar;
    private static final int spinnyFPS = 10;
    private Thread spinnyThread;


    void showBusy(String activityString, int progress) {
        mProgress = progress;
        TextView tv = (TextView)findViewById(R.id.textViewActivity);
        tv.setText(activityString);
        linearProgressBar.setProgress(progress);
        if (progress > 0) {
            spinnyProgressBar.setVisibility(View.VISIBLE);
            if (spinnyThread == null) {
                spinnyThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while ((mProgress > 0) && (mProgress < 100)) {
                            mSpinnyProgress += 100 / spinnyFPS;
                            spinnyProgressBar.setProgress(mSpinnyProgress);
                            SystemClock.sleep(1000 / spinnyFPS);
                        }
                        spinnyThread = null;
                    }
                });
                spinnyThread.start();
            }
        } else {
            spinnyProgressBar.setVisibility(View.INVISIBLE);
        }
    }


    void showIdle() {
        showBusy("Idle", 0);
    }


    void setRileylinkStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldRileyLink);
        field.setText(statusMessage);
    }


    void setPumpStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldPump);
        field.setText(statusMessage);
    }


    public void onTunePumpButtonClicked(View view) {
        MainApp.getServiceClientConnection().doTunePump();
    }


    public void onFetchHistoryButtonClicked(View view) {
        /* does not work. Crashes sig 11 */
        showBusy("Fetch history page 0", 50);
        MainApp.getServiceClientConnection().doFetchPumpHistory();
    }


    public void onFetchSavedHistoryButtonClicked(View view) {
        showBusy("Fetching history (not saved)", 50);
        MainApp.getServiceClientConnection().doFetchSavedHistory();
    }


    public void onReadPumpClockButtonClicked(View view) {
        showBusy("Reading Pump Clock", 50);
        MainApp.getServiceClientConnection().readPumpClock();
    }


    public void onGetISFProfileButtonClicked(View view) {
        // ServiceCommand getISFProfileCommand = ServiceClientActions.makeReadISFProfileCommand();
        // roundtripServiceClientConnection.sendServiceCommand(getISFProfileCommand);
        MainApp.getServiceClientConnection().readISFProfile();
    }


    public void onViewEventLogButtonClicked(View view) {
        startActivity(new Intent(getApplicationContext(), ServiceMessageViewListActivity.class));
    }


    public void onUpdateAllStatusButtonClicked(View view) {
        MainApp.getServiceClientConnection().updateAllStatus();
    }


    public void onShowAAPSButtonClicked(View view) {
        try {
            // startActivity(new Intent(getApplicationContext(), ShowAAPSActivity.class));
            Intent i=new Intent(this,ShowAAPS2Activity.class);
            startActivity(i);
        } catch (Exception ex) {
            LOG.error("Error loading activity: " + ex.getMessage(), ex);
        }
    }


    public void onGetCarbProfileButtonClicked(View view) {
        // MainApp.getServiceClientConnection().re(ServiceClientActions.makeReadBolusWizardCarbProfileCommand());
    }


    /* UI Setup */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(mDrawerLinear);
                return true;

            default:
                return true;
        }
    }


    public void setupMenuAndToolbar() {
        // Setup menu
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLinear = (LinearLayout)findViewById(R.id.left_drawer);
        toolbar = (Toolbar)findViewById(R.id.mainActivityToolbar);
        Drawable logsIcon = getDrawable(R.drawable.file_chart);
        Drawable historyIcon = getDrawable(R.drawable.history);
        Drawable settingsIcon = getDrawable(R.drawable.settings);
        Drawable catIcon = getDrawable(R.drawable.cat);
        Drawable apsIcon = getDrawable(R.drawable.refresh);

        logsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        historyIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        settingsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        catIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        apsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);

        ListView mDrawerList = (ListView)findViewById(R.id.navList);
        ArrayList<NavItem> menuItems = new ArrayList<>();
        menuItems.add(new NavItem("APS Integration", apsIcon));
        menuItems.add(new NavItem("Pump History", historyIcon));
        menuItems.add(new NavItem("Treatment Logs", logsIcon));
        menuItems.add(new NavItem("Settings", settingsIcon));
        menuItems.add(new NavItem("View LogCat", catIcon));
        DrawerListAdapter adapterMenu = new DrawerListAdapter(this, menuItems);
        mDrawerList.setAdapter(adapterMenu);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // Check APS App Connectivity
                        // sendAPSAppMessage(view);
                        break;
                    case 1:
                        // Pump History
                        startActivity(new Intent(getApplicationContext(), HistoryPageListActivity.class));
                        break;
                    case 2:
                        // Treatment Logs
                        startActivity(new Intent(getApplicationContext(), TreatmentHistory.class));
                        break;
                    case 3:
                        // Settings
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case 4:
                        // View LogCat
                        tools.showLogging();
                        break;
                }
                mDrawerLayout.closeDrawers();
            }
        });

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
            R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                // Insulin Integration App, try and connect
                // checkInsulinAppIntegration(false);
            }


            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /* Functions for APS App Service */

    // Our Service that APS App will connect to
    private Messenger myService = null;
    private ServiceConnection myConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            myService = new Messenger(service);

            // Broadcast there has been a connection
            Intent intent = new Intent("APS_CONNECTED");
            LocalBroadcastManager.getInstance(MainApp.instance()).sendBroadcast(intent);
        }


        @Override
        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            // FYI, only called if Service crashed or was killed, not on unbind
        }
    };

    // public void sendAPSAppMessage(final View view) {
    // //listen out for a successful connection
    // apsAppConnected = new BroadcastReceiver() {
    // @Override
    // public void onReceive(Context context, Intent intent) {
    //
    // Resources appR = view.getContext().getResources();
    // CharSequence txt = appR.getText(appR.getIdentifier("app_name", "string", view.getContext().getPackageName()));
    //
    // Message msg = Message.obtain();
    // Bundle bundle = new Bundle();
    // bundle.putString(RT2Const.commService.ACTION, RT2Const.commService.OUTGOING_TEST_MSG);
    // bundle.putString(RT2Const.commService.REMOTE_APP_NAME, txt.toString());
    // msg.setData(bundle);
    //
    // try {
    // myService.send(msg);
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // //cannot Bind to service
    // Snackbar snackbar = Snackbar
    // .make(view, "error sending msg: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE);
    // snackbar.show();
    // }
    //
    // if (apsAppConnected != null)
    // LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(apsAppConnected); //Stop listening for
    // new connections
    // MainApp.instance().unbindService(myConnection);
    // }
    // };
    // LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(apsAppConnected, new
    // IntentFilter("APS_CONNECTED"));
    //
    // connect_to_aps_app(MainApp.instance());
    // }

    // //Connect to the APS App Treatments Service
    // private void connect_to_aps_app(Context c) {
    // // TODO: 16/06/2016 add user selected aps app
    // Intent intent = new Intent("com.hypodiabetic.happ.services.TreatmentService");
    // intent.setPackage("com.hypodiabetic.happ");
    // c.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    // }

}

class NavItem {

    String mTitle;
    Drawable mIcon;


    public NavItem(String title, Drawable icon) {
        mTitle = title;
        mIcon = icon;
    }
}

class DrawerListAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<NavItem> mNavItems;


    public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
        mContext = context;
        mNavItems = navItems;
    }


    @Override
    public int getCount() {
        return mNavItems.size();
    }


    @Override
    public Object getItem(int position) {
        return mNavItems.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.menu_item, null);
        } else {
            view = convertView;
        }

        TextView titleView = (TextView)view.findViewById(R.id.menuText);
        ImageView iconView = (ImageView)view.findViewById(R.id.menuIcon);

        titleView.setText(mNavItems.get(position).mTitle);
        iconView.setBackground(mNavItems.get(position).mIcon);
        return view;
    }
}
