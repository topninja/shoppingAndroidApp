package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.entage.nrd.entage.R;

public class MessageDialog {

    public MessageDialog() {
    }

    public void errorMessage(Context context, String message){
        if(context != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBlue);
            builder.setTitle(context.getString(R.string.happened_wrong_title));
            builder.setMessage(message);
            builder.setNegativeButton(context.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void errorMessage(Context context, String title, String message){
        if(context != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBlue);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setNegativeButton(context.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void errorMessage(Context context, View view){
        if(context != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBlue);
            builder.setView(view);
            builder.setNegativeButton(context.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
