package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.newItem.AddNewItemActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DeleteItemsAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;

    private Context mContext;
    private HashMap<String, Item> itemsByIds;
    private ArrayList<String> ids;

    public DeleteItemsAdapter(Context context, HashMap<String, Item> itemsByIds, ArrayList<String> ids) {
        this.mContext = context;
        this.itemsByIds = itemsByIds;
        this.ids = ids;

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name, description;

        private ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.entage_name);
            description = itemView.findViewById(R.id.description);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        view = layoutInflater.inflate(R.layout.layout_delete_items, parent, false);
        viewHolder = new DeleteItemsAdapter.ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof DeleteItemsAdapter.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            UniversalImageLoader.setImage(itemsByIds.get(ids.get(position)).getImages_url().get(0),
                    itemViewHolder.imageView, null ,"");

            itemViewHolder.name.setText(itemsByIds.get(ids.get(position)).getName_item());

            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog(ids.get(position), itemsByIds.get(ids.get(position)).getName_item());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    private void dialog(final String id, final String name) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle("حذف:  " + name);

        builder.setPositiveButton("تعديل", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent =  new Intent(mContext, AddNewItemActivity.class);
                intent.putExtra("entagePageSelected", itemsByIds.get(id).getEntage_page_id());
                intent.putExtra(mContext.getString(R.string.field_item_id), itemsByIds.get(id).getItem_id());

                mContext.startActivity(intent);

            }
        });
        builder.setNeutralButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteItem(id, name);

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

    private void deleteItem(final String id, String name) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle("حذف:  " + name);

        builder.setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final FirebaseStorage mAuthStorage = FirebaseStorage.getInstance();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                final Item item = itemsByIds.get(id);

                final DatabaseReference reference = firebaseDatabase.getReference();


                reference.child(mContext.getString(R.string.dbname_entage_page_categories))
                        .child(item.getEntage_page_id())
                        .child(item.getItem_in_categorie())
                        .child(mContext.getString(R.string.field_categorie_items))
                        .child(id)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                final com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(id)
                                        .setAttributesToRetrieve("objectID");
                                String APIKey = "cf51400386c025e21bbbff240f715906";
                                GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
                                Client client = new Client(globalVariable.getApplicationID(), APIKey);
                                final Index index_items = client.getIndex("entage_items");
                                index_items.searchAsync(query, new CompletionHandler() {
                                    @Override
                                    public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                                        try {
                                            JSONArray jsonArray = content.getJSONArray("hits");
                                            for(int i=0 ; i < jsonArray.length(); i++){
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                String objectId = jsonObject.get("objectID").toString();

                                                index_items.deleteObjectAsync(objectId, null);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });



                                for(String url : item.getImages_url()){
                                    StorageReference storageReference = mAuthStorage.getReferenceFromUrl(url);
                                    if(storageReference != null){
                                        storageReference.delete();
                                    }
                                }

                                /*if(item.getType_data_descriptions() != null){
                                    for (TypeDataDescription description : item.getType_data_descriptions()){
                                        if(description.getType().equals("image")){
                                            StorageReference storageReference = mAuthStorage.getReferenceFromUrl(description.getData());
                                            if(storageReference != null){
                                                storageReference.delete();
                                            }
                                        }
                                    }
                                }*/

                                reference.child(mContext.getString(R.string.dbname_items)).child(id).removeValue();

                                for(String path : item.getCategories_item()){
                                    ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
                                    DatabaseReference databaseReference =  reference.child(mContext.getString(R.string.dbname_items_by_categories));
                                    for(String catId : cat){
                                        databaseReference = databaseReference.child(catId);
                                        databaseReference.child("items_id")
                                                .child(id)
                                                .removeValue();
                                    }
                                }

                                ids.remove(id);
                                itemsByIds.remove(id);
                                notifyDataSetChanged();
                            }
                        });

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


}
