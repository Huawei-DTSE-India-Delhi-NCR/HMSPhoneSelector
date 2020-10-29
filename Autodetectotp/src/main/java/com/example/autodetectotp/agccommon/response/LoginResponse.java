package com.example.autodetectotp.agccommon.response;
import com.example.autodetectotp.agccommon.utils.Constant;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {


    @SerializedName(Constant.PROFILE)
    private ProfileResponse profileResponse;

    @SerializedName(Constant.TOKEN)
    private String token;

    @SerializedName(Constant.ERR_MSG)
    private String errorMsg;

    @SerializedName(Constant.CODE)
    private int code;

    @SerializedName(Constant.PROVIDER_ID)
    private String provideId;

    @SerializedName(value = Constant.UID, alternate = {"id", "sub"})
    private String UID;

//    @SerializedName(value = Constant.IS_EMAIL_VERIFIED, alternate = {"email_verified"})
//    private boolean isEmailVerified;

    public void setToken(String token) {
        this.token = token;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setProvideId(String provideId) {
        this.provideId = provideId;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setProfileResponse(ProfileResponse profileResponse) {
        this.profileResponse = profileResponse;
    }
}
