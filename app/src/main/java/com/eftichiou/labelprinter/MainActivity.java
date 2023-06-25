package com.eftichiou.labelprinter;

import java.util.ArrayList;
import java.util.Collections;
//import java.util.List;
import java.util.Set;

import android.support.annotation.NonNull;
import com.bixolon.commonlib.BXLCommonConst;
import com.bixolon.commonlib.common.BXLFileHelper;
import com.bixolon.commonlib.log.LogService;
import com.bixolon.labelprinter.BixolonLabelPrinter;
import com.bixolon.labelprinter.R;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint({"HandlerLeak", "NewApi"})
public class MainActivity extends ListActivity {
    private static final String[] FUNCTIONS = {"Print Label", "Network Settings"};
    private String mConnectedDeviceName = null;
    private final int REQUEST_PERMISSION = 0;
    private ListView mListView;
    private boolean mIsConnected;
    static BixolonLabelPrinter mBixolonLabelPrinter;
    static int count = 1;
    //    public ArrayAdapter<String> adapter = null;
//    public ArrayList<BluetoothDevice> m_LeDevices;
    final private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.count = 1;
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, FUNCTIONS);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
//		mListView.setEnabled(false);
        mBixolonLabelPrinter = new BixolonLabelPrinter(this, mHandler, Looper.getMainLooper());
        LogService.InitDebugLog(true, true, BXLCommonConst._LOG_LEVEL_HIGH);
        final int ANDROID_NOUGAT = 24;
        if (Build.VERSION.SDK_INT >= ANDROID_NOUGAT) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        checkVerify();
        Thread.setDefaultUncaughtExceptionHandler(new SampleUncaughtExceptionHandler());
        String privatePath = getFilesDir().getPath();
//        String fileName = "20200121_ALT20201-10.pdf";
        if (!BXLFileHelper.ExistDirectory(privatePath)) {
            BXLFileHelper.CreateDirectory(privatePath);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mBixolonLabelPrinter.disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mIsConnected) {
            menu.getItem(0).setEnabled(true);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                try {
                    mBixolonLabelPrinter.findBluetoothPrinters();
                } catch (Exception exception) {
                    Toast.makeText(getApplicationContext(), "Please Switch On Bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.item6:
                mBixolonLabelPrinter.disconnect();
                break;
        }
        return false;
    }
//    private void setScanCallback() throws NoSuchMethodException {
//        ScanCallback mScanCallback = new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                super.onScanResult(callbackType, result);
//                Log.i("callbackType", String.valueOf(callbackType));
//                Log.i("result", result.toString());
//                BluetoothDevice btDevice = result.getDevice();
//                if (!m_LeDevices.contains(btDevice)) {
//                    m_LeDevices.add(btDevice);
//                    adapter.add(btDevice.getName() + "\n" + btDevice.getAddress());
//                } else {
//                    return;
//                }
//            }
//
//            @Override
//            public void onBatchScanResults(List<ScanResult> results) {
//                super.onBatchScanResults(results);
//                for (ScanResult sr : results) {
//                    Log.i("ScanResult - Results", sr.toString());
//                }
//            }
//
//            @Override
//            public void onScanFailed(int errorCode) {
//                super.onScanFailed(errorCode);
//                Log.e("Scan Failed", "Error Code: " + errorCode);
//            }
//        };
//    }
    Intent intent;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                intent = new Intent(MainActivity.this, PrintLabel.class);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(MainActivity.this, NetworkSettings.class);
                startActivity(intent);
                break;
        }
    }

    private void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subtitle) {
        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setSubtitle(subtitle);
    }

    @SuppressLint("HandlerLeak")
    private void dispatchMessage(Message msg) {
        switch (msg.arg1) {
            case BixolonLabelPrinter.PROCESS_GET_STATUS:
                byte[] report = (byte[]) msg.obj;
                StringBuilder buffer = new StringBuilder();
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_PAPER_EMPTY) == BixolonLabelPrinter.STATUS_1ST_BYTE_PAPER_EMPTY) {
                    buffer.append("Paper Empty.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) == BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) {
                    buffer.append("Cover open.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) == BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) {
                    buffer.append("Cutter jammed.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) == BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) {
                    buffer.append("TPH(thermal head) overheat.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) == BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) {
                    buffer.append("Gap detection error. (Auto-sensing failure)\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_RIBBON_END_ERROR) == BixolonLabelPrinter.STATUS_1ST_BYTE_RIBBON_END_ERROR) {
                    buffer.append("Ribbon end error.\n");
                }
                if (report.length == 2) {
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_BUILDING_IN_IMAGE_BUFFER) == BixolonLabelPrinter.STATUS_2ND_BYTE_BUILDING_IN_IMAGE_BUFFER) {
                        buffer.append("On building label to be printed in image buffer.\n");
                    }
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_PRINTING_IN_IMAGE_BUFFER) == BixolonLabelPrinter.STATUS_2ND_BYTE_PRINTING_IN_IMAGE_BUFFER) {
                        buffer.append("On printing label in image buffer.\n");
                    }
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_PAUSED_IN_PEELER_UNIT) == BixolonLabelPrinter.STATUS_2ND_BYTE_PAUSED_IN_PEELER_UNIT) {
                        buffer.append("Issued label is paused in peeler unit.\n");
                    }
                }
                if (buffer.length() == 0) {
                    buffer.append("No error");
                }
                Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_SHORT).show();
                break;
            case BixolonLabelPrinter.PROCESS_GET_INFORMATION_MODEL_NAME:
            case BixolonLabelPrinter.PROCESS_GET_INFORMATION_FIRMWARE_VERSION:
            case BixolonLabelPrinter.PROCESS_EXECUTE_DIRECT_IO:
                Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                break;
            case BixolonLabelPrinter.PROCESS_OUTPUT_COMPLETE:
                Toast.makeText(getApplicationContext(), "Output Complete", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mListView.setEnabled(true);
                            mIsConnected = true;
                            invalidateOptionsMenu();
                            break;
                        case BixolonLabelPrinter.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BixolonLabelPrinter.STATE_NONE:
                            Log.e("NONE", msg.toString());
                            setStatus(R.string.title_not_connected);
                            //mListView.setEnabled(false);
                            mIsConnected = false;
                            invalidateOptionsMenu();
                            break;
                    }
                    break;
                case BixolonLabelPrinter.MESSAGE_READ:
                    MainActivity.this.dispatchMessage(msg);
                    break;
                case BixolonLabelPrinter.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(BixolonLabelPrinter.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    break;
                case BixolonLabelPrinter.MESSAGE_TOAST:
                    mListView.setEnabled(false);
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BixolonLabelPrinter.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case BixolonLabelPrinter.MESSAGE_LOG:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BixolonLabelPrinter.LOG),
                            Toast.LENGTH_SHORT).show();
                    break;
                case BixolonLabelPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(getApplicationContext(), "No paired device", Toast.LENGTH_SHORT).show();
                    } else {
                        DialogManager.showBluetoothDialog(MainActivity.this, (Set<BluetoothDevice>) msg.obj);
                    }
                    break;
            }
        }
    };

    public void checkVerify() {
        int result;
        ArrayList<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() > 0) {
            String[] reqPermissionArray = new String[permissionList.size()];
            reqPermissionArray = permissionList.toArray(reqPermissionArray);
            requestPermissions(reqPermissionArray, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.notice)).setMessage(getResources().getString(R.string.grant_permission))
                                .setPositiveButton(getResources().getString(R.string.exit),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }).setNegativeButton(getResources().getString(R.string.set_permission),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                                getApplicationContext().startActivity(intent);
                                            }
                                        }).setCancelable(false).show();
                        return;
                    }
                }
            }
        }
    }

    public class SampleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            // 여기서 에러를 처리
            finish();
        }
    }
}
