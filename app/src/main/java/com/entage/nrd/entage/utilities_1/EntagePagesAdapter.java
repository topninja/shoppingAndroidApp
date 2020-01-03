package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.EntagePageShortData;
import com.entage.nrd.entage.Models.Following;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntagePagesAdapter  extends RecyclerView.Adapter{
    private static final String TAG = "EntagePagesAdapter";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int LOAD_MORE_VIEW = 2;

    //firebase
    private String user_id;
    private boolean isUserAnonymous;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;


    private Context mContext;
    ArrayList<EntagePageShortData> entagePages = null;
    private GlobalVariable globalVariable;


    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener, onClickListenerFollow, onClickListenerFollowed, onClickListenerNnotification;
    private Drawable defaultImage ;

    public EntagePagesAdapter(Context context, RecyclerView recyclerView, ArrayList<EntagePageShortData> entagePages) {
        this.mContext = context;
        this.entagePages = entagePages;
        this.recyclerView = recyclerView;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        setupFirebaseAuth();

        defaultImage = mContext.getResources().getDrawable(R.drawable.ic_default);
        setupOnClickListener();
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder{
        TextView text;

        private LoadMoreViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    public class EntagePageViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView name, description, follow, followed;
        RelativeLayout relLayout_followed, relLayouText;
        ImageView notification;

        private EntagePageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.entage_name);
            description = itemView.findViewById(R.id.description);
            follow = itemView.findViewById(R.id.follow);
            relLayout_followed = itemView.findViewById(R.id.relLayout_followed);
            followed = itemView.findViewById(R.id.followed);
            notification = itemView.findViewById(R.id.notification);
            relLayouText = itemView.findViewById(R.id.relLayouText);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_loadMore);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(entagePages.get(position) == null){
            return PROGRESS_VIEW;
        }else if (entagePages.get(position).getEntage_id() == null){
            return LOAD_MORE_VIEW;
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
            view = layoutInflater.inflate(R.layout.layout_search_list_entagepage, parent, false);
            viewHolder = new EntagePagesAdapter.EntagePageViewHolder(view);

        }else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_entage_page_adapter_progressbar, parent, false);
            viewHolder = new EntagePagesAdapter.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof EntagePagesAdapter.EntagePageViewHolder) {
            EntagePageViewHolder entagePageHolder = (EntagePageViewHolder)holder;

            if(entagePages.get(position).getProfile_photo() != null){
                UniversalImageLoader.setImage(entagePages.get(position).getProfile_photo(), entagePageHolder.imageView, null ,"");
            }else {
                entagePageHolder.imageView.setImageDrawable(defaultImage);
            }

            entagePageHolder.name.setText(entagePages.get(position).getName_entage_page());
            entagePageHolder.description.setText(entagePages.get(position).getDescription());

            String id = entagePages.get(position).getEntage_id();
            entagePageHolder.relLayout_followed.setVisibility(View.GONE);
            if(globalVariable.getFollowingData().containsKey(id)){
                if(globalVariable.getFollowingData().get(id) == null){
                    entagePageHolder.follow.setVisibility(View.VISIBLE);
                    entagePageHolder.follow.setOnClickListener(onClickListenerFollow);

                }else {
                    entagePageHolder.follow.setVisibility(View.GONE);
                    entagePageHolder.follow.setOnClickListener(null);

                    if(globalVariable.getFollowingData().get(id).isIs_notifying()){
                        entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
                    }

                    entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                    entagePageHolder.followed.setEnabled(true);
                    entagePageHolder.notification.setEnabled(true);
                    entagePageHolder.notification.setOnClickListener(onClickListenerNnotification);
                    entagePageHolder.followed.setOnClickListener(onClickListenerFollowed);
                }
            }else {
                entagePageHolder.follow.setVisibility(View.VISIBLE);
                entagePageHolder.follow.setOnClickListener(onClickListenerFollow);
            }

            entagePageHolder.imageView.setOnClickListener(onClickListener);
            entagePageHolder.relLayouText.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if(entagePages==null) {
            entagePages = new ArrayList<>();
        }
        return entagePages.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra("entagePageId", entagePages.get(itemPosition).getEntage_id());
                mContext.startActivity(intent);
            }
        };
        onClickListenerFollow = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) (v.getParent()).getParent());
                setFollowing(entagePages.get(itemPosition), itemPosition);
            }
        };
        onClickListenerFollowed = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) ((v.getParent()).getParent()).getParent());
                setUnFollowing(entagePages.get(itemPosition), itemPosition);
            }
        };
        onClickListenerNnotification = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) ((v.getParent()).getParent()).getParent());

                if(globalVariable.getFollowingData().containsKey(entagePages.get(itemPosition).getEntage_id())){
                    Following followingData = globalVariable.getFollowingData().get(entagePages.get(itemPosition).getEntage_id());
                    if(followingData != null){
                        if(followingData.isIs_notifying()){
                            setUnNotifying(entagePages.get(itemPosition), itemPosition);
                        }else {
                            setNotifying(entagePages.get(itemPosition), itemPosition);
                        }
                    }
                }
            }
        };
    }

    private void setFollowing(final EntagePageShortData entagePage, final int position){
        if(!checkIsUserAnonymous()) {

            if (!isUserPage(entagePage) && globalVariable.getFollowingData().get(entagePage.getEntage_id()) == null) {

                final EntagePagesAdapter.EntagePageViewHolder entagePageHolder =
                        (EntagePagesAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

                if(entagePageHolder != null){

                    entagePageHolder.follow.setVisibility(View.GONE);
                    entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                    entagePageHolder.followed.setEnabled(false);
                    entagePageHolder.notification.setEnabled(false);

                    final Following following = new Following(user_id,
                            entagePage.getEntage_id(), DateTime.getTimestamp(), false);

                    final String topic = Topics.getTopicsFollowing(entagePage.getEntage_id());

                    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .setValue(true);

                                        myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .setValue(following);

                                        myRef.child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .setValue(following);


                                        globalVariable.getFollowingData().put(entagePage.getEntage_id(), following);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);
                                        entagePageHolder.followed.setOnClickListener(onClickListenerFollowed);
                                        entagePageHolder.notification.setOnClickListener(onClickListenerNnotification);

                                    } else {

                                        entagePageHolder.follow.setVisibility(View.VISIBLE);
                                        entagePageHolder.relLayout_followed.setVisibility(View.GONE);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }

            }

        }
    }

    private void setNotifying(final EntagePageShortData entagePage, final int position){

        final EntagePagesAdapter.EntagePageViewHolder entagePageHolder = (EntagePagesAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){

            entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
            entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
            entagePageHolder.followed.setEnabled(false);
            entagePageHolder.notification.setEnabled(false);

            final String topic = Topics.getTopicsNotifying(entagePage.getEntage_id());
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                // push to dbname_users_notifications
                                myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                        .child(user_id)
                                        .child(topic)
                                        .setValue(true);

                                myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                        .child(entagePage.getEntage_id())
                                        .child(user_id)
                                        .child(mContext.getString(R.string.field_is_notifying))
                                        .setValue(true);

                                myRef.child(mContext.getString(R.string.dbname_following_user))
                                        .child(user_id)
                                        .child(entagePage.getEntage_id())
                                        .child(mContext.getString(R.string.field_is_notifying))
                                        .setValue(true);

                                globalVariable.getFollowingData().get(entagePage.getEntage_id()).setIs_notifying(true);
                                entagePageHolder.followed.setEnabled(true);
                                entagePageHolder.notification.setEnabled(true);

                            }else {

                                entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                                entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                                entagePageHolder.followed.setEnabled(true);
                                entagePageHolder.notification.setEnabled(true);

                                Toast.makeText(mContext, mContext.getString(R.string.error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }

    private void setUnFollowing(final EntagePageShortData entagePage, final int position){

        final EntagePagesAdapter.EntagePageViewHolder entagePageHolder = (EntagePagesAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){
            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
            TextView unfollow = _view.findViewById(R.id.unfollow);
            TextView cancel = _view.findViewById(R.id.cancel);

            ((TextView)_view.findViewById(R.id.message)).setText(mContext.getString(R.string.do_you_want_unfollow));
            unfollow.setText(mContext.getString(R.string.unfollow));

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
            //builder.setTitle(mContext.getString(R.string.do_you_want_unfollow));
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            unfollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alert.dismiss();

                    entagePageHolder.relLayout_followed.setVisibility(View.GONE);
                    entagePageHolder.follow.setEnabled(false);
                    entagePageHolder.follow.setVisibility(View.VISIBLE);


                    final String topic = Topics.getTopicsFollowing(entagePage.getEntage_id());
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .removeValue();

                                        myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .removeValue();


                                        myRef.child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .removeValue();

                                        // UnsubscribeFromTopic Notifying
                                        String topic1 = Topics.getTopicsNotifying(entagePage.getEntage_id());
                                        com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic1);
                                        // push to dbname_users_notifications
                                        myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic1)
                                                .removeValue();

                                        globalVariable.getFollowingData().put(entagePage.getEntage_id(), null);
                                        entagePageHolder.notification.setBackground(mContext.getResources()
                                                .getDrawable(R.drawable.border_curve_entage_blue_1));
                                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                                        entagePageHolder.follow.setEnabled(true);
                                        entagePageHolder.follow.setOnClickListener(onClickListenerFollow);

                                    }else {

                                        entagePageHolder.follow.setVisibility(View.GONE);
                                        entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                                        entagePageHolder.follow.setEnabled(true);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
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
    }

    private void setUnNotifying(final EntagePageShortData entagePage, final int position){
        final EntagePagesAdapter.EntagePageViewHolder entagePageHolder = (EntagePagesAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){
            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
            TextView unNotify = _view.findViewById(R.id.unfollow);
            TextView cancel = _view.findViewById(R.id.cancel);

            ((TextView)_view.findViewById(R.id.message)).setText(mContext.getString(R.string.do_you_want_unnotify));
            unNotify.setText(mContext.getString(R.string.unnotify));


            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
            //builder.setTitle(mContext.getString(R.string.do_you_want_unfollow));
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            unNotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alert.dismiss();

                    entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                    entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                    entagePageHolder.followed.setEnabled(false);
                    entagePageHolder.notification.setEnabled(false);

                    final String topic = Topics.getTopicsNotifying(entagePage.getEntage_id());
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .removeValue();

                                        myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .child(mContext.getString(R.string.field_is_notifying))
                                                .setValue(false);

                                        myRef.child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .child(mContext.getString(R.string.field_is_notifying))
                                                .setValue(false);

                                        globalVariable.getFollowingData().get(entagePage.getEntage_id()).setIs_notifying(false);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);

                                    }else {
                                        entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
    }

    private boolean checkIsUserAnonymous(){
        if(isUserAnonymous){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            ((Activity)mContext).overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            return true;
        }else {
            return false;
        }
    }

    private boolean isUserPage(EntagePageShortData entagePage){

        boolean boo = false;
        for(String id :  entagePage.getUsers_ids()){
            if(user_id.equals(id)){
                boo = true;
                Toast.makeText(mContext, mContext.getString(R.string.error_cant_follow),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return boo;
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }

}
