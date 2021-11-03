package com.mobile.USSD;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.romellfudi.ussdlibrary.USSDController;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class USSD extends ReactContextBaseJavaModule {

    private final TelephonyManager[] SIMManagers = new TelephonyManager[3];
    private final USSDController ussdApi;
    private final HashMap map = new HashMap<>();
    private final int SIMCount;
    private final ReactApplicationContext context;

    @SuppressLint("MissingPermission")
    USSD(ReactApplicationContext context) {
        super(context);
        this.context = context;
        map.put("KEY_LOGIN",new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR",new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));

        ussdApi = USSDController.getInstance(context);
        Log.v("USSD", Context.TELECOM_SERVICE);
        Log.v("USSD", Context.TELEPHONY_SERVICE);
        Log.v("USSD", Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
        SIMCount = phoneAccountHandleList.size();

    }

    @Override
    @NonNull
    public String getName() {
        return "USSD";
    }

    @ReactMethod
    public void getSIMCount(Promise promise) {
        promise.resolve(SIMCount);
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void getCarrier(int sim, Promise promise) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (SIMManagers[0] == null) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            for (int i=0; i<SIMCount; i++) {
                SIMManagers[i] = telephonyManager.createForSubscriptionId(subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i).getSubscriptionId());
            }
            ussdApi.verifyAccesibilityAccess(context.getCurrentActivity());
            ussdApi.verifyOverLay(context.getCurrentActivity());
        }
        promise.resolve(SIMManagers[sim].getSimOperatorName());
    }

    @ReactMethod
    public void executeUSSD(String ussd, int sim, Promise promise) {
        ussdApi.callUSSDInvoke(ussd, sim, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                // message has the response string data
                promise.resolve(message);
            }

            @Override
            public void over(String message) {
                // message has the response string data from USSD or error
                // response no have input text, NOT SEND ANY DATA
                promise.resolve(message);
            }
        });
    }
}