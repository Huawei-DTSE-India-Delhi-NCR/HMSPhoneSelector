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

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }



}
