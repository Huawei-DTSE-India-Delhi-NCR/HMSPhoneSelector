package com.example.autodetectotp.agccommon.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.regex.Pattern;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Utils {

    private static Gson gson;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (phone.trim().length() > 0) {
            if (!Pattern.matches("[a-zA-Z]+", phone)) {
                if (phone.length() == 10) {
                    // if(phone.length() != 10) {
                    check = true;
                } else {
                    check = false;
                }
            } else {
                check = false;
            }
        } else {
            check = false;
        }
        return check;
    }

    public static String getValue(View editText) {
        if (editText != null && editText instanceof EditText) {
            return ((EditText) editText).getText().toString().trim();
        } else if (editText != null && editText instanceof TextView) {
            return ((TextView) editText).getText().toString().trim();
        }
        return null;
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

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

}
