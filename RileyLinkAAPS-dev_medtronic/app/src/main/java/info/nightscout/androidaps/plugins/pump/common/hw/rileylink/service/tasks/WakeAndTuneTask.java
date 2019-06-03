package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/16/16.
 */
public class WakeAndTuneTask extends PumpTask {

    private static final String TAG = "WakeAndTuneTask";


    public WakeAndTuneTask() {
    }


    public WakeAndTuneTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {
        RileyLinkMedtronicService.getInstance().doTuneUpDevice();
    }

}
