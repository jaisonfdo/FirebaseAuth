package com.droidmentor.firebaseservicesdemo.utils;

/**
 * Created by Jaison.
 */

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility methods for manipulating the onscreen keyboard
 */
public class KeyboardUtil {

    // Class is not instantiable
    private KeyboardUtil() {
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        assert inputMethodManager != null;
        inputMethodManager.showSoftInput(view, 0);
    }
}
