package com.entage.nrd.entage.personal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentEditAuthInfo extends Fragment{
    private static final String TAG = "FragmentEditPerso";

    private OnActivityListener mOnActivityListener;

    private Context mContext;
    private View view;
    private ListView listView;
    private ArrayList<String> optionsNames;
    private HashMap<String, Fragment> optionsFragments;

    private ImageView backArrow;
    private TextView titlePage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_auth_info , container , false);
        mContext = getActivity();

        inti();

        return view;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti(){
        initWidgets();
        setupSettingsList();

        onClickListener();
    }

    private void initWidgets(){
        backArrow = (ImageView) view.findViewById(R.id.back);
        listView = (ListView) view.findViewById(R.id.listViewOptions);
        titlePage = (TextView) view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.my_auth_info));

        backArrow.setVisibility(View.VISIBLE);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void openFragment(String nameOption){
        Fragment fragment = optionsFragments.get(nameOption);
        if ( fragment != null){
            mOnActivityListener.onActivityListener(fragment);
        }
    }

    private void setupSettingsList(){
        Log.d(TAG, "setupSettingsList: intitializing 'Entage Setting' list");

        // Create an ArrayAdapter from List
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (mContext , android.R.layout.simple_list_item_1, myList()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text size 25 dip for ListView each item
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);

                // Return the view
                return view;
            }
        };

        //ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1 , myList() );
        listView.setAdapter(arrayAdapter);
        /*CustomListAdapter adapter = new CustomListAdapter(mContext, generateData());
        listView.setAdapter(adapter);*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id){
                Log.d(TAG, "onItemClick: navigating to fargment: " + postion);
                openFragment(optionsNames.get(postion));
            }
        });
    }

    private ArrayList<String> myList(){
        optionsNames = new ArrayList<>();
        optionsFragments = new HashMap<String, Fragment> ();

        optionsNames.add(mContext.getString(R.string.change_username)); // fragment 0
        optionsFragments.put(optionsNames.get(0), new FragmentChangeUsername());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getEmail()!= null && firebaseUser.getEmail().length() > 0){
            optionsNames.add(mContext.getString(R.string.change_email));// fragment 1
            optionsFragments.put(optionsNames.get(1), new FragmentChangeEmail());

            optionsNames.add(mContext.getString(R.string.change_password)); // fragment 2
            optionsFragments.put(optionsNames.get(2), new FragmentChangePassword());
        }

        if (firebaseUser.getPhoneNumber()!= null && firebaseUser.getPhoneNumber().length() > 0){
            optionsNames.add(mContext.getString(R.string.change_phone));// fragment 1
            optionsFragments.put(optionsNames.get(1), new FragmentChangePhoneNumber());
        }

        return optionsNames;
    }


}
