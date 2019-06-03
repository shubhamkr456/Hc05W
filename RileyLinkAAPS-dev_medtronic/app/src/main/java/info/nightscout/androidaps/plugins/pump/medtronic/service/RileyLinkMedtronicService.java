package info.nightscout.androidaps.plugins.pump.medtronic.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.R;
import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.FetchPumpHistoryTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadBolusWizardCarbProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadISFProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadPumpClockTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.RetrieveHistoryPageTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.UpdatePumpStatusTask;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.PumpHistoryManager;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkBLE;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkService;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkServiceData;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceNotification;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceResult;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.ServiceTask;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.ServiceTaskExecutor;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.WakeAndTuneTask;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.Page;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicDeviceType;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.PumpDeviceState;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicConst;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.androidaps.utils.SP;

/**
 * RileyLinkMedtronicService is intended to stay running when the gui-app is closed.
 */
public class RileyLinkMedtronicService extends RileyLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkMedtronicService.class);

    private static RileyLinkMedtronicService instance;

    // saved settings
    // private String pumpIDString;
    // private byte[] pumpIDBytes;
    private static ServiceTask currentTask = null;
    public MedtronicCommunicationManager medtronicCommunicationManager;
    // cache of most recently received set of pump history pages. Probably shouldn't be here.
    ArrayList<Page> mHistoryPages;
    PumpHistoryManager pumpHistoryManager;


    // MedtronicPumpStatus pumpStatus = (MedtronicPumpStatus) MedtronicPumpPlugin.getPlugin().getPumpStatusData();

    public RileyLinkMedtronicService() {
        super(MainApp.instance().getApplicationContext());
        instance = this;
        LOG.debug("RileyLinkMedtronicService newly constructed");
        MedtronicUtil.setMedtronicService(this);
        // pumpStatus = (MedtronicPumpStatus) MedtronicPumpPlugin.getPlugin().getPumpStatusData();
    }


    public static RileyLinkMedtronicService getInstance() {
        return instance;
    }


    public static MedtronicCommunicationManager getCommunicationManager() {
        return instance.medtronicCommunicationManager;
    }


    public boolean handleDeviceSpecificBroadcasts(Intent intent) {
        String action = intent.getAction();

        LOG.error("handleDeviceSpecificBroadcasts NOT Handled.");

//        if (action.equals(RT2Const.IPC.MSG_PUMP_fetchHistory)) {
//            mHistoryPages = medtronicCommunicationManager.getAllHistoryPages();
//            final boolean savePages = true;
//            if (savePages) {
//                for (int i = 0; i < mHistoryPages.size(); i++) {
//                    String filename = "PumpHistoryPage-" + i;
//                    LOG.warn("Saving history page to file " + filename);
//                    FileOutputStream outputStream;
//                    try {
//                        outputStream = openFileOutput(filename, 0);
//                        byte[] rawData = mHistoryPages.get(i).getRawData();
//                        if (rawData != null) {
//                            outputStream.write(rawData);
//                        }
//                        outputStream.close();
//                    } catch (FileNotFoundException fnf) {
//                        fnf.printStackTrace();
//                    } catch (IOException ioe) {
//                        ioe.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//
//            Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC, 0, 0);
//            // Create a bundle with the data
//            Bundle bundle = new Bundle();
//            bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_history);
//            ArrayList<Bundle> packedPages = new ArrayList<>();
//            for (Page page : mHistoryPages) {
//                packedPages.add(page.pack());
//            }
//            bundle.putParcelableArrayList(RT2Const.IPC.MSG_PUMP_history_key, packedPages);
//
//            // save it to SQL.
//            pumpHistoryManager.clearDatabase();
//            pumpHistoryManager.initFromPages(bundle);
//            // write html page to documents folder
//            pumpHistoryManager.writeHtmlPage();
//
//            // Set payload
//            msg.setData(bundle);
//            RileyLinkUtil.getRileyLinkIPCConnection().sendMessage(msg, null/* broadcast */);
//            LOG.debug("sendMessage: sent Full history report");
//
//            return true;
//        } else if (RT2Const.IPC.MSG_PUMP_fetchSavedHistory.equals(action)) {
//            LOG.info("Fetching saved history");
//            FileInputStream inputStream;
//            ArrayList<Page> storedHistoryPages = new ArrayList<>();
//            for (int i = 0; i < 16; i++) {
//
//                String filename = "PumpHistoryPage-" + i;
//                try {
//                    inputStream = openFileInput(filename);
//                    byte[] buffer = new byte[1024];
//                    int numRead = inputStream.read(buffer, 0, 1024);
//                    if (numRead == 1024) {
//                        Page p = new Page();
//                        // p.parseFrom(buffer, PumpModel.MM522);
//                        // FIXME
//                        p.parseFrom(buffer, MedtronicDeviceType.Medtronic_522);
//                        storedHistoryPages.add(p);
//                    } else {
//                        LOG.error(filename + " error: short file");
//                    }
//                } catch (FileNotFoundException fnf) {
//                    LOG.error("Failed to open " + filename + " for reading.");
//                } catch (IOException e) {
//                    LOG.error("Failed to read " + filename);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            mHistoryPages = storedHistoryPages;
//            if (storedHistoryPages.isEmpty()) {
//                LOG.error("No stored history pages loaded");
//            } else {
//                Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC, 0, 0);
//                // Create a bundle with the data
//                Bundle bundle = new Bundle();
//                bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_history);
//                ArrayList<Bundle> packedPages = new ArrayList<>();
//                for (Page page : mHistoryPages) {
//                    packedPages.add(page.pack());
//                }
//                bundle.putParcelableArrayList(RT2Const.IPC.MSG_PUMP_history_key, packedPages);
//
//                // save it to SQL.
//                pumpHistoryManager.clearDatabase();
//                pumpHistoryManager.initFromPages(bundle);
//                // write html page to documents folder
//                pumpHistoryManager.writeHtmlPage();
//
//                // Set payload
//                msg.setData(bundle);
//                RileyLinkUtil.getRileyLinkIPCConnection().sendMessage(msg, null/* broadcast */);
//            }
//
//            return true;
//
//        }

        return false;
    }


    @Override
    public void registerDeviceSpecificBroadcasts(IntentFilter intentFilter) {
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_fetchHistory);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LOG.warn("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return RileyLinkUtil.getRileyLinkIPCConnection().doOnBind(intent);
    }


    @Override
    public RileyLinkEncodingType getEncoding() {
        return RileyLinkEncodingType.FourByteSixByteLocal;
    }


    /**
     * If you have customized RileyLinkServiceData you need to override this
     */
    public void initRileyLinkServiceData() {

        rileyLinkServiceData = new RileyLinkServiceData(RileyLinkTargetDevice.MedtronicPump);

        RileyLinkUtil.setRileyLinkServiceData(rileyLinkServiceData);
        RileyLinkUtil.setTargetDevice(RileyLinkTargetDevice.MedtronicPump);

        setPumpIDString(SP.getString(MedtronicConst.Prefs.PumpSerial, "000000"));

        // get most recently used RileyLink address
        rileyLinkServiceData.rileylinkAddress = SP.getString(RileyLinkConst.Prefs.RileyLinkAddress, "");

        rileyLinkBLE = new RileyLinkBLE(this.context); // or this
        rfspy = new RFSpy(rileyLinkBLE);
        rfspy.startReader();

        RileyLinkUtil.setRileyLinkBLE(rileyLinkBLE);

        // init rileyLinkCommunicationManager
        medtronicCommunicationManager = new MedtronicCommunicationManager(context, rfspy);

        // FIXME remove
        pumpHistoryManager = new PumpHistoryManager(this.context);
    }


    public void resetRileyLinkDevice() {
        rfspy.resetRileyLinkDevice();
    }


    @Override
    public RileyLinkCommunicationManager getDeviceCommunicationManager() {
        return this.medtronicCommunicationManager;
    }


    public void setPumpIDString(String pumpID) {
        if (pumpID.length() != 6) {
            LOG.error("setPumpIDString: invalid pump id string: " + pumpID);
            return;
        }

        byte[] pumpIDBytes = ByteUtil.fromHexString(pumpID);

        if (pumpIDBytes == null) {
            LOG.error("Invalid pump ID? - PumpID is null.");

            rileyLinkServiceData.setPumpID("000000", new byte[] { 0, 0, 0 });

        } else if (pumpIDBytes.length != 3) {
            LOG.error("Invalid pump ID? " + ByteUtil.shortHexString(pumpIDBytes));

            rileyLinkServiceData.setPumpID("000000", new byte[] { 0, 0, 0 });

        } else if (pumpID.equals("000000")) {
            LOG.error("Using pump ID " + pumpID);

            rileyLinkServiceData.setPumpID(pumpID, new byte[] { 0, 0, 0 });

        } else {
            LOG.info("Using pump ID " + pumpID);

            rileyLinkServiceData.setPumpID(pumpID, pumpIDBytes);

            return;
        }

        MedtronicUtil.setPumpDeviceState(PumpDeviceState.InvalidConfiguration);

        // LOG.info("setPumpIDString: saved pumpID " + idString);
    }


    @Override
    public String getDeviceSpecificBroadcastsIdentifierPrefix() {

        return RT2Const.IPC.MSG_PUMP_Prefix;
    }


    // TODO will be probably removed in AAPS
    public boolean handleIncomingServiceTransport(Intent intent) {

        Bundle bundle = intent.getBundleExtra(RT2Const.IPC.bundleKey);

        ServiceTransport serviceTransport = new ServiceTransport(bundle);

        if (serviceTransport.getServiceCommand().isPumpCommand()) {
            switch (serviceTransport.getOriginalCommandName()) {
                case "ReadPumpClock": {
                    ServiceTaskExecutor.startTask(new ReadPumpClockTask(serviceTransport));
                    return true;
                }

                case "FetchPumpHistory": {
                    ServiceTaskExecutor.startTask(new FetchPumpHistoryTask(serviceTransport));
                    return true;
                }

                case "RetrieveHistoryPage": {
                    ServiceTask task = new RetrieveHistoryPageTask(serviceTransport);
                    ServiceTaskExecutor.startTask(task);
                    return true;
                }

                case "ReadISFProfile": {
                    ServiceTaskExecutor.startTask(new ReadISFProfileTask(serviceTransport));
                    /*
                     * ISFTable table = pumpCommunicationManager.getPumpISFProfile();
                     * ServiceResult result = new ServiceResult();
                     * if (table.isValid()) {
                     * // convert from ISFTable to ISFProfile
                     * Bundle map = result.getMap();
                     * map.putIntArray("times", table.getTimes());
                     * map.putFloatArray("rates", table.getRates());
                     * map.putString("ValidDate", TimeFormat.standardFormatter().print(table.getValidDate()));
                     * result.setMap(map);
                     * result.setResultOK();
                     * }
                     * sendServiceTransportResponse(serviceTransport,result);
                     */
                    return true;
                }

                case "ReadBolusWizardCarbProfile": {
                    ServiceTaskExecutor.startTask(new ReadBolusWizardCarbProfileTask());
                    return true;
                }

                case "UpdatePumpStatus": {
                    ServiceTaskExecutor.startTask(new UpdatePumpStatusTask());
                    return true;
                }

                case "WakeAndTune": {
                    ServiceTaskExecutor.startTask(new WakeAndTuneTask());
                    return true;
                }

                default: {
                    LOG.error("Failed to handle pump command: " + serviceTransport.getOriginalCommandName());
                    return false;
                }
            }
        } else {
            switch (serviceTransport.getOriginalCommandName()) {
                case "SetPumpID": {
                    // This one is a command to RileyLinkMedtronicService, not to the MedtronicCommunicationManager
                    String pumpID = serviceTransport.getServiceCommand().getMap().getString("pumpID", "");
                    ServiceResult result = new ServiceResult();
                    if ((pumpID != null) && (pumpID.length() == 6)) {
                        setPumpIDString(pumpID);
                        result.setResultOK();
                    } else {
                        LOG.error("handleIncomingServiceTransport: SetPumpID bundle missing 'pumpID' value");
                        result.setResultError(-1, "Invalid parameter (missing pumpID)");
                    }
                    sendServiceTransportResponse(serviceTransport, result);
                    return true;
                }

                case "UseThisRileylink": {
                    // If we are not connected, connect using the given address.
                    // If we are connected and the addresses differ, disconnect, connect to new.
                    // If we are connected and the addresses are the same, ignore.
                    String deviceAddress = serviceTransport.getServiceCommand().getMap().getString("rlAddress", "");
                    if ("".equals(deviceAddress)) {
                        LOG.error("handleIPCMessage: null RL address passed, disconnecting RL if it was connected.");
                        disconnectRileyLink();
                    } else {
                        reconfigureRileyLink(deviceAddress);
                    }
                    return true;
                }

                case "WakeAndTune": {
                    ServiceTaskExecutor.startTask(new WakeAndTuneTask());
                    return true;
                }

                default: {
                    LOG.error("handleIncomingServiceTransport: Failed to handle service command '"
                        + serviceTransport.getOriginalCommandName() + "'");
                    return false;
                }

            }
        }
    }


    public void announceProgress(int progressPercent) {
        if (currentTask != null) {
            ServiceNotification note = new ServiceNotification(RT2Const.IPC.MSG_note_TaskProgress);
            note.getMap().putInt("progress", progressPercent);
            note.getMap().putString("task", currentTask.getServiceTransport().getOriginalCommandName());
            Integer senderHashcode = currentTask.getServiceTransport().getSenderHashcode();
            RileyLinkUtil.getRileyLinkIPCConnection().sendNotification(note, senderHashcode);
        } else {
            LOG.error("announceProgress: No current task");
        }
    }


    public void saveHistoryPage(int pagenumber, Page page) {
        if ((page == null) || (page.getRawData() == null)) {
            return;
        }
        String filename = "history-" + pagenumber;
        FileOutputStream os;
        try {
            os = openFileOutput(filename, Context.MODE_PRIVATE);
            os.write(page.getRawData());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
