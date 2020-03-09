package com.droidmentor.firebaseservicesdemo.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Jaison.
 */
public class HintRequestHelper {

    public static final int RC_HINT_EMAIL = 100;
    public static final int RC_HINT_MOBILE = 101;
    private static final String TAG = "HintRequestHelper";
    private Activity activity;
    private OnHintRequestResultListener onHintRequestResultListener;

    public HintRequestHelper(Activity activity, OnHintRequestResultListener onHintRequestResultListener) {
        this.activity = activity;
        this.onHintRequestResultListener = onHintRequestResultListener;
    }

    public void emailSuggestions() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setEmailAddressIdentifierSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        CredentialsClient mCredentialsClient;

        mCredentialsClient = Credentials.getClient(activity);

        PendingIntent intent = mCredentialsClient.getHintPickerIntent(hintRequest);
        try {
            activity.startIntentSenderForResult(intent.getIntentSender(), RC_HINT_EMAIL, null,
                    0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Could not start hint picker Intent", e);
        }
    }

    public void mobileNumberSuggestions() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        CredentialsClient mCredentialsClient;

        mCredentialsClient = Credentials.getClient(activity);

        PendingIntent intent = mCredentialsClient.getHintPickerIntent(hintRequest);
        try {
            activity.startIntentSenderForResult(intent.getIntentSender(), RC_HINT_MOBILE, null,
                    0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Could not start hint picker Intent", e);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_HINT_EMAIL) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                if (credential != null && !TextUtils.isEmpty(credential.getId())) {
                    Log.d(TAG, "onActivityResult: Email " + credential.getId());

                    if (onHintRequestResultListener != null)
                        onHintRequestResultListener.onHintResult(credential.getId());
                } else
                    Log.d(TAG, "onActivityResult: No Selection");

            } else {
                Log.e(TAG, "Hint Read: NOT OK");
                //  Toast.makeText(activity, "Hint Read Failed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_HINT_MOBILE) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                if (credential != null && !TextUtils.isEmpty(credential.getId())) {
                    Log.d(TAG, "onActivityResult: Mobile " + credential.getId());

                    if (onHintRequestResultListener != null)
                        onHintRequestResultListener.onHintResult(credential.getId());
                } else
                    Log.d(TAG, "onActivityResult: No Selection");

            } else {
                Log.e(TAG, "Hint Read: NOT OK");
                //  Toast.makeText(activity, "Hint Read Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract static class OnHintRequestResultListener {
        public abstract void onHintResult(String payload);
    }
}