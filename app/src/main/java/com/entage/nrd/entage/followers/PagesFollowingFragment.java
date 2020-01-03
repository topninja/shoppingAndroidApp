package com.entage.nrd.entage.followers;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.entage.nrd.entage.Models.EntagePageShortData;
import com.entage.nrd.entage.Models.Following;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.EntagePagesAdapter;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class PagesFollowingFragment extends Fragment {
    private static final String TAG = "PagesFollowingFr";

    //vars
    private ArrayList<EntagePageShortData> resultEntagePages;
    private EntagePagesAdapter entagePagesAdapter;
    private RecyclerView recyclerView;

    private View view;
    private Context mContext;
    private DatabaseReference reference;
    private String user_id;
    private FirebaseAuth mAuth;

    private ImageView backArrow;
    private TextView titlePage;
    private GlobalVariable globalVariable;
    private boolean isUserAnonymous;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_recycler_view_bar, container , false);
            mContext = getActivity();

            setupFirebaseAuth();
            init();
        }

        return view;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setDataToList();
        getFollowingPageIds();
    }

    private void initWidgets(){
        resultEntagePages = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);

        backArrow = (ImageView) view.findViewById(R.id.back);
        titlePage = (TextView) view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.following));
        backArrow.setVisibility(View.VISIBLE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void getFollowingPageIds(){
        Log.d(TAG, "getFollowing: searching for following");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_following_user))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        getPages(singleSnapshot.getKey());
                    }

                }else {
                    recyclerView.setVisibility(View.GONE);
                    view.findViewById(R.id.text0).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setDataToList(){
        Log.d(TAG, "updateUsersList: updating users list");

        recyclerView =  view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        resultEntagePages = new ArrayList<>();
        resultEntagePages.add(null);resultEntagePages.add(null);resultEntagePages.add(null);
        resultEntagePages.add(null);resultEntagePages.add(null);resultEntagePages.add(null);
        resultEntagePages.add(null);resultEntagePages.add(null);resultEntagePages.add(null);

        entagePagesAdapter = new EntagePagesAdapter(mContext,recyclerView, resultEntagePages);
        recyclerView.setAdapter(entagePagesAdapter);
    }

    private void getPages(String idPage){
        Query query = reference
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(idPage);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(resultEntagePages.get(0) == null){
                        resultEntagePages.clear();
                        entagePagesAdapter.notifyDataSetChanged();
                    }

                    int index = resultEntagePages.size();
                    resultEntagePages.add(index, dataSnapshot.getValue(EntagePageShortData.class));
                    entagePagesAdapter.notifyItemInserted(index);

                    String id = resultEntagePages.get(index).getEntage_id();
                    if(!isUserAnonymous && !globalVariable.getFollowingData().containsKey(id)) {
                        checkFollowing(id, index);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkFollowing(final String entagePageId, final int index){
        Query query = reference
                .child(mContext.getString(R.string.dbname_following_user))
                .child(user_id)
                .child(entagePageId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    globalVariable.getFollowingData().put(entagePageId, dataSnapshot.getValue(Following.class));
                }else {
                    globalVariable.getFollowingData().put(entagePageId, null);
                }
                entagePagesAdapter.notifyItemChanged(index);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }

}
