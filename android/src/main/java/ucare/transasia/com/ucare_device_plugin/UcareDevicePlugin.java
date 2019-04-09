package ucare.transasia.com.ucare_device_plugin;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.widget.Toast;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.Manifest.permission;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.view.View;
import android.content.res.Resources;

import com.garmin.health.GarminHealth;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.GarminDeviceScanCallback;
import com.garmin.health.GarminHealthInitializationException;
import com.garmin.health.ScannedDevice;


/** FlutterPlugin */
public class UcareDevicePlugin implements MethodCallHandler {
  /** Plugin registration. */

  private  final MethodChannel channel;
  private Registrar registrar;
  private Activity activity;
  private static final int REQUEST_COARSE_LOCATION = 1;
  private final static int REQUEST_ENABLE_BT = 1;

  private DeviceManager deviceManager;

  private List<ScannedDevice> scannedDeviceList;

  private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
  private BluetoothLeScanner bluetoothScanner;
  private BluetoothManager btManager;



  private static final String LICENSE = "CgwCAwQFBgcICQoLDA4SgAIeCHUUoWcrOFjnJsFw14DCGjuUKcMXnpcyILioLWo1vxwYrwiwx+oSMJXM/bei8gWR8ND25zRHh8HYBPy3390fDsFcluiC1dVcR0LEuFGgxiuS6fK2R7+RmpUNxFZ72vyMS0PMH23IyVQOWoyAwIgdXd0npYwwGeCWMONYeZUMBbbh2HgPNqds1ZyaL7S1EQOubka00TnUVopSyVbwOeQTikRTPUwG1LlD7jJ0oPER7Mf1+v3fhaOaCS0Sl2UetQAuGscoRxqw8n6fJbD1SKi6nMcoLxYTu+q3SCXJ+Pf7F2Zq/4I97IaEa4Np5gzkTFjluUSqreegeuq6xONES1q1GIDwitWxLSoDAQID";
  private boolean mPreferSystemBonding =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;


  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "ucare_device_plugin");
    channel.setMethodCallHandler(new UcareDevicePlugin(registrar,channel));
  }

  private UcareDevicePlugin(Registrar registrar, MethodChannel channel){

      this.registrar = registrar;
      this.activity = registrar.activity();
      this.channel = channel;

      //this.btManager = (BluetoothManager) this.activity.getSystemService(this.activity.getApplicationContext().BLUETOOTH_SERVICE);
      this.channel.setMethodCallHandler(this);

      try {
        GarminHealth.initialize(this.activity.getApplicationContext(), mPreferSystemBonding, LICENSE);
        this.deviceManager = DeviceManager.getDeviceManager();
        checkBt();
      }
      catch (Exception e){
        Log.e("DeviceManagerConstruct", "DeviceManager exception in constructor", e);
      }

  }

  private void checkBt() {
    try{
      if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
    catch (Exception e){
      Log.e("Consturctor error", "constructor erro", e);
    }

  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);

      try
      {
        if(this == null){
          Log.e("NULL", "ERROR");
        }


        Toast.makeText(activity, "Sucess", Toast.LENGTH_LONG).show();
        Log.i("Info", "information");


      }
      catch (Exception e)
      {
        // Handle the exception
        Log.e("Exception", "Exception", e);
        Toast.makeText(activity, "Exception", Toast.LENGTH_LONG).show();

      }

      if(!verifyPermissions())
      {

        activity.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_EXTERNAL_STORAGE }, REQUEST_COARSE_LOCATION);

      }


    }
    else if(call.method.equals("getPairedDevices")){
      List<Device> paireddevices = getPairedDevices();
      result.success(paireddevices);
    }

    else if(call.method.equals("scanForDevice")){
      startScan();
      result.success("Scanning Started");
    }
    else if(call.method.equals("stopScanForDevice")){
      stopScan();
      result.success("Scanning Stopped");
    }
    else {
      result.notImplemented();
    }
  }


  /**
   * Checks if the location permissions are enabled or not.
   *
   * @return true if permissions are available.
   */
  private boolean verifyPermissions()
  {
    boolean buildCondition = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;


    int locationPermission = ContextCompat.checkSelfPermission(activity.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION);
    int storagePermission = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

    boolean permCondition = (locationPermission == PackageManager.PERMISSION_GRANTED) && (storagePermission == PackageManager.PERMISSION_GRANTED);

    return buildCondition || permCondition;

  }

  /**
   * Checks if the location services are enabled or not.
   *
   * @return true if services are available.
   */
  private boolean verifyLocationServices()
  {
    LocationManager locationManager = (LocationManager)activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
  }


  public List<Device> getPairedDevices(){
    Log.i("getPairedDevices", "Inside getPairedDevices");

    List<Device> devices = new ArrayList<>(deviceManager.getPairedDevices());
    Log.i("getPairedDevices", "Inside after getPairedDevices");

    return devices;
  }

  private void startScan()
  {
    try{
      if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
      else {
        scanForDevice();
      }
    }
    catch (Exception e){
      Log.e("Consturctor error", "constructor erro", e);
    }
  }

  private void stopScan()
  {


    if (bluetoothScanner != null) {
      bluetoothScanner.stopScan(callback);
    }

  }

  private void scanForDevice() {

    Log.i("scanForDevice", "scanForDevice");

    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

    if(mBtAdapter == null){
      checkBt();
    }

    if(callback == null){
      Log.d("NULL Callback", "callback is null");
    }
    else{
      Log.d("CAllbakc object", callback.toString());
      if(bluetoothScanner == null){
        Log.d("NULL Scanner", "Scanner is null");
      }
      else {
        Log.d("Scanner object", bluetoothScanner.toString());
        bluetoothScanner.flushPendingScanResults(callback);
        bluetoothScanner.startScan(callback);
      }
    }

  }


  GarminDeviceScanCallback callback = new GarminDeviceScanCallback()
  {
    @Override
    public void onBatchScannedDevices(List<ScannedDevice> devices) {

    }

    public void onScannedDevice(ScannedDevice device) {

      for (Device mDevice : deviceManager.getPairedDevices()) {
        if (mDevice.address().equalsIgnoreCase(device.address())) {
          return;
        }
      }
      Log.d("Scanned Device", device.toString());
      //scannedDeviceList.add(device);
    }

    public void onScanFailed(int errorCode) {
      Toast.makeText(activity, "Scanning failed", Toast.LENGTH_SHORT).show();
    }
  };




}
