package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;


public class SharingLink {
    private static final String TAG = "SharingLink";


    private  Context context;
    private String text, type, link, titleSocial, description, imageUrl, msgUser, itemId;

    // create new link and send
    public SharingLink(Context context, String type, String text, String titleSocial, String description, String imageUrl,
                       String msgUser, String itemId) {
        this.context = context;
        this.text = text;
        this.titleSocial = titleSocial;
        this.description = description;
        this.imageUrl = imageUrl;
        this.msgUser = msgUser;
        this.itemId = itemId;
        this.type = type;

        create_sendingShareLink();
    }

    // create new link entaji page or item
    public SharingLink(Context context, String type, String text, String titleSocial, String description, String imageUrl, String itemId) {
        this.context = context;
        this.text = text;
        this.titleSocial = titleSocial;
        this.description = description;
        this.imageUrl = imageUrl;
        this.itemId = itemId;
        this.type = type;

        shareLink();
    }

    // if I have the link
    public SharingLink(Context context, String link, String msgUser) {
        this.context = context;
        this.link = link;
        this.msgUser = msgUser;

        sendingShareLink();
    }

    private Uri buildDynamicLink(){

        String your_deep_link = context.getString(R.string.app_link);

        if(text != null){
            if(type.equals(context.getString(R.string.field_search))){  // search
                text = text.replace(" ","_");
                your_deep_link += context.getString(R.string.app_link_search);
                /*if(type.equals(context.getString(R.string.field_entage_page))){ // entaji-page
                    your_deep_link +=  context.getString(R.string.app_link_entajipage) + text;

                }else if(type.equals(context.getString(R.string.field_item))){  // item
                    your_deep_link += context.getString(R.string.app_link_item) + text;
                }*/
            }

        }else {
            if(type.equals(context.getString(R.string.field_entage_page))){ // entaji-page
                your_deep_link += context.getString(R.string.app_link_entajipage) + itemId;

            }else if(type.equals(context.getString(R.string.field_item))){  // item
                your_deep_link += context.getString(R.string.app_link_item) + itemId;
            }
        }

        String your_subdomain = "https://testentaji.page.link";
        String package_name = "com.entage.nrd.entage";
        String minimum_version = "1.0.9";
        String fallback_link = "fallback_link";

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(your_deep_link)) // https://www.example.com/
                .setDomainUriPrefix(your_subdomain)

                .setAndroidParameters( new DynamicLink.AndroidParameters.Builder(package_name)
                                .setMinimumVersion(0)
                                .build())

                /*.setIosParameters( new DynamicLink.IosParameters.Builder("com.example.ios")
                                .setAppStoreId("123456789")
                                .setMinimumVersion("1.0.1")
                                .build())*/

                /*.setGoogleAnalyticsParameters(new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())*/

                /*.setItunesConnectAnalyticsParameters( new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                .setProviderToken("123456")
                                .setCampaignToken("example-promo")
                                .build())*/

                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(titleSocial)
                                .setDescription(description)
                                .setImageUrl(Uri.parse(imageUrl))
                                .build())

                .buildDynamicLink();

        // -- https://your_subdomain.page.link/?link=your_deep_link&apn=package_name[&amv=minimum_version][&afl=fallback_link]
        /*return "https://" + your_subdomain + ".page.link/?link=" + your_deep_link
                + "&apn=" + package_name + "&st" + titleSocial + "&sd" + description + "&si" + imageUrl
                + "[&amv=" + minimum_version + "][&afl=" + fallback_link + "]";*/

        return dynamicLink.getUri();
    }

    private void create_sendingShareLink(){
       Task<ShortDynamicLink> shortDynamicLinkTask = FirebaseDynamicLinks .getInstance().createDynamicLink()
                .setLongLink(buildDynamicLink())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(task.isSuccessful()){
                            Uri shortLink = task.getResult().getShortLink();
                            //Uri flowchartLink = task.getResult().getPreviewLink();

                            Log.d(TAG, "shortLink: " + shortLink);

                            link = shortLink.toString();

                            saveShareLink();

                            sendingShareLink();

                        }else {
                            Log.d(TAG, "shortLink: fail" + task.getException().getMessage());
                        }
                    }
                });
    }

    private void sendingShareLink(){
        Intent intent = new Intent();
        String msg = context.getString(R.string.on_app_name) + "\n" + link;
        if(msgUser != null && msgUser.length()>0){
            msg += "\n\n" +msgUser;
        }
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    private void shareLink(){
        Task<ShortDynamicLink> shortDynamicLinkTask = FirebaseDynamicLinks .getInstance().createDynamicLink()
                .setLongLink(buildDynamicLink())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(task.isSuccessful()){
                            Uri shortLink = task.getResult().getShortLink();
                            //Uri flowchartLink = task.getResult().getPreviewLink();

                            Log.d(TAG, "shortLink: " + shortLink);

                            link = shortLink.toString();

                        }else {
                            Log.d(TAG, "shortLink: fail" + task.getException().getMessage());
                        }
                    }
                });
    }

    public String getSharingLink() {
        return link;
    }

    private void saveShareLink(){
        if(type.equals(context.getString(R.string.field_entage_page))){ // entaji-page
            FirebaseDatabase.getInstance().getReference()
                    .child(context.getString(R.string.dbname_sharing_links))
                    .child(context.getString(R.string.field_sharing_links_entaji_pages))
                    .child(itemId)
                    .child(context.getString(R.string.field_sharing_link))
                    .setValue(link);

        }else if(type.equals(context.getString(R.string.field_item))){  // item
            FirebaseDatabase.getInstance().getReference()
                    .child(context.getString(R.string.dbname_sharing_links))
                    .child(context.getString(R.string.field_sharing_links_items))
                    .child(itemId)
                    .child(context.getString(R.string.field_sharing_link))
                    .setValue(link);

        }else if(type.equals(context.getString(R.string.field_search))){  // item
            FirebaseDatabase.getInstance().getReference()
                    .child(context.getString(R.string.dbname_sharing_links))
                    .child(context.getString(R.string.field_sharing_links_search))
                    .child(itemId)
                    .child(context.getString(R.string.field_sharing_link))
                    .setValue(link);
        }
    }

}
