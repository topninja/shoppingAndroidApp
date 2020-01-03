package com.entage.nrd.entage.utilities_1;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.entage.nrd.entage.home.FragmentHome;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    private static final String NOTIFICATION_CHANNEL_ID = "com.entage.nrd.entage.test";

    public static final String GROUP_KEY_1 = "GROUP_KEY_1";

    public static final String GROUP_KEY_2 = "GROUP_KEY_2";

    public static final int NOTIFY_ID = 99;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: " + s);
        getSharedPreferences("token_id", MODE_PRIVATE).edit().putString("token_id", s).apply();
        /*FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            if(!firebaseUser.isAnonymous()){
                String id = firebaseUser.getUid();
                FirebaseDatabase.getInstance().getReference().child("users_token").child(id).setValue(s);
            }
        }*/
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("token_id", MODE_PRIVATE).getString("token_id", null);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        /* run in foreground or background or closed  */

        if (remoteMessage.getData().size() > 0) {
            //this data message was sent from our cloud function
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            setupNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData());

        }else {
            if (remoteMessage.getNotification() != null) {
                //this notification was sent from the Firebase console
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

                if(remoteMessage.getData().isEmpty()){
                    setupNotification(remoteMessage.getNotification().getTitle() ,remoteMessage.getNotification().getBody(), null);

                }else {
                    //this data message was sent from our cloud function
                    setupNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData());
                }
            }

        }
    }

    private void setupNotification(String title, String body, Map<String, String> data){
        Log.d(TAG, "setupNotification: " + data.toString());

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        String flag = data.get("flag");
        String group_key;
        if(flag != null){
            if(flag.equals(getString(R.string.notif_flag_new_message)) || flag.equals(getString(R.string.notif_flag_new_order)) ||
                    flag.equals(getString(R.string.notif_flag_open_chatting))){
                group_key = GROUP_KEY_1;
            }else {
                group_key = GROUP_KEY_2;
            }

            if(flag.equals(getString(R.string.notif_flag_new_message))){
                ConversationMessages.init(this);
                String text = ConversationMessages.getMessageText(body);
                body = text.length()>0? "- " + text : body;


                if(data.get("extra_data") != null &&  FragmentHome.isAlive && FragmentHome.obsInt != null ) {
                    if (data.get("extra_data").equals("entagePageToUser")) {
                        FragmentHome.obsInt.set(1, false);
                    } else if (data.get("extra_data").equals("UserToEntagePage")) {
                        FragmentHome.obsInt.set(1, true);
                    }
                }

            }else {
                body = UtilitiesMethods.getMessage(this,body, null);
            }



            String notifyTag = "entage";
            if(data.get("notify_id") != null){
                notifyTag = data.get("notify_id");
            }


            nBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setSmallIcon(R.drawable.ic_entage_select)
                    .setColor(this.getResources().getColor(R.color.entage_blue))
                    .setContentTitle(UtilitiesMethods.getMessage(this, title, data.get("extra_data")))
                    .setContentText(body)
                    .setGroup(group_key)
                    .setContentInfo("Info");

            if(data.get("img_url") != null && !data.get("img_url").equals("-1")){
                nBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(loadBitmap(data.get("img_url")))
                        /*.bigLargeIcon(null)*/);
            }

            showNotification(nBuilder, data, notifyTag, group_key);
        }
    }

    private void showNotification(NotificationCompat.Builder nBuilder, Map<String, String> data ,String notifyTag, String group_key){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Entage Channel");
            notificationChannel.setLightColor(R.color.entage_blue);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableLights(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }


        //Log.d(TAG, "showNotification: data:" + data);
        if(data != null){
            Intent notificationIntent = chickFlag(data, isAppRunning());

            if(notificationIntent != null){
                PendingIntent intent = null;

                intent = PendingIntent.getActivity(this, 0,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                nBuilder.setContentIntent(intent);
            }
        }


        //new Random().nextInt();
        notificationManager.notify(notifyTag, NOTIFY_ID, nBuilder.build());
        summaryNotification(notificationManager, group_key);
    }

    private void summaryNotification(NotificationManager notificationManager, String grpupKey) {
        StatusBarNotification[] statusBarNotification = notificationManager.getActiveNotifications();
        int countGroup_1 = 0;
        int countGroup_2 = 0;
        for(StatusBarNotification notification : statusBarNotification){
            String key = notification.getGroupKey();
            if(key != null){
                if(key.contains(GROUP_KEY_1)){
                    countGroup_1++;
                }else {
                    countGroup_2++;
                }
            }
        }
        //Log.d(TAG, "showNotification: countGroup_1: " + countGroup_1 +", countGroup_2: " + countGroup_2);
        if((grpupKey.equals(GROUP_KEY_1) && countGroup_1 == 1) || (grpupKey.equals(GROUP_KEY_2) && countGroup_2 == 1) ){

            NotificationCompat.Builder newMessageNotification1 =
                    new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                            /*.setContentTitle("Entaji")
                            //set content text to support devices running API level < 24
                            .setContentText("Two new messages")*/
                            .setSmallIcon(R.drawable.ic_entage_select)
                            .setColor(this.getResources().getColor(R.color.entage_blue))
                            //build summary info into InboxStyle template
                            /*.setStyle(new NotificationCompat.InboxStyle()
                            .addLine("Alex Faarborg  Check this out")
                            .addLine("Jeff Chang    Launch Party")
                            .setBigContentTitle("2 new messages")
                            .setSummaryText("janedoe@example.com"))*/

                            //specify which group this notification belongs to
                            .setGroup(grpupKey)
                            //set this notification as the summary for the group
                            .setGroupSummary(true);

            notificationManager.notify(grpupKey, NOTIFY_ID, newMessageNotification1.build());
        }
    }

    private Intent chickFlag(Map<String, String> data, boolean isAppRunning){
        Intent notificationIntent = null;

        String flag = data.get("flag");
        String entage_page_id = data.get("entage_page_id"); // can be item id
        String item_id = data.get("item_id"); // can be question_id, order_id, message_id, item id
        String extra_data = data.get("extra_data");

        com.entage.nrd.entage.Models.Notification notification = new com.entage.nrd.entage.Models.Notification
                (entage_page_id, item_id, null, null, null, null, flag,
                        null, null, extra_data, null, null);

        if(flag.equals("-1")){
            if(!isAppRunning){
                notificationIntent = new Intent(this, MainActivity.class);
            }

            else {
                notificationIntent = bringCurrentActivityToFront();
            }

        }else {
            notificationIntent = new Intent(this, ViewActivity.class);
            if(!isAppRunning){
                notificationIntent.setAction(Long.toString(System.currentTimeMillis()));
            }

            notificationIntent.putExtra("notification", notification);
        }

        return notificationIntent;
    }

    private Intent bringCurrentActivityToFront(){
        Intent notificationIntent = null;
        notificationIntent = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        notificationIntent.setPackage(null); // The golden row !!!
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return notificationIntent;
    }

    public boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    private boolean isAppRunning() {
        ActivityManager m = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n=0;
        while(itr.hasNext()){
            n++;
            itr.next();
        }
        if(n==1){ // App is killed
            return false;
        }

        return true; // App is in background or foreground
    }

    public Bitmap loadBitmap(String url) {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);
            bm = BitmapFactory.decodeStream(bis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }

}