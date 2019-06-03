package info.nightscout.androidaps.plugins.pump.common.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gxwtech.roundtrip2.R;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.GattAttributes;
import info.nightscout.androidaps.plugins.pump.common.utils.LocationHelper;
import info.nightscout.androidaps.plugins.pump.medtronic.driver.MedtronicPumpStatus;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.androidaps.utils.SP;

// IMPORTANT: This activity needs to be called from RileyLinkSelectPreference (see pref_medtronic.xml as example)
public class RileyLinkBLEScanActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkBLEScanActivity.class);

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 30241; // arbitrary.
    private static final int REQUEST_ENABLE_BT = 30242; // arbitrary

    private static String TAG = "RileyLinkBLEScanActivity";

    // Stops scanning after 30 seconds.
    private static final long SCAN_PERIOD = 30000;
    public boolean mScanning;
    public Snackbar snackbar;
    public ScanSettings settings;
    public List<ScanFilter> filters;
    public ListView listBTScan;
    public Toolbar toolbarBTScan;
    public Context mContext = this;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rileylink_scan_activity);

        // Initializes Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listBTScan = (ListView)findViewById(R.id.rileylink_listBTScan);
        listBTScan.setAdapter(mLeDeviceListAdapter);
        listBTScan.setOnItemClickListener((parent, view, position, id) -> {

            // stop scanning if still active
            if (mScanning) {
                mScanning = false;
                mLEScanner.stopScan(mScanCallback2);
            }

            TextView textview = (TextView)view.findViewById(R.id.rileylink_device_address);
            String bleAddress = textview.getText().toString();

            SP.putString(RileyLinkConst.Prefs.RileyLinkAddress, bleAddress);

            RileyLinkUtil.getRileyLinkSelectPreference().setSummary(bleAddress);

            MedtronicPumpStatus pumpStatus = MedtronicUtil.getPumpStatus();
            pumpStatus.verifyConfiguration(); // force reloading of address

            // MainApp.bus().post(new EventMedtronicPumpConfigurationChanged());

            // RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.Intents.RileyLinkNewAddressSet);

            finish();
        });

        toolbarBTScan = (Toolbar)findViewById(R.id.rileylink_toolbarBTScan);
        toolbarBTScan.setTitle(R.string.rileylink_scanner_title);
        setSupportActionBar(toolbarBTScan);

        // TODO remove snackbar, stop needs to be on same button as start
        snackbar = Snackbar.make(findViewById(R.id.RileyLinkScan), "Scanning...", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("STOP", new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                scanLeDevice(false);
            }
        });

        startScanBLE();
    }


    // @Override
    // protected void onPause() {
    // super.onPause();
    // scanLeDevice(false);
    // mLeDeviceListAdapter.clear();
    // mLeDeviceListAdapter.notifyDataSetChanged();
    // }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rileylink_ble_scan, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rileylink_miScan:
                scanLeDevice(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void startScanBLE() {
        // https://developer.android.com/training/permissions/requesting.html
        // http://developer.radiusnetworks.com/2015/09/29/is-your-beacon-app-ready-for-android-6.html
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "R.string.ble_not_supported", Toast.LENGTH_SHORT).show();
        } else {
            // Use this check to determine whether BLE is supported on the device. Then
            // you can selectively disable BLE-related features.
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // your code that requires permission
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_COARSE_LOCATION);
            }

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "R.string.ble_not_enabled", Toast.LENGTH_SHORT).show();
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Will request that GPS be enabled for devices running Marshmallow or newer.
                    if (!LocationHelper.isLocationEnabled(this)) {
                        LocationHelper.requestLocationForBluetooth(this);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters = Arrays.asList(new ScanFilter.Builder().setServiceUuid(
                    ParcelUuid.fromString(GattAttributes.SERVICE_RADIO)).build());

                scanLeDevice(true);
            }
        }

        RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.Intents.RileyLinkDisconnect);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // User allowed Bluetooth to turn on
            } else if (resultCode == RESULT_CANCELED) {
                // Error, or user said "NO"
                finish();
            }
        }
    }

    private ScanCallback mScanCallback2 = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, final ScanResult scanRecord) {

            // final BleAdvertisedData badata = RileyLinkUtil.parseAdertisedData(scanRecord);

            Log.d(TAG, scanRecord.toString());

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (addDevice(scanRecord))
                        mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }


        @Override
        public void onBatchScanResults(final List<ScanResult> results) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    boolean added = false;

                    for (ScanResult result : results) {

                        if (addDevice(result))
                            added = true;
                    }

                    if (added)
                        mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }


        private boolean addDevice(ScanResult result) {

            BluetoothDevice device = result.getDevice();

            // BluetoothClass bluetoothClass = device.getBluetoothClass();
            //
            // StringBuilder sb = new StringBuilder("Class: ");
            // sb.append("MajorClass: " + bluetoothClass.getMajorDeviceClass());
            // sb.append("DeviceClass: " + bluetoothClass.getDeviceClass());
            // Log.d(TAG, sb.toString());
            // FIXME remove most of code
            List<ParcelUuid> serviceUuids = result.getScanRecord().getServiceUuids();

            if (serviceUuids == null || serviceUuids.size() == 0) {
                Log.v(TAG, "Device " + device.getAddress() + " has no serviceUuids (Not RileyLink).");
            } else if (serviceUuids.size() > 1) {
                Log.v(TAG, "Device " + device.getAddress() + " has too many serviceUuids (Not RileyLink).");
            } else {

                String uuid = serviceUuids.get(0).getUuid().toString().toLowerCase();

                if (uuid.equals(GattAttributes.SERVICE_RADIO)) {
                    Log.i(TAG, "Found RileyLink with address: " + device.getAddress());
                    mLeDeviceListAdapter.addDevice(result);
                    return true;
                } else {
                    Log.v(TAG, "Device " + device.getAddress() + " has incorrect uuid (Not RileyLink).");
                }
            }

            return false;
        }


        private String getDeviceDebug(BluetoothDevice device) {

            return "BluetoothDevice [name=" + device.getName() + ", address=" + device.getAddress() + //
                ", type=" + device.getType(); // + ", alias=" + device.getAlias();
        }

        // @Override
        // public void onScanFailed(int errorCode) {
        //
        // Log.e("Scan Failed", "Error Code: " + errorCode);
        // Toast.makeText(mContext, "Scan Failed " + errorCode, Toast.LENGTH_LONG).show();
        // }
    };


    private void scanLeDevice(final boolean enable) {

        // FIXME

        if (enable) {

            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mScanning = false;
                    mLEScanner.stopScan(mScanCallback2);
                    LOG.debug("scanLeDevice: Scanning Stop");
                    // Toast.makeText(mContext, "Scanning finished", Toast.LENGTH_SHORT).show();
                    snackbar.dismiss();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mLEScanner.startScan(filters, settings, mScanCallback2);
            LOG.debug("scanLeDevice: Scanning Start");
            // Toast.makeText(this, "Scanning", Toast.LENGTH_SHORT).show();
            snackbar.show();
        } else {
            mScanning = false;
            mLEScanner.stopScan(mScanCallback2);

            LOG.debug("scanLeDevice: Scanning Stop");
            // Toast.makeText(this, "Scanning finished", Toast.LENGTH_SHORT).show();
            snackbar.dismiss();

        }
    }

    private class LeDeviceListAdapter extends BaseAdapter {

        private ArrayList<BluetoothDevice> mLeDevices;
        private Map<BluetoothDevice, Integer> rileyLinkDevices;
        private LayoutInflater mInflator;
        String currentlySelectedAddress;


        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            rileyLinkDevices = new HashMap<>();
            mInflator = RileyLinkBLEScanActivity.this.getLayoutInflater();
            currentlySelectedAddress = SP.getString(RileyLinkConst.Prefs.RileyLinkAddress, "");
        }


        public void addDevice(ScanResult result) {

            if (!mLeDevices.contains(result.getDevice())) {
                mLeDevices.add(result.getDevice());
            }
            rileyLinkDevices.put(result.getDevice(), result.getRssi());
            notifyDataSetChanged();
        }


        public void clear() {
            mLeDevices.clear();
            rileyLinkDevices.clear();
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return mLeDevices.size();
        }


        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }


        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.rileylink_scan_item, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView)view.findViewById(R.id.rileylink_device_address);
                viewHolder.deviceName = (TextView)view.findViewById(R.id.rileylink_device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            String deviceName = device.getName();

            if (StringUtils.isBlank(deviceName)) {
                deviceName = "RileyLink";
            }

            deviceName += " [" + rileyLinkDevices.get(device).intValue() + "]";

            if (currentlySelectedAddress.equals(device.getAddress())) {
                // viewHolder.deviceName.setTextColor(getColor(R.color.secondary_text_light));
                // viewHolder.deviceAddress.setTextColor(getColor(R.color.secondary_text_light));
                deviceName += " (" + getResources().getString(R.string.rileylink_scanner_selected_device) + ")";
            }

            viewHolder.deviceName.setText(deviceName);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }

    }

    static class ViewHolder {

        TextView deviceName;
        TextView deviceAddress;
    }

}
