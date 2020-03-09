package com.droidmentor.firebaseservicesdemo.utils;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.droidmentor.firebaseservicesdemo.R;
import com.droidmentor.firebaseservicesdemo.databinding.OtpIntermediateScreenBinding;


/**
 * Created by Jaison.
 */
public class OTPScreenHelper {
    private static final String TAG = "OTPScreenHelper";

    // Define a Boolean value to mark whether it is inflated
    private boolean isInflated;

    private ViewStub mViewStub;
    private View inflatedOTPScreen;
    private View parentLayout;

    private String mobileNumber;
    private OnOTPResultListener onOTPResultListener;

    private OtpIntermediateScreenBinding otpScreenBinding;
    private View.OnClickListener onClickListener;

    private TextView[] tvInputFields;
    private String passCodeValue = "";
    private int count = 0;
    private CountDownTimer countDownTimer;

    public OTPScreenHelper(View parentLayout, ViewStub mViewStub) {
        this.parentLayout = parentLayout;
        this.mViewStub = mViewStub;
        initView();
    }

    public void showIntermediateStates(boolean isVerificationFailed, String mobileNumber) {

        this.mobileNumber = mobileNumber;

        if (mViewStub != null) {
            inflateView();
            Log.d(TAG, "showIntermediateStates: stub not null");

            TextView tvDescription = inflatedOTPScreen.findViewById(R.id.tvDescription);
            tvDescription.setText(isVerificationFailed ? R.string.authMobileInvalidCode : R.string.authMobileEnterCode);

            if (isVerificationFailed)
                resetInputFields();

            TextView tvMobileNumber = inflatedOTPScreen.findViewById(R.id.tvMobileNumber);
            if (!TextUtils.isEmpty(mobileNumber))
                tvMobileNumber.setText(mobileNumber);

            Button btnResendCode = inflatedOTPScreen.findViewById(R.id.btnResendCode);
            startCountDownTimer(btnResendCode);

            mViewStub.setVisibility(View.VISIBLE);
            parentLayout.setVisibility(View.GONE);
        } else
            Log.d(TAG, "showIntermediateStates: stub null");
    }

    public void hideIntermediateScreen() {
        if (mViewStub != null)
            mViewStub.setVisibility(View.GONE);

        parentLayout.setVisibility(View.VISIBLE);
    }

    private void initView() {
        // Set the listener
        assert mViewStub != null;
        mViewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                otpScreenBinding = DataBindingUtil.bind(inflated);
                otpScreenBinding.setListener(getOnClickListener());

                tvInputFields = new TextView[6];
                tvInputFields[0] = otpScreenBinding.tvCode1;
                tvInputFields[1] = otpScreenBinding.tvCode2;
                tvInputFields[2] = otpScreenBinding.tvCode3;
                tvInputFields[3] = otpScreenBinding.tvCode4;
                tvInputFields[4] = otpScreenBinding.tvCode5;
                tvInputFields[5] = otpScreenBinding.tvCode6;
                isInflated = true;
            }
        });
    }

    private void startCountDownTimer(final Button btnResend) {

        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                btnResend.setText("Resend code to SMS in: " + millisUntilFinished / 1000);
                btnResend.setEnabled(false);
            }

            public void onFinish() {
                btnResend.setText("Resend code to SMS");
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void inflateView() {
        if (!isInflated) {
            //Only inflate when not inflate
            inflatedOTPScreen = mViewStub.inflate();
        }
    }

    public View.OnClickListener getOnClickListener() {
        if (onClickListener == null)
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.btnResendCode:
                            if (onOTPResultListener != null)
                                onOTPResultListener.onResendCodeClick();
                            break;
                        case R.id.btnContinue:
                            if (count == 6) {
                                String code = getPassCodeFromInputFields();

                                if (onOTPResultListener != null)
                                    onOTPResultListener.onSubmitClick(code);
                            } else
                                Log.d(TAG, "onClick: Continue");
                            break;
                        case R.id.btn_bs:
                            if (count > 0) {
                                count--;
                                tvInputFields[count].setText("");

                                if (otpScreenBinding != null)
                                    otpScreenBinding.setIsValidOTP(false);
                            }
                            break;
                        default:
                            if (count < 5) {
                                setInputValue(view);

                                if (otpScreenBinding != null)
                                    otpScreenBinding.setIsValidOTP(false);
                            } else if (count == 5) {
                                setInputValue(view);
                                if (otpScreenBinding != null)
                                    otpScreenBinding.setIsValidOTP(true);
                            }
                            break;
                    }
                }
            };

        return onClickListener;
    }

    /**
     * To get the user click action and set the value into the respective input fields
     *
     * @param view User clicked view
     */
    private void setInputValue(View view) {
        Button btn;
        btn = view.findViewById(view.getId());
        tvInputFields[count].setText(btn.getText());
        count++;
    }

    /**
     * To clear the pass code input fields
     */
    private void resetInputFields() {
        count = 0;
        for (TextView tvInputField : tvInputFields)
            tvInputField.setText("");
    }

    /**
     * To get the pass code value from the user click action
     *
     * @return Passcode value
     */
    private String getPassCodeFromInputFields() {
        StringBuilder codeBuilder = new StringBuilder();

        for (TextView tvInputField : tvInputFields)
            codeBuilder.append(tvInputField.getText().toString().trim());

        return codeBuilder.toString();
    }


    public void setOnOTPResultListener(OnOTPResultListener onOTPResultListener) {
        this.onOTPResultListener = onOTPResultListener;
    }

    public abstract static class OnOTPResultListener {
        public void onSubmitClick(String code) {
        }

        public void onResendCodeClick() {
        }
    }

}
