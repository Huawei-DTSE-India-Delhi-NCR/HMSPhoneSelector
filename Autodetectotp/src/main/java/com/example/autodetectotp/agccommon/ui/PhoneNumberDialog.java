package com.example.autodetectotp.agccommon.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.autodetectotp.R;
import com.example.autodetectotp.agccommon.interfaces.ConnectionCallback;
import com.example.autodetectotp.databinding.PhonenumberLayoutBinding;

import java.util.ArrayList;

public class PhoneNumberDialog extends Dialog implements View.OnClickListener {
    private final ConnectionCallback connectionCallback;
    private final ArrayList<String> phoneNumberList;
    private final String countryCode;
    private PhonenumberLayoutBinding phonenumberLayoutBinding;
    public PhoneNumberDialog(@NonNull Context context, ArrayList<String> phoneNumberList, final String countryCode, ConnectionCallback connectionCallback) {
        super(context);
        this.connectionCallback = connectionCallback;
        this.phoneNumberList=phoneNumberList;
        this.countryCode=countryCode;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        phonenumberLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout. phonenumber_layout, null, false);
        setContentView(phonenumberLayoutBinding.getRoot());
        setCanceledOnTouchOutside(false);
        initView();
        phoneNumbersData();
    }

    private void phoneNumbersData() {
        boolean isDualSim = false;
        if (phoneNumberList.size() == 2)
            isDualSim = true;

        if (isDualSim) {
            phonenumberLayoutBinding.simTwo.setVisibility(View.VISIBLE);
            phonenumberLayoutBinding.simOne.setText(phoneNumberList.get(0));
            phonenumberLayoutBinding.simTwo.setText(phoneNumberList.get(1));
        } else {
            phonenumberLayoutBinding.simOne.setText(phoneNumberList.get(0));
            phonenumberLayoutBinding.simTwo.setVisibility(View.GONE);
        }
    }

    private void initView() {
        phonenumberLayoutBinding.simOne.setOnClickListener(this);
        phonenumberLayoutBinding.simTwo.setOnClickListener(this);
        phonenumberLayoutBinding.tryItAnotherText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sim_one) {
            String validPhoneNumber = getValidPhoneNumber(phonenumberLayoutBinding.simOne.getText().toString(), countryCode);
            connectionCallback.connectionSelectedPhoneNumber(validPhoneNumber);
            dismiss();
        } else if (id == R.id.sim_two) {
            String validPhoneNumber = getValidPhoneNumber(phonenumberLayoutBinding.simTwo.getText().toString(), countryCode);
            connectionCallback.connectionSelectedPhoneNumber(validPhoneNumber);
            dismiss();
        } else if (id == R.id.tryItAnother_text) {
            connectionCallback.connectionTryItMessage();
            dismiss();
        }
    }
    private String getValidPhoneNumber(String selectedPhoneNumber, String countryCode) {
        String countryCodeWithOutPlus = countryCode.replace("+", "");
        String validPhoneNumber = "";
        if (selectedPhoneNumber.contains(countryCode)) {
            validPhoneNumber = selectedPhoneNumber.replace(countryCode, "");
        } else if (selectedPhoneNumber.startsWith(countryCodeWithOutPlus)) {
            validPhoneNumber = selectedPhoneNumber.replace(countryCodeWithOutPlus, "");
        } else {
            validPhoneNumber = selectedPhoneNumber;
        }
        return validPhoneNumber;
    }
}
