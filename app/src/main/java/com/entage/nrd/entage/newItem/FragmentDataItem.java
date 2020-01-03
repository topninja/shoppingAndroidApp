package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class FragmentDataItem extends Fragment {
    private static final String TAG = "FragmentAddItem";

    private Context mContext ;
    private View view ;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems;

    private OnActivityDataItemListener mOnActivityDataItemListener;


    private String entagePageId, itemId, dateCreated;
    private View.OnClickListener onClickListener;

    private ImageView iconPublishDataItem, iconItemName, iconPhoto, iconCategories, iconDescription, iconSpecifications,
            iconOptions, iconDimension, iconPrices, iconShipping, iconDiscountOffers, iconOffers, iconSendNotification,
            iconAddAd, iconDeleteItem, iconDeleteEditCopy, iconiChangeDivisionItem;
    private ImageView bubbleItemName, bubblePhoto, bubbleCategories, bubbleDescription, bubblePrices, bubbleShipping;
    private RelativeLayout relLayouWaitChecking;
    private ProgressBar progressDeleteCopy, progressDeleteItem;
    private LinearLayout loadingLayout;
    private TextView textCreatedIn, textLastPublishIn, textDateLastEdit;
    private ArrayList<ImageView> icons;

    private boolean isNewItem = false;

    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_data_item, container, false);
            mContext = getActivity();

            getIncomingBundle();

            if(entagePageId == null || itemId == null){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

            }else {
                setupFirebaseAuth();
                init();
            }

        }else {
            getDatesFromArchives();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                itemId = bundle.getString(mContext.getString(R.string.field_item_id));
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        Log.d(TAG, "init: "  + itemId);

        initWidgets();
        onClickListener();

        clickableButtons(false);

        getDatesFromArchives();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.setting_item));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        icons = new ArrayList<>();

        loadingLayout = view.findViewById(R.id.loading_layout);

        textCreatedIn = view.findViewById(R.id.date_created);
        textLastPublishIn = view.findViewById(R.id.last_publish_was_in);
        textDateLastEdit = view.findViewById(R.id.date_last_edit);

        iconPublishDataItem = view.findViewById(R.id.publish_data_item);
        relLayouWaitChecking = view.findViewById(R.id.relLayou_wait_checking);
        icons.add(iconPublishDataItem);

        // -----
        iconItemName = view.findViewById(R.id.icon_item_name);
        bubbleItemName = view.findViewById(R.id.bubble_item_name);
        icons.add(iconItemName);

        iconPhoto = view.findViewById(R.id.icon_photos);
        bubblePhoto = view.findViewById(R.id.bubble_photos);
        icons.add(iconPhoto);

        iconCategories = view.findViewById(R.id.icon_categories);
        bubbleCategories = view.findViewById(R.id.bubble_categories);
        icons.add(iconCategories);

        iconDescription = view.findViewById(R.id.icon_description);
        icons.add(iconDescription);

        iconSpecifications = view.findViewById(R.id.icon_specifications);
        icons.add(iconSpecifications);

        iconOptions = view.findViewById(R.id.icon_options);
        icons.add(iconOptions);

        iconDimension = view.findViewById(R.id.icon_dimension);
        icons.add(iconDimension);

        iconShipping = view.findViewById(R.id.icon_shipping);
        bubbleShipping = view.findViewById(R.id.bubble_shipping);
        icons.add(iconShipping);

        iconPrices = view.findViewById(R.id.icon_prices);
        bubblePrices = view.findViewById(R.id.bubble_prices);
        icons.add(iconPrices);

        // -----
        iconDiscountOffers = view.findViewById(R.id.icon_discount_offers);
        icons.add(iconDiscountOffers);

        iconOffers = view.findViewById(R.id.icon_offers);
        icons.add(iconOffers);

        iconSendNotification = view.findViewById(R.id.icon_send_notification);
        icons.add(iconSendNotification);

        iconAddAd = view.findViewById(R.id.ic_add_ad);
        icons.add(iconAddAd);

        // -----
        iconDeleteItem = view.findViewById(R.id.icon_delete_item);
        ((RelativeLayout)iconDeleteItem.getParent()).setVisibility(View.GONE);
        icons.add(iconDeleteItem);

        iconDeleteEditCopy = view.findViewById(R.id.icon_delete_edit_copy);
        ((RelativeLayout)iconDeleteEditCopy.getParent()).setVisibility(View.GONE);
        icons.add(iconDeleteEditCopy);

        iconiChangeDivisionItem = view.findViewById(R.id.icon_change_division_item);
        ((RelativeLayout)iconiChangeDivisionItem.getParent()).setVisibility(View.GONE);
        icons.add(iconiChangeDivisionItem);

        progressDeleteItem = view.findViewById(R.id.progress_delete_item);
        progressDeleteCopy = view.findViewById(R.id.progress_delete_copy);

    }

    private void onClickListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickIcon(v);
            }
        };

        for(ImageView img : icons){
            img.setOnClickListener(onClickListener);
        }
    }

    private void onClickIcon(View _v){
        Log.d(TAG, "onClickIcon: " );

        if(_v == iconItemName){
            mOnActivityDataItemListener.onActivityListener(new FragmentNameItem());
        }
        else if(_v == iconPhoto){
            mOnActivityDataItemListener.onActivityListener(new FragmentImagesItem());
        }
        else if(_v == iconCategories){
            mOnActivityDataItemListener.onActivityListener(new FragmentCategoriesItem());
        }
        else if(_v ==  iconDescription){
            mOnActivityDataItemListener.onActivityListener(new FragmentDescriptionItem());
        }
        else if(_v ==  iconSpecifications){
            mOnActivityDataItemListener.onActivityListener(new FragmentSpecificationsItem());
        }
        else if(_v ==  iconOptions){
            mOnActivityDataItemListener.onActivityListener(new FragmentOptionsItem());
        }
        else if(_v ==  iconPrices){
            mOnActivityDataItemListener.onActivityListener(new FragmentPriceItem());
        }
        else if(_v ==  iconShipping){
            mOnActivityDataItemListener.onActivityListener(new FragmentShippingItem());
        }

        else if(_v ==  iconDimension){
            //mOnActivityDataItemListener.onActivityListener(new FragmentImagesItem());
        }
        else if(_v ==  iconDiscountOffers){
            Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                    Toast.LENGTH_SHORT).show();
            //mOnActivityDataItemListener.onActivityListener(new FragmentSaleItem());
        }
        else if(_v ==  iconOffers){
            Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                    Toast.LENGTH_SHORT).show();
            //mOnActivityDataItemListener.onActivityListener(new FragmentOffersItem());
        }
        else if(_v ==  iconSendNotification){
            Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                    Toast.LENGTH_SHORT).show();
        }
        else if(_v ==  iconAddAd){
            Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                    Toast.LENGTH_SHORT).show();
        }
        else if(_v ==  iconPublishDataItem){
            publishData();
        }
        else if(_v ==  iconDeleteItem){
            dialogDeletePublishedItem();
        }
        else if(_v ==  iconDeleteEditCopy){
            dialogDeleteEditCopy();
        }
        else if(_v ==  iconiChangeDivisionItem){
            mOnActivityDataItemListener.onActivityListener(new FragmentDivisionItem());
        }
    }

    private void getDatesFromArchives(){
        myRefArchives
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        Item item = dataSnapshot.getValue(Item.class);

                        if(item.getLast_publish_was_in() != null && isAdded() && mContext != null){ // item not publish yet, all dates exist
                            Log.d(TAG, "status item: item not publish yet, all dates exist \n" + item.toString() );
                            isNewItem = true;

                            setDateLastEdit(DateTime.getFullTimestamp(item.getLast_edit_was_in()));
                            setDateCreatedIn(DateTime.getFullTimestamp(item.getDate_created()));
                            setDateLastPublishIn(mContext.getString(R.string.not_publish_yet));

                            removeLoading(View.GONE);
                            showTextDates(View.VISIBLE);

                            checkingDataEntry(item);

                            ((RelativeLayout)iconDeleteEditCopy.getParent()).setVisibility(View.VISIBLE);
                            clickableButtons(true);

                        }
                        else { // item published , there is edit copy, date last edit is exist
                            Log.d(TAG, "status item: item published , there is edit copy, date last edit is exist");
                            if(item.getLast_edit_was_in() != null && isAdded() && mContext != null){
                                setDateLastEdit(DateTime.getFullTimestamp(item.getLast_edit_was_in()));
                                setDateLastPublishIn(mContext.getString(R.string.lase_edit_not_published));

                                if (dateCreated == null){
                                    getDatesFromPublished(false, false); // get only created date and once
                                }

                                ((RelativeLayout)iconDeleteItem.getParent()).setVisibility(View.VISIBLE);
                                ((RelativeLayout)iconDeleteEditCopy.getParent()).setVisibility(View.VISIBLE);
                                ((RelativeLayout)iconiChangeDivisionItem.getParent()).setVisibility(View.VISIBLE);
                                clickableButtons(true);
                            }
                        }

                    }
                    else { // item published , but no edit copy, get all dates from published
                        Log.d(TAG, "status item: item published , but no edit copy, get all dates from published");
                        if(isAdded() && mContext != null){
                            getDatesFromPublished(true, true);

                            ((RelativeLayout)iconDeleteItem.getParent()).setVisibility(View.VISIBLE);
                            ((RelativeLayout)iconiChangeDivisionItem.getParent()).setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    setMsgError(databaseError.getMessage());
                }
            });
    }

    private void getDatesFromPublished(final boolean last_edit, final boolean last_published){
        myRefItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Item item = dataSnapshot.getValue(Item.class);

                            if(isAdded() && mContext != null){
                                if(last_edit){
                                    setDateLastEdit(DateTime.getFullTimestamp(item.getLast_edit_was_in()));
                                }

                                if(last_published){
                                    setDateLastPublishIn(DateTime.getFullTimestamp(item.getLast_publish_was_in()));
                                }

                                dateCreated = DateTime.getFullTimestamp(item.getDate_created());
                                setDateCreatedIn(dateCreated);

                                removeLoading(View.GONE);
                                showTextDates(View.VISIBLE);

                                clickableButtons(true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        setMsgError(databaseError.getMessage());
                    }
                });
    }

    private void checkingDataEntry(Item item){
        // check from name, images, categories, shipping and prices
        int red = mContext.getColor(R.color.entage_red);
        int blue = mContext.getColor(R.color.entage_blue);

        bubbleItemName.setColorFilter(item.getName_item()!=null? blue:red, PorterDuff.Mode.SRC_ATOP);
        bubbleItemName.setVisibility(View.VISIBLE);

        bubblePhoto.setColorFilter(item.getImages_url()!=null? blue:red, PorterDuff.Mode.SRC_ATOP);
        bubblePhoto.setVisibility(View.VISIBLE);

        bubbleCategories.setColorFilter(item.getCategories_item()!=null? blue:red, PorterDuff.Mode.SRC_ATOP);
        bubbleCategories.setVisibility(View.VISIBLE);

        bubbleShipping.setColorFilter(item.getShipping_information()!=null? blue:red, PorterDuff.Mode.SRC_ATOP);
        bubbleShipping.setVisibility(View.VISIBLE);

        bubblePrices.setColorFilter(item.getOptions_prices()!=null? blue:red, PorterDuff.Mode.SRC_ATOP);

        bubblePrices.setColorFilter((item.getOptions_prices()!=null && item.getOptions_prices().getMain_price()!=null &&
                !item.getOptions_prices().getMain_price().equals("0.0"))?
                blue:red, PorterDuff.Mode.SRC_ATOP);
        bubblePrices.setVisibility(View.VISIBLE);

    }

    // dates
    private void setDateCreatedIn(String date) {
        textCreatedIn.setText(mContext.getString(R.string.created_in) + " " + date );
    }

    private void setDateLastPublishIn(String date) {
        textLastPublishIn.setText(mContext.getString(R.string.last_publish_was_in) + " " + date );
    }

    private void setDateLastEdit(String date) {
        textDateLastEdit.setText(mContext.getString(R.string.last_edit_was_in) + " " + date );
    }

    // utilities
    private void showTextDates(int visibility){
        textCreatedIn.setVisibility(visibility);
        textDateLastEdit.setVisibility(visibility);
        textLastPublishIn.setVisibility(visibility);
    }

    private void removeLoading(int visibility){
        loadingLayout.setVisibility(visibility);
    }

    private void clickableButtons(boolean isClickable){
        for(ImageView img : icons){
            img.setClickable(isClickable);
        }
    }

    private void setMsgError(String msg){
        loadingLayout.setVisibility(View.VISIBLE);
        loadingLayout.getChildAt(0).setVisibility(View.GONE);
        ((TextView)loadingLayout.getChildAt(1)).setText(mContext.getString(R.string.happened_wrong_title)+"\n"+ msg);
    }

    //
    private void publishData(){
        // if new item check for name, images, categories, shipping and prices
        // if edit item check for  prices

        clickableButtons(false);
        relLayouWaitChecking.setVisibility(View.VISIBLE);

        if(isNewItem){
            myRefArchives
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            clickableButtons(true);
                            relLayouWaitChecking.setVisibility(View.GONE);

                            if(dataSnapshot.exists()){
                                Item item = dataSnapshot.getValue(Item.class);

                                if(item.getName_item() == null ||
                                        item.getImages_url() == null ||
                                        item.getCategories_item() == null ||
                                        item.getShipping_information() == null ||
                                        item.getOptions_prices() == null ||
                                        item.getOptions_prices().getMain_price() == null ||
                                        item.getOptions_prices().getMain_price().equals("0.0")){

                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.item_not_good_to_published),
                                            mContext.getString(R.string.done_all_steps_first));

                                }
                                else {
                                    if(mOnActivityDataItemListener != null){
                                        mOnActivityDataItemListener.onActivityListener(new FragmentReviewItem());
                                    }
                                }

                            }else {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                clickableButtons(true);
                                relLayouWaitChecking.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                            clickableButtons(true);
                            relLayouWaitChecking.setVisibility(View.GONE);
                        }
                    });
        }else {
            myRefArchives.child(mContext.getString(R.string.field_options_prices))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            clickableButtons(true);
                            relLayouWaitChecking.setVisibility(View.GONE);

                            if(dataSnapshot.exists()){
                                OptionsPrices optionsPrices = dataSnapshot.getValue(OptionsPrices.class);

                                if(optionsPrices == null ||
                                        optionsPrices.getMain_price() == null ||
                                        optionsPrices.getMain_price().equals("0.0")){

                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.item_not_good_to_published),
                                            mContext.getString(R.string.write_the_price_item));

                                }else {
                                    if(mOnActivityDataItemListener != null){
                                        mOnActivityDataItemListener.onActivityListener(new FragmentReviewItem());
                                    }
                                }

                            }else {
                                if(mOnActivityDataItemListener != null){
                                    mOnActivityDataItemListener.onActivityListener(new FragmentReviewItem());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                            clickableButtons(true);
                            relLayouWaitChecking.setVisibility(View.GONE);
                        }
                    });
        }
    }

    // deleteEditCopy
    private void dialogDeleteEditCopy(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setMessage(mContext.getString(R.string.delete_last_Edit_copy));
        builder.setPositiveButton(mContext.getString(R.string.delete) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                deleteEditCopy();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteEditCopy(){
        clickableButtons(false);
        showTextDates(View.GONE);
        removeLoading(View.VISIBLE);
        progressDeleteCopy.setVisibility(View.VISIBLE);

        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        final ArrayList<String> urls = new ArrayList<>();

        // start with collect urls image if exist
        myRefArchives.child(mContext.getString(R.string.field_images_url))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            ArrayList<String> arrayList = (ArrayList<String>) dataSnapshot.getValue();
                            if(arrayList != null){
                                urls.addAll(arrayList);
                            }
                        }

                        // next collect urls of descriptions if exist
                        myRefArchives.child(mContext.getString(R.string.field_description_item))
                                .child("images_url")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            ArrayList<String> imagesUrls = (ArrayList<String>) dataSnapshot.getValue();

                                            if(imagesUrls != null){
                                                urls.addAll(imagesUrls);
                                            }
                                        }

                                        //delete
                                        for(String imgURL : urls){
                                            firebaseStorage.getReferenceFromUrl(imgURL).delete();
                                        }

                                        //now delete from Archive
                                        myRefArchives.removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            progressDeleteCopy.setVisibility(View.GONE);
                                                            removeLoading(View.GONE);
                                                            setMsgError(mContext.getString(R.string.successfully_delete));
                                                            dialogSuccessfullyDelete();

                                                        }else {
                                                            clickableButtons(true);
                                                            removeLoading(View.GONE);
                                                            showTextDates(View.VISIBLE);
                                                            progressDeleteCopy.setVisibility(View.GONE);
                                                            messageDialog.errorMessage(mContext, task.getException().getMessage());
                                                        }
                                                    }
                                                });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        clickableButtons(true);
                                        removeLoading(View.GONE);
                                        showTextDates(View.VISIBLE);
                                        progressDeleteCopy.setVisibility(View.GONE);
                                        messageDialog.errorMessage(mContext, databaseError.getMessage());
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        clickableButtons(true);
                        removeLoading(View.GONE);
                        showTextDates(View.VISIBLE);
                        progressDeleteCopy.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, databaseError.getMessage());
                    }
                });
    }

    // deletePublishedItem
    private void dialogDeletePublishedItem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setMessage(mContext.getString(R.string.delete_item));
        builder.setPositiveButton(mContext.getString(R.string.delete) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                checkItemInBasket();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkItemInBasket(){
        clickableButtons(false);
        showTextDates(View.GONE);
        removeLoading(View.VISIBLE);
        progressDeleteItem.setVisibility(View.VISIBLE);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items_on_basket))
                .child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    clickableButtons(true);
                    removeLoading(View.GONE);
                    showTextDates(View.VISIBLE);
                    progressDeleteItem.setVisibility(View.GONE);

                    if(isAdded() && mContext != null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                        builder.setMessage(mContext.getString(R.string.items_in_users_basket));
                        builder.setPositiveButton(mContext.getString(R.string.delete) , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                checkNoOrders();
                            }
                        });
                        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        final AlertDialog alert = builder.create();
                        alert.show();
                    }
                }else {
                    checkNoOrders();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                clickableButtons(true);
                removeLoading(View.GONE);
                showTextDates(View.VISIBLE);
                progressDeleteItem.setVisibility(View.GONE);
                messageDialog.errorMessage(mContext, databaseError.getMessage());
            }
        });
    }

    private void checkNoOrders(){
        clickableButtons(false);
        showTextDates(View.GONE);
        removeLoading(View.VISIBLE);
        progressDeleteItem.setVisibility(View.VISIBLE);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items_on_order))
                .child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            clickableButtons(true);
                            removeLoading(View.GONE);
                            showTextDates(View.VISIBLE);
                            progressDeleteItem.setVisibility(View.GONE);

                            if(isAdded() && mContext != null){
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.items_has_open_orders));
                            }

                        }else {
                            checkNoEditCopy();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        clickableButtons(true);
                        removeLoading(View.GONE);
                        showTextDates(View.VISIBLE);
                        progressDeleteItem.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, databaseError.getMessage());
                    }
                });

    }

    private void checkNoEditCopy(){
        myRefArchives
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            clickableButtons(true);
                            removeLoading(View.GONE);
                            showTextDates(View.VISIBLE);
                            progressDeleteItem.setVisibility(View.GONE);

                            if(isAdded() && mContext != null){
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.items_has_edit_copy));
                            }

                        }else {
                            deletePublishedItem();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        clickableButtons(true);
                        removeLoading(View.GONE);
                        showTextDates(View.VISIBLE);
                        progressDeleteItem.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, databaseError.getMessage());
                    }
                });
    }

    private void  deletePublishedItem(){
        if(!isNewItem){ // just for sure

            final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

            final ArrayList<String> urls = new ArrayList<>();

            myRefItems.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Item item = dataSnapshot.getValue(Item.class);

                        myRefItems.removeValue();
                        mFirebaseDatabase.getReference()
                                .child(mContext.getString(R.string.dbname_entage_page_categories))
                                .child(entagePageId)
                                .child(item.getItem_in_categorie())
                                .child("categorie_items")
                                .child(itemId)
                                .removeValue();

                        mFirebaseDatabase.getReference()
                                .child(mContext.getString(R.string.dbname_deleted_items))
                                .child(entagePageId)
                                .child(itemId)
                                .setValue(item)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDeleteItem.setVisibility(View.GONE);
                                        removeLoading(View.GONE);

                                        if(task.isSuccessful()){
                                            setMsgError(mContext.getString(R.string.successfully_delete));
                                            dialogSuccessfullyDelete();

                                        }else {
                                            clickableButtons(true);
                                            showTextDates(View.VISIBLE);
                                            messageDialog.errorMessage(mContext, task.getException().getMessage());
                                        }
                                    }
                                });

                    }else {
                        clickableButtons(true);
                        removeLoading(View.GONE);
                        showTextDates(View.VISIBLE);
                        progressDeleteItem.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    clickableButtons(true);
                    removeLoading(View.GONE);
                    showTextDates(View.VISIBLE);
                    progressDeleteItem.setVisibility(View.GONE);
                    messageDialog.errorMessage(mContext, databaseError.getMessage());
                }
            });

          /*  // start with collect urls image if exist
            myRefItems.child(mContext.getString(R.string.field_images_url))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                ArrayList<String> arrayList = (ArrayList<String>) dataSnapshot.getValue();
                                if(arrayList != null){
                                    urls.addAll(arrayList);
                                }
                            }

                            // next collect urls of descriptions if exist
                            myRefItems.child(mContext.getString(R.string.field_type_data_descriptions))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                ArrayList<TypeDataDescription> descriptions = new ArrayList<>();
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    descriptions.add(snapshot.getValue(TypeDataDescription.class));
                                                }

                                                for(TypeDataDescription description : descriptions){
                                                    if(description.getType().equals("image")){
                                                        urls.add(description.getData());
                                                    }
                                                }
                                            }

                                            //delete
                                            for(String imgURL : urls){
                                                firebaseStorage.getReferenceFromUrl(imgURL).delete();
                                            }


                                            //now delete from Archive
                                            myRefItems.removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                progressDeleteItem.setVisibility(View.GONE);
                                                                removeLoading(View.GONE);
                                                                setMsgError(mContext.getString(R.string.successfully_delete));
                                                                dialogSuccessfullyDelete();

                                                            }else {
                                                                clickableButtons(true);
                                                                removeLoading(View.GONE);
                                                                showTextDates(View.VISIBLE);
                                                                progressDeleteItem.setVisibility(View.GONE);
                                                                messageDialog.errorMessage(mContext, task.getException().getMessage());
                                                            }
                                                        }
                                                    });
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            clickableButtons(true);
                                            removeLoading(View.GONE);
                                            showTextDates(View.VISIBLE);
                                            progressDeleteItem.setVisibility(View.GONE);
                                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                                        }
                                    });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            clickableButtons(true);
                            removeLoading(View.GONE);
                            showTextDates(View.VISIBLE);
                            progressDeleteItem.setVisibility(View.GONE);
                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                        }
                    });*/
        }
    }

    private void dialogSuccessfullyDelete(){
        if(isAdded() && mContext != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setMessage(mContext.getString(R.string.successfully_delete));

            builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().onBackPressed();
                }
            });

            AlertDialog _alertDialog = builder.create();
            _alertDialog.setCancelable(false);
            _alertDialog.setCanceledOnTouchOutside(false);
            _alertDialog.show();
        }
    }


    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId);
        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId);

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }
}
