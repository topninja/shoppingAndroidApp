package com.entage.nrd.entage.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.Models.OptionsItem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomListView;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterFieldsOptions extends RecyclerView.Adapter{
    private static final String TAG = "AdapterFieldsDescr";

    private static final int VIEW = 0;

    private Context mContext;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private View.OnClickListener onClickListenerHide, onClickListenerDelete;
    private int swapPosition = -1;

    private Transition transition;
    private String hide, show;
    private ArrayList<OptionsItem> optionsItems;
    private HashMap<String, ArrayList<String> > myOptionsDb;
    private View.OnClickListener onClickListenerEditList;

    public AdapterFieldsOptions(Context context, RecyclerView recyclerView, ArrayList<OptionsItem> optionsItems,
                                HashMap<String, ArrayList<String> > myOptionsDb, View.OnClickListener onClickListenerEditList){
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.optionsItems = optionsItems;
        this.myOptionsDb = myOptionsDb;
        this.onClickListenerEditList = onClickListenerEditList;

        hide = mContext.getString(R.string.hide);
        show = mContext.getString(R.string.show);
        setupOnClickListener();

        transition = new Slide(Gravity.TOP);
        transition.setDuration(400);
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder{
        TextView titleText, remove, hide_show, count, edit_my_options;
        RelativeLayout relativeLayout;
        ImageView move, setHere;
        CustomListView listView;

        private TitleViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title);
            remove = itemView.findViewById(R.id.remove);
            hide_show = itemView.findViewById(R.id.hide_show);
            listView =  itemView.findViewById(R.id.listView);
            relativeLayout = itemView.findViewById(R.id.relLayout);
            move = itemView.findViewById(R.id.move);
            setHere = itemView.findViewById(R.id.set_here);
            count = itemView.findViewById(R.id.count);
            edit_my_options = itemView.findViewById(R.id.edit_my_options);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == VIEW){
            view = layoutInflater.inflate(R.layout.template_add_option, parent, false);
            viewHolder = new AdapterFieldsOptions.TitleViewHolder(view);

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterFieldsOptions.TitleViewHolder) {
            TitleViewHolder viewHolder = (TitleViewHolder) holder;

            setupListView(optionsItems.get(position), viewHolder.listView, viewHolder.count);

            viewHolder.titleText.setText((position+1)+ ": " + optionsItems.get(position).getTitle());
            viewHolder.count.setText("( "+optionsItems.get(position).getSelectedOptions().size()+" )");

            viewHolder.hide_show.setOnClickListener(onClickListenerHide);
            viewHolder.remove.setOnClickListener(onClickListenerDelete);

            if(myOptionsDb != null && myOptionsDb.containsKey(optionsItems.get(position).getTitle())){
                viewHolder.edit_my_options.setVisibility(View.VISIBLE);
                viewHolder.edit_my_options.setOnClickListener(onClickListenerEditList);
            }else {
                viewHolder.edit_my_options.setVisibility(View.GONE);
                viewHolder.edit_my_options.setOnClickListener(null);
            }

        }
    }

    @Override
    public int getItemCount() {
        return optionsItems.size();
    }

    private void setupListView(final OptionsItem optionsItem, final ListView listView, final TextView countText){
        Log.d(TAG, "setupListView: " + optionsItem.getOptions());
        ArrayAdapter<String> adapter_listView = new ArrayAdapter<>(mContext, R.layout.custom_list_item_multiple_choice,
                optionsItem.getOptions());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + position + " : " + listView.isItemChecked(position));
                if(listView.isItemChecked(position)){
                    optionsItem.getSelectedOptions().add((String)listView.getItemAtPosition(position));
                }else {
                    optionsItem.getSelectedOptions().remove(listView.getItemAtPosition(position));
               }
                countText.setText("( "+optionsItem.getSelectedOptions().size()+" )");
            }
        });
        listView.setAdapter(adapter_listView);

        ArrayList<String> remove = new ArrayList<>(); // if case in change list of options
        for(String item : optionsItem.getSelectedOptions()){
            if(optionsItem.getOptions().contains(item)){
                listView.setItemChecked(optionsItem.getOptions().indexOf(item), true);
            }else {
                remove.add(item);
            }
        }

        optionsItem.getSelectedOptions().removeAll(remove);
    }

    private void setupOnClickListener(){
        onClickListenerHide = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout = (RelativeLayout) v.getParent().getParent();
                TextView textView = (TextView) v;
                if(textView.getText().equals(hide)){

                    relativeLayout.getChildAt(1).setVisibility(View.GONE);
                    textView.setText(show);

                }else {
                    transition.addTarget(relativeLayout.getChildAt(1));
                    TransitionManager.beginDelayedTransition(relativeLayout, transition);
                    relativeLayout.getChildAt(1).setVisibility(View.VISIBLE);
                    //UtilitiesMethods.expand(relativeLayout.getChildAt(1));
                    textView.setText(hide);
                }
            }
        };

        onClickListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
                confirmDeleting(itemPosition);
            }
        };
    }

    private void confirmDeleting(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//

        String type = optionsItems.get(position).getTitle();

        builder.setTitle(mContext.getString(R.string.remove) +" "+ (position+1) + ": "+ type);

        builder.setPositiveButton(mContext.getString(R.string.remove), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               // fetchData();

                optionsItems.remove(position);
                notifyItemRemoved(position);

                //notifyDataSetChanged();
                //notifyItemRangeChanged(position, descriptions.size()-position+1);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void clear(){
        if(optionsItems.size() > 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//

            builder.setTitle(mContext.getString(R.string.remove_all));

            builder.setPositiveButton(mContext.getString(R.string.remove), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    optionsItems.clear();
                    notifyDataSetChanged();

                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void hideAll(){

       /* for (int i = 0; i < linearLayoutManager.getItemCount(); i++) {
            String data = null;
            RecyclerView.ViewHolder holder =  recyclerView.findViewHolderForAdapterPosition(i);
            if(holder instanceof  TitleViewHolder){
                UtilitiesMethods.collapse(((TitleViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  SubTitleViewHolder){
                UtilitiesMethods.collapse(((SubTitleViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  TextViewHolder){
                UtilitiesMethods.collapse(((TextViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  ImageViewHolder){
                UtilitiesMethods.collapse(((ImageViewHolder) holder).relLayou);
            }
        }*/
    }

    public void showAll(){

       /* for (int i = 0; i < linearLayoutManager.getItemCount(); i++) {
            String data = null;
            RecyclerView.ViewHolder holder =  recyclerView.findViewHolderForAdapterPosition(i);
            if(holder instanceof  TitleViewHolder){
                UtilitiesMethods.expand(((TitleViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  SubTitleViewHolder){
                UtilitiesMethods.expand(((SubTitleViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  TextViewHolder){
                UtilitiesMethods.expand(((TextViewHolder) holder).relLayou);
            }
            else  if(holder instanceof  ImageViewHolder){
                UtilitiesMethods.expand(((ImageViewHolder) holder).relLayou);
            }
        }*/
    }

}
