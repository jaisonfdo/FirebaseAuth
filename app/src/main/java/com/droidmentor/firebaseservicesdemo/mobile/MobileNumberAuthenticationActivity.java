package com.droidmentor.firebaseservicesdemo.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.droidmentor.firebaseservicesdemo.R;
import com.droidmentor.firebaseservicesdemo.databinding.ActivityMobileNumberAuthenticationBinding;
import com.droidmentor.firebaseservicesdemo.mobile.FirebaseMobileAuthenticationService.MobileAuthenticationListener;
import com.droidmentor.firebaseservicesdemo.utils.AuthInterimScreenHelper;
import com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState;
import com.droidmentor.firebaseservicesdemo.utils.HintRequestHelper;
import com.droidmentor.firebaseservicesdemo.utils.KeyboardUtil;
import com.droidmentor.firebaseservicesdemo.utils.OTPScreenHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;

import static com.droidmentor.firebaseservicesdemo.MainActivity.BUNDLE_KEY_PAYLOAD;
import static com.droidmentor.firebaseservicesdemo.MainActivity.BUNDLE_KEY_UNIQUE_ID;
import static com.droidmentor.firebaseservicesdemo.utils.HintRequestHelper.RC_HINT_MOBILE;
import static com.droidmentor.firebaseservicesdemo.utils.NetworkUtil.isNetworkAvailable;

public class MobileNumberAuthenticationActivity extends AppCompatActivity {

    private static final String TAG = "MobileNumberAuthenticat";
    public static int LOTTIE_ANIMATION_DELAY = 2000;

    ActivityMobileNumberAuthenticationBinding activityBinding;

    FirebaseMobileAuthenticationService firebaseMobileAuthenticationService;
    String phoneNumber;
    String countryCode;
    private HintRequestHelper hintRequestHelper;
    private AuthInterimScreenHelper authInterimScreenHelper;
    private OTPScreenHelper otpScreenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_mobile_number_authentication);
        countryCode = getString(R.string.authMobileCountryCode);
        doActionBarSetup();
        doViewPortSetup();
        doInterimStateSetup();
        doOTPStateSSetup();
        doFirebaseMobileServiceSetup();
        doHintRequestSetup();

    }

    public void doActionBarSetup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
            if (upArrow != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    upArrow.setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(this, R.color.loginAccountKitThemeColorAccent), BlendMode.SRC_ATOP));
                } else {
                    upArrow.setColorFilter(ContextCompat.getColor(this, R.color.loginAccountKitThemeColorAccent), PorterDuff.Mode.SRC_ATOP);
                }
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        }
    }

    public void doViewPortSetup() {
        activityBinding.etMobileNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim()) && s.toString().trim().length() == 10) {
                    phoneNumber = s.toString().trim();
                    activityBinding.setIsValidMobileNumber(true);
                } else
                    activityBinding.setIsValidMobileNumber(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        activityBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNetworkAvailable(MobileNumberAuthenticationActivity.this, true)) {
                    return;
                }

                if (firebaseMobileAuthenticationService != null) {
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        firebaseMobileAuthenticationService.initMobileAuthenticationFlow(countryCode + phoneNumber);
                        KeyboardUtil.hideSoftKeyboard(MobileNumberAuthenticationActivity.this);
                        authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFYING_MOBILE);
                    }
                }
            }
        });
    }

    public void doHintRequestSetup() {
        // To show the registered phone number listing

        hintRequestHelper = new HintRequestHelper(this, new HintRequestHelper.OnHintRequestResultListener() {
            @Override
            public void onHintResult(String payload) {
                Log.d(TAG, "onHintResult: ");
                String selectedNumber = payload;
                if (!TextUtils.isEmpty(payload)) {
                    if (payload.contains(countryCode)) {
                        selectedNumber = selectedNumber.replace(countryCode, "");
                    }

                    selectedNumber = selectedNumber.replace("+", "");

                    activityBinding.etMobileNumberInput.setText(selectedNumber);
                    activityBinding.etMobileNumberInput.post(new Runnable() {
                        @Override
                        public void run() {
                            activityBinding.etMobileNumberInput.setSelection(activityBinding.etMobileNumberInput.getText().length());
                        }
                    });
                }
            }
        });

        hintRequestHelper.mobileNumberSuggestions();
    }


    public void doFirebaseMobileServiceSetup() {
        firebaseMobileAuthenticationService = new FirebaseMobileAuthenticationService(this, new MobileAuthenticationListener() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                otpScreenHelper.hideIntermediateScreen();
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.SENT);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        authInterimScreenHelper.hideIntermediateScreen();
                        otpScreenHelper.showIntermediateStates(false, countryCode + phoneNumber);
                    }
                }, LOTTIE_ANIMATION_DELAY);
            }

            @Override
            public void onSignInComplete(final FirebaseUser user) {
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFIED);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(BUNDLE_KEY_UNIQUE_ID, user.getUid());
                        returnIntent.putExtra(BUNDLE_KEY_PAYLOAD, user.getPhoneNumber());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }, LOTTIE_ANIMATION_DELAY);
            }

            @Override
            public void onTooManyRequestsFailure(@NonNull Exception e) {
                otpScreenHelper.hideIntermediateScreen();
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.TOO_MANY_CODE_REQUEST);
            }

            @Override
            public void onInvalidCredentialFailure(@NonNull Exception e) {
                authInterimScreenHelper.hideIntermediateScreen();
                otpScreenHelper.showIntermediateStates(true, countryCode + phoneNumber);
            }

            @Override
            public void onInvalidPhoneNumber(@NonNull Exception e) {
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.INVALID_PHONE_NUMBER);
            }

            @Override
            public void onVerificationFailed(@NonNull Exception e) {
                otpScreenHelper.hideIntermediateScreen();
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.SOMETHING_WRONG);
            }
        });
    }


    public void doInterimStateSetup() {
        authInterimScreenHelper = new AuthInterimScreenHelper(activityBinding.parentLayout,
                activityBinding.intermediateViewStub.getViewStub());

        authInterimScreenHelper.setOnInterimResultListener(new AuthInterimScreenHelper.OnInterimResultListener() {
            @Override
            public void onLaterClick() {
                super.onLaterClick();
                authInterimScreenHelper.hideIntermediateScreen();
                activityBinding.etMobileNumberInput.setText("");
            }

            @Override
            public void onSomethingWrongLaterClick() {
                super.onSomethingWrongLaterClick();
                authInterimScreenHelper.hideIntermediateScreen();
                activityBinding.etMobileNumberInput.setText("");
            }

            @Override
            public void onTryAgainClick() {
                super.onTryAgainClick();
                authInterimScreenHelper.hideIntermediateScreen();
                activityBinding.etMobileNumberInput.setText("");
            }
        });
    }


    public void doOTPStateSSetup() {

        otpScreenHelper = new OTPScreenHelper(activityBinding.parentLayout,
                activityBinding.otpViewStub.getViewStub());

        otpScreenHelper.setOnOTPResultListener(new OTPScreenHelper.OnOTPResultListener() {
            @Override
            public void onSubmitClick(String code) {
                super.onSubmitClick(code);
                otpScreenHelper.hideIntermediateScreen();
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFYING_CODE);
                firebaseMobileAuthenticationService.validateTheCode(code);
            }

            @Override
            public void onResendCodeClick() {
                super.onResendCodeClick();
                if (!isNetworkAvailable(MobileNumberAuthenticationActivity.this, true)) {
                    return;
                }
                firebaseMobileAuthenticationService.initResendToken(countryCode + phoneNumber);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (hintRequestHelper != null && requestCode == RC_HINT_MOBILE)
            hintRequestHelper.onActivityResult(requestCode, resultCode, data);
    }
}
