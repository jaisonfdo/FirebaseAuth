package com.droidmentor.firebaseservicesdemo.mail;

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

import com.droidmentor.firebaseservicesdemo.MainActivity;
import com.droidmentor.firebaseservicesdemo.R;
import com.droidmentor.firebaseservicesdemo.databinding.ActivityMailAuthenticationBinding;
import com.droidmentor.firebaseservicesdemo.utils.AuthInterimScreenHelper;
import com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState;
import com.droidmentor.firebaseservicesdemo.utils.HintRequestHelper;
import com.droidmentor.firebaseservicesdemo.utils.HintRequestHelper.OnHintRequestResultListener;
import com.droidmentor.firebaseservicesdemo.utils.KeyboardUtil;
import com.droidmentor.firebaseservicesdemo.utils.LocalData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Log.d;
import static com.droidmentor.firebaseservicesdemo.MainActivity.BUNDLE_KEY_EMAIL_LINK;
import static com.droidmentor.firebaseservicesdemo.MainActivity.BUNDLE_KEY_PAYLOAD;
import static com.droidmentor.firebaseservicesdemo.MainActivity.BUNDLE_KEY_UNIQUE_ID;
import static com.droidmentor.firebaseservicesdemo.utils.HintRequestHelper.RC_HINT_EMAIL;
import static com.droidmentor.firebaseservicesdemo.utils.LocalData.TEMP_EMAIL_ID;
import static com.droidmentor.firebaseservicesdemo.utils.LocalData.USER_ID;
import static com.droidmentor.firebaseservicesdemo.utils.NetworkUtil.isNetworkAvailable;

public class MailAuthenticationActivity extends AppCompatActivity {

    private static final String TAG = "MailAuthenticationActiv";
    public static int LOTTIE_ANIMATION_DELAY = 2000;

    ActivityMailAuthenticationBinding mailAuthenticationBinding;
    LocalData localData;
    HintRequestHelper hintRequestHelper;
    AuthInterimScreenHelper authInterimScreenHelper;

    FirebaseMailAuthenticationService firebaseMailAuthenticationService;

    String mailID;
    String storedEmail = "jaisonfdo@gmail.com";
    private boolean isVerificationMailRedirection;

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doActionBarSetup();
        mailAuthenticationBinding = DataBindingUtil.setContentView(this, R.layout.activity_mail_authentication);
        localData = new LocalData(this);
        doFirebaseMailServiceSetup();
        doInterimStateSetup();
        doViewPortSetup();

        if (!TextUtils.isEmpty(localData.getString(USER_ID))) {
            authInterimScreenHelper.showIntermediateStates(VerificationInterimState.ALREADY_LOGGED_IN);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent homePageIntent = new Intent(MailAuthenticationActivity.this, MainActivity.class);
                    homePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homePageIntent);
                    finish();
                }
            }, LOTTIE_ANIMATION_DELAY);

            return;
        } else {
            isVerificationMailRedirection = performEmailverificationRedirection();

            if (isVerificationMailRedirection) {
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFYING_EMAIL);
                return;
            }
        }

        doHintRequestSetup();
    }

    public void doActionBarSetup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                upArrow.setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(this, R.color.loginAccountKitThemeColorAccent), BlendMode.SRC_ATOP));
            } else {
                upArrow.setColorFilter(ContextCompat.getColor(this, R.color.loginAccountKitThemeColorAccent), PorterDuff.Mode.SRC_ATOP);
            }
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    public void doViewPortSetup() {
        mailAuthenticationBinding.etMailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim()) && isEmailValid(s.toString().trim())) {
                    d(TAG, "onTextChanged: valid email");
                    mailID = s.toString().trim();
                    mailAuthenticationBinding.setIsValidEmail(true);
                } else {
                    d(TAG, "onTextChanged: Invalid email");
                    mailAuthenticationBinding.setIsValidEmail(false);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mailAuthenticationBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable(MailAuthenticationActivity.this, true)) {
                    return;
                }

                if (firebaseMailAuthenticationService != null) {
                    if (!TextUtils.isEmpty(mailID)) {
                        storedEmail = mailID;
                        firebaseMailAuthenticationService.initMailAuthenticationFlow(mailID);
                        KeyboardUtil.hideSoftKeyboard(MailAuthenticationActivity.this);
                        authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFYING_EMAIL);
                    }
                }
            }
        });
    }

    public void doHintRequestSetup() {
        // To show the registered Email listing

        hintRequestHelper = new HintRequestHelper(this, new OnHintRequestResultListener() {
            @Override
            public void onHintResult(String payload) {
                if (!TextUtils.isEmpty(payload)) {
                    mailAuthenticationBinding.etMailInput.setText(payload);

                    mailAuthenticationBinding.etMailInput.post(new Runnable() {
                        @Override
                        public void run() {
                            mailAuthenticationBinding.etMailInput.setSelection(Objects.requireNonNull(mailAuthenticationBinding.etMailInput.getText()).length());
                        }
                    });
                }
            }
        });

        hintRequestHelper.emailSuggestions();

    }

    public void doInterimStateSetup() {
        authInterimScreenHelper = new AuthInterimScreenHelper(mailAuthenticationBinding.parentLayout,
                mailAuthenticationBinding.intermediateViewStub.getViewStub());

        authInterimScreenHelper.setOnInterimResultListener(new AuthInterimScreenHelper.OnInterimResultListener() {
            @Override
            public void onOpenMailClick() {
                super.onOpenMailClick();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                startActivity(intent);
            }

            @Override
            public void onResendMailClick() {
                super.onResendMailClick();
                authInterimScreenHelper.hideIntermediateScreen();
                mailAuthenticationBinding.btnContinue.setText(R.string.authSendNewMail);
            }

            @Override
            public void onTryAgainClick() {
                super.onTryAgainClick();
                authInterimScreenHelper.hideIntermediateScreen();
                if (TextUtils.isEmpty(storedEmail)) {
                    mailAuthenticationBinding.btnContinue.setText(R.string.authNext);
                    mailAuthenticationBinding.etMailInput.setText("");
                } else {
                    mailAuthenticationBinding.btnContinue.setText(R.string.authSendNewMail);
                    mailAuthenticationBinding.etMailInput.setText(storedEmail);
                }
            }

            @Override
            public void onSomethingWrongLaterClick() {
                super.onSomethingWrongLaterClick();
                onBackPressed();
            }
        });

    }

    public void doFirebaseMailServiceSetup() {
        firebaseMailAuthenticationService = new FirebaseMailAuthenticationService(this, new FirebaseMailAuthenticationService.MailAuthenticationListener() {
            @Override
            public void onVerificationEmailSent(Task<Void> task) {
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.SENT);
                localData.setString(TEMP_EMAIL_ID, storedEmail);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        authInterimScreenHelper.showIntermediateStates(VerificationInterimState.MAIL_VERIFICATION_PENDING);
                    }
                }, LOTTIE_ANIMATION_DELAY);
            }

            @Override
            public void onVerificationFailed(@NonNull Exception e) {
                super.onVerificationFailed(e);
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.SOMETHING_WRONG);
            }

            @Override
            public void onSignInFailed(@NonNull Exception e) {
                super.onSignInFailed(e);
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.SOMETHING_WRONG);
            }

            @Override
            public void onSignInComplete(final FirebaseUser user) {
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.VERIFIED);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isVerificationMailRedirection && isTaskRoot()) {
                            // To complete the login flow
                            Intent homePageIntent = new Intent(MailAuthenticationActivity.this, MainActivity.class);
                            homePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            homePageIntent.putExtra(BUNDLE_KEY_UNIQUE_ID, user.getUid());
                            homePageIntent.putExtra(BUNDLE_KEY_PAYLOAD, user.getEmail());
                            startActivity(homePageIntent);
                            finish();
                        } else {
                            // To complete the login flow
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(BUNDLE_KEY_UNIQUE_ID, user.getUid());
                            returnIntent.putExtra(BUNDLE_KEY_PAYLOAD, user.getEmail());
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                }, LOTTIE_ANIMATION_DELAY);

            }

            @Override
            public void onInvalidEmailRedirection(Exception exception) {
                Log.d(TAG, "onInvalidEmailRedirection: ");
                authInterimScreenHelper.showIntermediateStates(VerificationInterimState.INVALID_EMAIL);
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

        if (hintRequestHelper != null && requestCode == RC_HINT_EMAIL)
            hintRequestHelper.onActivityResult(requestCode, resultCode, data);
    }

    public boolean performEmailverificationRedirection() {
        Bundle payload = getIntent().getExtras();
        String emailLink = "";


        if (payload != null) {
            emailLink = payload.getString(BUNDLE_KEY_EMAIL_LINK, "");
        }

        if (!TextUtils.isEmpty(emailLink) && firebaseMailAuthenticationService != null) {
            Log.d(TAG, "performEmailverificationRedirection: success");
            storedEmail = localData.getString(TEMP_EMAIL_ID);
            if (!TextUtils.isEmpty(storedEmail)) {
                firebaseMailAuthenticationService.signInWithEmailLink(storedEmail, emailLink);
                return true;
            }
        } else
            Log.d(TAG, "performEmailverificationRedirection: fails");

        return false;
    }
}
