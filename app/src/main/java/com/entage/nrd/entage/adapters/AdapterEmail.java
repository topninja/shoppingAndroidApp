package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterEmail extends RecyclerView.Adapter{
    private static final String TAG = "AdapterEmail";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<NotificationOnApp> emails;
    private View.OnClickListener onClickListener;
    private String textQuestion, textAnswer;

    public AdapterEmail(Context context, RecyclerView recyclerView, ArrayList<NotificationOnApp> emails) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.emails = emails;

        setupOnClickListener();

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        textQuestion = mContext.getString(R.string.question);
        textAnswer = mContext.getString(R.string.answer);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView title, message, message1, time;
        ImageView circle;
        CircleImageView image;

        private ItemViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            title = itemView.findViewById(R.id.title);
            message1 = itemView.findViewById(R.id.message1);
            circle = itemView.findViewById(R.id.circle);
            image = itemView.findViewById(R.id.image);
            time = itemView.findViewById(R.id.time);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(emails.get(position) == null){
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
            view = layoutInflater.inflate(R.layout.layout_news, parent, false);
            viewHolder = new AdapterEmail.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterEmail.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterEmail.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.title.setText(UtilitiesMethods.getMessage(mContext, emails.get(position).getTitle(),
                    emails.get(position).getExtra_data()));

            itemViewHolder.message.setText(UtilitiesMethods.getMessage(mContext, emails.get(position).getBody(),
                    emails.get(position).getExtra_data()));


            String extraData = emails.get(position).getExtra_data();
            if(extraData != null && !emails.get(position).getTitle().equals(mContext.getString(R.string.notif_title_add_new_item)) && !extraData.equals("userToEntagePage") &&
                !extraData.equals("entagePageToUser") && !emails.get(position).getFlag().equals("OPEN_USER_WALLET")){

                if(emails.get(position).getTitle().equals(mContext.getString(R.string.notif_title_add_new_question))){
                    extraData = textQuestion +": "+emails.get(position).getExtra_data();
                }
                itemViewHolder.message1.setText(extraData);
                itemViewHolder.message1.setVisibility(View.VISIBLE);
            }else {
                itemViewHolder.message1.setVisibility(View.GONE);
            }

            if(!emails.get(position).isIs_read()){
                itemViewHolder.circle.setColorFilter(mContext.getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            }else {
                itemViewHolder.circle.setColorFilter(mContext.getColor(R.color.gray0), PorterDuff.Mode.SRC_ATOP);
            }

            itemViewHolder.time.setText(UtilitiesMethods.getTimeDiff(mContext, emails.get(position).getTime()));

            itemViewHolder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);

                if(emails.get(itemPosition).getBody().equals(NotificationsTitles.newUpdate(mContext))){
                    setRead(mContext.getString(R.string.notif_title_app_new_update), Topics.getTopicsEntajiApp());
                }

                if(!emails.get(itemPosition).getFlag().equals("-1") &&
                        !emails.get(itemPosition).getFlag().equals(mContext.getString(R.string.notif_flag_entaji_page))){

                    emails.get(itemPosition).setIs_read(true);
                    notifyItemChanged(itemPosition);

                    Notification notification = new Notification(emails.get(itemPosition).getEntage_page_id(),
                            emails.get(itemPosition).getItem_id(),
                            null, null, null, null,
                            emails.get(itemPosition).getFlag(),
                            null, null, emails.get(itemPosition).getExtra_data(), null, null);


                    Intent intent = new Intent(mContext, ViewActivity.class);
                    intent.putExtra("notification", notification);
                    intent.putExtra("isComingFromEmail", true);
                    mContext.startActivity(intent);
                }

            }
        };
    }

    private void setRead(String idNotification, String topic){
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_subscriptions_emails))
                .child(topic)
                .child(mContext.getString(R.string.field_messages))
                .child(idNotification)
                .child("users_read")
                .child(user_id)
                .child("user_id")
                .setValue(user_id);
    }

}
