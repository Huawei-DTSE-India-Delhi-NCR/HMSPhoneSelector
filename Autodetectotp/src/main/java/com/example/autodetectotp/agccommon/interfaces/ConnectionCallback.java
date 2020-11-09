package com.example.autodetectotp.agccommon.interfaces;

public interface ConnectionCallback {
    void connectionSuccessMessage(String successMessage);
    void connectionErrorMessage(String errorMessage);
    void connectionSelectedPhoneNumber(String phoneNumber);
    void connectionTryItMessage();
}
