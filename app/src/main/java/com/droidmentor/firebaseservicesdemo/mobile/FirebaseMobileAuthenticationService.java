package com.droidmentor.firebaseservicesdemo.mobile;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

import java.util.concurrent.TimeUnit;

/**
 * Created by Jaison.
 */
public class FirebaseMobileAuthenticationService {

    private static final String TAG = "FBMobileAuthentication";

    private FirebaseAuth mAuth;

    private OnVerificationStateChangedCallbacks verificationCallback;
    private MobileAuthenticationListener mobileAuthenticationListener;
    private Activity activity;

    private String mVerificationId;
    private ForceResendingToken resendingToken;

    public FirebaseMobileAuthenticationService(Activity activity, MobileAuthenticationListener mobileAuthenticationListener) {
        this.mobileAuthenticationListener = mobileAuthenticationListener;
        this.activity = activity;
        initFirebaseAuthentication();
    }

    private void initFirebaseAuthentication() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("en");
    }

    public void initMobileAuthenticationFlow(String mobileNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                getVerificationCallback());        // OnVerificationStateChangedCallbacks
    }

    public void initResendToken(String mobileNumber) {
        if (resendingToken != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    mobileNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    activity,               // Activity (for callback binding)
                    getVerificationCallback(), resendingToken);        // OnVerificationStateChangedCallbacks
        }
    }

    public void validateTheCode(String code) {
        Log.d(TAG, "validateTheCode: " + code);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            if (task.getResult() != null) {
                                FirebaseUser user = task.getResult().getUser();
                                if (user != null && !TextUtils.isEmpty(user.getPhoneNumber())) {
                                    mobileAuthenticationListener.onSignInComplete(user);
                                    Log.d(TAG, "signInWithCredential: " + user.getPhoneNumber());
                                }
                            }
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (mobileAuthenticationListener != null)
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // Invalid request
                                    Log.d(TAG, "onVerificationFailed: Invalid");
                                    mobileAuthenticationListener.onInvalidCredentialFailure(task.getException());
                                } else if (task.getException() instanceof FirebaseTooManyRequestsException) {
                                    // The SMS quota for the project has been exceeded
                                    mobileAuthenticationListener.onTooManyRequestsFailure(task.getException());
                                    Log.d(TAG, "onVerificationFailed: FirebaseTooManyRequestsException");
                                } else
                                    mobileAuthenticationListener.onSignInFailed(task.getException());
                        }
                    }
                });
    }

    private OnVerificationStateChangedCallbacks getVerificationCallback() {
        if (verificationCallback == null)
            verificationCallback = new OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);

                    if (mobileAuthenticationListener != null)
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "onVerificationFailed: Invalid" + e.getLocalizedMessage());
                            if (e.getLocalizedMessage().contains("The format of the phone number provided is incorrect."))
                                mobileAuthenticationListener.onInvalidPhoneNumber(e);
                            else
                                mobileAuthenticationListener.onInvalidCredentialFailure(e);
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            mobileAuthenticationListener.onTooManyRequestsFailure(e);
                            Log.d(TAG, "onVerificationFailed: FirebaseTooManyRequestsException");
                        } else
                            mobileAuthenticationListener.onVerificationFailed(e);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull ForceResendingToken token) {

                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.

                    Log.d(TAG, "onCodeSent:" + verificationId);
                    mobileAuthenticationListener.onCodeSent(verificationId, token);

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    resendingToken = token;
                }
            };

        return verificationCallback;
    }

    public abstract static class MobileAuthenticationListener {

        public void onVerificationFailed(@NonNull Exception e) {

        }

        public abstract void onTooManyRequestsFailure(@NonNull Exception e);

        public abstract void onInvalidCredentialFailure(@NonNull Exception e);

        public abstract void onInvalidPhoneNumber(@NonNull Exception e);

        public abstract void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token);

        public abstract void onSignInComplete(FirebaseUser user);

        public void onSignInFailed(Exception e) {

        }
    }
}
