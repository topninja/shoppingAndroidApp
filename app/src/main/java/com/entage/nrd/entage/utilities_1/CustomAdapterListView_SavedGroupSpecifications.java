package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import java.util.ArrayList;

public class CustomAdapterListView_SavedGroupSpecifications extends ArrayAdapter<String> {
    private static final String TAG = "CustomAdapterListView_C";

    private ArrayList<String> dataSet;
    private Context mContext;
    private String entagePageId;
    private int resource;
    private ArrayList<ArrayList<DataSpecifications>> specifications;
    private View view;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView preview;
    }

    public CustomAdapterListView_SavedGroupSpecifications(Context context, int resource, ArrayList<String> data,
                                                          String entagePageId, ArrayList<ArrayList<DataSpecifications>>  specifications,
                                                          View view) {
        super(context, resource, data);
        this.dataSet = data;
        this.resource = resource;
        this.mContext=context;
        this.entagePageId = entagePageId;
        this.specifications = specifications;
        this.view = view;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final String dataModel = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(resource,  parent, false);

            viewHolder.name =  convertView.findViewById(R.id.text);
            viewHolder.preview =  convertView.findViewById(R.id.preview);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.name.setText(getItem(position));

        viewHolder.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview(position);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void preview(int position){
        final RelativeLayout relativeLayout = view.findViewById(R.id.preview_specifications_layout);

        //relativeLayout.setVisibility(View.VISIBLE);
        TextView closeLayout = view.findViewById(R.id.close_layou);
        ((LinearLayout)  view.findViewById(R.id.titles)).removeAllViews();
        ((LinearLayout)  view.findViewById(R.id.texts)).removeAllViews();

        for(DataSpecifications dataSpecifications : specifications.get(position)){
            String title = dataSpecifications.getSpecifications();
            String data = dataSpecifications.getData();

            setupTextView(title, data);
        }
        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Transition transition = new Fade();
                Transition transition = new Slide(Gravity.BOTTOM);
                transition.setDuration(200);
                TransitionManager.beginDelayedTransition((ViewGroup) relativeLayout.getParent(), transition);
                relativeLayout.setVisibility(View.GONE);

            }
        });

        //Transition transition = new Fade();
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition((ViewGroup) relativeLayout.getParent(), transition);
        relativeLayout.setVisibility(View.VISIBLE);

    }

    private void setupTextView(String title, String text){
        TextView textViewTitle = new TextView(mContext);
        TextView textViewData = new TextView(mContext);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(0, 0, 0, 10);

        textViewTitle.setLayoutParams(layoutParams2);
        textViewData.setLayoutParams(layoutParams2);

        textViewTitle.setTextColor(mContext.getResources().getColor(R.color.black));
        textViewData.setTextColor(mContext.getResources().getColor(R.color.gray1));

        textViewTitle.setText(title);
        textViewData.setText(text);

        textViewTitle.setTextSize(18);

        textViewData.setTextSize(18);

        ((LinearLayout)  view.findViewById(R.id.titles)).addView(textViewTitle);
        ((LinearLayout)  view.findViewById(R.id.texts)).addView(textViewData);
    }



}