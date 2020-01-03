package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

import java.util.ArrayList;

public class AdapterFieldsSpecification extends RecyclerView.Adapter{
    private static final String TAG = "AdapterFieldsSpeci";

    private static final int TEXT = 0;

    private Context mContext;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private String hide, show;
    private View.OnClickListener onClickListenerHide, onClickListenerDelete, onClickListenerSwap, onClickListenerSetHere;
    private int swapPosition = -1;

    private ArrayList<DataSpecifications> specifications;

    public AdapterFieldsSpecification(Context context, RecyclerView recyclerView, ArrayList<DataSpecifications> specifications,
                                      LinearLayoutManager linearLayoutManager){
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.specifications = specifications;
        this.linearLayoutManager = linearLayoutManager;

        hide = mContext.getString(R.string.hide);
        show = mContext.getString(R.string.show);

        setupOnClickListener();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder{
        TextView title, remove, hide_show;
        EditText editText;
        ImageView move, setHere;
        RelativeLayout relLayou;

        private TextViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            remove = itemView.findViewById(R.id.remove);
            hide_show = itemView.findViewById(R.id.hide_show);
            editText = itemView.findViewById(R.id.edit_text);
            move = itemView.findViewById(R.id.move);
            setHere = itemView.findViewById(R.id.set_here);
            relLayou = itemView.findViewById(R.id.relLayou1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TEXT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == TEXT){
            view = layoutInflater.inflate(R.layout.template_add_description, parent, false);
            viewHolder = new AdapterFieldsSpecification.TextViewHolder(view);

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterFieldsSpecification.TextViewHolder){
            TextViewHolder viewHolder = (TextViewHolder) holder;

            viewHolder.editText.setVisibility(View.VISIBLE);
            viewHolder.editText.setText(specifications.get(position).getData());
            viewHolder.editText.setHint(specifications.get(position).getSpecifications());

            viewHolder.title.setText((position+1) +": "+specifications.get(position).getSpecifications());
            viewHolder.move.setVisibility(View.VISIBLE);
            viewHolder.setHere.setVisibility(View.GONE);

            if(swapPosition != -1){
                viewHolder.move.setVisibility(View.GONE);
                if(swapPosition != position){
                    viewHolder.setHere.setVisibility(View.VISIBLE);
                }
            }

            viewHolder.setHere.setOnClickListener(onClickListenerSetHere);
            viewHolder.move.setOnClickListener(onClickListenerSwap);
            viewHolder.hide_show.setOnClickListener(onClickListenerHide);
            viewHolder.remove.setOnClickListener(onClickListenerDelete);
        }
    }

    @Override
    public int getItemCount() {
        return specifications.size();
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        //Log.d(TAG, "onViewDetachedFromWindow: Detached: " + holder.getAdapterPosition());
        String data = null;
        int position = holder.getAdapterPosition();
        if(position != -1){
            if(holder instanceof  TextViewHolder){
                data = ((TextViewHolder) holder).editText.getText().toString();
                specifications.get(position).setData(data);
            }
        }
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        //Log.d(TAG, "onViewDetachedFromWindow: Attached: " + holder.getAdapterPosition());
        super.onViewAttachedToWindow(holder);
    }

    private void setupOnClickListener(){
        onClickListenerHide = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout = (RelativeLayout) v.getParent().getParent();
                TextView textView = (TextView) v;
                if(textView.getText().equals(hide)){
                    UtilitiesMethods.collapse(relativeLayout.getChildAt(1));
                    textView.setText(show);
                }else {
                    UtilitiesMethods.expand(relativeLayout.getChildAt(1));
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

        onClickListenerSwap = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();

                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
                swapPosition = itemPosition;
                notifyDataSetChanged();
            }
        };

        onClickListenerSetHere = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();

                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
                if(swapPosition != -1){
                    swap(itemPosition);

                }else {
                    notifyDataSetChanged();
                }
            }
        };
    }

    private void confirmDeleting(final int position){
        disableSwapMode();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//

        String type = specifications.get(position).getSpecifications();

        builder.setTitle(mContext.getString(R.string.remove) +" "+ (position+1) + ": "+ type);

        builder.setPositiveButton(mContext.getString(R.string.remove), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fetchData();

                specifications.remove(position);
                notifyItemRemoved(position);

                notifyDataSetChanged();
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

    private void swap(int position) {
        DataSpecifications description = specifications.get(swapPosition);

        specifications.remove(swapPosition);
        specifications.add(position, description);

        swapPosition = -1;

        notifyDataSetChanged();
    }

    public void disableSwapMode(){
        if(swapPosition != -1){
            fetchData();

            swapPosition = -1;
            notifyDataSetChanged();
        }
    }

    private ArrayList<DataSpecifications> removeAllEmptyData(){
        ArrayList<DataSpecifications> arrayList = new ArrayList<>();
        for(int i=0 ; i <specifications.size() ; i++){
            String data =  specifications.get(i).getData();

            if(data != null && data.length()>0){
                arrayList.add(specifications.get(i));
            }
        }

        return arrayList;
    }

    public ArrayList<DataSpecifications> getSpecification() {
        //removeAllEmptyData();
        fetchData();

        return removeAllEmptyData();
    }

    public void clear(boolean confirm){
        disableSwapMode();

        if(specifications.size() > 0){
            if(confirm){
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//

                builder.setTitle(mContext.getString(R.string.remove_all));

                builder.setPositiveButton(mContext.getString(R.string.remove), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        specifications.clear();
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

            }else {

                specifications.clear();
                notifyDataSetChanged();
            }
        }
    }

    public void fetchData(){
        for (int i = 0; i < linearLayoutManager.getItemCount(); i++) {
            String data = null;
            RecyclerView.ViewHolder holder =  recyclerView.findViewHolderForAdapterPosition(i);
            if(holder instanceof  TextViewHolder){
                data = ((TextViewHolder) holder).editText.getText().toString();
                specifications.get(i).setData(data);
            }
        }
    }

    public void hideAll(){
        disableSwapMode();

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
        disableSwapMode();

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

    public ArrayList<String> getNamesSpecification(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(DataSpecifications specification : specifications){
            arrayList.add(specification.getSpecifications());
        }
        return arrayList;
    }

}
