package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingAppFragment extends Fragment {
    private static final String TAG = "SettingAppFragment";

    private OnActivityListener mOnActivityListener;
    private View view;
    private Context mContext;

    private ImageView back;
    private ListView listView;
    private InputMethodManager imm;
    private ArrayAdapter<String> adapterListView;
    private HashMap<String, Fragment> fragmentHashMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_setting_app , container , false);
        mContext = getActivity();
        inti();

        return view;
    }

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
        onClickListener();

        setupListView();
    }

    private void initWidgets() {
        listView = view.findViewById(R.id.listView);
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
    }

    private void setupListView() {

        fragmentHashMap = new HashMap<>();
        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add(mContext.getString(R.string.setting_categories));
        arrayList.add(mContext.getString(R.string.delete_item));
        arrayList.add(mContext.getString(R.string.edit_entage_pages));
        arrayList.add("حذف الصفحة");

        fragmentHashMap.put(mContext.getString(R.string.setting_categories), new SettingCategoriesFragment());
        fragmentHashMap.put(mContext.getString(R.string.delete_item), new SettingDeleteItemsFragment());
        fragmentHashMap.put(mContext.getString(R.string.edit_entage_pages), new SettingEditePagesFragment());
        fragmentHashMap.put("حذف الصفحة", new SettingDeletePageFragment());


        adapterListView = new ArrayAdapter<String>(mContext,  android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapterListView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                mOnActivityListener.onActivityListener(fragmentHashMap.get(value));
            }
        });
    }
}
