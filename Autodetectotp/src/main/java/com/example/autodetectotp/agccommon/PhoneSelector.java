package com.example.autodetectotp.agccommon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.autodetectotp.R;
import com.example.autodetectotp.agccommon.interfaces.PhoneOTPListener;
import com.example.autodetectotp.agccommon.response.LoginResponse;
import com.example.autodetectotp.agccommon.response.ProfileResponse;
import com.example.autodetectotp.agccommon.receiver.SMSListener;
import com.example.autodetectotp.agccommon.utils.AGCErrorCode;
import com.example.autodetectotp.agccommon.utils.AGCErrorMessage;
import com.example.autodetectotp.agccommon.utils.Constant;
import com.example.autodetectotp.agccommon.utils.Utils;
import com.example.autodetectotp.databinding.BottomdialogLayoutBinding;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;
import java.util.ArrayList;
import java.util.List;
public class PhoneSelector {
    ProfileResponse profileResponse = new ProfileResponse();
    private AppCompatActivity appCompatActivity;
    ConnectionCallback connectionCallback;
    BottomDialog bottomDialog;
    BottomdialogLayoutBinding customView;
    private int interval;
    private static PhoneSelector phoneSelectorInstance = null;

    public static PhoneSelector getInstance(Context context) {
        if (phoneSelectorInstance == null) {
            phoneSelectorInstance = new PhoneSelector(context);
        }
        return phoneSelectorInstance;
    }
    private PhoneSelector(Context context) {
        this.appCompatActivity = (AppCompatActivity) context;
        if (context instanceof ConnectionCallback) {
            connectionCallback = (ConnectionCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SampleCallback");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void getNumberList(Context context,String countryCodeWithPlus) {
        ArrayList<String> phoneNumberList=new  ArrayList<String>();
        SubscriptionManager subManager = SubscriptionManager.from(context);
        @SuppressLint("MissingPermission") final List<SubscriptionInfo> activeSubscriptionInfoList = subManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size() > 0) {
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                if(subscriptionInfo.getNumber()!=null&&!subscriptionInfo.getNumber().equals(""))
                phoneNumberList.add(subscriptionInfo.getNumber());
            }
        }
        if (phoneNumberList.size() != 0) {
            popupNumbers(phoneNumberList,countryCodeWithPlus);
        } else {
            Toast.makeText(context, "Please check sim slots are empty", Toast.LENGTH_LONG).show();
        }
    }
    private void popupNumbers(ArrayList<String> phoneNumberList, final String countryCodeWithPlus) {
        boolean isDualSim=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        // set the custom layout
        final View customLayout = appCompatActivity.getLayoutInflater().inflate(R.layout.custom_phonelayout, null);
        builder.setView(customLayout);
        // create and show
        // the alert dialog
        final AlertDialog dialog = builder.create();
        final TextView simone_text = customLayout.findViewById(R.id.sim_one);
        final TextView simtwo_text = customLayout.findViewById(R.id.sim_two);
        TextView tryItAnother_text = customLayout.findViewById(R.id.tryItAnother_text);
        if (phoneNumberList.size() == 2)
            isDualSim = true;
        if (isDualSim) {
            simtwo_text.setVisibility(View.VISIBLE);
            simone_text.setText(phoneNumberList.get(0));
            simtwo_text.setText(phoneNumberList.get(1));
        } else {
            simone_text.setText(phoneNumberList.get(0));
            simtwo_text.setVisibility(View.GONE);
        }
        simone_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validSim = simNumberValid(simone_text.getText().toString(),countryCodeWithPlus);
                connectionCallback.connectionSelectedMobile(validSim);
                dialog.dismiss();
            }
        });
        simtwo_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validSim = simNumberValid(simtwo_text.getText().toString(),countryCodeWithPlus);
                connectionCallback.connectionSelectedMobile(validSim);
                dialog.dismiss();
            }
        });
        tryItAnother_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                connectionCallback.connectionTryItMessage();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private String simNumberValid(String data,String countryCodeWithPlus) {
        String removed = countryCodeWithPlus;
        String countryCode=countryCodeWithPlus.replace("+", "");
        String num = "";
        System.out.println("cpp " + removed);
        System.out.println("cpp " + countryCodeWithPlus);
        if (data.contains(removed)) {
            num = data.replace(removed, " ");
        }else if(data.startsWith(countryCode)){
            num = data.replace(countryCode, " ");
        }else {
            num=data;
        }
        return num;
    }

    private void login(final String countryCode, final String phoneNumber) {
        PhoneUser phoneUser = new PhoneUser.Builder()
                .setCountryCode(countryCode)
                .setPhoneNumber(phoneNumber)
                .setVerifyCode(customView.etVerifyCode.getText().toString().trim())
                .build();
        AGConnectAuth.getInstance().createUser(phoneUser)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        Log.d("HMS Library", signInResult.getUser().getProviderInfo() + "");
                        AGConnectUser user = signInResult.getUser();
                        profileResponse.setPhone(user.getPhone());
                        profileResponse.setDisplayName(user.getDisplayName());
                        profileResponse.setEmail(user.getEmail());
                        setSuccessLoginResponse("token");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e.getMessage().contains(AGCErrorMessage.ALREADY_REGISTERED)) {
                            AGConnectAuthCredential credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, "", customView.etVerifyCode.getText().toString().trim());
                            loginRegisteredUser(credential);
                        } else {
                            showErrorResponse(AGCErrorCode.ERROR_PHONE_AUTH_FAILED, e.getMessage());
                        }
                    }
                });
    }

    private void loginRegisteredUser(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // Obtain sign-in information.
                        AGConnectUser user = signInResult.getUser();
                        profileResponse.setPhone(user.getPhone());
                        profileResponse.setDisplayName(user.getDisplayName());
                        profileResponse.setEmail(user.getEmail());
                        setSuccessLoginResponse("token");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if(bottomDialog!=null)
                        bottomDialog.dismiss();
                        showErrorResponse(AGCErrorCode.ERROR_PHONE_AUTH_FAILED, e.getMessage());
                    }
                });
    }

    private void showErrorResponse(int errorCode, String msg) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setErrorMsg(msg);
        loginResponse.setCode(errorCode);
        sendErrorResult(new Gson().toJson(loginResponse));
    }

    private void sendErrorResult(String response) {
        connectionCallback.connectionErrorMessage(response);
    }

    private void setSuccessLoginResponse(String token) {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null) {
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setProfileResponse(profileResponse);
            loginResponse.setUID(user.getUid());
            loginResponse.setProvideId(user.getProviderId());
            loginResponse.setToken(token);
            loginResponse.setCode(AGCErrorCode.SUCCESS_CODE);
            loginResponse.setErrorMsg(AGCErrorMessage.AGC_USER_LOGIN_SUCCESS);
            Log.d(Constant.LOG_TAG, "Sign in Done");
            sendSuccessResult(Utils.getGson().toJson(loginResponse));
        } else {
            showErrorResponse(AGCErrorCode.ERROR_CODE_AGC_USER_NOT_FOUND, AGCErrorMessage.AGC_USER_NOT_FOUND);
        }
    }

    private void sendSuccessResult(String response) {
        bottomDialog.dismiss();
        connectionCallback.connectionSuccessMessage(response);
    }

    public void signOut() {
        AGConnectAuth.getInstance().signOut();
    }

    public void verifyCode(final String countryCode1, final String phoneNumber){
        if(bottomDialog!=null)
        bottomDialog.dismiss();
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30) //shortest send interval ，30-120s
                .build();
        Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode("+" + countryCode1, phoneNumber, settings);
        task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
            @Override
            public void onSuccess(VerifyCodeResult verifyCodeResult) {
                sampleVerifyOTPView(countryCode1, phoneNumber);
                interval = Integer.parseInt(verifyCodeResult.getShortestInterval());
                Toast.makeText(appCompatActivity, "OTP sent successfully.", Toast.LENGTH_SHORT).show();
                interval = Integer.parseInt(verifyCodeResult.getShortestInterval());
                new CountDownTimer(
                        interval * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        customView.txtCounter.setText(appCompatActivity.getString(R.string.lbl_resend_otp_in) + " " + String.valueOf(interval) + " Seconds");
                        interval--;
                    }

                    @Override
                    public void onFinish() {
                        customView.btnResend.setVisibility(View.VISIBLE);
                        customView.txtTapping.setText(appCompatActivity.getString(R.string.lbl_resend_otp_message));
                        customView.txtCounter.setVisibility(View.GONE);
                    }
                }.start();
                customView.etVerifyCode.setVisibility(View.VISIBLE);
                customView.etVerifyCode.requestFocus();
                customView.btnRegister.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(appCompatActivity, "" + e, Toast.LENGTH_SHORT).show();
                Log.d("OTP request", "Exception :" + e);
            }
        });
    }

    public interface ConnectionCallback {
        void connectionSuccessMessage(String successMessage);
        void connectionErrorMessage(String errorMessage);
        void connectionSelectedMobile(String number);
        void connectionTryItMessage();
    }

    private void sampleVerifyOTPView(final String countryCode,final String mobileNumber) {
        LayoutInflater inflater = (LayoutInflater)appCompatActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView= DataBindingUtil.inflate(inflater,R.layout.bottomdialog_layout, null, true);
        bottomDialog = new BottomDialog.Builder(appCompatActivity)
                .setTitle("OTP Authentication")
                .setContent("OTP Sent Successfully. Enter the OTP.")
                .setCustomView(customView.getRoot())
                .show();
        customView.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               login(countryCode,mobileNumber);
            }
        });
        customView.btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode(countryCode,mobileNumber);
            }
        });
        customView.txtTapping.setText("");
        customView.txtCounter.setVisibility(View.VISIBLE);
        smsListner();
    }
    private void smsListner() {
        SMSListener.bindListener(new PhoneOTPListener() {
            @Override
            public void onOTPReceived(String otp) {
                String number = otp.replaceAll("[^0-9]", "");
                customView.etVerifyCode.setText(number);
                customView.btnRegister.performClick();
            }
        });
    }

}
