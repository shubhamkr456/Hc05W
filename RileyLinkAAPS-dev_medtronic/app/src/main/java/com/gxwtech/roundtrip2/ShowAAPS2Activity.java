package com.gxwtech.roundtrip2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.MedtronicCommunicationManager;
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
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;

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


    public ShowAAPS2Activity(String data1) {
        this.data1=data1;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_aaps2);

        this.textViewComm = findViewById(R.id.textViewComm);

        this.tvDuration = findViewById(R.id.tvDuration);
        this.tvAmount = findViewById(R.id.tvAmount);

        this.tfAmount = findViewById(R.id.tfAmount);
        this.tfDuration = findViewById(R.id.tfDuration);

        this.btnStart = findViewById(R.id.btnStart);
        this.btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // bolus, duration
                startAction();
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
                Boolean response = (Boolean)data;
                Float amount;
                if (data1.equals("")) {
                    amount = getAmount();
                }else{
                    amount=Float.parseFloat(data1);
                }
                putOnDisplay(String.format("Bolus: %.2f - %s", amount, (response ? "Was set." : "Was NOT set.")));
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
                        if (data1.equals("")) {
                            amount = getAmount();
                        }else {
                            amount=Float.parseFloat(data1);
                        }
                        if (amount != null)
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

                if (returnData == null) {
                    data = null;
                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = returnData;
                    errorCode = null;
                    RileyLinkUtil.sendBroadcastMessage(selectedCommandAction.intentString);
                }

            }
        }).start();

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

}
