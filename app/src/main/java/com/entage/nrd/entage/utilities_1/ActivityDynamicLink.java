package com.entage.nrd.entage.utilities_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;


public class ActivityDynamicLink extends AppCompatActivity {
    private static final String TAG = "ActivityDynamicLink";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entage_options);

        inti();
        //String dynamicLink = getIntent().getData().toString();
        //Log.d(TAG, "DynamicLink: " + dynamicLink);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();

                        }else {
                            Toast.makeText(ActivityDynamicLink.this,  getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        if (deepLink != null) {
                            Log.d(TAG, "onSuccess: " + deepLink);
                            getLink_openActivity(deepLink.toString());

                        }else {
                            Toast.makeText(ActivityDynamicLink.this,  getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "getDynamicLink:onFailure", e);
                        Toast.makeText(ActivityDynamicLink.this,  getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void inti(){
        ImageView back = findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getLink_openActivity(String deepLink){
        // entaji page or item : https://entaji.store/item/-L_wVytql8ogwn4ye56I
        // search : https://entaji.store/search/item/جوال_سامسونج
        String id = deepLink.replace(getString(R.string.app_link),""); // remove https://entaji.store/
        id = id.replace(getString(R.string.app_link_entajipage),""); // remove entajipage/
        id = id.replace(getString(R.string.app_link_item),""); // remove item/
        id = id.replace(getString(R.string.app_link_search),""); // remove search/


        Notification notification = new Notification(id, id, null, null,
                null, null, null, null, null, null, null, null);

        if(deepLink.contains(getString(R.string.app_link_entajipage))){ // search
            notification.setFlag(getString(R.string.notif_flag_open_entaji_page));
        }

        else if(deepLink.contains(getString(R.string.app_link_item))){ // entaji page or item
            notification.setFlag(getString(R.string.notif_flag_open_item));
        }

        else if(deepLink.contains(getString(R.string.app_link_search))){ // entaji page or item
            notification.setFlag(getString(R.string.notif_flag_open_search_page));
        }


        if(notification.getFlag() != null){
            //Log.d(TAG, "getLink_openActivity: " + id);
            Intent intent = new Intent(ActivityDynamicLink.this, ViewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("notification", notification);
            startActivity(intent);
            finish();

        }else {
            Toast.makeText(this,  getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
            Intent intent = bringCurrentActivityToFront();
            startActivity(intent);
            finish();
        }
    }

    private Intent bringCurrentActivityToFront(){
        Intent notificationIntent = null;
        notificationIntent = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        notificationIntent.setPackage(null); // The golden row !!!
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return notificationIntent;
    }

}
