package com.example.autodetectotp.agccommon.response;


import com.example.autodetectotp.agccommon.utils.Constant;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName(value = Constant.DISPLAY_NAME)
    private String displayName;

    @SerializedName(Constant.PHONE)
    private String phone;

    @SerializedName(Constant.EMAIL)
    private String email;

    @SerializedName(Constant.LAST_NAME)
    private String lName;

    @SerializedName(Constant.FIRST_NAME)
    private String fName;

    @SerializedName(Constant.PROFILE_URL)
    private String profileUrl;

  @SerializedName(Constant.IS_EMAIL_VERIFIED)
    private boolean is_email_verified;


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setIsEmailVerified(boolean is_email_verified) {
        this.is_email_verified = is_email_verified;
    }
}
