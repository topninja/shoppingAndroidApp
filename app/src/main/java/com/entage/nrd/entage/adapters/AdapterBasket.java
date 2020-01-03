package com.entage.nrd.entage.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.ItemBasket;
import com.entage.nrd.entage.basket.FragmentInitOrder;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.entage.nrd.entage.utilities_1.ViewOptionsPrices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class AdapterBasket extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int GROUP_VIEW = 0;
    private static final int ITEM_VIEW = 3;
    private static final int PROGRESS_VIEW = 1;
    private static final int SEND_SUCCESSFULLY = 2;

    private  OnActivityListener mOnActivityListener;
    private Context mContext;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private HashMap<String, ItemShortData> items;
    private HashMap<String, String> entagePageName;
    private HashMap<String, Integer> countItemsInStore;
    private ArrayList<Object> objects;

    private ArrayList<String> itemRefusal;

    private MessageDialog messageDialog = new MessageDialog();

    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener, onClickListenerSendOrder, onClickListenerDelete;

    private GlobalVariable globalVariable;
    private TextView textView;

    public AdapterBasket(Context context, RecyclerView recyclerView, TextView textView,
                         OnActivityListener mOnActivityListener) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.textView = textView;
        this.mOnActivityListener = mOnActivityListener;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        setupOnClickListener();

        itemRefusal = new ArrayList<>();

        countItemsInStore = new HashMap<>();
        entagePageName = new HashMap<>();
        objects = new ArrayList<>();
        items = new HashMap<>();
        objects.add(null);objects.add(null);objects.add(null);
        objects.add(null);objects.add(null);objects.add(null);
        objects.add(null);objects.add(null);objects.add(null);
        setupFirebaseAuth();
    }

    public class GroupItemViewHolder extends RecyclerView.ViewHolder{
        TextView entagePageName, countOrders, sendOrder;
        ImageView more_options;
        RelativeLayout relLayout;

        private GroupItemViewHolder(View itemView) {
            super(itemView);
            entagePageName = itemView.findViewById(R.id.entagePageName);
            countOrders = itemView.findViewById(R.id.countOrders);
            more_options = itemView.findViewById(R.id.more_options);
            relLayout = itemView.findViewById(R.id.relLayout);
            sendOrder = itemView.findViewById(R.id.send_order);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView nameItem, error, error_1;
        ImageView imageView, delete;
        EditText quantity;
        RelativeLayout relLayout, priceLayout, relLayoutPrice;
        LinearLayout containerOptions;

        private ItemViewHolder(View itemView) {
            super(itemView);
            relLayout = itemView.findViewById(R.id.relLayout);
            nameItem = itemView.findViewById(R.id.item_name);
            imageView = itemView.findViewById(R.id.profile_image);
            quantity = itemView.findViewById(R.id.quantity);
            delete = itemView.findViewById(R.id.delete);
            error = itemView.findViewById(R.id.error);
            containerOptions = itemView.findViewById(R.id.containerOptions);
            relLayoutPrice = itemView.findViewById(R.id.relLayoutPrice);
            priceLayout  = itemView.findViewById(R.id.layout_price);
            error_1 = itemView.findViewById(R.id.error_1);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(objects.get(position) == null){
            return PROGRESS_VIEW;

        }else if (objects.get(position) instanceof  String){
            return GROUP_VIEW;

        }else {
            ItemBasket ib = (ItemBasket) objects.get(position);

            if(ib.getTime_added() == null){
                return SEND_SUCCESSFULLY;
            }
            else {
                return ITEM_VIEW;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == GROUP_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_basket_items_group, parent, false);
            viewHolder = new AdapterBasket.GroupItemViewHolder(view);
        }

        else if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_basket_items, parent, false);
            viewHolder = new AdapterBasket.ItemViewHolder(view);
        }

        else {
            view = layoutInflater.inflate(R.layout.layout_adapter_basket_progressbar, parent, false);
            viewHolder = new AdapterBasket.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterBasket.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            ItemBasket itemBasket = (ItemBasket) objects.get(position);
            ItemShortData item = items.get(itemBasket.getItem_id());

            if(item != null && item.getName_item()!=null){ // we check alse the name because some cases item empty
                itemViewHolder.nameItem.setText(item.getName_item());
                UniversalImageLoader.setImage(item.getImages_url().get(0), itemViewHolder.imageView, null ,"");

                itemViewHolder.quantity.setText("");
                //setupCurrency(position, itemViewHolder.priceLayout);

                itemViewHolder.error_1.setVisibility(View.GONE);
                itemViewHolder.containerOptions.setVisibility(View.GONE);
                itemViewHolder.containerOptions.removeAllViews();

                boolean isItemRefusal = false;
                if(item.getOptions_prices() !=null){
                    if(item.getOptions_prices().getOptionsTitle() != null){
                        if( itemBasket.getOptions()!=null &&
                                item.getOptions_prices().getLinkingOptions() != null &&
                                item.getOptions_prices().getLinkingOptions().contains(itemBasket.getOptions())){

                            int index = item.getOptions_prices().getLinkingOptions().indexOf(itemBasket.getOptions());
                            if(item.getOptions_prices().getPrices() != null && item.getOptions_prices().getPrices().get(index) != null &&
                                    !itemBasket.getPrice().equals(item.getOptions_prices().getPrices().get(index))){ // checking price
                                itemViewHolder.error_1.setText(mContext.getString(R.string.data_item_had_changed));
                                itemViewHolder.error_1.setVisibility(View.VISIBLE);
                                isItemRefusal = true;

                            }else {
                                itemViewHolder.containerOptions.setVisibility(View.VISIBLE);
                                new ViewOptionsPrices(mContext, item.getOptions_prices(), itemViewHolder.containerOptions,
                                        itemViewHolder.priceLayout, itemBasket.getOptions(), itemBasket.getPrice(), 13, 15,
                                        mContext.getColor(R.color.entage_blue), globalVariable);
                            }

                        }
                        else {
                            itemViewHolder.error_1.setText(mContext.getString(R.string.data_item_had_changed));
                            itemViewHolder.error_1.setVisibility(View.VISIBLE);
                            isItemRefusal = true;
                        }

                    }
                    else { // only setup price
                        if(!item.getOptions_prices().getMain_price().equals(itemBasket.getPrice())){ // checking price
                            itemViewHolder.error_1.setText(mContext.getString(R.string.data_item_had_changed));
                            itemViewHolder.error_1.setVisibility(View.VISIBLE);
                            isItemRefusal = true;

                        }
                        else {
                            itemViewHolder.containerOptions.setVisibility(View.VISIBLE);
                            new ViewOptionsPrices(mContext, item.getOptions_prices(), itemViewHolder.priceLayout, 15,
                                    mContext.getColor(R.color.entage_blue), globalVariable);
                        }
                    }
                }

                if(isItemRefusal && !itemRefusal.contains(itemBasket.getItem_id())){
                    itemRefusal.add(itemBasket.getItem_id());
                }

                itemViewHolder.delete.setOnClickListener(onClickListenerDelete);
                itemViewHolder.relLayout.setOnClickListener(onClickListener);

                //
                itemViewHolder.delete.setVisibility(View.VISIBLE);
                itemViewHolder.delete.setClickable(true);
                itemViewHolder.error.setVisibility(View.GONE);
            }
        }

        if (holder instanceof AdapterBasket.GroupItemViewHolder) {
            GroupItemViewHolder groupItemViewHolder = (GroupItemViewHolder) holder;

            String id = (String) objects.get(position);
            String name = entagePageName.get(id);

            if(name != null){
                groupItemViewHolder.entagePageName.setText(name);
                groupItemViewHolder.countOrders.setText(countItemsInStore.get(id)+"");

                groupItemViewHolder.sendOrder.setOnClickListener(onClickListenerSendOrder);

                //
                groupItemViewHolder.sendOrder.setEnabled(true);
                groupItemViewHolder.sendOrder.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());

                Bundle bundle = new Bundle();
                bundle.putString(mContext.getString(R.string.field_item_id), ((ItemBasket)objects.get(itemPosition)).getItem_id());
                mOnActivityListener.onActivityListener(new ViewItemFragment(), bundle);
            }
        };

        onClickListenerSendOrder = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent().getParent().getParent());

                if(objects.get(itemPosition) instanceof  String){
                    sendToInitOrder((String) objects.get(itemPosition));
                }
            }
        };

        onClickListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent().getParent());
                v.setClickable(false);
                v.setVisibility(View.INVISIBLE);
                deleteFromBasket((ItemBasket) objects.get(itemPosition));
            }
        };

    }

    private void removeProgressBar(){
        if(objects.size() >0 && objects.get(0) == null){
            objects.clear();
            notifyDataSetChanged();
        }
    }

    private void fetchBaskets(){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_users_basket))
                .child(user_id)
                .orderByChild(mContext.getString(R.string.field_entage_page_id));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeProgressBar();
                if(dataSnapshot.exists()){
                    HashMap<String, ArrayList<Integer>> hashMap = new HashMap<>();

                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        ItemBasket itemBasket = singleSnapshot.getValue(ItemBasket.class);
                        if(itemBasket != null){
                            String entage_page_id = itemBasket.getEntage_page_id();
                            if(!objects.contains(entage_page_id)){
                                int index = objects.size();
                                objects.add(entage_page_id);
                                countItemsInStore.put(entage_page_id, 0);
                                notifyItemInserted(index);
                                fetchEntagePageName(entage_page_id, index);
                            }
                            int index = objects.indexOf(entage_page_id) + countItemsInStore.get(entage_page_id) + 1;
                            objects.add(index, itemBasket);
                            countItemsInStore.put(entage_page_id, countItemsInStore.get(entage_page_id)+1);
                            notifyItemInserted(index);

                            if(!hashMap.containsKey(itemBasket.getItem_id())){
                                hashMap.put(itemBasket.getItem_id(), new ArrayList<Integer>());
                            }
                            hashMap.get(itemBasket.getItem_id()).add(index);
                        }
                    }

                    for(String itemId : hashMap.keySet()){
                        fetchItem(itemId, hashMap.get(itemId));
                    }

                }else {
                    textView.setText(mContext.getString(R.string.basket_empty));
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void fetchItem(final String itemId, final ArrayList<Integer> indexes){
        //items.put(itemToBasket.getItem_id(), new ItemShortData());
        Query query = myRef
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ItemShortData item = dataSnapshot.getValue(ItemShortData.class);
                    if(item != null){
                        items.put(item.getItem_id(), item);
                        notifyItemChanged(objects.indexOf(item.getEntage_page_id()));
                        for(int index: indexes){
                            notifyItemChanged(index);
                        }
                    }

                }else {
                    // this mean item deleted from db
                    for(int index: indexes){
                        deleteFromBasket((ItemBasket) objects.get(index));
                    }

                        /*objects.remove(position);
                        countItemsInStore.put(itemToBasket.getEntage_page_id(), countItemsInStore.get(itemToBasket.getEntage_page_id())-1);
                        notifyItemChanged(objects.indexOf(itemToBasket.getEntage_page_id()));
                        notifyItemRemoved(position);*/
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void fetchEntagePageName(final String id, final int position){
        if(!entagePageName.containsKey(id)){
            Query query = myRef
                    .child(mContext.getString(R.string.dbname_entaji_pages_names))
                    .child(id)
                    .child(mContext.getString(R.string.field_name_entage_page));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        entagePageName.put(id, dataSnapshot.getValue().toString());
                        notifyItemChanged(position);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void deleteFromBasket(final ItemBasket itemBasket){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_basket))
                .child(user_id)
                .child(itemBasket.getItem_basket_id())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance().getReference()
                                    .child(mContext.getString(R.string.dbname_items_on_basket))
                                    .child(itemBasket.getItem_id())
                                    .child(user_id)
                                    .removeValue();

                            String entagePageId = itemBasket.getEntage_page_id();

                            items.remove(itemBasket.getItem_basket_id());
                            int index = objects.indexOf(itemBasket);
                            objects.remove(index);
                            notifyItemRemoved(index);

                            countItemsInStore.put(entagePageId, countItemsInStore.get(entagePageId)-1);
                            // check if this only item under group
                            int in = objects.indexOf(entagePageId);
                            if(countItemsInStore.get(entagePageId) == 0){
                                objects.remove(in);
                                notifyItemRemoved(in);
                            }else {
                                notifyItemChanged(in);
                            }

                            if(objects.size()==0){
                                textView.setText(mContext.getString(R.string.basket_empty));
                                textView.setVisibility(View.VISIBLE);
                            }

                            // update Count Basket
                            int countBasket = mContext.getSharedPreferences("entaji_app",
                                    MODE_PRIVATE).getInt("countBasket", -1);
                            int i =  countBasket==-1? -1 : (countBasket-1);
                            mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                                    .putInt("countBasket", i).apply();

                        }else {
                            failure(itemBasket, task.getException().getMessage());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failure(itemBasket, e.getLocalizedMessage());
                    }
                });

    }

    private void failure(ItemBasket itemBasket, String msg){
        int index = objects.indexOf(itemBasket);
        notifyItemChanged(index);//

        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) +"\n"+ msg);
    }

    private void sendToInitOrder(final String entagePageId){
        // get all item under this store $entagePageId
        final ArrayList<ItemBasket> itemBaskets = new ArrayList<>();
        Log.d(TAG, "sendMyOrder: " + itemRefusal.toString());

        // add all items belong to this page to view them in list
        for(Object object : objects){
            if(object instanceof ItemBasket &&
                    ((ItemBasket) object).getEntage_page_id().equals(entagePageId) &&
                       !itemRefusal.contains(((ItemBasket) object).getItem_id()) ){
                itemBaskets.add((ItemBasket) object);
            }
        }

        final HashMap<String, String> itemsNamesMap = new HashMap<>();
        final ArrayList<String> itemsNamesList = new ArrayList<>();
        for(ItemBasket ib : itemBaskets){
            if(items.get(ib.getItem_id()) == null){ // in case item not fetch yet
                return;
            }
            String itemName = items.get(ib.getItem_id()).getName_item() +(StringManipulation.printArrayListItemsOrder(ib.getOptions()));
            itemsNamesMap.put(ib.getItem_basket_id(), itemName);
            itemsNamesList.add(itemName);
        }

        if(itemBaskets.size() == 1){
            if(items.get(itemBaskets.get(0).getItem_id()) == null){ // in case item not fetch yet
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(mContext.getString(R.string.field_entage_page_id), entagePageId);
            bundle.putString(mContext.getString(R.string.field_name_entage_page), entagePageName.get(entagePageId));
            bundle.putParcelableArrayList("ItemBasket", itemBaskets);
            bundle.putSerializable("itemsNames",itemsNamesMap );
            mOnActivityListener.onActivityListener(new FragmentInitOrder(), bundle);

        }
        else if(itemBaskets.size() > 1){
            View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_init_orders, null);
            final ListView listView =  dialogView.findViewById(R.id.listView);
            listView.setVisibility(View.VISIBLE);
            TextView sendOrder = dialogView.findViewById(R.id.send_order);
            TextView cancel = dialogView.findViewById(R.id.cancel);
            final TextView error = dialogView.findViewById(R.id.error);

            final ArrayList<ItemBasket> selectedItems = new ArrayList<>();
            final ArrayList<String> selectedItemsNames = new ArrayList<>();


            ArrayAdapter<String> listView_Adapter = new ArrayAdapter<>(mContext, R.layout.custom_list_item_multiple_choice, itemsNamesList);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(listView_Adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //boolean isCheck = listView.isItemChecked(position);
                    if(selectedItems.contains(itemBaskets.get(position))){
                        selectedItems.remove(itemBaskets.get(position));
                        selectedItemsNames.remove(itemsNamesList.get(position));
                    }
                    else {
                        selectedItems.add(itemBaskets.get(position));
                        selectedItemsNames.add(itemsNamesList.get(position));
                    }

                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(dialogView);

            final AlertDialog alert = builder.create();
            //alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });

            sendOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    error.setText("");
                    if(selectedItems.size() == 0){
                        error.setText(mContext.getString(R.string.atleast_one_item));
                    }else {
                        alert.dismiss();

                        Bundle bundle = new Bundle();
                        bundle.putString(mContext.getString(R.string.field_entage_page_id), entagePageId);
                        bundle.putString(mContext.getString(R.string.field_name_entage_page), entagePageName.get(entagePageId));
                        bundle.putParcelableArrayList("ItemBasket", selectedItems);
                        bundle.putSerializable("itemsNames",itemsNamesMap);
                        mOnActivityListener.onActivityListener(new FragmentInitOrder(), bundle);
                    }
                }
            });

            alert.show();
        }
        else {
            messageDialog.errorMessage(mContext, mContext.getString(R.string.no_items_for_create_order));
        }
    }

    /*
   -------------------------------Firebase-------------------------------------------------------
   */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();

            fetchBaskets();

        }else {
            removeProgressBar();
            textView.setText(mContext.getString(R.string.basket_empty));
            textView.setVisibility(View.VISIBLE);
        }
    }
}
