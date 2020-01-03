package com.entage.nrd.entage.home;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterGridItems;
import com.entage.nrd.entage.utilities_1.EntagePageSearchingAdapter;

public class ViewResultSearchingFragment extends Fragment {
    private static final String TAG = "ViewResultSearchi";

    private Context mContext ;
    private View view;

    private AdapterGridItems adapterItems;
    private EntagePageSearchingAdapter entagePagesAdapter;

    private String facet, facetText, text;
    private String indexNameAtAlgolia;
    private boolean searchingForItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_grid_image, container, false);
            mContext = getActivity();

            getIncomingBundle();
            init();
        }
        return view;
    }

    public void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                facet = bundle.getString("facet");
                facetText = bundle.getString("facetText");
                text = bundle.getString("text");
                indexNameAtAlgolia = bundle.getString("indexNameAtAlgolia");
                searchingForItems = bundle.getBoolean("searchingForItems");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {

        /*ProgressBar progressbar = view.findViewById(R.id.progressBar2);
        progressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);
        progressbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);*/

        initWidgets();
    }

    private void initWidgets() {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_items);
        LinearLayout no_result_found = view.findViewById(R.id.no_result_found);

        TextView notify_me_adding_in_categorie = view.findViewById(R.id.notify_me_adding_in_categorie);
        notify_me_adding_in_categorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        if(searchingForItems){
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
            recyclerView.setLayoutManager(gridLayoutManager);

            adapterItems = new AdapterGridItems(mContext, recyclerView,
                    facet, facetText, text, indexNameAtAlgolia, no_result_found);
            recyclerView.setAdapter(adapterItems);

        }else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
            recyclerView.setLayoutManager(linearLayoutManager);

            entagePagesAdapter = new EntagePageSearchingAdapter(mContext, recyclerView,
                    facet, facetText, text, indexNameAtAlgolia, no_result_found);
            recyclerView.setAdapter(entagePagesAdapter);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if(entagePagesAdapter != null){
            entagePagesAdapter.notifyDataSetChanged();
        }

        if(adapterItems != null){
            adapterItems.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
