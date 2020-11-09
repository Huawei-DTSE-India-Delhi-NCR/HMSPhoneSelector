package com.example.autodetectotp.agccommon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.autodetectotp.R;
import com.example.autodetectotp.agccommon.interfaces.ConnectionCallback;
import com.example.autodetectotp.agccommon.response.LoginResponse;
import com.example.autodetectotp.agccommon.response.ProfileResponse;
import com.example.autodetectotp.agccommon.ui.PhoneNumberDialog;
import com.example.autodetectotp.agccommon.ui.VerifyOTPAlert;
import com.example.autodetectotp.agccommon.utils.AGCErrorCode;
import com.example.autodetectotp.agccommon.utils.AGCErrorMessage;
import com.example.autodetectotp.agccommon.utils.Constant;
import com.example.autodetectotp.agccommon.utils.Utils;
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
   private final ProfileResponse profileResponse ;
    private final Context context;
    ConnectionCallback connectionCallback;
    private static PhoneSelector phoneSelectorInstance = null;

    public static synchronized PhoneSelector getInstance(Context context) {
        if (phoneSelectorInstance == null) {
            phoneSelectorInstance = new PhoneSelector(context);
        }
        return phoneSelectorInstance;
    }
    private PhoneSelector(Context context) {
        this.context =  context;
        profileResponse= new ProfileResponse();
        if (context instanceof ConnectionCallback) {
            connectionCallback = (ConnectionCallback) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void getPhoneNumberList(Context context, String countryCodeWithPlus) {
        ArrayList<String> phoneNumberList=new  ArrayList<String>();
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        @SuppressLint("MissingPermission") final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size() > 0) {
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                if(subscriptionInfo.getNumber()!=null&&!subscriptionInfo.getNumber().equals(""))
                phoneNumberList.add(subscriptionInfo.getNumber());
            }
        }
        if (phoneNumberList.size() != 0) {
            PhoneNumberDialog phoneNumberDialog=new PhoneNumberDialog(context,phoneNumberList,countryCodeWithPlus,connectionCallback);
            phoneNumberDialog.show();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.simslot_empty), Toast.LENGTH_LONG).show();
        }
    }

    public void login(final Context context, final String countryCode, final String phoneNumber, final String etVerifyCode, final BottomDialog bottomDialog) {
        PhoneUser phoneUser = new PhoneUser.Builder()
                .setCountryCode(countryCode)
                .setPhoneNumber(phoneNumber)
                .setVerifyCode(etVerifyCode)
                .build();
        AGConnectAuth.getInstance().createUser(phoneUser)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        Log.d("Phone Selector Library", signInResult.getUser().getProviderInfo() + "");
                        AGConnectUser user = signInResult.getUser();
                        profileResponse.setPhone(user.getPhone());
                        profileResponse.setDisplayName(user.getDisplayName());
                        profileResponse.setEmail(user.getEmail());
                        setSuccessLoginResponse(bottomDialog);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e.getMessage().contains(AGCErrorMessage.ALREADY_REGISTERED)) {
                            AGConnectAuthCredential credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, "", etVerifyCode);
                            loginRegisteredUser(credential,bottomDialog);
                        } else {
                            if(e.getMessage().contains(AGCErrorMessage.VERIFY_PHONE)||e.getMessage().contains(AGCErrorMessage.VERIFY_CODE))
                                Toast.makeText(context,context.getResources().getString(R.string.verify_phone),Toast.LENGTH_LONG).show();
                            else
                            showErrorResponse(AGCErrorCode.ERROR_PHONE_AUTH_FAILED, e.getMessage());
                        }
                    }
                });
    }

    private void loginRegisteredUser(AGConnectAuthCredential credential, final BottomDialog bottomDialog) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // Obtain sign-in information.
                        AGConnectUser user = signInResult.getUser();
                        profileResponse.setPhone(user.getPhone());
                        profileResponse.setDisplayName(user.getDisplayName());
                        profileResponse.setEmail(user.getEmail());
                        setSuccessLoginResponse(bottomDialog);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if(e.getMessage().contains(AGCErrorMessage.VERIFY_PHONE)||e.getMessage().contains(AGCErrorMessage.VERIFY_CODE))
                            Toast.makeText(context,context.getResources().getString(R.string.verify_phone),Toast.LENGTH_LONG).show();
                        else
                            showErrorResponse(AGCErrorCode.ERROR_PHONE_AUTH_FAILED, e.getMessage());
                    }
                });
    }

    private void showErrorResponse(int errorCode, String msg) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setErrorMsg(msg);
        loginResponse.setCode(errorCode);
        sendErrorResultToActivity(new Gson().toJson(loginResponse));
    }

    private void sendErrorResultToActivity(String response) {
        connectionCallback.connectionErrorMessage(response);
    }

    private void setSuccessLoginResponse(BottomDialog bottomDialog) {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null) {
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setProfileResponse(profileResponse);
            loginResponse.setUID(user.getUid());
            loginResponse.setProvideId(user.getProviderId());
            loginResponse.setCode(AGCErrorCode.SUCCESS_CODE);
            loginResponse.setErrorMsg(AGCErrorMessage.AGC_USER_LOGIN_SUCCESS);
            Log.d(Constant.LOG_TAG, "Sign in Done");
            sendSuccessResultToActivity(Utils.getGson().toJson(loginResponse),bottomDialog);
        } else {
            showErrorResponse(AGCErrorCode.ERROR_CODE_AGC_USER_NOT_FOUND, AGCErrorMessage.AGC_USER_NOT_FOUND);
        }
    }

    private void sendSuccessResultToActivity(String response,BottomDialog bottomDialog) {
        if(bottomDialog!=null)
            bottomDialog.dismiss();
        connectionCallback.connectionSuccessMessage(response);
    }

    public void signOut() {
        AGConnectAuth.getInstance().signOut();
    }

    public void verifyCode(final String countryCode, final String phoneNumber){
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30) //shortest send interval ，30-120s
                .build();
        Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode("+" + countryCode, phoneNumber, settings);
        task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
            @Override
            public void onSuccess(VerifyCodeResult verifyCodeResult) {
                Toast.makeText(context, context.getResources().getString(R.string.otp_sent), Toast.LENGTH_SHORT).show();
                VerifyOTPAlert verifyOTPAlert=new VerifyOTPAlert(context,countryCode,phoneNumber,verifyCodeResult);
            }
        }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if(e.getMessage().contains(AGCErrorMessage.INVALID_PHONE))
                Toast.makeText(context,context.getResources().getString(R.string.invalid_phone),Toast.LENGTH_LONG).show();
                else
                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                Log.d(context.getResources().getString(R.string.otp_request), "Exception :" + e);
            }
        });
    }
}
