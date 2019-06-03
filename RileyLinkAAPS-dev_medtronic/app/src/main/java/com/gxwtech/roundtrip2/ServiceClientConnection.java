package com.gxwtech.roundtrip2;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

import com.gxwtech.roundtrip2.ServiceData.ServiceClientActions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceCommand;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by Tim on 27/06/2016.
 * Object that interfaces with the RT2 Service and Client Actions
 */
public class ServiceClientConnection {

    private static String TAG = "ServiceClientConnection";
    private static final Logger LOG = LoggerFactory.getLogger(ServiceClientConnection.class);


    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private Context context = MainApp.instance();


    public ServiceClientConnection() {
        roundtripServiceClientConnection = new RoundtripServiceClientConnection(context);

        //Connect to the RT service
        doBindService();
    }


    /*
    *
    *  Functions to work with the RT2 Service
    *
    */
    private void doBindService() {
        context.bindService(new Intent(context, RileyLinkMedtronicService.class), roundtripServiceClientConnection.getServiceConnection(), Context.BIND_AUTO_CREATE);
        Log.d(TAG, "doBindService: binding.");
        LOG.debug("doBindService Logger: binding.");
    }


    private void doUnbindService() {
        ServiceConnection conn = roundtripServiceClientConnection.getServiceConnection();
        roundtripServiceClientConnection.unbind();
        context.unbindService(conn);
        Log.d(TAG, "doUnbindService: unbinding.");
    }

    // send one-liner message to RileyLinkMedtronicService
    //private void sendIPCMessage(String ipcMsgType) {
    // Create a bundle with the data
    //    Bundle bundle = new Bundle();
    //    bundle.putString(RT2Const.IPC.messageKey, ipcMsgType);
    //    if (sendMessage(bundle)) {
    //        Log.d(TAG,"sendIPCMessage: sent "+ipcMsgType);
    //    } else {
    //        Log.e(TAG,"sendIPCMessage: send failed");
    //    }
    //}

    //private boolean sendMessage(Bundle bundle) {
    //return roundtripServiceClientConnection.sendMessage(bundle);
    //}

    /*
    *
    *  functions the client can call
    *
     */
    //public void sendBLEaccessGranted() { sendIPCMessage(RT2Const.IPC.MSG_BLE_accessGranted); }

    //public void sendBLEaccessDenied() { sendIPCMessage(RT2Const.IPC.MSG_BLE_accessDenied); }


    public void setThisRileylink(String address) {
        //Bundle bundle = new Bundle();
        //bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_BLE_useThisDevice);
        //bundle.putString(RT2Const.IPC.MSG_BLE_useThisDevice_addressKey,address);
        //sendMessage(bundle);
        ServiceCommand command = ServiceClientActions.makeUseThisRileylinkCommand(address);
        roundtripServiceClientConnection.sendServiceCommand(command);
        Log.d(TAG, "sendIPCMessage: (use this address) " + address);
    }


    public void sendPUMP_useThisDevice(String pumpIDString) {
        //Bundle bundle = new Bundle();
        //bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_useThisAddress);
        //bundle.putString(RT2Const.IPC.MSG_PUMP_useThisAddress_pumpIDKey,pumpIDString);
        //sendMessage(bundle);
        ServiceCommand command = ServiceClientActions.makeSetPumpIDCommand(pumpIDString);
        roundtripServiceClientConnection.sendServiceCommand(command);
        Log.d(TAG, "sendPUMP_useThisDevice: " + pumpIDString);
    }


    public void doTunePump() {
        ServiceCommand command = ServiceClientActions.makeTunePumpCommand();
        roundtripServiceClientConnection.sendServiceCommand(command);
    }


    public void getHistory() {
        //sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchHistory);
    }


    public void getSavedHistory() {
        //sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
    }


    public void setTempBasal(double amountUnitsPerHour, int durationMinutes) {
        ServiceCommand command = ServiceClientActions.makeSetTempBasalCommand(amountUnitsPerHour, durationMinutes);
        roundtripServiceClientConnection.sendServiceCommand(command);
    }


    public void readPumpClock() {
        ServiceCommand command = ServiceClientActions.makeReadPumpClockCommand();
        roundtripServiceClientConnection.sendServiceCommand(command);
    }


    public void readISFProfile() {
        ServiceCommand getISFProfileCommand = ServiceClientActions.makeReadISFProfileCommand();
        roundtripServiceClientConnection.sendServiceCommand(getISFProfileCommand);
    }


    public void updateAllStatus() {
        ServiceCommand command = ServiceClientActions.makeUpdateAllStatusCommand();
        roundtripServiceClientConnection.sendServiceCommand(command);
    }


    public void doFetchPumpHistory() {
        ServiceCommand retrievePageCommand = ServiceClientActions.makeFetchPumpHistoryCommand();
        roundtripServiceClientConnection.sendServiceCommand(retrievePageCommand);
    }


    public void doFetchSavedHistory() {
        // Does not (at the moment) fetch saved history :(
        ServiceCommand cmd = ServiceClientActions.makeFetchPumpHistoryCommand();
        roundtripServiceClientConnection.sendServiceCommand(cmd);
    }

}
