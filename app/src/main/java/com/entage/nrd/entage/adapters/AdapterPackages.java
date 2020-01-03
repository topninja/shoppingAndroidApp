package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ConvertCurrency;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.SubscribeActivity;
import com.entage.nrd.entage.utilities_1.Currencylayer;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.DescriptionsPackages;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;

public class AdapterPackages extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<SubscriptionPackage> packages;
    private HashMap<String, SubscriptionPackage> packagesHashMap;
    private View.OnClickListener onClickListener;

    private GlobalVariable globalVariable;
    private boolean isForShow = false;
    private boolean processing = false;
    private String entajiPagesId;
    private String user_id;

    public static final int RB1_ID = 1000;//first radio button id, dont change this, its very important
    public static final int RB2_ID = 1001;//second radio button id, dont change this, its very important
    public static final int RB3_ID = 1002;//third radio button id, dont change this, its very important

    public AdapterPackages(Context context, RecyclerView recyclerView, ArrayList<SubscriptionPackage> packages,
                           HashMap<String, SubscriptionPackage> packagesHashMap, boolean isForShow, String entajiPagesId) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.packages = packages;
        this.packagesHashMap = packagesHashMap;
        this.isForShow = isForShow;
        this.entajiPagesId = entajiPagesId;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        setupOnClickListener();

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        DescriptionsPackages.init(mContext);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView package_name, package_description, package_price, package_price_months,
                subscribe, dates, package_period_3, package_period_6, package_period_12,
                package_price_3, package_price_6, package_price_12;
        LinearLayout layout_starter, layout_3612;

        private ItemViewHolder(View itemView) {
            super(itemView);
            package_name = itemView.findViewById(R.id.package_name);
            package_description = itemView.findViewById(R.id.package_description);
            package_price = itemView.findViewById(R.id.package_price);
            package_price_months = itemView.findViewById(R.id.package_price_months);
            subscribe = itemView.findViewById(R.id.subscribe);
            layout_starter = itemView.findViewById(R.id.layout_starter);
            dates = itemView.findViewById(R.id.dates);
            layout_3612 = itemView.findViewById(R.id.layout_3612);
            package_period_3 = itemView.findViewById(R.id.package_period_3);
            package_period_6 = itemView.findViewById(R.id.package_period_6);
            package_period_12 = itemView.findViewById(R.id.package_period_12);
            package_price_3 = itemView.findViewById(R.id.package_price_3);
            package_price_6 = itemView.findViewById(R.id.package_price_6);
            package_price_12 = itemView.findViewById(R.id.package_price_12);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(packages.get(position) == null){
            return PROGRESS_VIEW;
        }else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_entaji_page_packages, parent, false);
            viewHolder = new AdapterPackages.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_entaji_page_packages_progressbar, parent, false);
            viewHolder = new AdapterPackages.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterPackages.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            SubscriptionPackage _package = packages.get(position);
            if(packagesHashMap != null){
                _package = packagesHashMap.get(packages.get(position).getPackage_id());
            }

            //String name = DescriptionsPackages.getMessageText(_package.getPackage_name());
            itemViewHolder.package_name.setText(_package.getPackage_name());

            itemViewHolder.package_description.setText("");
            StringBuilder text = new StringBuilder();
            for(String s : _package.getPackage_description()){
                text.append(itemViewHolder.package_description.getText()).append("- ").append(DescriptionsPackages.getMessageText(s))
                        .append("\n\n");
            }
            itemViewHolder.package_description.setText(text.toString().trim());

            if(_package.getPrice_12()!=null){
                itemViewHolder.layout_starter.setVisibility(View.GONE);
                itemViewHolder.layout_3612.setVisibility(View.VISIBLE);

                itemViewHolder.package_period_3.setText("3 "+ mContext.getString(R.string.months));
                itemViewHolder.package_price_3.setText(_package.getPrice_3() + " ر.س");

                itemViewHolder.package_period_6.setText("6 "+ mContext.getString(R.string.months));
                itemViewHolder.package_price_6.setText(_package.getPrice_6() + " ر.س");

                itemViewHolder.package_period_12.setText("12 "+ mContext.getString(R.string.months));
                itemViewHolder.package_price_12.setText(_package.getPrice_12() + " ر.س");
            }
            else {
                itemViewHolder.layout_3612.setVisibility(View.GONE);
                itemViewHolder.layout_starter.setVisibility(View.VISIBLE);

                itemViewHolder.package_price_months.setText(_package.getPrice_3() + " ر.س");
                itemViewHolder.package_price.setText("3 "+ mContext.getString(R.string.months));
            }


            if(packagesHashMap == null){
                itemViewHolder.subscribe.setOnClickListener(onClickListener);

            }else {
                itemViewHolder.layout_3612.setVisibility(View.GONE);
                itemViewHolder.layout_starter.setVisibility(View.GONE);

                if(position == 0){
                    itemViewHolder.subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_entage_gray_ops));
                    itemViewHolder.subscribe.setText(mContext.getString(R.string.subscribed));
                }else {
                    itemViewHolder.subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_ops));
                    itemViewHolder.subscribe.setText(mContext.getString(R.string.previous_subscription));
                }

                itemViewHolder.dates.setText( mContext.getString(R.string.start_subscription_date)+": "+
                        packages.get(position).getStart_date()+"\n"+
                        mContext.getString(R.string.expire_subscription_date)+": "+packages.get(position).getExpire_date());
                itemViewHolder.dates.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isForShow){
                    int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());

                    if(itemPosition != -1){
                        SubscriptionPackage _package = packages.get(itemPosition);
                        subscribe(_package);
                    }
                /*if(((TextView)v).getText().equals(mContext.getString(R.string.subscribed))){
                    Toast.makeText(mContext,  mContext.getString(R.string.subscribed) ,
                            Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                            Toast.LENGTH_SHORT).show();
                }*/
                }else {
                    Toast.makeText(mContext,  mContext.getString(R.string.create_entage_page_first) ,
                            Toast.LENGTH_SHORT).show();
                }

            }
        };
    }


    private void subscribe(final SubscriptionPackage _package){
        if(!processing && entajiPagesId!=null){
            processing = true;

            if(user_id == null){
                user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_subscribe, null, false);
            TextView package_name = _view.findViewById(R.id.package_name);
            final TextView error = _view.findViewById(R.id.error);
            TextView subscribe = _view.findViewById(R.id.subscribe);
            TextView cancel = _view.findViewById(R.id.cancel);
            ProgressBar progress = _view.findViewById(R.id.progress);
            final RadioGroup radioGrp =  _view.findViewById(R.id.radioGrp);

            package_name.setText(_package.getPackage_name());
            if(_package.getPrice_3()!=null){
                ((RadioButton)_view.findViewById(R.id.radio_butt_1)).setText(  _package.getPrice_3() + " ر.س" + " / "+
                        "3 "+ mContext.getString(R.string.months));
                _view.findViewById(R.id.radio_butt_1).setId(RB1_ID);
            }
            if(_package.getPrice_6()!=null){
                ((RadioButton)_view.findViewById(R.id.radio_butt_2)).setText(_package.getPrice_6() + " ر.س"+ " / "+
                        "6 "+ mContext.getString(R.string.months));
                _view.findViewById(R.id.radio_butt_2).setVisibility(View.VISIBLE);
                _view.findViewById(R.id.radio_butt_2).setId(RB2_ID);
            }
            if(_package.getPrice_12()!=null){
                ((RadioButton)_view.findViewById(R.id.radio_butt_3)).setText( _package.getPrice_12() + " ر.س"+ " / "+
                        "12 "+ mContext.getString(R.string.months));
                _view.findViewById(R.id.radio_butt_3).setVisibility(View.VISIBLE);
                _view.findViewById(R.id.radio_butt_3).setId(RB3_ID);
            }

            radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            checkCurrentSubscribe(_package, error, subscribe, alert);

            subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    error.setVisibility(View.GONE);

                    int buttonId = radioGrp.getCheckedRadioButtonId();

                    Intent intent = new Intent(mContext, SubscribeActivity.class);
                    intent.putExtra("entajiPagesId", entajiPagesId);
                    intent.putExtra("package", _package);
                    if(buttonId == RB1_ID){
                        processing = false;
                        alert.dismiss();
                        intent.putExtra("subscribe_type", RB1_ID);
                        ((Activity) mContext).startActivityForResult(intent, 99);
                    }
                    else if(buttonId == RB2_ID){
                        processing = false;
                        alert.dismiss();
                        intent.putExtra("subscribe_type", RB2_ID);
                        ((Activity) mContext).startActivityForResult(intent, 99);
                    }
                    else if(buttonId == RB3_ID){
                        processing = false;
                        alert.dismiss();
                        intent.putExtra("subscribe_type", RB3_ID);
                        ((Activity) mContext).startActivityForResult(intent, 99);
                    }
                    else{
                        error.setText(mContext.getString(R.string.Select_subscribe_type));
                        error.setVisibility(View.VISIBLE);
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processing = false;
                    alert.dismiss();
                }
            });

            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }

    private void checkCurrentSubscribe(final SubscriptionPackage _package, final TextView error, final TextView subscribe,
                                       final AlertDialog alert){
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entajiPagesId)
                .child(mContext.getString(R.string.dbname_current_subscription))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        SubscriptionPackage subscriptionPackage = dataSnapshot.getValue(SubscriptionPackage.class);
                        if(mContext!=null && alert.isShowing()){
                            if(subscriptionPackage.getPackage_id().equals(_package.getPackage_id()) && subscriptionPackage.isIs_running()){
                                error.setText(mContext.getString(R.string.current_packages_entage_page_is_1)+" "+_package.getPackage_name());
                                error.setVisibility(View.VISIBLE);
                                subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_ops));
                                subscribe.setClickable(false);

                            }
                            else if(subscriptionPackage.getPackage_id().equals("2_flame") && _package.getPackage_id().equals("1_starter")
                                    && subscriptionPackage.isIs_running()){
                                error.setText(mContext.getString(R.string.cant_subscribe_less_current_subscription));
                                error.setVisibility(View.VISIBLE);
                                subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_ops));
                                subscribe.setClickable(false);

                            }
                            else if(subscriptionPackage.getPackage_id().equals("1_starter") && _package.getPackage_id().equals("2_flame")
                                    && subscriptionPackage.isIs_running()){
                                subscribe.setClickable(true);
                            }
                            else if(!subscriptionPackage.isIs_running()){
                                subscribe.setClickable(true);
                            }
                            else {
                                error.setText(mContext.getString(R.string.happened_wrong_try_again)+"_1234");
                                error.setVisibility(View.VISIBLE);
                                subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_ops));
                                subscribe.setClickable(false);
                            }
                            subscribe.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mContext!=null && alert.isShowing()){
                        error.setText(mContext.getString(R.string.happened_wrong_title)+" "+databaseError.getMessage());
                        error.setVisibility(View.VISIBLE);
                        subscribe.setVisibility(View.VISIBLE);
                        subscribe.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_ops));
                        subscribe.setClickable(false);
                    }
                }
            });
    }

    private void subscribe(){

    }

}
