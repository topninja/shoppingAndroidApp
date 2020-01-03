package com.entage.nrd.entage.emails;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterEmail;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class FragmentMyNotifications extends Fragment {
    private static final String TAG = "FragmentMessages";

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, referenceSubscriptions;
    private String user_id;

    private Context mContext ;
    private View view;

    private RecyclerView recyclerView;
    private AdapterEmail newsAdapter;
    private ArrayList<NotificationOnApp> news;
    private String entajiPagesIds;

    private Map<Date, String> messagesUnread, messagesRead;
    private boolean isRemovedProgress = false;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_recycler_view, container , false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();

            init();
        }
        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entajiPagesIds =  bundle.getString("entajiPagesIds");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();
        setupAdapter();

        if(entajiPagesIds == null){
            fetchMySubscriptions();
        }
        fetchMyEmailsNotifications();
    }

    private void initWidgets() {
        messagesUnread = new TreeMap<>(Collections.reverseOrder());
        messagesRead = new TreeMap<>(Collections.reverseOrder());
    }

    private void onClickListener() {
    }

    private void setupAdapter(){
        recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        news = new ArrayList<>();
        news.add(null);news.add(null);news.add(null);
        news.add(null);news.add(null);news.add(null);
        news.add(null);news.add(null);news.add(null);

        newsAdapter = new AdapterEmail(mContext, recyclerView, news);
        recyclerView.setAdapter(newsAdapter);
    }

    private void fetchMySubscriptions(){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_subscribes))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<String> arrayList = new ArrayList<>();
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        String topic = singleSnapshot.getKey();
                        arrayList.add(topic);
                    }
                    fetchSubscriptionNotifications(arrayList);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void fetchSubscriptionNotifications(ArrayList<String> topics){
        for(final String topic : topics){
            Query query = referenceSubscriptions
                    .child(topic)
                    .child(mContext.getString(R.string.field_notifications));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                            String key = singleSnapshot.getKey();
                            NotificationOnApp notification = singleSnapshot.getValue(NotificationOnApp.class);

                            if (notification != null) {
                                if(singleSnapshot.child("users_read").child(user_id).exists()){ // read
                                    notification.setIs_read(true);
                                    messagesRead.put( DateTime.getTimestamp(notification.getTime()), key);
                                    addToAdapter_read(key, notification);

                                }else { //  unread
                                    notification.setIs_read(false);
                                    messagesUnread.put(DateTime.getTimestamp(notification.getTime()), key);
                                    addToAdapter_unread(key, notification, topic);
                                }
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private void fetchMyEmailsNotifications() {
        if(myRef != null){
            final ArrayList<String> arrayList = new ArrayList<>();

            Query query1 = myRef
                    .orderByChild("is_read")
                    .equalTo(false);
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    removeProgressBar();
                    if(dataSnapshot.exists()){
                        for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                            String key = singleSnapshot.getKey();
                            NotificationOnApp notification = singleSnapshot.getValue(NotificationOnApp.class);
                            if (notification != null) {
                                arrayList.add(key);
                                messagesUnread.put( DateTime.getTimestamp(notification.getTime()), key);
                                addToAdapter_unread(key, notification, null);
                            }
                        }
                    }

                    Query query = myRef
                            .orderByChild("is_read")
                            .equalTo(true);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            removeProgressBar();
                            if(dataSnapshot.exists()){
                                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                    String key = singleSnapshot.getKey();
                                    NotificationOnApp notification = singleSnapshot.getValue(NotificationOnApp.class);
                                    if (notification != null && !arrayList.contains(key)) {
                                        messagesRead.put( DateTime.getTimestamp(notification.getTime()), key);
                                        addToAdapter_read(key, notification);
                                    }
                                }
                            }

                            if(news.size() == 0){
                                ((TextView)view.findViewById(R.id.text0)).setText(mContext.getString(R.string.notifications_list_empty));
                                view.findViewById(R.id.text0).setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void setRead(String idNotification){
        myRef.child(idNotification).child("is_read").setValue(true);
    }

    private void removeProgressBar(){
        if(!isRemovedProgress){
            isRemovedProgress  =true;
            if(news.size()>0 && news.get(0) == null){
                news.clear();
                newsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void addToAdapter_read(String notificationId, NotificationOnApp notification){
        removeProgressBar();

        ArrayList keys = new ArrayList(messagesRead.values());
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(notificationId)){
                index = i;
                break;
            }
        }

        index = index + messagesUnread.size();

        news.add(index, notification);
        newsAdapter.notifyItemInserted(index);
        recyclerView.scrollToPosition(0);
    }

    private void addToAdapter_unread(String notificationId, NotificationOnApp notification, String topic){
        removeProgressBar();

        ArrayList keys = new ArrayList(messagesUnread.values());
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(notificationId)){
                index = i;
                break;
            }
        }

        news.add(index, notification);
        newsAdapter.notifyItemInserted(index);
        recyclerView.scrollToPosition(0);
        setRead(notificationId, topic);
    }

    private void setRead(String idNotification, String topic){
        if(topic != null){
            referenceSubscriptions.child(topic)
                    .child(mContext.getString(R.string.field_notifications))
                    .child(idNotification)
                    .child("users_read")
                    .child(user_id)
                    .child("user_id")
                    .setValue(user_id);
        }else {
            myRef.child(idNotification).child("is_read").setValue(true);
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
    */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        referenceSubscriptions = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_subscriptions_emails));

        if(entajiPagesIds != null){
            myRef = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_page_email_notifications))
                    .child(entajiPagesIds);

        }else{
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser != null){
                user_id = firebaseUser.getUid();
                myRef = mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_users_email_notifications))
                        .child(user_id);

            }else {
                ((TextView)view.findViewById(R.id.text0)).setText(mContext.getString(R.string.need_to_sign_in));
                view.findViewById(R.id.text0).setVisibility(View.VISIBLE);
            }
        }
    }
}
