package com.example.hmsphoneselector;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.example.autodetectotp.agccommon.PhoneSelector;
import com.example.autodetectotp.agccommon.interfaces.ConnectionCallback;
import com.example.autodetectotp.agccommon.utils.Utils;
import com.example.hmsphoneselector.databinding.AgcPhoneBinding;
import com.huawei.agconnect.auth.AGConnectAuth;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;

public class MainActivity extends AppCompatActivity implements ConnectionCallback,View.OnClickListener {
    //
    AgcPhoneBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 200;
    PhoneSelector phoneSelector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.agc_phone);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECEIVE_SMS, Manifest.permission.READ_PHONE_NUMBERS, RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, 101);
        initView();
    }

    private void initView() {
        phoneSelector =PhoneSelector.getInstance(this);
        if(AGConnectAuth.getInstance().getCurrentUser()!=null){
            phoneSelector.signOut();
        }
        binding.etAccount.requestFocus();
        binding.btnSend.setOnClickListener(this);
        binding.etAccount.setOnClickListener(this);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_account) {
            if (binding.ccp.getSelectedCountryCode() != null) {
                if (binding.etAccount.getText().toString().length() == 0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        phoneSelector.getPhoneNumberList(this,binding.ccp.getSelectedCountryCodeWithPlus());
                    }
                }
            }else {
                Toast.makeText(this, getResources().getString(R.string.select_countryCode), Toast.LENGTH_SHORT).show();
            }
        }else if (v.getId() == R.id.btn_send) {
            if(isNetworkAvailable(this)) {
                if (binding.etAccount.getText().toString().trim().length() > 0)
                    phoneSelector.verifyCode(binding.ccp.getSelectedCountryCode(), binding.etAccount.getText().toString().trim());
                else
                    Toast.makeText(this, getResources().getString(R.string.input_number), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
        }
    }

    ///==============Check Permissions================================
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECEIVE_SMS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_NUMBERS);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ RECEIVE_SMS,Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readsms = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean phonenumber = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean phonestate = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (readsms && phonenumber && phonestate) {
                        phoneSelector.getPhoneNumberList(this,binding.ccp.getSelectedCountryCodeWithPlus());
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel(getResources().getString(R.string.access_permission),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE},
                                                        PERMISSION_REQUEST_CODE);
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public void connectionSuccessMessage(String successMessage) {
        Intent intent= new Intent(this,UserDetails.class);
        intent.putExtra("userData",successMessage);
        startActivity(intent);
         binding.etAccount.getText().clear();
    }

    @Override
    public void connectionErrorMessage(String errorMessage) {
    Toast.makeText(this,errorMessage,Toast.LENGTH_LONG).show();
    }

    @Override
    public void connectionSelectedPhoneNumber(String number) {
        binding.etAccount.setText(number.trim());
        binding.btnSend.performClick();
    }

    @Override
    public void connectionTryItMessage() {
        binding.etAccount.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean have_WIFI= false;
        boolean have_MobileData = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    // connected to wifi
                    have_WIFI=true;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    have_MobileData=true;
                    break;
                default:
                    break;
            }
        }
        return have_WIFI||have_MobileData;
    }
}