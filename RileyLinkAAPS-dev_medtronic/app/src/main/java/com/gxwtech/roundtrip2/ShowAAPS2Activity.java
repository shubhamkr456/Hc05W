package com.gxwtech.roundtrip2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gxwtech.roundtrip2.HistoryActivity.HistoryPageListActivity;
import com.gxwtech.roundtrip2.ServiceData.BolusWizardCarbProfile;
import com.gxwtech.roundtrip2.ServiceData.ISFProfile;
import com.gxwtech.roundtrip2.ServiceData.PumpModelResult;
import com.gxwtech.roundtrip2.ServiceData.ReadPumpClockResult;

import info.nightscout.androidaps.interfaces.PumpDescription;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceNotification;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history_old.record.ChangeBolusScrollStepSizePumpEvent;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.history.pump.PumpHistoryEntry;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.history.pump.PumpHistoryResult;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.BasalProfile;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.BasalProfileEntry;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.BatteryStatusDTO;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.ClockDTO;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.PumpSettingDTO;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.TempBasalPair;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.BatteryType;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicDeviceType;
import info.nightscout.androidaps.plugins.pump.medtronic.driver.MedtronicPumpStatus;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicConst;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.androidaps.utils.SP;

public class ShowAAPS2Activity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(ShowAAPS2Activity.class);

    Spinner spinner;
    String data1="";
    Button btnStart;

    Map<String, CommandAction> allCommands = new HashMap<>();
    private BroadcastReceiver mBroadcastReceiver;
    private TextView tvDuration, tvAmount, tvCommandStatusText, textViewComm;
    private EditText tfDuration, tfAmount;
    CommandAction selectedCommandAction = null;
    private PumpHistoryEntry lastEntry;
    BroadcastReceiver btReceiver;
    Context ctx;



    public ShowAAPS2Activity(String data1,Context ctx) {
        this.data1=data1;
        this.ctx=ctx;
        addCommandAction("Get Model", ImplementationStatus.Done, "RefreshData.PumpModel");

        addCommandAction("Set TBR", ImplementationStatus.Done, "RefreshData.SetTBR");
        addCommandAction("Cancel TBR", ImplementationStatus.Done, "RefreshData.CancelTBR");
        addCommandAction("Status - TBR", ImplementationStatus.Done, "RefreshData.GetTBR");

        addCommandAction("Get Basal Profile", ImplementationStatus.Done, "RefreshData.BasalProfile");
        addCommandAction("Set Bolus", ImplementationStatus.Done, "RefreshData.SetBolus");

        addCommandAction("Status - Remaining Insulin", ImplementationStatus.Done, "RefreshData.RemainingInsulin");
        addCommandAction("Status - Get Time", ImplementationStatus.Done, "RefreshData.GetTime");
        addCommandAction("Status - Settings", ImplementationStatus.Done, "RefreshData.GetSettings");
        addCommandAction("Status - Remaining Power", ImplementationStatus.Done, "RefreshData.RemainingPower");

        // addCommandAction("Status - Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetStatus"); // weird on
        // 512?

        // STATUS: has Bolus / is running / is beeing primed

        // WORK IN PROGRESS - waiting for something

        // LOW PRIORITY
        addCommandAction("Read History", ImplementationStatus.WorkInProgress, "RefreshData.GetHistory");
        addCommandAction("Read History 2", ImplementationStatus.WorkInProgress, "RefreshData.GetHistory2");
        // addCommandAction("Set Extended Bolus", ImplementationStatus.WorkInProgress, "RefreshData.SetExtendedBolus");
        // addCommandAction("Status - Ext. Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetBolus");
        // addCommandAction("Load TDD", ImplementationStatus.NotStarted, null); Not needed, we have good history

        // DONE

        // TODO
        addCommandAction("Set Basal Profile", ImplementationStatus.WorkInProgress, "RefreshData.SetBasalProfile");

        // NOT SUPPORTED
        // addCommandAction("Cancel Ext Bolus", ImplementationStatus.NotSupportedByDevice, null);
        // addCommandAction("Cancel Bolus", ImplementationStatus.NotSupportedByDevice, null);

    }
    public ShowAAPS2Activity() {
        this.data1="";
        addCommandAction("Get Model", ImplementationStatus.Done, "RefreshData.PumpModel");

        addCommandAction("Set TBR", ImplementationStatus.Done, "RefreshData.SetTBR");
        addCommandAction("Cancel TBR", ImplementationStatus.Done, "RefreshData.CancelTBR");
        addCommandAction("Status - TBR", ImplementationStatus.Done, "RefreshData.GetTBR");

        addCommandAction("Get Basal Profile", ImplementationStatus.Done, "RefreshData.BasalProfile");
        addCommandAction("Set Bolus", ImplementationStatus.Done, "RefreshData.SetBolus");

        addCommandAction("Status - Remaining Insulin", ImplementationStatus.Done, "RefreshData.RemainingInsulin");
        addCommandAction("Status - Get Time", ImplementationStatus.Done, "RefreshData.GetTime");
        addCommandAction("Status - Settings", ImplementationStatus.Done, "RefreshData.GetSettings");
        addCommandAction("Status - Remaining Power", ImplementationStatus.Done, "RefreshData.RemainingPower");

        // addCommandAction("Status - Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetStatus"); // weird on
        // 512?

        // STATUS: has Bolus / is running / is beeing primed

        // WORK IN PROGRESS - waiting for something

        // LOW PRIORITY
        addCommandAction("Read History", ImplementationStatus.WorkInProgress, "RefreshData.GetHistory");
        addCommandAction("Read History 2", ImplementationStatus.WorkInProgress, "RefreshData.GetHistory2");
        // addCommandAction("Set Extended Bolus", ImplementationStatus.WorkInProgress, "RefreshData.SetExtendedBolus");
        // addCommandAction("Status - Ext. Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetBolus");
        // addCommandAction("Load TDD", ImplementationStatus.NotStarted, null); Not needed, we have good history

        // DONE

        // TODO
        addCommandAction("Set Basal Profile", ImplementationStatus.WorkInProgress, "RefreshData.SetBasalProfile");

        // NOT SUPPORTED
        // addCommandAction("Cancel Ext Bolus", ImplementationStatus.NotSupportedByDevice, null);
        // addCommandAction("Cancel Bolus", ImplementationStatus.NotSupportedByDevice, null);

    }


    private void addCommandAction(String action, ImplementationStatus implementationStatus, String intent) {
        allCommands.put(action, new CommandAction(action, implementationStatus, intent));
    }
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    Intent bindIntent;
    private ProgressBar linearProgressBar;
    private ProgressBar spinnyProgressBar;
    Bundle storeForHistoryViewer;
    String note;
    boolean q=false;
    BroadcastReceiver apsAppConnected;
    Boolean openloopcheck;
    Boolean checkcon1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_aaps2);
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString("loop","false");
        String checkcon = appSharedPrefs.getString("CheckCon", "");
        prefsEditor.commit();
        openloopcheck=getIntent().getBooleanExtra("fromopenloop",false);
        checkcon1=openloopcheck;

        this.textViewComm = findViewById(R.id.textViewComm);

        this.tvDuration = findViewById(R.id.tvDuration);
        this.tvAmount = findViewById(R.id.tvAmount);

        this.tfAmount = findViewById(R.id.tfAmount);
        this.tfDuration = findViewById(R.id.tfDuration);

        this.btnStart = findViewById(R.id.btnStart);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
            Log.d("BT messagemainactivity:", "...Switching Bluetooth ON...");
            finish();
        }
        bindIntent = new Intent(this, RileyLinkMedtronicService.class);
        startService(bindIntent);
        if(openloopcheck==true) {

            setRileylinkStatusMessage("OK");
            setPumpStatusMessage("OK");
            linearProgressBar = (ProgressBar) findViewById(R.id.progressBarCommandActivity);
            spinnyProgressBar = (ProgressBar) findViewById(R.id.progressBarSpinny);
            spinnyProgressBar.setVisibility(View.INVISIBLE);
            showIdle();
            // Sets default Preferences
        }
        else {
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



            linearProgressBar = (ProgressBar) findViewById(R.id.progressBarCommandActivity);
            spinnyProgressBar = (ProgressBar) findViewById(R.id.progressBarSpinny);
        }

        this.btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // bolus, duration
                startAction2();
            }
        });

        tvCommandStatusText = (TextView)findViewById(R.id.tvCommandStatusText);
        spinner = (Spinner)findViewById(R.id.spinnerPumpCommands);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                commandSelected(itemAtPosition);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                commandSelected(null);
            }
        });
        MedtronicUtil.setPumpStatus(new MedtronicPumpStatus(new PumpDescription()));
        Intent bindIntent = new Intent(this, RileyLinkMedtronicService.class);
        startService(bindIntent);


        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                /*
                 * here we can listen for local broadcasts, then send ourselves
                 * a specific intent to deal with them, if we wish
                 */
                if (intent == null) {
                    LOG.error("onReceive: received null intent");
                } else {
                    String action = intent.getAction();
                    sendData(action);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        for (CommandAction commandAction : allCommands.values()) {

            if (commandAction.implementationStatus == ImplementationStatus.Done || //
                commandAction.implementationStatus == ImplementationStatus.WorkInProgress) {
                if (commandAction.intentString != null) {
                    intentFilter.addAction(commandAction.intentString);
                }
            }
        }

        intentFilter.addAction("RefreshData.ErrorCode");

        LocalBroadcastManager.getInstance(MainApp.instance().getApplicationContext()).registerReceiver(
            mBroadcastReceiver, intentFilter);
    }
    public Boolean connection(){
        return checkcon1;
    }

    private void startAction2() {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(ShowAAPS2Activity.this);
        a_builder.setMessage("Please Check the values!!!,are you sure to proceed")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAction();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }) ;
        AlertDialog alert = a_builder.create();
        alert.setTitle("Alert !!!");
        alert.show();
    }


    public void commandSelected(Object id) {

        if (id == null) {
            tvCommandStatusText.setText("Nothing");
            enableFields(false, false);
            this.btnStart.setEnabled(false);
        } else {

            this.selectedCommandAction = allCommands.get((String)id);
            tvCommandStatusText.setText(selectedCommandAction.implementationStatus.text);
            enableFields(isAmountEnabled(), isDurationEnabled());
            this.btnStart.setEnabled((selectedCommandAction.implementationStatus == ImplementationStatus.Done || //
                selectedCommandAction.implementationStatus == ImplementationStatus.WorkInProgress));
        }

    }
    public Boolean commandSelected1(String id) {
        Boolean conncheck;
        if (id.equals("")){
            //tvCommandStatusText.setText("Nothing");
            //enableFields(false, false);
            //this.btnStart.setEnabled(false);
            conncheck=true;
        } else {

            this.selectedCommandAction = allCommands.get(id);
           // tvCommandStatusText.setText(selectedCommandAction.implementationStatus.text);
//            enableFields(isAmountEnabled(), isDurationEnabled());
            if(selectedCommandAction.implementationStatus == ImplementationStatus.Done || //
                    selectedCommandAction.implementationStatus == ImplementationStatus.WorkInProgress) {
                conncheck = startAction1();
            }else{
                conncheck=true;

            }
        }
    return conncheck;
    }
    @Override
    public void onBackPressed()
    {
//        Intent ifl=new Intent(this,MainActivity.class);
//        startActivity(ifl);
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }


    private boolean isAmountEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals("Set TBR") || //
            action.equals("Set Bolus") || //
            action.equals("Set Basal Profile") || //
        action.equals("Set Extended Bolus") //
        );
    }


    private boolean isDurationEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals("Set TBR") || action.equals("Set Extended Bolus"));
    }


    private void enableFields(boolean amount, boolean duration) {

        tfDuration.setEnabled(duration);
        tvDuration.setEnabled(duration);
        if (!duration)
            tfDuration.setText("");

        tvAmount.setEnabled(amount);
        tfAmount.setEnabled(amount);

        if (!amount)
            tfAmount.setText("");

    }


    public void putOnDisplay(String text) {
        this.textViewComm.append(text + "\n");
    }

    public void closedloop(View view) {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString("loop","true");
        prefsEditor.commit();
        Intent closedloop=new Intent (this,MainActivity.class);
        closedloop.putExtra("fromopenloop",openloopcheck);
        startActivity(closedloop);
    }

    public enum ImplementationStatus {
        NotStarted("Not Started"), //
        WorkInProgress("Work In Progress"), //
        Done("Command Done"), //
        NotSupportedByDevice("Not supported by device"); //

        String text;


        ImplementationStatus(String text) {
            this.text = text;
        }
    }

    public class CommandAction {

        String action;
        ImplementationStatus implementationStatus;
        String intentString;


        public CommandAction(String action, //
                ImplementationStatus implementationStatus, //
                String intentString) {
            this.action = action;

            this.implementationStatus = implementationStatus;
            this.intentString = intentString;
        }

    }

    MedtronicCommunicationManager mcmInstance = null;


    private MedtronicCommunicationManager getCommunicationManager() {
        if (mcmInstance == null) {
            mcmInstance = MedtronicCommunicationManager.getInstance();
        }

        return mcmInstance;
    }

    Object data;
    String errorCode;



    public void sendData(String action ) {

        // FIXME
        switch (action) {
            case "RefreshData.PumpModel": {
                MedtronicDeviceType pumpModel = (MedtronicDeviceType)data;
                putOnDisplay("Model: " + pumpModel.name());
            }
                break;

            case "RefreshData.BasalProfile": {
                BasalProfile basalProfile = (BasalProfile)data;
                putOnDisplay("Basal Profile: " + basalProfile.getBasalProfileAsString());
            }
                break;

            case "RefreshData.RemainingInsulin": {
                Float remainingInsulin = (Float)data;
                putOnDisplay("Remaining Insulin: " + remainingInsulin);
            }
                break;

            case "RefreshData.RemainingPower": {
                BatteryStatusDTO status = (BatteryStatusDTO)data;
                putOnDisplay("Remaining Battery: " + status.batteryStatusType.name() + //
                    ", voltage=" + status.voltage + //
                    ", percent(Alkaline)=" + status.getCalculatedPercent(BatteryType.Alkaline) + //
                    ", percent(Lithium)=" + status.getCalculatedPercent(BatteryType.Lithium));
            }
                break;

            case "RefreshData.GetTime": {
                ClockDTO ldt = (ClockDTO)data;
                putOnDisplay("Pump Time: " + ldt.pumpTime.toString("dd.MM.yyyy HH:mm:ss"));
                putOnDisplay("Local Time: " + ldt.localDeviceTime.toString("dd.MM.yyyy HH:mm:ss"));
                //long diff = ldt.pumpTime.minus(ldt.localDeviceTime);
                //putOnDisplay("Difference: " + ldt.pumpTime.toString("dd.MM.yyyy HH:mm:ss"));
            }
                break;

            case "RefreshData.ErrorCode": {
                putOnDisplay("Error: " + errorCode);
            }
                break;

            case "RefreshData.SetTBR": {
                Boolean response = (Boolean)data;
                TempBasalPair tbr = getTBRSettings();

                putOnDisplay(String.format("TBR: Amount: %.3f, Duration: %s - %s", tbr.getInsulinRate(),
                    "" + tbr.getDurationMinutes(), (response ? "Was set." : "Was NOT set.")));
            }
                break;

            case "RefreshData.GetTBR": {
                TempBasalPair tbr = (TempBasalPair)data;

                putOnDisplay(String.format("TBR: Amount: %s, Duration: %s", "" + tbr.getInsulinRate(),
                    "" + tbr.getDurationMinutes()));
            }
                break;

            case "RefreshData.SetBolus": {
                Boolean response = true;
                Float amount;
                if (data1.equals("")) {
                    amount = getAmount();
                }else{
                    amount=Float.parseFloat(data1);
                }
               // putOnDisplay(String.format("Bolus: %.2f - %s", amount, (response ? "Was set." : "Was NOT set.")));
            }
                break;

            case "RefreshData.SetExtendedBolus": {
                Boolean response = (Boolean)data;

                TempBasalPair tbr = new TempBasalPair(0.5d, false, 30); // getTBRSettings();

                putOnDisplay(String.format("Extended Bolus: Amount: %.3f, Duration: %s - %s", tbr.getInsulinRate(), ""
                    + tbr.getDurationMinutes(), (response ? "Was set." : "Was NOT set.")));
            }
                break;

            case "RefreshData.CancelTBR": {
                Boolean response = (Boolean)data;

                putOnDisplay(String.format("TBR %s cancelled.", (response ? "was" : "was NOT")));
            }
                break;

            case "RefreshData.SetBasalProfile": {
                Boolean response = (Boolean)data;

                putOnDisplay(String.format("Basal profile %s set.", (response ? "was" : "was NOT")));
            }
                break;

            case "RefreshData.GetStatus": {
                // FIXME
                putOnDisplay("Status undefined ?");
            }
                break;

            case "RefreshData.GetHistory": {

                PumpHistoryResult result = (PumpHistoryResult)data;

                List<PumpHistoryEntry> validEntries = result.getValidEntries();

                if (validEntries != null) {

                    putOnDisplay("History Entries: (" + validEntries.size() + ")");
                    LOG.debug("History Entries: (" + validEntries.size() + ")");
                    for (PumpHistoryEntry entry : validEntries) {
                        putOnDisplay(entry.DT + "   " + entry.getEntryType().name());
                    }

                    if (validEntries.size() > 6) {
                        this.lastEntry = validEntries.get(5);
                    }
                } else {
                    putOnDisplay("No History entries.");
                }

            }
                break;

            case "RefreshData.GetHistory2": {

                PumpHistoryResult result = (PumpHistoryResult)data;

                List<PumpHistoryEntry> validEntries = result.getValidEntries();

                if (validEntries != null) {

                    putOnDisplay("History Entries 2: (" + validEntries.size() + ")");
                    LOG.debug("History Entries: (" + validEntries.size() + ")");
                    for (PumpHistoryEntry entry : validEntries) {
                        putOnDisplay(entry.DT + "   " + entry.getEntryType().name());
                    }
                }

            }
                break;

            case "RefreshData.GetSettings": {
                Map<String, PumpSettingDTO> settings = (Map<String, PumpSettingDTO>)data;

                putOnDisplay("Settings on pump: (" + settings.size() + "/" + settings.values().size() + ")");
                LOG.debug("Settings on front: " + settings);
                for (PumpSettingDTO entry : settings.values()) {
                    putOnDisplay(entry.key + " = " + entry.value);
                }
            }
                break;

            default:
                putOnDisplay("Unsupported action: " + action);
        }

        this.data = null;
        this.btnStart.setEnabled((selectedCommandAction.implementationStatus == ImplementationStatus.Done || //
            selectedCommandAction.implementationStatus == ImplementationStatus.WorkInProgress));
    }


    public void startAction() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                commandSelected(itemAtPosition);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                commandSelected(null);
            }
        });

        putOnDisplay("Start Action: " + selectedCommandAction.action);

        this.btnStart.setEnabled(false);



        // FIXME
        new Thread(new Runnable() {

            @Override
            public void run() {

                LOG.info("start Action: " + selectedCommandAction.action);

                Object returnData = null;

                switch (selectedCommandAction.intentString) {
                    case "RefreshData.PumpModel": {
                        returnData = getCommunicationManager().getPumpModel();
                    }
                    break;

                    case "RefreshData.BasalProfile": {
                        returnData = getCommunicationManager().getBasalProfile();
                    }
                    break;

                    case "RefreshData.RemainingInsulin": {
                        returnData = getCommunicationManager().getRemainingInsulin();
                    }
                    break;

                    case "RefreshData.GetTime": {
                        returnData = getCommunicationManager().getPumpTime();
                    }
                    break;

                    case "RefreshData.RemainingPower": {
                        returnData = getCommunicationManager().getRemainingBattery();
                    }
                    break;

                    case "RefreshData.SetTBR": {
                        TempBasalPair tbr = getTBRSettings();
                        if (tbr != null) {
                            returnData = getCommunicationManager().setTBR(tbr);
                        }
                    }
                    break;

                    // case "RefreshData.SetExtendedBolus": {
                    // // TempBasalPair tbr = getTBRSettings();
                    // // if (tbr != null) {
                    // // returnData = getCommunicationManager().setExtendedBolus(tbr.getInsulinRate(),
                    // // tbr.getDurationMinutes());
                    // // }
                    //
                    // //returnData = getCommunicationManager().setExtendedBolus(0.5d, 30);
                    //
                    // }
                    // break;

                    case "RefreshData.GetTBR": {
                        returnData = getCommunicationManager().getTemporaryBasal();
                    }
                    break;

                    case "RefreshData.GetStatus": {
                        //returnData = getCommunicationManager().getPumpState();
                    }
                    break;

                    case "RefreshData.GetHistory": {
                        LocalDateTime ldt = new LocalDateTime();
                        ldt = ldt.minus(Hours.hours(36));

                        returnData = getCommunicationManager().getPumpHistory(null, ldt);
                    }
                    break;

                    case "RefreshData.GetHistory2": {
                        returnData = getCommunicationManager().getPumpHistory(lastEntry, null);
                    }
                    break;

                    case "RefreshData.GetBolus": {
                        //returnData = getCommunicationManager().getBolusStatus();
                    }
                    break;

                    case "RefreshData.GetSettings": {
                        returnData = getCommunicationManager().getPumpSettings();
                    }
                    break;

                    case "RefreshData.SetBolus": {
                        Float amount;
                        String BolusList="";
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor prefsEditor = sp.edit();
                        if (data1.equals("")) {
                            amount = getAmount();
                        }else {
                            amount=Float.parseFloat(data1);
                        }
                        if (amount != null)

                            BolusList= sp.getString("MyBolusList", "");
                        BolusList=BolusList+amount.toString();
                        prefsEditor.putString("MyBolusList", BolusList);
                        prefsEditor.commit();
                        returnData = getCommunicationManager().setBolus(amount);
                    }
                    break;

                    case "RefreshData.CancelTBR": {
                        returnData = getCommunicationManager().cancelTBR();
                    }
                    break;

                    case "RefreshData.SetBasalProfile": {

                        Float amount;
                        if (data1.equals("")) {
                            amount = getAmount();
                        }else {
                            amount=Float.parseFloat(data1);
                        }
                        if (amount != null) {

                            BasalProfile profile = new BasalProfile();

                            int basalStrokes1 = MedtronicUtil.getBasalStrokesInt(amount);
                            int basalStrokes2 = MedtronicUtil.getBasalStrokesInt(amount * 2);

                            for (int i = 0; i < 24; i++) {
                                profile.addEntry(new BasalProfileEntry(i % 2 == 0 ? amount : amount * 2.0d, i, 0));
                            }

                            profile.generateRawDataFromEntries();

                            returnData = getCommunicationManager().setBasalProfile(profile);
                        }

                    }
                    break;

                    default:
                        LOG.warn("Action is not supported {}.", selectedCommandAction);

                }

                if (returnData == null || returnData.equals(false)) {
                    data = null;
                    checkcon1=false;

                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = returnData;
                    checkcon1=true;

                    errorCode = null;
                    RileyLinkUtil.sendBroadcastMessage(selectedCommandAction.intentString);
                }

            }
        }).start();
    if (checkcon1==false){
        setPumpStatusMessage("(notfound)");
        setRileylinkStatusMessage("(notfound)");
    }
    else{
        setPumpStatusMessage("OK");
        setRileylinkStatusMessage("OK");
    }


    }
    public Boolean startAction1() {


       // this.btnStart.setEnabled(false);

        // FIXME
        new Thread(new Runnable() {

            @Override
            public void run() {

                LOG.info("start Action: " + selectedCommandAction.action);

                Object returnData = null;

                switch (selectedCommandAction.intentString) {
                    case "RefreshData.PumpModel": {
                        returnData = getCommunicationManager().getPumpModel();
                    }
                    break;

                    case "RefreshData.BasalProfile": {
                        returnData = getCommunicationManager().getBasalProfile();
                    }
                    break;

                    case "RefreshData.RemainingInsulin": {
                        returnData = getCommunicationManager().getRemainingInsulin();
                    }
                    break;

                    case "RefreshData.GetTime": {
                        returnData = getCommunicationManager().getPumpTime();
                    }
                    break;

                    case "RefreshData.RemainingPower": {
                        returnData = getCommunicationManager().getRemainingBattery();
                    }
                    break;

                    case "RefreshData.SetTBR": {
                        TempBasalPair tbr = getTBRSettings();
                        if (tbr != null) {
                            returnData = getCommunicationManager().setTBR(tbr);
                        }
                    }
                    break;

                    // case "RefreshData.SetExtendedBolus": {
                    // // TempBasalPair tbr = getTBRSettings();
                    // // if (tbr != null) {
                    // // returnData = getCommunicationManager().setExtendedBolus(tbr.getInsulinRate(),
                    // // tbr.getDurationMinutes());
                    // // }
                    //
                    // //returnData = getCommunicationManager().setExtendedBolus(0.5d, 30);
                    //
                    // }
                    // break;

                    case "RefreshData.GetTBR": {
                        returnData = getCommunicationManager().getTemporaryBasal();
                    }
                    break;

                    case "RefreshData.GetStatus": {
                        //returnData = getCommunicationManager().getPumpState();
                    }
                    break;

                    case "RefreshData.GetHistory": {
                        LocalDateTime ldt = new LocalDateTime();
                        ldt = ldt.minus(Hours.hours(36));

                        returnData = getCommunicationManager().getPumpHistory(null, ldt);
                    }
                    break;

                    case "RefreshData.GetHistory2": {
                        returnData = getCommunicationManager().getPumpHistory(lastEntry, null);
                    }
                    break;

                    case "RefreshData.GetBolus": {
                        //returnData = getCommunicationManager().getBolusStatus();
                    }
                    break;

                    case "RefreshData.GetSettings": {
                        returnData = getCommunicationManager().getPumpSettings();
                    }
                    break;

                    case "RefreshData.SetBolus": {
                        Float amount;
//                        String BolusList="";
//                        SharedPreferences sp = PreferenceManager
//                                .getDefaultSharedPreferences(getApplicationContext());
//                        SharedPreferences.Editor prefsEditor = sp.edit();
                        if (data1.equals("")) {
                            amount = getAmount();
                        }else {
                            amount=Float.parseFloat(data1);
                        }
                        if (amount != null)

//                            BolusList= sp.getString("MyBolusList", "");
//                        BolusList=BolusList+amount.toString();
//                        prefsEditor.putString("MyBolusList", BolusList);
//                        prefsEditor.commit();
                        returnData = getCommunicationManager().setBolus(amount);
                    }
                    break;

                    case "RefreshData.CancelTBR": {
                        returnData = getCommunicationManager().cancelTBR();
                    }
                    break;

                    case "RefreshData.SetBasalProfile": {

                        Float amount;
                        if (data1.equals("")) {
                            amount = getAmount();
                        }else {
                            amount=Float.parseFloat(data1);
                        }
                        if (amount != null) {

                            BasalProfile profile = new BasalProfile();

                            int basalStrokes1 = MedtronicUtil.getBasalStrokesInt(amount);
                            int basalStrokes2 = MedtronicUtil.getBasalStrokesInt(amount * 2);

                            for (int i = 0; i < 24; i++) {
                                profile.addEntry(new BasalProfileEntry(i % 2 == 0 ? amount : amount * 2.0d, i, 0));
                            }

                            profile.generateRawDataFromEntries();

                            returnData = getCommunicationManager().setBasalProfile(profile);
                        }

                    }
                    break;

                    default:
                        LOG.warn("Action is not supported {}.", selectedCommandAction);

                }
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(ctx);

                if (returnData == null || returnData.equals(false) ) {
                    data = null;
                    checkcon1=false;
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    prefsEditor.putString("CheckCon", checkcon1.toString());
                    prefsEditor.commit();
                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = returnData;
                    checkcon1=true;
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    prefsEditor.putString("CheckCon", checkcon1.toString());
                    prefsEditor.commit();
                    errorCode = null;
                    RileyLinkUtil.sendBroadcastMessage(selectedCommandAction.intentString);
                }

            }
        }).start();
        return checkcon1;
    }


    private TempBasalPair getTBRSettings() {

        Float valAmount;
        if (data1.equals("")) {
            valAmount = getAmount();
        }else {
            valAmount=Float.parseFloat(data1);
        }

        TempBasalPair tbp = new TempBasalPair();

        if (valAmount != null) {
            tbp.setInsulinRate(valAmount);
        } else
            return null;

        Integer dur = getDuration();

        if (dur != null) {
            tbp.setDurationMinutes(dur);
            return tbp;
        }

        return null;
    }


    private Float getAmount() {
        CharSequence am = tfAmount.getText();
        String amount = am.toString().replaceAll(",", ".");

        try {
            return Float.parseFloat(amount);
        } catch (Exception ex) {
            putOnDisplay("Error parsing amount: " + ex.getMessage());
            return null;
        }

    }


    private Integer getDuration() {
        CharSequence am = tfDuration.getText();
        String duration = am.toString();

        int timeMin = 0;

        if (duration.contains(".") || duration.contains(",")) {
            putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
            return null;
        }

        if (duration.contains(":")) {
            String[] time = duration.split(":");

            if ((!time[1].equals("00")) && (!time[1].equals("30"))) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            try {
                timeMin += Integer.parseInt(time[0]) * 60;
            } catch (Exception ex) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            if (time[1].equals("30")) {
                timeMin += 30;
            }
        } else {
            try {
                timeMin += Integer.parseInt(duration) * 60;
            } catch (Exception ex) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }
        }

        return timeMin;
    }
    private static final String TAG = "ShowAAps2Activity";
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
                                showBusy("Connecting to Service", 50);
                                MainApp.getServiceClientConnection().setThisRileylink(RileylinkBLEAddress);
                            }
                            break;

                        case RT2Const.local.INTENT_NEW_disconnectRileyLink: {
                            showBusy("Connecting to Service", 50);
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
//                                        TextView pumpTimeTextView = (TextView)findViewById(R.id.textViewPumpClockTime);
//                                        pumpTimeTextView.setText(clockResult.getTimeString());
                                        Toast.makeText(getApplicationContext(), clockResult.getTimeString(), Toast.LENGTH_SHORT).show();
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
                                        com.gxwtech.roundtrip2.ServiceData.BasalProfile basalProfile = new com.gxwtech.roundtrip2.ServiceData.BasalProfile();
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
                            note = notification.getNotificationType();
                            switch (note) {
                                case RT2Const.IPC.MSG_BLE_RileyLinkReady:
                                    setRileylinkStatusMessage("OK");
                                    break;
                                case RT2Const.IPC.MSG_PUMP_pumpFound:
                                    setPumpStatusMessage("OK");
                                    q=true;
                                    openloopcheck=true;
                                    break;
                                case RT2Const.IPC.MSG_PUMP_pumpLost:
                                    setPumpStatusMessage("Lost");
                                    openloopcheck=false;
                                    q=false;
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
    private int mProgress = 0;
    private int mSpinnyProgress = 0;
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
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (btReceiver != null) {
            unregisterReceiver(btReceiver);
        }
    }
}
