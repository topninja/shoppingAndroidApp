package com.entage.nrd.entage.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.entage.nrd.entage.Models.ItemBasket;
import com.entage.nrd.entage.basket.UserBasketActivity;
import com.entage.nrd.entage.Models.CustomerQuestion;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.Models.UserQuestion;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.NotificationsPriority;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.entage.nrd.entage.utilities_1.ViewOptionsPrices;
import com.entage.nrd.entage.utilities_1.ViewShippingInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    private String userID;
    private Context mContext;
    private double mPhotoUploadProgress =0;

    //private GlobalVariable globalVariable;


    public FirebaseMethods(Context mContext) {
        this.mContext = mContext;
        /*mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();


        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }*/
    }

    // addItemToBasket
    public static void addItemToBasket(final Context mContext, ItemShortData itemShortData, String id_user,
                                       ArrayList<String> selectedOptions, boolean isInAdapter){
        String itemName = itemShortData.getName_item();
        String entage_page_id =  itemShortData.getEntage_page_id();
        String item_id =itemShortData.getItem_id();
        OptionsPrices optionsPrices =  itemShortData.getOptions_prices();
        ShippingInformation shippingInformation =  itemShortData.getShipping_information();
        long item_number = itemShortData.getItem_number()==null? 0 : (long)itemShortData.getItem_number();

        addItemToBasket(mContext, entage_page_id, id_user, item_id, item_number, itemName, optionsPrices, shippingInformation, selectedOptions, isInAdapter);
    }


    public static void addItemToBasket(final Context mContext, final String entage_page_id, final String id_user, final String item_id,
                                       final long item_number, String itemName, OptionsPrices
                                               optionsPrices, final ShippingInformation shippingInformation,
                                       ArrayList<String> selectedOptions, boolean isInAdapter){

        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_adding_item_basket, null);
        TextView name = _view.findViewById(R.id.itemName);
        LinearLayout containerOptions = _view.findViewById(R.id.container_options);
        containerOptions.setVisibility(View.VISIBLE);
        final TextView addToBasket = _view.findViewById(R.id.add_to_basket);
        TextView cancel = _view.findViewById(R.id.cancel);
        final TextView error = _view.findViewById(R.id.error);
        final ProgressBar progressBar_addToBasket = _view.findViewById(R.id.progressBar_addToBasket);
        final RelativeLayout take_me_to_basket = _view.findViewById(R.id.take_me_to_basket);
        final GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        name.setText(itemName);
        final ViewOptionsPrices viewOptionsPrices = new ViewOptionsPrices(mContext, optionsPrices, containerOptions,
                (RelativeLayout) _view.findViewById(R.id.layout_price), selectedOptions, 14, 16,
                mContext.getColor(R.color.entage_blue), globalVariable);
        if(optionsPrices.getOptionsTitle() == null){
            _view.findViewById(R.id.line1).setVisibility(View.GONE);
            _view.findViewById(R.id.container_options).setVisibility(View.GONE);
        }

        if(shippingInformation !=null && isInAdapter){
            _view.findViewById(R.id.layout_shipping).setVisibility(View.VISIBLE);
            new ViewShippingInfo(mContext, (LinearLayout) _view.findViewById(R.id.layout_shipping), shippingInformation,
                    false, globalVariable);

            _view.findViewById(R.id.more_details_shipping_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =  new Intent(mContext, ViewActivity.class);
                    intent.putExtra("shippingInformation", shippingInformation);
                    intent.putExtra("area_shipping_available", shippingInformation.getArea_shipping_available());
                    mContext.startActivity(intent);
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        final AlertDialog alert = builder.create();
        //alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);

        addToBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get choosed options
                ArrayList<String> myOptions = viewOptionsPrices.getSelectedOptions();
                String options_id = viewOptionsPrices.getOptionsId();
                String item_basket_id = item_id+"_"+options_id;

                if(id_user == null || item_id == null || entage_page_id == null || options_id.length()==0){
                    error.setText(mContext.getString(R.string.happened_wrong_try_again));

                }
                else {
                    ItemBasket itemBasket = new ItemBasket(id_user, entage_page_id, item_id, item_basket_id,
                            item_number, myOptions, options_id, viewOptionsPrices.getmPrice(), DateTime.getTimestamp());

                    addToBasket.setVisibility(View.INVISIBLE);
                    progressBar_addToBasket.setVisibility(View.VISIBLE);
                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_users_basket))
                            .child(id_user)
                            .child(item_basket_id)
                            .setValue(itemBasket)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    FirebaseDatabase.getInstance().getReference()
                                            .child(mContext.getString(R.string.dbname_items_on_basket))
                                            .child(item_id)
                                            .child(id_user)
                                            .setValue(id_user);

                                    //
                                    int countBasket = mContext.getSharedPreferences("entaji_app",
                                            MODE_PRIVATE).getInt("countBasket", -1);
                                    int i =  countBasket==-1? 1 : (countBasket+1);
                                    mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                                            .putInt("countBasket", i).apply();

                                    progressBar_addToBasket.setVisibility(View.GONE);
                                    take_me_to_basket.setVisibility(View.VISIBLE);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    error.setText(mContext.getString(R.string.happened_wrong_try_again) + "\n" + e.getLocalizedMessage());
                                    progressBar_addToBasket.setVisibility(View.GONE);
                                    addToBasket.setVisibility(View.VISIBLE);
                                }
                            }) ;
                }
            }
        });

        take_me_to_basket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();

                Intent intent = new Intent(mContext, UserBasketActivity.class);
                intent.putExtra("type", "basket");
                mContext.startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    // postQuestion
    public static void postQuestion(final Context mContext, final String id_user, final String item_id, final String entage_page_id,
                                    final String name_item, String username, String question, final EditText questionField,
                                    final TextView postQuestion, final TextView error_question, final boolean entagePage_isNotifying_questions,
                                    final boolean item_isNotifying_questions, final CountDownTimer refreshOnDone){

        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);

        final FirebaseDatabase myDb =  FirebaseDatabase.getInstance();
        DatabaseReference myRef_iCq = myDb.getReference().child(mContext.getString(R.string.dbname_item_customers_questions));
        final DatabaseReference myRef_user_data = myDb.getReference().child(mContext.getString(R.string.dbname_user_data));

        final String question_id = myRef_iCq.push().getKey();

        if(question_id != null){
            final CustomerQuestion customerQuestion = new CustomerQuestion();
            customerQuestion.setUser_id(id_user);
            customerQuestion.setItem_id(item_id);
            customerQuestion.setTime_create(DateTime.getTimestamp());
            customerQuestion.setUser_name(username);
            customerQuestion.setQuestion_id(question_id);
            customerQuestion.setQuestion(question);
            customerQuestion.setNotify_ids(new HashMap<String, String>());
            customerQuestion.getNotify_ids().put(id_user, id_user);

            // set question to item_customers_questions
            myRef_iCq.child(item_id)
                    .child(question_id)
                    .setValue(customerQuestion)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // set question to user_data
                            myRef_user_data
                                    .child(mContext.getString(R.string.field_user_questions))
                                    .child(id_user)
                                    .child(question_id)
                                    .setValue(new UserQuestion(id_user, question_id, item_id));

                            // add to entage page notifications
                            String title = NotificationsTitles.addNewQuestion_Title(mContext);
                            myDb.getReference().child(mContext.getString(R.string.dbname_entage_page_email_notifications))
                                    .child(entage_page_id)
                                    .child(question_id)
                                    .setValue(new NotificationOnApp(item_id, question_id,
                                            null, null, title, "- "+name_item,
                                            mContext.getString(R.string.notif_flag_open_question),id_user, null,
                                            customerQuestion.getQuestion(), DateTime.getTimestamp(), NotificationsPriority.addNewQuestion(),
                                            false));

                            if(entagePage_isNotifying_questions && item_isNotifying_questions){
                                // Notify entage page, new question
                                String topic = Topics.getTopicsCustomersInEntagePage(entage_page_id);
                                myDb.getReference().child(mContext.getString(R.string.dbname_notifications))
                                        .child(mContext.getString(R.string.field_notification_to_topic))
                                        .child(question_id)
                                        .setValue(new Notification(item_id, question_id,
                                                "-1", topic, title,
                                                "- "+name_item,
                                                mContext.getString(R.string.notif_flag_open_question),id_user, "topic",
                                                "-1", "-1", question_id));
                            }

                            /*if(countQuestions <= 3){
                                setFirstThreeQuestions();
                            }*/

                            Log.d(TAG, "onSuccess: " + ((Activity)mContext).isFinishing());
                            if(!((Activity)mContext).isFinishing()){
                                questionField.setText("");
                                postQuestion.setVisibility(View.VISIBLE);
                                if(refreshOnDone != null){
                                    refreshOnDone.start();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(!((Activity)mContext).isFinishing()){
                                if(error_question!=null){
                                    error_question.setVisibility(View.VISIBLE);
                                    error_question.setText(mContext.getString(R.string.happened_wrong_try_again) +  e.getMessage());
                                }
                                postQuestion.setVisibility(View.VISIBLE);
                            }
                        }
                    }) ;
        }
    }


    // fetch Currency USD_SAR
    public static void fetchCurrencyUSD_SAR(Context mContext) {
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.dbname_app_data))
                    .child("usd_sar")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                PaymentsUtil.setPayPal_SAR_USD((String) dataSnapshot.getValue());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
        }
    }

    public static void setItems_on_views(Context mContext, DatabaseReference reference, String child, String itemId, String userId){
        if(mContext != null){
            long longtime = DateTime.getDateToday().getTime();

            if(child.equals(mContext.getString(R.string.mt_users_search))){
                reference // already exist: .child(mContext.getString(R.string.dbname_movement_tracking))
                        .child(child)
                        .child(userId)
                        .child(longtime+"")
                        .setValue(itemId); // test search


            }else {
                reference
                        .child(mContext.getString(R.string.dbname_movement_tracking))
                        .child(child)
                        .child(itemId)
                        .child(longtime+"")
                        .child(userId)
                        .setValue(true);

                reference
                        .child(mContext.getString(R.string.dbname_movement_tracking))
                        .child(mContext.getString(R.string.mt_users_views))
                        .child(userId)
                        .child(child)
                        .child(itemId)
                        .child("time")
                        .setValue(longtime+"");
            }
        }
    }



}
