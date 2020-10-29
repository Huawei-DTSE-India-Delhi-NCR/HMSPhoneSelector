package com.example.hmsphoneselector;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.autodetectotp.agccommon.PhoneSelector;
import com.example.autodetectotp.agccommon.utils.Utils;
import com.example.hmsphoneselector.databinding.AgcPhoneBinding;
import com.huawei.agconnect.auth.AGConnectAuth;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;

public class MainActivity extends AppCompatActivity implements PhoneSelector.ConnectionCallback,View.OnClickListener {
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
        //phoneSelector=new PhoneSelector(this);
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
                        phoneSelector.getNumberList(this,binding.ccp.getSelectedCountryCodeWithPlus());
                    }
                }
            }else {
                Toast.makeText(this, "Please select country code first.", Toast.LENGTH_SHORT).show();
            }
        }else if (v.getId() == R.id.btn_send) {
            if(Utils.isNetworkAvailable(this)) {
                if (binding.etAccount.getText().toString().trim().length() > 0)
                    phoneSelector.verifyCode(binding.ccp.getSelectedCountryCode(), binding.etAccount.getText().toString().trim());
                else
                    Toast.makeText(this, "Please input number.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please check internet.", Toast.LENGTH_SHORT).show();
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
                        phoneSelector.getNumberList(this,binding.ccp.getSelectedCountryCodeWithPlus());
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
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
    public void connectionSelectedMobile(String number) {
        binding.etAccount.setText(number.trim());
        binding.btnSend.performClick();
    }

    @Override
    public void connectionTryItMessage() {
        binding.etAccount.requestFocus();
    }
}