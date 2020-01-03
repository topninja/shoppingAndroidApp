package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class CustomAdapterListView_SavedSpecifications extends ArrayAdapter<String> {
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
        ImageView delete, preview;
        ProgressBar progressBar;
    }

    public CustomAdapterListView_SavedSpecifications(Context context, int resource, ArrayList<String> data,
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
            viewHolder.delete =  convertView.findViewById(R.id.delete);
            viewHolder.preview =  convertView.findViewById(R.id.preview);
            viewHolder.progressBar = convertView.findViewById(R.id.progressBar_0);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.name.setText(getItem(position));
        viewHolder.delete.setVisibility(View.VISIBLE);
        viewHolder.progressBar.setVisibility(View.GONE);

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDescriptions(getItem(position), position, viewHolder.delete ,viewHolder.progressBar);
            }
        });

        viewHolder.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview(position);
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }

    public void deleteDescriptions(final String name, final int position, final View view , final ProgressBar progressBar){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.delete_specifications) + " " + name);
        builder.setMessage("");
        builder.setPositiveButton(mContext.getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                view.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_entage_pages_settings))
                        .child(entagePageId)
                        .child(mContext.getString(R.string.field_saved_group_specifications))
                        .child(name)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                view.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                remove(getItem(position));
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                view.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                if(e.getMessage().equals("Permission denied")){
                                    Toast.makeText(mContext, mContext.getString(R.string.error_permission_denied),
                                            Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(mContext, mContext.getString(R.string.error_internet),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) ;

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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