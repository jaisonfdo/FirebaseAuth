package com.droidmentor.firebaseservicesdemo.utils;

/**
 * Created by Jaison.
 */
public class CustomDataTypes {

    public enum VerificationInterimState {
        SENT, VERIFIED, VERIFYING_EMAIL, VERIFYING_MOBILE, VERIFYING_CODE, MAIL_VERIFICATION_PENDING,
        INVALID_PHONE_NUMBER, TOO_MANY_CODE_REQUEST, INVALID_EMAIL, SOMETHING_WRONG, ALREADY_LOGGED_IN
    }
}
