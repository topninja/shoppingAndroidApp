package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.R;
import com.github.irshulx.Editor;
import com.github.irshulx.EditorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAdapterListView_SavedDescriptions extends ArrayAdapter<String> {
    private static final String TAG = "CustomAdapterListView_C";


    private ArrayList<String> dataSet;
    private Context mContext;
    private String entagePageId;
    private int resource;
    private List<DescriptionItem> descriptions;
    private View view;

    private Editor editor;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView delete, preview;
        ProgressBar progressBar;
    }

    public CustomAdapterListView_SavedDescriptions(Context context, int resource, ArrayList<String> data, String entagePageId,
                                                   List<DescriptionItem>  descriptions, View view) {
        super(context, resource, data);
        this.dataSet = data;
        this.resource = resource;
        this.mContext=context;
        this.entagePageId = entagePageId;
        this.descriptions = descriptions;
        this.view = view;

        setupAdapter();
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
                previewDescription(position);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    public void deleteDescriptions(final String name, final int position, final View view , final ProgressBar progressBar){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.delete_description) + " " + name);
        builder.setMessage("");
        builder.setPositiveButton(mContext.getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                view.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_entage_pages_settings))
                        .child(entagePageId)
                        .child(mContext.getString(R.string.field_saved_description))
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

    private void previewDescription(int position){
        final RelativeLayout relativeLayout = view.findViewById(R.id.preview_description_layout);
        TextView closeLayout = view.findViewById(R.id.close_layout);

        editor.clearAllContents();
        editor.render(descriptions.get(position).getContent_html());

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

    private void setupAdapter(){
        editor = view.findViewById(R.id.renderer);

        Map<Integer, String> headingTypeface = UtilitiesMethods.getHeadingTypeface();
        Map<Integer, String> contentTypeface = UtilitiesMethods.getContentface();
        editor.setHeadingTypeface(headingTypeface);
        editor.setContentTypeface(contentTypeface);
        editor.setDividerLayout(R.layout.tmpl_divider_layout);
        editor.setEditorImageLayout(R.layout.tmpl_image_view_render);
        editor.setListItemLayout(R.layout.tmpl_list_item);
    }


}