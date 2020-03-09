package com.droidmentor.firebaseservicesdemo.mail;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.droidmentor.firebaseservicesdemo.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Created by Jaison.
 */
public class FirebaseMailAuthenticationService {

    private static final String TAG = "FBMailAuthentication";

    private FirebaseAuth mAuth;
    private ActionCodeSettings actionCodeSettings;

    private MailAuthenticationListener mailAuthenticationListener;
    private Activity activity;


    public FirebaseMailAuthenticationService(Activity activity, MailAuthenticationListener mailAuthenticationListener) {
        this.mailAuthenticationListener = mailAuthenticationListener;
        this.activity = activity;
        initFirebaseAuthentication();
    }

    private void initFirebaseAuthentication() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("en");

        actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        .setUrl("https://droidmentor.com/finishSignUp")
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(BuildConfig.APPLICATION_ID, false, null)
                        .build();
    }


    public void initMailAuthenticationFlow(String emailID) {
        mAuth.sendSignInLinkToEmail(emailID, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: ");
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            if (mailAuthenticationListener != null)
                                mailAuthenticationListener.onVerificationEmailSent(task);
                        } else {
                            Objects.requireNonNull(task.getException()).printStackTrace();
                            if (mailAuthenticationListener != null)
                                mailAuthenticationListener.onVerificationFailed(task.getException());
                        }
                    }
                });
    }

    public void signInWithEmailLink(String emailID, String emailLink) {
        // Confirm the link is a sign-in with email link.
        if (mAuth.isSignInWithEmailLink(emailLink)) {

            // The client SDK will parse the code from the link for you.
            mAuth.signInWithEmailLink(emailID, emailLink)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Successfully signed in with email link!");
                                if (task.getResult() != null) {
                                    FirebaseUser user = task.getResult().getUser();
                                    if (user != null) {
                                        if (!TextUtils.isEmpty(user.getEmail()))
                                            Log.d(TAG, "signInWithCredential: " + user.getEmail());

                                        if (!TextUtils.isEmpty(user.getUid()))
                                            Log.d(TAG, "signInWithCredential: " + user.getUid());

                                        if (!TextUtils.isEmpty(user.getProviderId()))
                                            Log.d(TAG, "signInWithCredential: " + user.getProviderId());

                                        if (mailAuthenticationListener != null)
                                            mailAuthenticationListener.onSignInComplete(user);
                                    }

                                }
                                // You can access the new user via result.getUser()
                                // Additional user info profile *not* available via:
                                // result.getAdditionalUserInfo().getProfile() == null
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()
                            } else {
                                Log.e(TAG, "Error signing in with email link", task.getException());
                                if (mailAuthenticationListener != null) {
                                    if (task.getException() instanceof FirebaseAuthActionCodeException) {
                                        mailAuthenticationListener.onInvalidEmailRedirection(task.getException());
                                        // The verification code entered was invalid
                                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                                    } else
                                        mailAuthenticationListener.onSignInFailed(task.getException());
                                }
                            }
                        }
                    });
        }
    }


    public abstract static class MailAuthenticationListener {

        public abstract void onVerificationEmailSent(Task<Void> task);

        void onVerificationFailed(@NonNull Exception e) {

        }

        public abstract void onSignInComplete(FirebaseUser user);

        public abstract void onInvalidEmailRedirection(Exception exception);

        void onSignInFailed(Exception e) {

        }
    }

}
