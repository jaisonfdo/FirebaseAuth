package com.droidmentor.firebaseservicesdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.droidmentor.firebaseservicesdemo.databinding.ActivityMainBinding;
import com.droidmentor.firebaseservicesdemo.mail.MailAuthenticationActivity;
import com.droidmentor.firebaseservicesdemo.mobile.MobileNumberAuthenticationActivity;

import static com.droidmentor.firebaseservicesdemo.utils.NetworkUtil.isNetworkAvailable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static int MOBILE_LOGIN_REQUEST_CODE = 99;
    public static int EMAIL_LOGIN_REQUEST_CODE = 100;

    public static String BUNDLE_KEY_UNIQUE_ID = "uniqueID";
    public static String BUNDLE_KEY_PAYLOAD = "mailOrPhoneNumber";
    public static String BUNDLE_KEY_EMAIL_LINK = "mailVerificationLink";

    ActivityMainBinding activityMainBinding;

    View.OnClickListener mainScreenClickListener;
    String emailVerificationLink = "";
    private boolean isVerificationMailRedirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activityMainBinding.setListener(getOnClickListener());

        isVerificationMailRedirection = performEmailverificationRedirection();

        if (isVerificationMailRedirection) {
            Log.d(TAG, "onCreate: mail redirection");
            Intent mailIntent = new Intent(MainActivity.this, MailAuthenticationActivity.class);
            mailIntent.putExtra(BUNDLE_KEY_EMAIL_LINK, emailVerificationLink);
            startActivityForResult(mailIntent, EMAIL_LOGIN_REQUEST_CODE);
            finish();
        }

        // To handle the verification completion redirected from the MailAuthenticationActivity
        checkEmailVerificationPayload();
    }

    public View.OnClickListener getOnClickListener() {
        if (mainScreenClickListener == null)
            mainScreenClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.btnEmail:
                            if (!isNetworkAvailable(MainActivity.this, true)) {
                                return;
                            }
                            Intent mailntent = new Intent(MainActivity.this, MailAuthenticationActivity.class);
                            startActivityForResult(mailntent, EMAIL_LOGIN_REQUEST_CODE);
                            break;
                        case R.id.btnMobileNumber:
                            if (!isNetworkAvailable(MainActivity.this, true)) {
                                return;
                            }
                            Intent mobileIntent = new Intent(MainActivity.this, MobileNumberAuthenticationActivity.class);
                            startActivityForResult(mobileIntent, MOBILE_LOGIN_REQUEST_CODE);
                            break;
                    }
                }
            };

        return mainScreenClickListener;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && (requestCode == MOBILE_LOGIN_REQUEST_CODE || requestCode == EMAIL_LOGIN_REQUEST_CODE)) {
            Bundle resultBundle = data.getExtras();
            if (resultBundle != null) {
                String uniqueID = resultBundle.getString(BUNDLE_KEY_UNIQUE_ID);
                String payload = resultBundle.getString(BUNDLE_KEY_PAYLOAD);

                if (!TextUtils.isEmpty(uniqueID)) {
                    Toast.makeText(this, "Login request", Toast.LENGTH_SHORT).show();
                }

                if (!TextUtils.isEmpty(payload)) {
                    if (requestCode == MOBILE_LOGIN_REQUEST_CODE)
                        Log.d(TAG, "onActivityResult: Mobile number " + payload);
                    else if (requestCode == EMAIL_LOGIN_REQUEST_CODE)
                        Log.d(TAG, "onActivityResult: Email " + payload);
                }
            }
        }
    }

    public boolean performEmailverificationRedirection() {
        Intent intent = getIntent();

        if (intent.getData() != null) {
            emailVerificationLink = intent.getData().toString();
            return !TextUtils.isEmpty(emailVerificationLink);
        }

        return false;
    }

    public void checkEmailVerificationPayload() {
        Bundle emailPayloadCheck = getIntent().getExtras();

        if (emailPayloadCheck != null) {
            String uniqueID = emailPayloadCheck.getString(BUNDLE_KEY_UNIQUE_ID);
            String payload = emailPayloadCheck.getString(BUNDLE_KEY_PAYLOAD);

            if (!TextUtils.isEmpty(uniqueID) && !TextUtils.isEmpty(payload))
                Toast.makeText(this, "Proceed with user details", Toast.LENGTH_SHORT).show();
        }
    }
}
