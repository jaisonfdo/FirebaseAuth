package com.droidmentor.firebaseservicesdemo.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ContentLoadingProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.droidmentor.firebaseservicesdemo.R;
import com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState;

import static com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState.INVALID_EMAIL;
import static com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState.INVALID_PHONE_NUMBER;
import static com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState.MAIL_VERIFICATION_PENDING;
import static com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState.SOMETHING_WRONG;
import static com.droidmentor.firebaseservicesdemo.utils.CustomDataTypes.VerificationInterimState.TOO_MANY_CODE_REQUEST;


/**
 * Created by Jaison.
 */
public class AuthInterimScreenHelper {
    private static final String TAG = "AuthInterimScreenHelper";

    // Define a Boolean value to mark whether it is inflated
    private boolean isInflated;

    private ViewStub mViewStub;
    private View inflatedIntermediateScreen;
    private View parentLayout;
    private OnInterimResultListener onInterimResultListener;

    public AuthInterimScreenHelper(View parentLayout, ViewStub mViewStub) {
        this.parentLayout = parentLayout;
        this.mViewStub = mViewStub;
        initView();
    }

    public void setOnInterimResultListener(OnInterimResultListener onInterimResultListener) {
        this.onInterimResultListener = onInterimResultListener;
    }

    public void showIntermediateStates(final VerificationInterimState interimState) {

        if (mViewStub != null) {
            inflateView();
            Log.d(TAG, "showIntermediateStates: stub not null");


            TextView tvDescription = inflatedIntermediateScreen.findViewById(R.id.tvDescription);
            TextView tvFooter = inflatedIntermediateScreen.findViewById(R.id.tvFooter);
            tvFooter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onInterimResultListener != null)
                        if (interimState == MAIL_VERIFICATION_PENDING)
                            onInterimResultListener.onResendMailClick();
                }
            });
            ContentLoadingProgressBar loader = inflatedIntermediateScreen.findViewById(R.id.loadingProgressbar);
            ImageView ivState = inflatedIntermediateScreen.findViewById(R.id.ivVerificationState);
            LottieAnimationView animationView = inflatedIntermediateScreen.findViewById(R.id.animationView);
            Button btnContinue = inflatedIntermediateScreen.findViewById(R.id.btnContinue);
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onInterimResultListener != null)
                        if (interimState == INVALID_PHONE_NUMBER || interimState == INVALID_EMAIL)
                            onInterimResultListener.onTryAgainClick();
                        else if (interimState == MAIL_VERIFICATION_PENDING)
                            onInterimResultListener.onOpenMailClick();
                        else if (interimState == TOO_MANY_CODE_REQUEST)
                            onInterimResultListener.onLaterClick();
                        else if (interimState == SOMETHING_WRONG)
                            onInterimResultListener.onSomethingWrongLaterClick();
                }
            });

            // Change controls visibility based on the states
            switch (interimState) {
                case SENT:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authSent);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("success_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_verify);
                    loader.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case VERIFYING_MOBILE:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMobileVerifying);
                    loader.setVisibility(View.VISIBLE);
                    ivState.setVisibility(View.GONE);
                    animationView.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case VERIFYING_CODE:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authCodeVerifying);
                    loader.setVisibility(View.VISIBLE);
                    ivState.setVisibility(View.GONE);
                    animationView.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case VERIFIED:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authVerified);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("success_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_verify);
                    loader.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case INVALID_PHONE_NUMBER:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMobileValidNumber);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("warning_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_error);
                    loader.setVisibility(View.INVISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);
                    btnContinue.setText(R.string.authTryAgain);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case TOO_MANY_CODE_REQUEST:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMobileCodeManyRequest);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("warning_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_error);
                    loader.setVisibility(View.INVISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);
                    btnContinue.setText(R.string.authMobileLater);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case VERIFYING_EMAIL:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMailVerifying);
                    loader.setVisibility(View.VISIBLE);
                    animationView.setVisibility(View.GONE);
                    ivState.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case ALREADY_LOGGED_IN:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMailAlreadyLoggedIn);
                    loader.setVisibility(View.VISIBLE);
                    animationView.setVisibility(View.GONE);
                    ivState.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case MAIL_VERIFICATION_PENDING:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMailOpenMailBoxDescription);
                    animationView.setVisibility(View.GONE);
                    ivState.setVisibility(View.VISIBLE);
                    ivState.setImageResource(R.drawable.ic_email);
                    loader.setVisibility(View.INVISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);
                    btnContinue.setText(R.string.authMailOpenMailBox);
                    tvFooter.setVisibility(View.VISIBLE);
                    tvFooter.setText(R.string.authMailResend);
                    break;
                case INVALID_EMAIL:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authMailInvalid);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("warning_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_error);
                    loader.setVisibility(View.INVISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);
                    btnContinue.setText(R.string.authTryAgain);
                    tvFooter.setVisibility(View.GONE);
                    break;
                case SOMETHING_WRONG:
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.authSomethingWrong);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("warning_animation.json");
                    animationView.playAnimation();
                    ivState.setVisibility(View.GONE);
                    ivState.setImageResource(R.drawable.ic_error);
                    loader.setVisibility(View.INVISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);
                    btnContinue.setText(R.string.authTryAgain);
                    tvFooter.setVisibility(View.GONE);
                    break;
            }

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
                isInflated = true;
            }
        });
    }

    private void inflateView() {
        if (!isInflated) {
            //Only inflate when not inflate
            inflatedIntermediateScreen = mViewStub.inflate();
        }
    }

    public abstract static class OnInterimResultListener {
        public void onTryAgainClick() {
        }

        public void onOpenMailClick() {
        }

        public void onResendMailClick() {
        }

        public void onLaterClick() {
        }

        public void onSomethingWrongLaterClick() {
        }
    }
}
