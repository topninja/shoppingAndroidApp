package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageAdminData;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.newItem.AddNewItemActivity;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.Topics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingDeletePageFragment extends Fragment {
    private static final String TAG = "SettingDeletePageFrag";

    private OnActivityListener mOnActivityListener;
    private View view;
    private Context mContext;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private ImageView back;
    private RecyclerView recyclerView;
    private AdapterDeletePage adapterDeletePage;
    private ArrayList<EntagePage> entagePages;

    private View.OnClickListener onClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_delete_items, container, false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnActivityListener = (OnActivityListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickListener();

        setupAdapter();
        fetchItems();
    }

    private void initWidgets() {
        back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
    }

    private void onClickListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);

                confirmDelete(entagePages.get(itemPosition));
            }
        };
    }

    private void setupAdapter(){
        recyclerView =  view.findViewById(R.id.recyclerView_items);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        recyclerView.setVisibility(View.VISIBLE);
        entagePages = new ArrayList<>();
        adapterDeletePage = new AdapterDeletePage(mContext, entagePages, onClickListener);
        recyclerView.setAdapter(adapterDeletePage);
    }

    private void fetchItems(){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_pages));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int x = 0;
                if(dataSnapshot.exists()){
                    // keys
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        entagePages.add(snapshot.getValue(EntagePage.class));
                    }
                }

                adapterDeletePage.notifyDataSetChanged();
                view.findViewById(R.id.progressBarLoadMore).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void confirmDelete(final EntagePage entagePageAdminData){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle("حذف:  " + entagePageAdminData.getEntage_id());

        builder.setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete(entagePageAdminData);
            }
        });
        builder.setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void delete(EntagePage entagePage){

        String userId = entagePage.getUsers_ids().get(0);
        String entajiPageId = entagePage.getEntage_id();

        // entage_pages_admin_data
        myRef.child(mContext.getString(R.string.dbname_entage_pages_admin_data)).child(userId).child(entajiPageId).removeValue();

        // entage_pages_access
        myRef.child(mContext.getString(R.string.dbname_entage_pages_access)).child(userId).child(entajiPageId).removeValue();

        // entage_pages_settings
        myRef.child(mContext.getString(R.string.dbname_entage_pages_settings)).child(entajiPageId).removeValue();

        // entage_pages
        myRef.child(mContext.getString(R.string.dbname_entage_pages)).child(entajiPageId).removeValue();

        // Name Entage Page
        myRef.child(mContext.getString(R.string.dbname_entaji_pages_names)).child(entajiPageId).removeValue();

        // entage_pages_subscription
        myRef.child(mContext.getString(R.string.dbname_entage_pages_subscription)).child(entajiPageId).removeValue();

        //subscribesToTopics
        String topic_1 = "users_entage_page"; // USERS_ENTAGE_PAGE
        String topic_2 = "admin_users_entage_page"+entajiPageId; // ADMIN_USERS_ENTAGE_PAGE + entagePageId
        String topic_3 = "customers_in_entage_page"+entajiPageId; // CUSTOMER_IN_ENTAGE_PAGE + entagePageId
        //myRef.child(mContext.getString(R.string.dbname_users_subscribes)).child(userId).child(topic_1).removeValue();
        //myRef.child(mContext.getString(R.string.dbname_users_subscribes)).child(userId).child(topic_2).removeValue();
        //myRef.child(mContext.getString(R.string.dbname_users_subscribes)).child(userId).child(topic_3).removeValue();

        //FirebaseMessaging.getInstance().subscribeToTopic("", topic_1);
        //FirebaseMessaging.getInstance().send(new RemoteMessage(new Bundle()));

        // Page and Item Status
        myRef.child(mContext.getString(R.string.dbname_entage_pages_status)).child(entajiPageId).removeValue();

        // mNotificationOnApp
        myRef.child(mContext.getString(R.string.dbname_entage_page_email_messages)).child(entajiPageId).removeValue();

        // delete item list
        myRef.child(mContext.getString(R.string.dbname_entage_page_categories)).child(entajiPageId).removeValue();

        // delete items

        // delete entage_pages_by_categories
        for(String path : entagePage.getCategories_entage_page()){
            ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
            DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_entage_pages_by_categories));
            for(String catId : cat){
                databaseReference = databaseReference.child(catId);

                databaseReference.child("entage_page_id")
                        .child(entajiPageId)
                        .removeValue();
            }
        }

        // delete from algolia searching
        getObjectIdFromAlgolia(entagePage.getEntage_id());


        entagePages.remove(entagePage);
        adapterDeletePage.notifyDataSetChanged();
    }

    private void getObjectIdFromAlgolia(String entagPageId){
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(entagPageId)
                .setAttributesToRetrieve("objectID");
        //.setHitsPerPage(50);

        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        Client client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
        Index index_items = client.getIndex("entage_pages");

        index_items.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                try {
                    if(content != null){
                        JSONArray jsonArray = content.getJSONArray("hits");
                        for(int i=0 ; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String objectId = jsonObject.get("objectID").toString();

                            String APIKey = "cf51400386c025e21bbbff240f715906";
                            GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
                            Client client = new Client(globalVariable.getApplicationID(), APIKey);
                            Index index_items = client.getIndex("entage_pages");
                            index_items.deleteObjectAsync(objectId, null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
    }
}
