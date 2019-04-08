package ucare.transasia.com.ucare_device_plugin;

import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.os.Build;
import android.util.Log;
import android.app.Activity;
import android.widget.Toast;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.Manifest.permission;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import com.garmin.health.GarminHealth;
import com.garmin.health.GarminHealthInitializationException;
import com.garmin.health.GarminDeviceScanCallback;




/** FlutterPlugin */
public class UcareDevicePlugin implements MethodCallHandler {
  /** Plugin registration. */

  private  final MethodChannel channel;
  private Registrar registrar;
  private Activity activity;
  private static final int REQUEST_COARSE_LOCATION = 1;


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
    this.channel.setMethodCallHandler(this);
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
        GarminHealth.initialize(this.registrar.context(), mPreferSystemBonding, LICENSE);
        Toast.makeText(this.activity, "Sucess", Toast.LENGTH_LONG).show();
        Log.i("Info", "information");


      }
      catch (GarminHealthInitializationException e)
      {
        // Handle the exception
        Log.e("Exception", "Exception", e);
        Toast.makeText(this.activity, "Exception", Toast.LENGTH_LONG).show();

      }

      if(!verifyPermissions())
      {


        this.activity.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_EXTERNAL_STORAGE }, REQUEST_COARSE_LOCATION);

      }


    } else {
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


    int locationPermission = ContextCompat.checkSelfPermission(this.activity.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION);
    int storagePermission = ContextCompat.checkSelfPermission(this.activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
    LocationManager locationManager = (LocationManager)this.activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
  }

  private void scan() {
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = btAdapter.getBluetoothScanner();

    scanner.startScan(new GarminDeviceScanCallback() {
      public void onScannedDevice(int callbackType, ScannedDevice device) {
        mDeviceAdapter.addDevice(device);
      }

      public void onScanFailed(int errorCode) {
        showErrorDialog("Scanning failed");
      }
    }
  }

}
