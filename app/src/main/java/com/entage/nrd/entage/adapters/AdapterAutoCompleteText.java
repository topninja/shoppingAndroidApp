package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;

import com.entage.nrd.entage.R;

public class AdapterAutoCompleteText extends ArrayAdapter {
    private static final String TAG = "AdapterAutoCompleteText";

    private ArrayList<String>  dataList;
    private Context mContext;
    private int itemLayout;
    private View.OnClickListener clickListener;

    private ListFilter listFilter = new ListFilter();
    private List<String> dataListAllItems;



    public AdapterAutoCompleteText(Context context, int resource, ArrayList<String> storeDataLst, View.OnClickListener clickListener ) {
        super(context, resource, storeDataLst);
        dataList = storeDataLst;
        mContext = context;
        itemLayout = resource;
        this.clickListener = clickListener;

        if(storeDataLst.size()>0){
            storeDataLst.add(mContext.getString(R.string.delete_list_searched));
        }
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public String getItem(int position) {
        Log.d("CustomListAdapter",
                dataList.get(position));
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }

        TextView strName = (TextView) view.findViewById(R.id.textView);

        if(getItem(position).equals(mContext.getString(R.string.delete_list_searched))){
            strName.setVisibility(View.GONE);
            TextView delete = (TextView) view.findViewById(R.id.delete);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(clickListener);
        }else {
            view.findViewById(R.id.delete).setVisibility(View.GONE);
            strName.setVisibility(View.VISIBLE);
            strName.setText(getItem(position));
        }
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (dataListAllItems == null) {
                synchronized (lock) {
                    dataListAllItems = new ArrayList<String>(dataList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = dataListAllItems;
                    results.count = dataListAllItems.size();
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                ArrayList<String> matchValues = new ArrayList<String>();

                for (String dataItem : dataListAllItems) {
                    if (dataItem.toLowerCase().startsWith(searchStrLowerCase)) {
                        matchValues.add(dataItem);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<String>)results.values;
            } else {
                dataList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
