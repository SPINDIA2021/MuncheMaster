package ui.cart;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.spindia.food.MainActivity;
import com.spindia.food.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fragments.RestaurantFragment;
import models.CartItemDetail;
import models.FavoriteItemDetails;
import ui.location.ChangeLocationActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import ui.order.CheckoutActivity;
import utils.Preferences;

public class CartItemActivity extends AppCompatActivity {
    private FirebaseUser mCurrentUser;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<CartItemDetail, CartItemHolder> itemAdapter;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mCartItemRecyclerView;
    private TextView mRestaurantCartName;
    private TextView mUserAddressText;
    private TextView mTotalAmountText;
    private TextView mSavedAmountText;
    private String uid, userAddress,ruid,userName,userPhoneNum,resDeliveryTime,resSpotImage;
    private String extraIns = "none";
    private String USER_LIST = "UserList";
    private String CART_ITEMS = "CartItems";
    private EditText mExtraInstructionsText;
    LinearLayoutManager mCompletelinearLayoutManager;
    private RecyclerView mCompleteRecyclerView;

    double savedAmount=0.0;

    private NestedScrollView mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_item);

        init();
        setRestaurantName();
        getCartItems();

    }

    @SuppressLint("SetTextI18n")
    private void init() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        TextView mToolBarText = findViewById(R.id.confirmOrderText);
        mToolBarText.setText("Confirm Order");
        mRestaurantCartName = findViewById(R.id.restaurantCartName);
        mCompleteRecyclerView=findViewById(R.id.completeItemRecyclerView);
        TextView mChangeAddressText = findViewById(R.id.changeAddressText);
        mRootView=findViewById(R.id.viewContainer);

        mCompletelinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCompleteRecyclerView.setLayoutManager(mCompletelinearLayoutManager);

        mChangeAddressText.setOnClickListener(view -> {

            Intent intent = new Intent(getApplicationContext(), ChangeLocationActivity.class);
            intent.putExtra("INT","TWO");
            startActivity(intent);
            finish();

        });
        ImageView mCartBackBtn = findViewById(R.id.cartBackBtn);
        mCartBackBtn.setOnClickListener(view -> {
            this.onBackPressed();
        });
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mCartItemRecyclerView = findViewById(R.id.cartItemRecyclerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mCartItemRecyclerView.setLayoutManager(linearLayoutManager);
        mExtraInstructionsText = findViewById(R.id.extraInstructionEdiText);
        mUserAddressText = findViewById(R.id.userDeliveryAddress);
        mTotalAmountText = findViewById(R.id.totAmount);
        mSavedAmountText = findViewById(R.id.amountSaved);
        Button mCheckoutBtn = findViewById(R.id.payAmountBtn);
        mCheckoutBtn.setOnClickListener(view -> {
            calculateTotalPriceAndMove();
        });

        getCompleteDetails();


    }

    private void setRestaurantName() {
        db.collection(USER_LIST).document(uid).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                String resName = (String) documentSnapshot.get("restaurant_cart_name");
                ruid = String.valueOf(documentSnapshot.get("restaurant_cart_uid"));
                resDeliveryTime = String.valueOf(documentSnapshot.get("restaurant_delivery_time"));
                resSpotImage = String.valueOf(documentSnapshot.get("restaurant_cart_spotimage"));
                userName = String.valueOf(documentSnapshot.get("name"));
                userPhoneNum = String.valueOf(documentSnapshot.get("phonenumber"));
                userAddress = String.valueOf(documentSnapshot.get("address"));
                Preferences.saveValue(Preferences.CURRENT_RUID,String.valueOf(documentSnapshot.get("restaurant_cart_uid")));
                mRestaurantCartName.setText(resName);
                mUserAddressText.setText(userAddress);

            }else {
                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getCartItems() {
        Query query = db.collection(USER_LIST).document(uid).collection(CART_ITEMS);
        FirestoreRecyclerOptions<CartItemDetail> cartItemModel = new FirestoreRecyclerOptions.Builder<CartItemDetail>()
                .setQuery(query, CartItemDetail.class)
                .build();
        itemAdapter = new FirestoreRecyclerAdapter<CartItemDetail, CartItemHolder>(cartItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull CartItemHolder holder, int item, @NonNull CartItemDetail model) {

                String specImage = model.getSelect_specification();

                if (specImage.equals("Veg")){
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load(R.drawable.veg_symbol).into(holder.mFoodMarkImg);
                }else {
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load(R.drawable.non_veg_symbol).into(holder.mFoodMarkImg);
                }

                holder.mItemCartName.setText(model.getSelect_name());
                String itemCount = model.getItem_count();
                holder.mQtyPicker.setNumber(itemCount);
                int getItemPrice = Integer.parseInt(model.getSelect_price());
                int getItemCount = Integer.parseInt(model.getItem_count());
                int finalPrice = getItemPrice * getItemCount;
                holder.mItemCartPrice.setText("\u20b9 " + finalPrice);
                holder.mItemDiscount.setText("\u20b9 " + model.getDiscount());
                holder.mItemDiscount.setVisibility(GONE);

                holder.mQtyPicker.setOnValueChangeListener((view, oldValue, newValue) -> {

                    String updatedPrice = String.valueOf(newValue * Integer.parseInt(model.getSelect_price()));
                    holder.mItemCartPrice.setText("\u20b9 " + updatedPrice);

                    Map<String, Object> updatedPriceMap = new HashMap<>();
                    updatedPriceMap.put("item_count", String.valueOf(newValue));

                    db.collection(USER_LIST)
                            .document(uid)
                            .collection(CART_ITEMS)
                            .document(model.getSelect_name())
                            .update(updatedPriceMap)
                            .addOnCompleteListener(task ->
                                    Log.d("SUCCESS", "SUCESSSSSSS"));

                    if (newValue == 0){
                        db.collection(USER_LIST).document(uid).collection(CART_ITEMS).document(holder.mItemCartName.getText().toString())
                                .delete().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getApplicationContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                        });
                    }
                });

                calculateTotalPrice();
            }

            @NonNull
            @Override
            public CartItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cart_items_layout, parent, false);

                return new CartItemHolder(view);
            }
            @Override
            public void onError(@NotNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };
        itemAdapter.startListening();
        itemAdapter.notifyDataSetChanged();
        mCartItemRecyclerView.setAdapter(itemAdapter);

    }

    public static class CartItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.foodMarkCart)
        ImageView mFoodMarkImg;
        @BindView(R.id.itemNameCart)
        TextView mItemCartName;
        @BindView(R.id.itemPriceCart)
        TextView mItemCartPrice;
        @BindView(R.id.quantityPicker)
        ElegantNumberButton mQtyPicker;
        @BindView(R.id.itemDisc)
        TextView mItemDiscount;

        public CartItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void moveIfCartEmpty() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void calculateTotalPrice() {
        mCartItemRecyclerView.postDelayed(() -> {
            if (itemAdapter.getItemCount() == 0){
                moveIfCartEmpty();
            }else {
                if (Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(0)).itemView.findViewById(R.id.itemPriceCart) != null){
                    int totPrice = 0;
                    for (int i = 0; i < Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount() ; i++){
                        TextView textViewDisc = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemDisc);
                        TextView textView = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemPriceCart);
                        String priceText = textView.getText().toString().replace("\u20b9 " , "");
                        int price = Integer.parseInt(priceText);
                        totPrice = price + totPrice;

                        String discPriceText = textViewDisc.getText().toString().replace("\u20b9 " , "");
                        int discPrice = Integer.parseInt(discPriceText);
                        savedAmount = discPrice + savedAmount;
                    }
                    mTotalAmountText.setText("Amount Payable: \u20b9" + totPrice);
                    mSavedAmountText.setText("You Save: \u20b9 "+savedAmount);
                }
            }
        },5);

    }



    private void calculateTotalPriceAndMove() {
        mCartItemRecyclerView.postDelayed(() -> {
            if (itemAdapter.getItemCount() == 0){
                moveIfCartEmpty();
            }else {
                if (Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(0)).itemView.findViewById(R.id.itemPriceCart) != null){
                    int totPrice = 0;
                    String[] itemsArr = new String[Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount()];
                    String[] orderedItemsArr = new String[mCartItemRecyclerView.getAdapter().getItemCount()];

                    for (int i = 0; i < Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount() ; i++){
                        TextView textView = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemPriceCart);
                        TextView textView2 = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemNameCart);

                        ElegantNumberButton elegantNumberButton = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.quantityPicker);
                        String itemCount = elegantNumberButton.getNumber();
                        String priceText = textView.getText().toString().replace("\u20b9 " , "");
                        int price = Integer.parseInt(priceText);
                        totPrice = price + totPrice;




                        itemsArr[i] = textView2.getText().toString();
                        orderedItemsArr[i] = itemCount + " x " + textView2.getText().toString();
                    }

                    if(!TextUtils.isEmpty(mExtraInstructionsText.getText())){
                        extraIns = mExtraInstructionsText.getText().toString();
                    }

                    if (resSpotImage != null){
                        Intent intent = new Intent(CartItemActivity.this, CheckoutActivity.class);
                        intent.putExtra("TOTAL_AMOUNT", String.valueOf(totPrice));
                        intent.putExtra("ITEM_NAMES", itemsArr);
                        intent.putExtra("ITEM_ORDERED_NAME", orderedItemsArr);
                        intent.putExtra("RES_NAME", mRestaurantCartName.getText().toString());
                        intent.putExtra("RES_UID", ruid);
                        intent.putExtra("USER_ADDRESS",userAddress);
                        intent.putExtra("USER_NAME", userName);
                        intent.putExtra("USER_UID",uid);
                        intent.putExtra("EXTRA_INS", extraIns);
                        intent.putExtra("USER_PHONE", userPhoneNum);
                        intent.putExtra("DELIVERY_TIME", resDeliveryTime);
                        intent.putExtra("RES_IMAGE", resSpotImage);
                        startActivity(intent);
                        this.overridePendingTransition(0,0);
                    }
                }
            }
        },5);
    }


    String mRestName="R K Restuarant";
    String mResUid="fBG9p6jlfbSu1HEaV7vs9LphJ3g1";
    String mResDeliveryTime="5";
    String mResImage= "https://firebasestorage.googleapis.com/v0/b/food-393d3.appspot.com/o/restaurant_spot_image%2FfBG9p6jlfbSu1HEaV7vs9LphJ3g1.jpg?alt=media&token=9a1de45c-2e4e-4ea3-b1bc-8b3cef59ab15";

    private void getCompleteDetails() {
        Query query = db.collection("RestaurantList").document(mResUid).collection("CompleteItems");
        FirestoreRecyclerOptions<FavoriteItemDetails> favItemModel = new FirestoreRecyclerOptions.Builder<FavoriteItemDetails>()
                .setQuery(query, FavoriteItemDetails.class)
                .build();
        FirestoreRecyclerAdapter<FavoriteItemDetails, CompleteItemViewHolder> favItemAdapter = new FirestoreRecyclerAdapter<FavoriteItemDetails, CompleteItemViewHolder>(favItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull CompleteItemViewHolder holder, int position, @NonNull FavoriteItemDetails model) {
                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("RestaurantList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                            holder.mFavName.setText(model.getItem_name());
                            Glide.with(CartItemActivity.this)
                                    .load(model.getItem_image())
                                    .apply(new RequestOptions()
                                            .override(200,200))
                                    .centerCrop()
                                    .into(holder.mFavImage);
                            int netprice=Integer.parseInt(model.getItem_price())-Integer.parseInt(model.getDiscount());
                            holder.mAveragePrice.setText("\u20B9" + netprice);
                            holder.mActualPriceText.setText("\u20B9" +model.getItem_price());
                            holder.mDiscountText.setText("( \u20B9" + model.getDiscount()+" off)");

                            holder.mItemAddBtn.setOnClickListener(view -> {
                                String selectedItemName = holder.mFavName.getText().toString();
                                addItemToCartFav(selectedItemName, model, uid,netprice);
                                holder.mItemAddBtn.setVisibility(GONE);
                                holder.mQtyPicker.setNumber("1");
                                holder.mQtyPicker.setVisibility(View.VISIBLE);
                            });

                            holder.mQtyPicker.setOnValueChangeListener((view, oldValue, newValue) -> {

                                Map<String ,String> cartItemMap = new HashMap<>();
                                cartItemMap.put("select_name", holder.mFavName.getText().toString());
                                cartItemMap.put("select_price", String.valueOf(netprice));
                                cartItemMap.put("discount", model.getDiscount());
                                cartItemMap.put("select_specification", model.getSpecification());
                                cartItemMap.put("item_count", String.valueOf(newValue));
                                db.collection("UserList").document(uid).collection("CartItems")
                                        .document(holder.mFavName.getText().toString()).
                                        set(cartItemMap)
                                        .addOnSuccessListener(aVoid -> {

                                            db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful()){
                                                    int count = 0;
                                                    for (DocumentSnapshot ignored : Objects.requireNonNull(task1.getResult())) {
                                                        count++;
                                                    }
                                                    Snackbar snackbar = Snackbar
                                                            .make(mRootView, "Added " + count + " items in  your cart", Snackbar.LENGTH_INDEFINITE)
                                                            .setAction("Cart", view1 -> {
                                                                Snackbar snackbar1 = Snackbar.make(mRootView, "Message is restored!", Snackbar.LENGTH_SHORT);
                                                                Intent intent = new Intent(getApplicationContext(), CartItemActivity.class);
                                                                startActivity(intent);
                                                                snackbar1.dismiss();
                                                            });
                                                    snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(
                                                            getApplicationContext(), R.color.colorPrimary));
                                                    snackbar.show();
                                                    getCartItems();
                                                }
                                            });
                                        }).addOnFailureListener(e -> {
                                        });

                                HashMap<String, Object> resNameMap = new HashMap<>();
                                resNameMap.put("restaurant_cart_name", mRestName);
                                resNameMap.put("restaurant_cart_uid", mResUid);
                                resNameMap.put("restaurant_delivery_time", mResDeliveryTime);
                                resNameMap.put("restaurant_cart_spotimage", mResImage);

                                db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "Added Item Successfully", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Adding Item Failed", Toast.LENGTH_SHORT).show();
                                });




                                if (newValue == 0){
                                    String selectedItemName = holder.mFavName.getText().toString();;
                                    removeItemFromCart(selectedItemName, uid);
                                    holder.mQtyPicker.setVisibility(View.GONE);
                                    holder.mItemAddBtn.setVisibility(View.VISIBLE);
                                }
                            });

                            DocumentReference docRef1 = db.collection("UserList").document(uid).collection("CartItems").document(holder.mFavName.getText().toString());
                            docRef1.get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot1 = task1.getResult();
                                    if (Objects.requireNonNull(documentSnapshot1).exists()) {

                                        holder.mItemAddBtn.setVisibility(GONE);
                                        String count= (String) documentSnapshot1.get("item_count");
                                        holder.mQtyPicker.setNumber(count);
                                        holder.mQtyPicker.setVisibility(View.VISIBLE);

                                    }
                                }

                            });



                        }
                    });
                }

            }

            @NonNull
            @Override
            public CompleteItemViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.custom_cuisine_layout, group, false);
                return new CompleteItemViewHolder(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        favItemAdapter.startListening();
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mCompleteRecyclerView);
        favItemAdapter.notifyDataSetChanged();
        mCompleteRecyclerView.setAdapter(favItemAdapter);
    }

    public static class CompleteItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cuisineName)
        TextView mFavName;
        @BindView(R.id.cuisineImage)
        ImageView mFavImage;
        @BindView(R.id.addMenuItemBtn)
        Button mItemAddBtn;
        @BindView(R.id.quantityPicker)
        ElegantNumberButton mQtyPicker;
        @BindView(R.id.actual_price)
        TextView mActualPriceText;
        @BindView(R.id.discount_price)
        TextView mDiscountText;
        @BindView(R.id.average_price)
        TextView mAveragePrice;
      /*  @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.average_time)
        TextView mAverageDeliveryTime;*/


        public CompleteItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private void addItemToCartFav(String selectedItemName, FavoriteItemDetails model, String uid, int netprice){
        Map<String ,String> cartItemMap = new HashMap<>();
        cartItemMap.put("select_name", selectedItemName);
        cartItemMap.put("select_price", String.valueOf(netprice));
        cartItemMap.put("discount", model.getDiscount());
        cartItemMap.put("select_specification", model.getSpecification());
        cartItemMap.put("item_count", "1");
        db.collection("UserList").document(uid).collection("CartItems")
                .document(selectedItemName).
                set(cartItemMap)
                .addOnSuccessListener(aVoid -> {

                    db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()){
                            int count = 0;
                            for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                                count++;
                            }
                            Snackbar snackbar = Snackbar
                                    .make(mRootView, "Added " + count + " items in  your cart", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Cart", view -> {
                                        Snackbar snackbar1 = Snackbar.make(mRootView, "Message is restored!", Snackbar.LENGTH_SHORT);
                                        Intent intent = new Intent(this, CartItemActivity.class);
                                        startActivity(intent);
                                        snackbar1.dismiss();
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(
                                    this, R.color.colorPrimary));
                            snackbar.show();
                            getCartItems();
                        }
                    });
                }).addOnFailureListener(e -> {
                });

        HashMap<String, Object> resNameMap = new HashMap<>();
        resNameMap.put("restaurant_cart_name", mRestName);
        resNameMap.put("restaurant_cart_uid", mResUid);
        resNameMap.put("restaurant_delivery_time", mResDeliveryTime);
        resNameMap.put("restaurant_cart_spotimage", mResImage);

        db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });

    }


    private void removeItemFromCart(String selectedItemName,String uid) {
        db.collection("UserList").document(uid).collection("CartItems").document(selectedItemName).delete().addOnCompleteListener(task ->
                Toast.makeText(this, "Item Removed From Cart", Toast.LENGTH_SHORT).show());
        getCartItems();
    }

}