package com.droidmentor.firebaseservicesdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by Jaison.
 */
public class NetworkUtil {
    public static String networkErrorMessage = "No Internet Connection";
    public static boolean checkInternetConnection = true;

    /**
     * A method created to check whether Internet Connection available or not
     *
     * @param context a param has the context of current activity
     * @return it returns true if mobile has the internet connection
     */
    public static boolean isNetworkAvailable(Context context, boolean showErrorMessage) {

        if (checkInternetConnection) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
                return true;
            else {
                if (showErrorMessage)
                    Toast.makeText(context, networkErrorMessage, Toast.LENGTH_SHORT).show();

                return false;
            }
        } else
            return true;

    }

}
