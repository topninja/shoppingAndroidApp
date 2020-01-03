package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.entage.nrd.entage.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class ExceptionsMessages {
    private static final String TAG = "ExceptionsMessages";

    public static String getExceptionMessage(Context mContext, Exception exception){
        String message = null;

        //FirebaseAuthWeakPasswordException
        if(exception instanceof FirebaseAuthWeakPasswordException){
            message = showExceptionMessage(mContext, (FirebaseAuthWeakPasswordException)exception);
        }

        //FirebaseAuthUserCollisionException
        if(exception instanceof FirebaseAuthUserCollisionException){
            message = showExceptionMessage(mContext, (FirebaseAuthUserCollisionException)exception);
        }

        // FirebaseAuthRecentLoginRequiredException
        if(exception instanceof FirebaseAuthRecentLoginRequiredException){
            message = showExceptionMessage(mContext, (FirebaseAuthRecentLoginRequiredException)exception);
        }

        // if user enters wrong email.
        if(exception instanceof FirebaseAuthInvalidCredentialsException){
            message = showExceptionMessage(mContext, (FirebaseAuthInvalidCredentialsException)exception);
        }

        // if user enters wrong email.
        else if(exception instanceof FirebaseNetworkException){
            message = showExceptionMessage(mContext, (FirebaseNetworkException)exception);
        }

        // Auth Invalid User Exception
        else if(exception instanceof FirebaseAuthInvalidUserException){
            message = showExceptionMessage(mContext, (FirebaseAuthInvalidUserException)exception);
        }


        if(message == null){
            message =exception.getMessage();
        }

        Log.d(TAG, "ExceptionsMessages: " + exception);
        return message;
    }

    public static void showExceptionMessage(Context mContext, Exception exception){
        String message = getExceptionMessage(mContext, exception);;
        errorMessage(mContext, message);
    }

    // FirebaseAuthInvalidCredentialsException
    private static String showExceptionMessage(Context mContext, FirebaseAuthInvalidCredentialsException malformedEmail){
        String message = null;

        if(malformedEmail.getMessage().equals("The email address is badly formatted.")){
            message = mContext.getString(R.string.firebase_error_email_badly_format);

        }
        else if(malformedEmail.getMessage().equals("The password is invalid or the user does not have a password.")){
            message = mContext.getString(R.string.authentication_failed);

        }
        else if (malformedEmail.getMessage().equals("The sms verification code used to create the phone auth credential is invalid. Please resend the verification code sms and be sure use the verification code provided by the user.")){
            message = mContext.getString(R.string.error_verification_code_entered_invalid);
        }

        return message;
    }

    // FirebaseNetworkException
    private static String showExceptionMessage(Context mContext, FirebaseNetworkException malformedEmail){
        String message = null;

        if(malformedEmail.getMessage().equals("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")){
            message = mContext.getString(R.string.network_error_timeout);
        }

        return message;
    }

    // FirebaseAuthInvalidUserException
    private static String showExceptionMessage(Context mContext, FirebaseAuthInvalidUserException malformedEmail){
        String message = null;

        if(malformedEmail.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")){
            message = mContext.getString(R.string.firebase_error_no_account_with_this_email);
        }

        else if(malformedEmail.getMessage().equals("The user account has been disabled by an administrator.")){
            message = mContext.getString(R.string.user_account_has_been_disabled);
        }

        else if(malformedEmail.getMessage().equals("This credential is already associated with a different user account.")){
            message = mContext.getString(R.string.firebase_error_credential_exist);
        }

        return message;
    }

    // FirebaseAuthWeakPasswordException
    private static String showExceptionMessage(Context mContext, FirebaseAuthWeakPasswordException malformedEmail){
        String message = null;

        if(malformedEmail.getMessage().equals("The given password is invalid. [ Password should be at least 6 characters ]")){
            message = mContext.getString(R.string.error_password_weak);
        }

        return message;
    }

    // FirebaseAuthUserCollisionException
    private static String showExceptionMessage(Context mContext, FirebaseAuthUserCollisionException malformedEmail){
        String message = null;

        if(malformedEmail.getMessage().equals("The email address is already in use by another account.")){
            message = mContext.getString(R.string.error_email_token);
        }

        return message;
    }

    // FirebaseAuthUserCollisionException
    private static String showExceptionMessage(Context mContext, FirebaseAuthRecentLoginRequiredException malformedEmail){
        String message = null;

        //if(malformedEmail.getMessage().equals("The email address is already in use by another account.")){
            message = mContext.getString(R.string.firebase_error_change_phone);
        //}

        return message;
    }


    //
    public static void errorMessage(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBlue);
        builder.setTitle(context.getString(R.string.happened_wrong_title));
        builder.setMessage(message);
        builder.setNegativeButton(context.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

}
