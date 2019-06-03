package info.nightscout.androidaps.plugins.pump.medtronic.comm.history.pump;

import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.R;

import java.util.ArrayList;
import java.util.List;



/**
 * This file was taken from GGC - GNU Gluco Control and modified/extended for AAPS.
 *
 * Author: Andy {andy.rozman@gmail.com}
 */

public enum PumpHistoryEntryGroup {

    All(R.string.medtronic_history_group_all), //
    Bolus(R.string.danar_history_bolus), //
    Basal(R.string.medtronic_history_group_basal), //
    Prime(R.string.danar_history_prime), //
    Configuration(R.string.medtronic_history_group_configuration), //
    Alarm(R.string.danar_history_alarm), //
    Glucose(R.string.danar_history_glucose), //
    Notification(R.string.medtronic_history_group_notification), //
    Statistic(R.string.medtronic_history_group_statistic),
    Unknown(R.string.medtronic_history_group_unknown), //
    ;

    private int resourceId;
    private String translated;

    public static boolean doNotTranslate = false;

    private static List<PumpHistoryEntryGroup> list;

    static {
        list = new ArrayList<>();

        for (PumpHistoryEntryGroup pumpHistoryEntryGroup : values()) {
            if (doNotTranslate) {
                pumpHistoryEntryGroup.translated = MainApp.gs(pumpHistoryEntryGroup.resourceId);
            }
            list.add(pumpHistoryEntryGroup);
        }
    }


    PumpHistoryEntryGroup(int resourceId) {
        this.resourceId = resourceId;
        // this.translated = MainApp.gs(resourceId);
    }


    public static List<PumpHistoryEntryGroup> getList() {
        return list;
    }


    public int getResourceId() {
        return resourceId;
    }


    public String getTranslated() {
        return translated;
    }


    public String toString() {
        return this.translated;
    }
}
