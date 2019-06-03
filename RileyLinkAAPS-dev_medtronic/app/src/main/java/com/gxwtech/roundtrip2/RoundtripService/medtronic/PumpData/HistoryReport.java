package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.TempBasalEvent;

import java.util.ArrayList;

import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history_old.record.BolusWizardBolusEstimatePumpEvent;

/**
 * Created by geoff on 6/5/15.
 * <p>
 * This class is inteded to gather what information we've gleaned from the pump history
 * into one place, make it easier to move around.
 */
public class HistoryReport {
    public ArrayList<BolusWizardBolusEstimatePumpEvent> mBolusWizardEvents;
    public ArrayList<TempBasalEvent> mBasalEvents;


    public HistoryReport() {
        mBolusWizardEvents = new ArrayList<>();
        mBasalEvents = new ArrayList<>();
    }


    public void addBolusWizardEvent(BolusWizardBolusEstimatePumpEvent event) {
        mBolusWizardEvents.add(event);
    }


    public void addTempBasalEvent(TempBasalEvent event) {
        mBasalEvents.add(event);
    }
}
