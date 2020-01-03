package com.entage.nrd.entage.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Like;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterItems extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int LOAD_MORE_VIEW = 2;

    private final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    //firebase
    private String user_id;
    private boolean isUserAnonymous;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private Context mContext;
    private ArrayList<ItemShortData> items;

    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener, onClickListenerAddBasket, onClickListenerCurrencyExchange, onClickListenerWishList;

    private AlertDialog.Builder builder;
    private AlertDialog alert ;
    private GlobalVariable globalVariable;

    public AdapterItems(Context context, RecyclerView recyclerView, ArrayList<ItemShortData> items) {
        this.mContext = context;
        this.items = items;
        this.recyclerView = recyclerView;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        setupFirebaseAuth();
        setupOnClickListener();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        SqaureImageView imageView;
        TextView name;
        TextView addToBasket;
        RelativeLayout relLayout_wishList, priceLayout;

        private ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.item_name);
            addToBasket = itemView.findViewById(R.id.add_to_basket);
            relLayout_wishList = itemView.findViewById(R.id.relLayout_wishList);
            priceLayout  = itemView.findViewById(R.id.layout_price);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View v) {
            super(v);
        }
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder{
        TextView text;

        private LoadMoreViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(items.get(position) == null){
            return PROGRESS_VIEW;
        }else if (items.get(position).getItem_id() == null){
            return LOAD_MORE_VIEW;
        }else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_item_adapter, parent, false);
            viewHolder = new AdapterItems.ItemViewHolder(view);

        }else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterItems.ProgressViewHolder(view);

        }else if(viewType == LOAD_MORE_VIEW){
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterItems.LoadMoreViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterItems.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;


            UniversalImageLoader.setImage(items.get(position).getImages_url().get(0),
                    itemViewHolder.imageView, null ,"");

            itemViewHolder.relLayout_wishList.getChildAt(1).setVisibility(View.VISIBLE);

            itemViewHolder.name.setText(items.get(position).getName_item());

            // setupCurrency
            setupCurrency(position, itemViewHolder.priceLayout);
            //

            itemViewHolder.addToBasket.setOnClickListener(onClickListenerAddBasket);
            itemViewHolder.imageView.setOnClickListener(onClickListener);
            itemViewHolder.relLayout_wishList.setOnClickListener(onClickListenerWishList);
        }
    }

    @Override
    public int getItemCount() {
        if(items==null) {
            items = new ArrayList<>();
        }
        return items.size();
    }

    private void setupCurrency(int position, RelativeLayout priceLayout){
        UtilitiesMethods.setupOptionsPrices(mContext, items.get(position).getOptions_prices(), 15, priceLayout,  globalVariable);
        /*String itemPrice = items.get(position).getOptions_prices().getMain_price();
        Currency itemCurrency = Currency.getInstance(items.get(position).getOptions_prices().getCurrency_price());

        itemViewHolder.description.setText(StringManipulation.ConvertLanguageNumber(itemPrice) + " " + itemCurrency.getDisplayName());
        if(globalVariable.getCurrency() != null && !itemCurrency.equals(globalVariable.getCurrency())){
            UtilitiesMethods.rotateAnimation(itemViewHolder.line);
            itemViewHolder.currencyExchange.setVisibility(View.VISIBLE);
            Currencylayer.Converter(new ConvertCurrency(itemCurrency, globalVariable.getCurrency()
                    , Double.parseDouble(itemPrice), itemViewHolder.description, null
                    , itemViewHolder.currencyExchange, null, null));
        }else {
            itemViewHolder.currencyExchange.setVisibility(View.GONE);
        }*/
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra(mContext.getString(R.string.field_item_id), items.get(itemPosition).getItem_id());
                mContext.startActivity(intent);
            }
        };

        onClickListenerAddBasket = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                addItemToBasket(items.get(itemPosition));
            }
        };

        onClickListenerWishList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                addItemToWishList(itemPosition, items.get(itemPosition).getItem_id(), (ImageView) ((RelativeLayout)v).getChildAt(1));
            }
        };
    }

    private void addItemToWishList(final int position, final String itemId, final ImageView mItemWishList){
        if(!checkIsUserAnonymous()){
            final Like like = new Like(user_id);

            animationFill(mItemWishList, 200, 1f, 0f, false,true);
            myRef.child(mContext.getString(R.string.dbname_users_wish_list))
                    .child(user_id)
                    .child(itemId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            globalVariable.getWishList().remove(itemId);
                            items.remove(position);
                            notifyItemRemoved(position);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            animationFill(mItemWishList, 200, 0f, 1f, true,true);
                        }
                    }) ;
        }
    }

    private void addItemToBasket(final ItemShortData mItem){
        if(!checkIsUserAnonymous()){
            FirebaseMethods.addItemToBasket(mContext, mItem, user_id, null, true);
        }
    }

    private boolean checkIsUserAnonymous(){
        if(isUserAnonymous){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            ((Activity)mContext).overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            return true;
        }else {
            return false;
        }
    }

    private void animationFill(final View view, final int duration, final float from, final float to, boolean isIncrease ,boolean animate) {
        AnimatorSet animator = new AnimatorSet();
        view.setScaleX(from);
        view.setScaleY(from);

        final float[] _to = new float[1];
        if(animate) {
            if(isIncrease){
                _to[0] = to+0.3f;
            }else {
                _to[0] = from+0.3f;
            }
        }else {
            _to[0] = to;
        }

        ObjectAnimator scaleDownX_5 = ObjectAnimator.ofFloat(view, "scaleX", from, _to[0]);
        scaleDownX_5.setDuration(duration);
        scaleDownX_5.setInterpolator(DECELERATE_INTERPOLATOR);
        ObjectAnimator scaleDownY_5 = ObjectAnimator.ofFloat(view, "scaleY", from,  _to[0]);
        scaleDownY_5.setDuration(duration);
        scaleDownY_5.setInterpolator(DECELERATE_INTERPOLATOR);

        if(animate) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationFill(view, duration - (duration/2),  _to[0], to, false,false);
                }
            });
        }

        animator.playTogether(scaleDownY_5, scaleDownX_5);
        animator.start();
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }

}
