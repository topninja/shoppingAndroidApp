package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class CategoriesEditModeAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int TEXT_VIEW = 0;
    private static final int CHECKBOX = 1;

    private Context mContext;
    private TextView pathTextView;
    private ArrayList<String> paths;
    private CategorieWithChildren currentCategorie, parentCategorie;
    private String textCategories;
    private ArrayList<ArrayList<String>> selectedCategorie;
    private AutoCompleteTextView autoCompleteTextCat;
    private ArrayList<String> categoriesUsed;
    private MessageDialog messageDialog = new MessageDialog();


    public CategoriesEditModeAdapter(Context context, CategorieWithChildren categorie, TextView path,
                                     AutoCompleteTextView autoCompleteTextCat) {
        this.mContext = context;
        this.pathTextView = path;
        this.autoCompleteTextCat = autoCompleteTextCat;

        currentCategorie = categorie;

        selectedCategorie = new ArrayList<>();
        paths = new ArrayList<>();
        textCategories = mContext.getString(R.string.the_categories);

        categoriesUsed = new ArrayList<>();
    }

    public class TextViewViewHolder extends RecyclerView.ViewHolder{
        TextView text1;
        TextView remove;
        CheckBox checkBox;

        private TextViewViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
            remove = itemView.findViewById(R.id.remove);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TEXT_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        view = layoutInflater.inflate(R.layout.layout_categories_edit_mode, parent, false);
        viewHolder = new CategoriesEditModeAdapter.TextViewViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof CategoriesEditModeAdapter.TextViewViewHolder) {
            final String code = currentCategorie.getChildren().get(position);
            final String name = getName(code);

            ((TextViewViewHolder) holder).text1.setText(name+" _ " + code);

            ((CategoriesEditModeAdapter.TextViewViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewList(code);
                }
            });

            if(currentCategorie.getCategorieWithChildren().containsKey(currentCategorie.getChildren().get(position)) &&
                    currentCategorie.getCategorieWithChildren().get(currentCategorie.getChildren().get(position)).getChildren().size()>0){
                ((TextViewViewHolder) holder).checkBox.setVisibility(View.GONE);

                ((TextViewViewHolder) holder).remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog(code,   true);
                    }
                });

            }else {
                ((TextViewViewHolder) holder).checkBox.setVisibility(View.VISIBLE);

                ((TextViewViewHolder) holder).remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog(code,   false);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return currentCategorie.getChildren().size();
    }

    private void openNewList(String code){
        paths.add(getName(code));
        pathTextView.setText(textCategories + listToString(paths," > "));

        if(currentCategorie.getCategorieWithChildren().get(code) == null){
            currentCategorie.getCategorieWithChildren().put(code, new CategorieWithChildren(code, currentCategorie));

        }

        currentCategorie = currentCategorie.getCategorieWithChildren().get(code);
        parentCategorie = currentCategorie.getParent();
        addNewListToAutoCompleteText();
        notifyDataSetChanged();
    }

    private String getName(String code){
        return CategoriesItemList.getCategories_name(code);
    }

    public void goPrevious(){
        if(parentCategorie != null){
            String name = getName(currentCategorie.getCategorieCode());
            paths.remove(name);
            pathTextView.setText(textCategories + listToString(paths, " > "));

            currentCategorie = parentCategorie;
            parentCategorie = currentCategorie.getParent();
        }
        addNewListToAutoCompleteText();
        notifyDataSetChanged();
    }

    private String listToString(ArrayList<String> list, String divider) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(divider).append(list.get(i));
        }
        return result.toString();
    }

    private void addNewListToAutoCompleteText(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(String code : currentCategorie.getChildren()){
            arrayList.add(getName(code));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, arrayList);
        autoCompleteTextCat.setAdapter(adapter);
    }

    public void selectFromAutoCompleteText(String code){
        if(currentCategorie.getChildren().contains(code)){
            openNewList(code);
        }
    }

    public void addNewCategorieToList(String code){
        if(!checkIsExist(code)){
            categoriesUsed.add(code);
            currentCategorie.getChildren().add(code);
            addNewListToAutoCompleteText();
            notifyDataSetChanged();
        }else {
            messageDialog.errorMessage(mContext, getName(code)+  " : " + "مستخدم ");
        }
    }

    private void dialog(final String code, final boolean isParent){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle("حذف:  " + getName(code));

        builder.setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(isParent){
                    removeParent(code);
                }else {
                    removeChild(code);
                }
            }
        });
        builder.setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeParent(String code){
        categoriesUsed.remove(code);
        
        // remove all its children from categoriesUsed
        for(String childCode : currentCategorie.getCategorieWithChildren().get(code).getChildren()){
            categoriesUsed.remove(childCode);
            removeSingleParentWithChildren(currentCategorie.getCategorieWithChildren().get(code), childCode);
        }

        currentCategorie.getChildren().remove(code);
        currentCategorie.getCategorieWithChildren().remove(code);
        if(currentCategorie.getCategorieWithChildren().size() == 0){
            goPrevious();
        }else {
            addNewListToAutoCompleteText();
            notifyDataSetChanged();
        }
    }

    private void removeChild(String code){
        categoriesUsed.remove(code);
        currentCategorie.getChildren().remove(code);
        if(currentCategorie.getChildren().size() == 0){
            goPrevious();
        }else {
            addNewListToAutoCompleteText();
            notifyDataSetChanged();
        }
    }
    
    private void removeSingleParentWithChildren(CategorieWithChildren categorie, String code){
        Log.d(TAG, "removeSingleParentWithChildren: ");
        if(categorie.getCategorieWithChildren().containsKey(code)){
            CategorieWithChildren categorieChild = categorie.getCategorieWithChildren().get(code);
            for(String childCode : categorieChild.getChildren()){
                categoriesUsed.remove(childCode);
                removeSingleParentWithChildren(categorieChild, childCode);
            }
        }
    }

    private boolean checkIsExist(String code){
        return categoriesUsed.contains(code);
    }

    public CategorieWithChildren getCurrentCategorie() {
        return currentCategorie;
    }
}
