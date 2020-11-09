package com.example.autodetectotp.agccommon.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.autodetectotp.R;
import com.example.autodetectotp.agccommon.PhoneSelector;
import com.example.autodetectotp.agccommon.interfaces.PhoneOTPListener;
import com.example.autodetectotp.agccommon.receiver.SMSListener;
import com.example.autodetectotp.databinding.VerifyotpLayoutBinding;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.huawei.agconnect.auth.VerifyCodeResult;


public class VerifyOTPAlert extends BottomDialog.Builder implements View.OnClickListener {

    private final String countryCode;
    private final String phoneNumber;
    private final Context context;
    private final VerifyCodeResult verifyCodeResult;
    private VerifyotpLayoutBinding verifyotpLayoutBinding;
    private BottomDialog bottomDialog;
    private int interval;

    public VerifyOTPAlert(@NonNull Context context, final String countryCode, final String phoneNumber, VerifyCodeResult verifyCodeResult) {
        super(context);
        this.context=context;
        this.phoneNumber=phoneNumber;
        this.countryCode=countryCode;
        this.verifyCodeResult=verifyCodeResult;
        initBottomDialog();
    }

    protected void initBottomDialog() {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        verifyotpLayoutBinding= DataBindingUtil.inflate(layoutInflater, R.layout.verifyotp_layout, null, true);
        bottomDialog = new BottomDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.otp_authentication))
                .setContent(context.getResources().getString(R.string.enter_otp))
                .setCustomView(verifyotpLayoutBinding.getRoot())
                .show();
        verifyotpLayoutBinding.btnRegister.setOnClickListener(this);
        verifyotpLayoutBinding.btnResend.setOnClickListener(this);
        verifyotpLayoutBinding.txtTapping.setText("");
        verifyotpLayoutBinding.txtCounter.setVisibility(View.VISIBLE);
        interval = Integer.parseInt(verifyCodeResult.getShortestInterval());
        verifyotpLayoutBinding.etVerifyCode.setVisibility(View.VISIBLE);
        verifyotpLayoutBinding.etVerifyCode.requestFocus();
        verifyotpLayoutBinding.btnRegister.setVisibility(View.VISIBLE);
        countDownTimer();
        smsListner(verifyotpLayoutBinding);
    }

    private void countDownTimer() {
        new CountDownTimer(
                interval * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                verifyotpLayoutBinding.txtCounter.setText(context.getString(R.string.lbl_resend_otp_in) + " " + interval + " Seconds");
                interval--;
            }

            @Override
            public void onFinish() {
                verifyotpLayoutBinding.btnResend.setVisibility(View.VISIBLE);
                verifyotpLayoutBinding.txtTapping.setText(context.getString(R.string.lbl_resend_otp_message));
                verifyotpLayoutBinding.txtCounter.setVisibility(View.GONE);
            }
        }.start();
    }

    private void smsListner(final VerifyotpLayoutBinding verifyotpLayoutBinding) {
        SMSListener.bindListener(new PhoneOTPListener() {
            @Override
            public void onOTPReceived(String otp) {
                String number = otp.replaceAll("[^0-9]", "");
                verifyotpLayoutBinding.etVerifyCode.setText(number);
                verifyotpLayoutBinding.btnRegister.performClick();
            }
        });
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_register) {
            if (TextUtils.isEmpty(verifyotpLayoutBinding.etVerifyCode.getText()))
                Toast.makeText(context,context.getResources().getString(R.string.verifyCode_empty),Toast.LENGTH_LONG).show();
            else
                PhoneSelector.getInstance(context).login(context,countryCode, phoneNumber, verifyotpLayoutBinding.etVerifyCode.getText().toString().trim(),bottomDialog);

        } else if (id == R.id.btn_resend) {
            if(bottomDialog!=null)
                bottomDialog.dismiss();
            PhoneSelector.getInstance(context).verifyCode(countryCode,phoneNumber);
    }
    }
}
