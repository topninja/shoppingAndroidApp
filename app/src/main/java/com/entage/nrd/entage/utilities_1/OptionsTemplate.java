package com.entage.nrd.entage.utilities_1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class OptionsTemplate extends RelativeLayout {
    private static final String TAG = "DescriptionTemplate";

    public LayoutInflater mInflater;
    public View view;
    public Context context;

    public TextView titleText, remove, hide_show, count, edit_my_options;
    public RelativeLayout relativeLayout;
    public ImageView move, setHere;
    public CustomListView listView;

    private ArrayAdapter<String> adapter_listView ;

    public ArrayList<String> items, selectedItems;
    public String title;

    public OptionsTemplate(Context context , String title, ArrayList<String> items, ArrayList<String> selectedItems) {
        super(context);
        this.context = context;
        this.title = title;
        this.items = items;
        this.selectedItems = selectedItems;
        mInflater = LayoutInflater.from(context);
        init();

    }

    public OptionsTemplate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public OptionsTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init() {
        view = mInflater.inflate(R.layout.template_add_option, this, true);
        titleText = view.findViewById(R.id.title);
        remove = view.findViewById(R.id.remove);
        hide_show = view.findViewById(R.id.hide_show);
        listView =  view.findViewById(R.id.listView);
        relativeLayout = view.findViewById(R.id.relLayout);
        move = view.findViewById(R.id.move);
        setHere = view.findViewById(R.id.set_here);
        count = view.findViewById(R.id.count);
        edit_my_options = view.findViewById(R.id.edit_my_options);


        titleText.setText(title);
        setupListView();

        onClickListener();
    }

    private void setupListView(){
        adapter_listView = new ArrayAdapter<String>(context,
                R.layout.custom_list_item_multiple_choice, items);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + position + " : " + listView.isItemChecked(position));
                if(listView.isItemChecked(position)){
                    selectedItems.add((String)listView.getItemAtPosition(position));
                    count.setText("( "+String.valueOf(selectedItems.size())+" )");
                }else {
                    selectedItems.remove((String)listView.getItemAtPosition(position));
                    count.setText("( "+String.valueOf(selectedItems.size())+" )");
                }
            }
        });
        listView.setAdapter(adapter_listView);

        ArrayList<String> remove = new ArrayList<>(); // if case in change list of options
        for(String item : selectedItems){
            if(items.contains(item)){
                listView.setItemChecked(items.indexOf(item), true);
            }else {
                remove.add(item);
            }
        }

        selectedItems.removeAll(remove);
        if(listView.getCheckedItemCount() != 0){
            count.setText("( "+String.valueOf(listView.getCheckedItemCount())+" )");
        }
    }

    private void onClickListener() {
        hide_show.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getVisibility() == View.VISIBLE){
                    hideView();
                }else {
                    showView();
                }
            }
        });
    }

    public void hideView(){
        /*Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(400);
        transition.addTarget(listView);
        TransitionManager.beginDelayedTransition((ViewGroup) listView.getParent(), transition);*/
        listView.setVisibility(View.GONE);

        hide_show.setText(context.getString(R.string.show));
    }

    public void showView(){
        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(400);
        transition.addTarget(listView);
        TransitionManager.beginDelayedTransition((ViewGroup) listView.getParent(), transition);
        listView.setVisibility(View.VISIBLE);

        hide_show.setText(context.getString(R.string.hide));
    }

    public void setDefaultMode(){
        setHere.setVisibility(View.GONE);
        setHere.setColorFilter(context.getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
        setHere.setEnabled(true);
        move.setVisibility(View.VISIBLE);
    }

    public TextView getRemove() {
        return remove;
    }

    public String getTitle() {
        return titleText.getText().toString();
    }

    public ArrayList<String> getSelectedItems() {
        return selectedItems;
    }

    public TextView getEdit_my_options() {
        return edit_my_options;
    }
}
