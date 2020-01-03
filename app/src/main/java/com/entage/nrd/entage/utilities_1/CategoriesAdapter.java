package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CategoriesAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int TEXT_VIEW = 0;
    private static final int CHECKBOX = 1;

    private Context mContext;
    private TextView pathTextView;
    private ArrayList<String> paths;
    private CategorieWithChildren currentCategorie, parentCategorie;
    private TextView selectedCategoriesText;
    private String textCategories;
    private ArrayList<ArrayList<String>> selectedCategorie;
    private AutoCompleteTextView autoCompleteTextCat;
    private HashMap<String, CheckedTextView> checkedTextView;

    private boolean multiCategories;

    public CategoriesAdapter(Context context, CategorieWithChildren categorie, TextView path, TextView selectedCategoriesText,
             boolean multiCategories, AutoCompleteTextView autoCompleteTextCat) {
        this.mContext = context;
        this.currentCategorie = categorie;
        this.pathTextView = path;
        this.selectedCategoriesText = selectedCategoriesText;
        this.autoCompleteTextCat = autoCompleteTextCat;
        this.multiCategories = multiCategories;

        selectedCategorie = new ArrayList<>();
        paths = new ArrayList<>();
        textCategories = mContext.getString(R.string.the_categories);

        checkedTextView = new HashMap<>();
    }

    public class TextViewViewHolder extends RecyclerView.ViewHolder{
        TextView text1;

        private TextViewViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(R.id.text1);
        }
    }

    public class CheckBoxViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView text1;

        private CheckBoxViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(currentCategorie.getCategorieWithChildren().containsKey(currentCategorie.getChildren().get(position))){
            return TEXT_VIEW;
        }else {
            return CHECKBOX;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == TEXT_VIEW){
            view = layoutInflater.inflate(R.layout.custom_list_item_textview, parent, false);
            viewHolder = new CategoriesAdapter.TextViewViewHolder(view);

        }else if(viewType == CHECKBOX){
            view = layoutInflater.inflate(R.layout.custom_list_item_multiple_choice_1, parent, false);
            viewHolder = new CategoriesAdapter.CheckBoxViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof CategoriesAdapter.TextViewViewHolder) {
            final String code = currentCategorie.getChildren().get(position);
            final String name = getName(code);

            ((TextViewViewHolder) holder).text1.setText(name);
            ((CategoriesAdapter.TextViewViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewList(code);
                }
            });

        }else {
            final String code = currentCategorie.getChildren().get(position);
            final String name = getName(code);
            ((CheckBoxViewHolder) holder).text1.setText(name);

            ((CheckBoxViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!((CheckBoxViewHolder) holder).text1.isChecked()){
                        selectCategorie(code, ((CheckBoxViewHolder) holder).text1);
                    }else {
                        removeCategorie(code);
                    }
                    ((CheckBoxViewHolder) holder).text1.setChecked(!((CheckBoxViewHolder) holder).text1.isChecked());
                }
            });

            if(checkIfExist(code)){
                ((CheckBoxViewHolder) holder).text1.setChecked(true);
            }else {
                ((CheckBoxViewHolder) holder).text1.setChecked(false);
            }

            checkedTextView.put(code, ((CheckBoxViewHolder) holder).text1);
        }
    }

    @Override
    public int getItemCount() {
        return currentCategorie.getChildren().size();
    }

    private void openNewList(String code){
        paths.add(getName(code));
        pathTextView.setText(textCategories + listToString(paths," > "));

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
            addNewListToAutoCompleteText();
            notifyDataSetChanged();
        }
    }

    private String listToString(ArrayList<String> list, String divider) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(divider).append(list.get(i));
        }
        return result.toString();
    }

    private void selectCategorie(String code, CheckedTextView text1){
        ArrayList<String> arrayListCode = new ArrayList<>();
        arrayListCode.add(code);

        CategorieWithChildren categorie  = currentCategorie;
        while (categorie.getCategorieCode()!= null){
            arrayListCode.add(categorie.getCategorieCode());
            categorie = categorie.getParent();
        }

        Collections.reverse(arrayListCode);

        if(!multiCategories){
            if(!checkIsSelectFromAnotherCategorie(arrayListCode)){
                selectedCategorie.add(arrayListCode);
                selectedCategoriesText.setText(listToString(getSelectedCategorieNames(), " #"));
            }else {
                text1.setChecked(true);
            }
        }else {
            selectedCategorie.add(arrayListCode);
            selectedCategoriesText.setText(listToString(getSelectedCategorieNames(), " #"));
        }
    }

    private void removeCategorie(String code){

        ArrayList<String> arrayList = null;
        for(ArrayList<String> arrayList1 : selectedCategorie){
            if(arrayList1.get(arrayList1.size()-1).equals(code)){
                arrayList = arrayList1;
            }
        }

        if(arrayList != null){
            selectedCategorie.remove(arrayList);
        }

        selectedCategoriesText.setText(listToString(getSelectedCategorieNames(), " #"));
    }

    private boolean checkIfExist(String code){
        ArrayList<String> arrayListCode = new ArrayList<>();
        arrayListCode.add(code);

        CategorieWithChildren categorie  = currentCategorie;
        while (categorie.getCategorieCode()!= null){
            arrayListCode.add(categorie.getCategorieCode());
            categorie = categorie.getParent();
        }
        Collections.reverse(arrayListCode);

        if(selectedCategorie.contains(arrayListCode)){
            return true;
        }else {
            return false;
        }
    }

    public ArrayList<String> getSelectedCategorieNames(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(ArrayList<String> arrayList1 : selectedCategorie){
            for(String code : arrayList1){
                String name = getName(code);
                if(!arrayList.contains(name)){
                    arrayList.add(name);
                }
            }
        }
        return arrayList;
    }

    private boolean checkIsSelectFromAnotherCategorie(ArrayList<String> code){
        String mainCategorieCode = code.get(0);
        for(ArrayList<String> arrayList1 : selectedCategorie){
            if(!arrayList1.get(0).equals(mainCategorieCode)){
                warningUser();
                return true;
            }
        }
        return false;
    }

    private void warningUser(){
        // Warning
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
        TextView message = _view.findViewById(R.id.message);
        TextView ok = _view.findViewById(R.id.unfollow);
        TextView cancel = _view.findViewById(R.id.cancel);

        message.setText(mContext.getString(R.string.remove_categorie_from_another_main_parent_categorie));
        ok.setText(mContext.getString(R.string.remove_current_categorie));
        message.setGravity(Gravity.START);
        message.setTypeface(Typeface.DEFAULT);
        ok.setTextColor(mContext.getResources().getColor(R.color.blue));

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setView(_view);
        final AlertDialog alert = builder.create();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSelected();
                alert.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    public void removeAllSelected(){
        selectedCategorie.clear();
        selectedCategoriesText.setText("");
        notifyDataSetChanged();
    }

    public ArrayList<ArrayList<String>> getSelectedCategoriePaths(){
        return selectedCategorie;
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
            if(currentCategorie.getCategorieWithChildren().containsKey(code)){
                openNewList(code);

            }else {
                if(checkedTextView.containsKey(code)){
                    if(!checkedTextView.get(code).isChecked()){
                        selectCategorie(code, checkedTextView.get(code));
                    }else {
                        removeCategorie(code);
                    }
                    checkedTextView.get(code).setChecked(!checkedTextView.get(code).isChecked());
                }
            }
        }
    }

    public void setSelectedCategorie(ArrayList<ArrayList<String>> selectedCategorie) {
        if(selectedCategorie != null){
            this.selectedCategorie = selectedCategorie;
            selectedCategoriesText.setText(listToString(getSelectedCategorieNames(), " #"));
            notifyDataSetChanged();
        }
    }

    public void setSelectedCategorieDb(ArrayList<String> itemCategories) {
        for(String categoriePath : itemCategories){
            ArrayList arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(categoriePath);
            selectedCategorie.add(arrayList);
        }
        selectedCategoriesText.setText(listToString(getSelectedCategorieNames(), " #"));
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedCategorieToDb(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(ArrayList<String> arrayList1 : selectedCategorie){
            arrayList.add(arrayList1.toString());
        }
        return arrayList;
    }

}
