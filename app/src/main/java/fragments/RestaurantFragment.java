package fragments;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import adapters.SearchAdapter;
import models.FavoriteItemDetails;
import models.FavoriteRestaurantDetails;
import models.RestaurantMenuItems;
import models.SearchItemDetails;
import ui.cart.CartItemActivity;
import ui.cart.EmptyCartActivity;
import ui.main.MainRestaurantPageActivity;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.snackbar.Snackbar;
import com.spindia.food.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import models.RestaurantDetail;
import ui.location.ChangeLocationActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.nikartm.support.ImageBadgeView;
import utils.Preferences;

public class RestaurantFragment extends Fragment {

    private FirebaseUser mCurrentUser;
    private FirebaseFirestore db;
    private String address;
    LinearLayoutManager linearLayoutManager;
    LinearLayoutManager mFavlinearLayoutManager;
    private RecyclerView mRestaurantRecyclerView;
    private RecyclerView mFavouriteRecyclerView;
    private ImageBadgeView mImageBadgeView;
    private String[] fruitNames = {"Biryani","North Indian","South Indian","Cakes","Desserts","Burgers","Chinese","Rolls","Pizza"};
    private String biryaniImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fbiryani.jpg?alt=media&token=6eceb101-07c1-49ff-95a3-e540b1e0fb35";
    private String southIndianImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fsouth_indian.jpg?alt=media&token=e925ee1d-5855-484a-9928-9716025ecc43";
    private String northIndianImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fnorth_indian.jpg?alt=media&token=d24f26e0-071e-4eec-93f5-f8f063b0ad5a";
    private String rollsImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Frolls.jpg?alt=media&token=d52c1f03-0558-44a8-a7c3-90b8fda6d77f";
    private String burgersImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fburgers.jpg?alt=media&token=f6f2ca46-fc84-4ed6-84f2-2d199686f45e";
    private String pizzaImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fpizza.jpg?alt=media&token=3239cc9d-c2e6-4a8c-8a76-e8ee307ec72e";
    private String cakesImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fcake.jpeg?alt=media&token=1ded9af6-25a6-4deb-8b1a-0681d7a154d5";
    private String chineseImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fchinese.jpg?alt=media&token=b407830e-0e8d-47d2-8fd0-de43b34bb4d2";
    private String dessertsImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fdesserts.jpg?alt=media&token=209f4592-7b36-4768-9b71-f35d5c05ef74";
    private String[] fruitImages = {biryaniImg,northIndianImg,southIndianImg,cakesImg,dessertsImg,burgersImg,chineseImg,rollsImg,pizzaImg};
    private NestedScrollView mRootView;
    private RelativeLayout layDefault,laySearchContainer;
    private RecyclerView searchItemRecyclerView;
    private EditText searchEditText;
    private ArrayList<SearchItemDetails> searchList=new ArrayList<>();
    private SearchAdapter searchListAdapter;

    String mRestName="R K Restuarant";
    String mResUid="fBG9p6jlfbSu1HEaV7vs9LphJ3g1";

    String mResDeliveryTime="5";
    String mResImage= "https://firebasestorage.googleapis.com/v0/b/food-393d3.appspot.com/o/restaurant_spot_image%2FfBG9p6jlfbSu1HEaV7vs9LphJ3g1.jpg?alt=media&token=9a1de45c-2e4e-4ea3-b1bc-8b3cef59ab15";

    public RestaurantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        init(view);
        fetchLocation(view);
        getItemsInCartNo();
        getRestaurantDetails();
        getFavouriteDetails();


        return view;
    }

    private void init(View view) {

        LinearLayout mAddressContainer = view.findViewById(R.id.addressContainer);
      //  GridView mCuisineFoodView = view.findViewById(R.id.cuisineGridView);
       // CuisineImageAdapter adapter = new CuisineImageAdapter();
       // mCuisineFoodView.setAdapter(adapter);
        mAddressContainer.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ChangeLocationActivity.class);
            intent.putExtra("INT", "ONE");
            startActivity(intent);
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Toolbar mToolbar = view.findViewById(R.id.customToolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);
        mRootView = view.findViewById(R.id.content1);
        mRestaurantRecyclerView = view.findViewById(R.id.restaurant_recyclerView);
        mFavouriteRecyclerView = view.findViewById(R.id.cuisineGridView);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRestaurantRecyclerView.setLayoutManager(linearLayoutManager);

        mFavlinearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false);
        mFavouriteRecyclerView.setLayoutManager(mFavlinearLayoutManager);
        mFavouriteRecyclerView.setHasFixedSize(true);
        mImageBadgeView = view.findViewById(R.id.imageBadgeView);
        laySearchContainer = view.findViewById(R.id.searchItemContainer);
        layDefault = view.findViewById(R.id.lay_default);
        searchItemRecyclerView = view.findViewById(R.id.searchItemRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);


        searchItemRecyclerView.setLayoutManager( new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));



        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count>0)
                {
                    laySearchContainer.setVisibility(View.VISIBLE);
                    layDefault.setVisibility(GONE);
                    String searchText = searchEditText.getText().toString();

                    searchInFirestore(searchText.toLowerCase());
                }else {
                    laySearchContainer.setVisibility(GONE);
                    layDefault.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }

    private void searchInFirestore(String searchText) {

        db.collection("Menu").document(mResUid).collection("MenuItems").orderBy("search_menuitem")
                .startAt(searchText)
                .endAt(searchText+"\uf8ff").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try{
                    searchList = (ArrayList<SearchItemDetails>) task.getResult().toObjects(SearchItemDetails.class);
                    searchListAdapter=new SearchAdapter(getActivity(),searchList);
                    searchItemRecyclerView.setAdapter(searchListAdapter);
                    searchListAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else {
              //  Toast.makeText(getActivity(),"Uns",Toast.LENGTH_LONG).show();
            }
        });
    }


    private void fetchLocation(View view) {
        if (mCurrentUser != null) {
            DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    TextView textView = view.findViewById(R.id.userLocation);

                    if (Preferences.getString(Preferences.CurrentAddress)!=null&&!Preferences.getString(Preferences.CurrentAddress).equals(""))
                    {
                        String address=Preferences.getString(Preferences.CurrentAddress);
                        textView.setText(address);

                    }else {
                        address = String.valueOf(documentSnapshot.get("address"));
                        Preferences.saveValue(Preferences.CurrentAddress,address);
                        textView.setText(address);
                    }



                }
            });
        }
    }

    private void getItemsInCartNo() {
        db.collection("UserList").document(mCurrentUser.getUid()).collection("CartItems").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                int count = 0;
                for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                    count++;
                }
                mImageBadgeView.setBadgeValue(count);
                mImageBadgeView.setOnClickListener(view -> {
                    if (mImageBadgeView.getBadgeValue() != 0){
                        sendUserToCheckOut();
                    }else {
                        sendUserToEmptyCart();
                    }
                });

            }
        });
    }

    public  class CuisineImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return fruitImages.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View view1 = getLayoutInflater().inflate(R.layout.custom_cuisine_layout,null);
            //getting view in row_data
            TextView name = view1.findViewById(R.id.cuisineName);
            ImageView image = view1.findViewById(R.id.cuisineImage);


            name.setText(fruitNames[i]);
            Glide.with(requireContext())
                    .load(fruitImages[i])
                    .apply(new RequestOptions()
                            .override(200,200))
                    .centerCrop()
                    .into(image);
            return view1;
        }
    }

    private void getRestaurantDetails() {
        Query query = db.collection("Menu").document("fBG9p6jlfbSu1HEaV7vs9LphJ3g1").collection("MenuItems");
        FirestoreRecyclerOptions<RestaurantMenuItems> menuItemModel = new FirestoreRecyclerOptions.Builder<RestaurantMenuItems>()
                .setQuery(query, RestaurantMenuItems.class)
                .build();
        FirestoreRecyclerAdapter<RestaurantMenuItems, RestaurantItemViewHolder> restaurantAdapter = new FirestoreRecyclerAdapter<RestaurantMenuItems, RestaurantItemViewHolder>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull RestaurantItemViewHolder holder, int position, @NonNull RestaurantMenuItems model) {



                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
/*

                            if (Objects.equals(model.is_active(), "no")) {
                                holder.mRestaurantName.setText(model.getName());
                                holder.mDesc.setText(model.getCategory());
                                String specImage = String.valueOf(model.getSpecification());
                                if (specImage.equals("Veg")) {
                                    Glide.with(requireActivity())
                                            .load(R.drawable.veg_symbol).into(holder.foodSpecification);
                                } else {
                                    Glide.with(requireActivity())
                                            .load(R.drawable.non_veg_symbol).into(holder.foodSpecification);
                                }
                                holder.mAveragePrice.setText("\u20B9 " + model.getPrice());
                                holder.mItemAddBtn.setClickable(false);
                                holder.mNotAvailableText.setVisibility(View.VISIBLE);
                            } else if (Objects.equals(model.is_active(), "yes")) {
*/

                            try{
                                Glide.with(requireActivity())
                                        .load(model.getMenu_spot_image())
                                        .into(holder.mRestaurantSpotImage);
                            }catch (Exception e){}


                            int netprice=Integer.parseInt(model.getPrice())-Integer.parseInt(model.getDiscount());
                                holder.mAveragePrice.setText("\u20B9" + netprice);
                              holder.mActualPriceText.setText("\u20B9" +model.getPrice());
                            holder.mDiscountText.setText("( \u20B9" + model.getDiscount()+" off)");
                                holder.mRestaurantName.setText(model.getName());
                                holder.mCategoryType.setText(model.getCategory());
                                holder.mDescriptionText.setText(model.getDescription());
                                holder.mAverageDeliveryTime.setVisibility(View.GONE);
                            holder.mRatingBar.setVisibility(View.GONE);



                                String specImage = String.valueOf(model.getSpecification());
                                if (specImage.equals("Veg")) {
                                    try {
                                        Glide.with(requireActivity())
                                                .load(R.drawable.veg_symbol).into(holder.foodSpecification);
                                    }catch (Exception e){}

                                } else {
                                    try {
                                        Glide.with(requireActivity())
                                                .load(R.drawable.non_veg_symbol).into(holder.foodSpecification);
                                    }catch (Exception e){}

                                }

                            holder.mItemAddBtn.setVisibility(View.VISIBLE);
                            holder.mQtyPicker.setVisibility(GONE);


                                String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                                holder.mItemAddBtn.setOnClickListener(view -> {
                                    String selectedItemName = holder.mRestaurantName.getText().toString();
                                    addItemToCart(selectedItemName, model, uid,netprice);
                                    holder.mItemAddBtn.setVisibility(View.GONE);
                                    holder.mQtyPicker.setVisibility(View.VISIBLE);
                                    holder.mQtyPicker.setNumber("1");
                                });




                            holder.mQtyPicker.setOnValueChangeListener((view, oldValue, newValue) -> {


                                Map<String ,String> cartItemMap = new HashMap<>();
                                cartItemMap.put("select_name", holder.mRestaurantName.getText().toString());
                                cartItemMap.put("select_price", String.valueOf(netprice));
                                cartItemMap.put("discount", model.getDiscount());
                                cartItemMap.put("select_specification", model.getSpecification());
                                cartItemMap.put("item_count", String.valueOf(newValue));
                                db.collection("UserList").document(uid).collection("CartItems")
                                        .document(holder.mRestaurantName.getText().toString()).
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
                                                                Intent intent = new Intent(getActivity(), CartItemActivity.class);
                                                                startActivity(intent);
                                                                snackbar1.dismiss();
                                                            });
                                                    snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                                                    snackbar.show();
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
                                    Toast.makeText(getActivity(), "Added Item Successfully", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Adding Item Failed", Toast.LENGTH_SHORT).show();
                                });



                                if (newValue == 0){
                                    String selectedItemName = holder.mRestaurantName.getText().toString();;
                                    removeItemFromCart(selectedItemName, uid);
                                    holder.mQtyPicker.setVisibility(View.GONE);
                                    holder.mItemAddBtn.setVisibility(View.VISIBLE);
                                }
                            });

                            DocumentReference docRef1 = db.collection("UserList").document(uid).collection("CartItems").document(holder.mRestaurantName.getText().toString());
                            docRef1.get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot1 = task1.getResult();
                                    if (Objects.requireNonNull(documentSnapshot1).exists()) {
                                        String count= (String) documentSnapshot1.get("item_count");
                                        holder.mQtyPicker.setNumber(count);
                                        holder.mItemAddBtn.setVisibility(GONE);
                                        holder.mQtyPicker.setVisibility(View.VISIBLE);
                                    }
                                }

                            });

                           /* holder.itemView.setOnClickListener(view -> {

                                Intent intent = new Intent(getActivity(), MainRestaurantPageActivity.class);
                                intent.putExtra("RUID", RUID);
                                intent.putExtra("NAME", model.getRestaurant_name());
                                intent.putExtra("DISTANCE", String.valueOf(distanceInMeters / 1000));
                                intent.putExtra("TIME", deliveryTime);
                                intent.putExtra("PRICE", model.getAverage_price());
                                intent.putExtra("RES_IMAGE", model.getRestaurant_spotimage());
                                intent.putExtra("RES_NUM", model.getRestaurant_phonenumber());
                                startActivity(intent);
                                requireActivity().overridePendingTransition(0, 0);

                            });*/
                          //  }
                        }
                    });
                }

            }

            @NonNull
            @Override
            public RestaurantItemViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.restaurant_main_detail, group, false);
                return new RestaurantItemViewHolder(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        restaurantAdapter.startListening();
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRestaurantRecyclerView);
        restaurantAdapter.notifyDataSetChanged();
        mRestaurantRecyclerView.setAdapter(restaurantAdapter);
    }

    public static class RestaurantItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.resName)
        TextView mRestaurantName;
        @BindView(R.id.resImage)
        ImageView mRestaurantSpotImage;
        @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.average_time)
        TextView mAverageDeliveryTime;
        @BindView(R.id.resCuisine)
        TextView mCategoryType;
        @BindView(R.id.foodMark)
        ImageView foodSpecification;
        @BindView(R.id.addMenuItemBtn)
        Button mItemAddBtn;
        @BindView(R.id.notAvailableText)
        TextView mNotAvailableText;
        @BindView(R.id.actual_price)
        TextView mActualPriceText;
        @BindView(R.id.discount_price)
        TextView mDiscountText;
        @BindView(R.id.tv_desc)
        TextView mDescriptionText;
        @BindView(R.id.ratingBar)
        RatingBar mRatingBar;
        @BindView(R.id.quantityPicker)
        ElegantNumberButton mQtyPicker;


        public RestaurantItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private void removeItemFromCart(String selectedItemName,String uid) {
        db.collection("UserList").document(uid).collection("CartItems").document(selectedItemName).delete().addOnCompleteListener(task ->
                Toast.makeText(getActivity(), "Item Removed From Cart", Toast.LENGTH_SHORT).show());
    }



    private void addItemToCart(String selectedItemName, RestaurantMenuItems model, String uid, int netprice){
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
                                        Intent intent = new Intent(getActivity(), CartItemActivity.class);
                                        startActivity(intent);
                                        snackbar1.dismiss();
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                            snackbar.show();
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
            Toast.makeText(getActivity(), "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });

    }



    private void getFavouriteDetails() {
        Query query = db.collection("RestaurantList").document(mResUid).collection("FavoriteItems");
        FirestoreRecyclerOptions<FavoriteItemDetails> favItemModel = new FirestoreRecyclerOptions.Builder<FavoriteItemDetails>()
                .setQuery(query, FavoriteItemDetails.class)
                .build();
        FirestoreRecyclerAdapter<FavoriteItemDetails, FavouriteItemViewHolder> favItemAdapter = new FirestoreRecyclerAdapter<FavoriteItemDetails, FavouriteItemViewHolder>(favItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull FavouriteItemViewHolder holder, int position, @NonNull FavoriteItemDetails model) {
                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("RestaurantList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            int netprice=Integer.parseInt(model.getItem_price())-Integer.parseInt(model.getDiscount());
                            holder.mAveragePrice.setText("\u20B9" + netprice);
                            holder.mActualPriceText.setText("\u20B9" +model.getItem_price());
                            holder.mDiscountText.setText("( \u20B9" + model.getDiscount()+" off)");
                            holder.mFavName.setText(model.getItem_name());

                            try{
                                Glide.with(requireContext())
                                        .load(model.getItem_image())
                                        .apply(new RequestOptions()
                                                .override(200,200))
                                        .centerCrop()
                                        .into(holder.mFavImage);
                            }catch (Exception e){}


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
                                                                Intent intent = new Intent(getActivity(), CartItemActivity.class);
                                                                startActivity(intent);
                                                                snackbar1.dismiss();
                                                            });
                                                    snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                                                    snackbar.show();
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
                                    Toast.makeText(getActivity(), "Added Item Successfully", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Adding Item Failed", Toast.LENGTH_SHORT).show();
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
            public FavouriteItemViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.custom_cuisine_layout, group, false);
                return new FavouriteItemViewHolder(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        favItemAdapter.startListening();
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mFavouriteRecyclerView);
        favItemAdapter.notifyDataSetChanged();
        mFavouriteRecyclerView.setAdapter(favItemAdapter);
    }

    public static class FavouriteItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cuisineName)
        TextView mFavName;
        @BindView(R.id.cuisineImage)
        ImageView mFavImage;
        @BindView(R.id.addMenuItemBtn)
        Button mItemAddBtn;
        @BindView(R.id.actual_price)
        TextView mActualPriceText;
        @BindView(R.id.discount_price)
        TextView mDiscountText;
        @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.quantityPicker)
        ElegantNumberButton mQtyPicker;
      /*  @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.average_time)
        TextView mAverageDeliveryTime;*/


        public FavouriteItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }





    private void addItemToCartFav(String selectedItemName, FavoriteItemDetails model, String uid,int netprice){
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
                                        Intent intent = new Intent(getActivity(), CartItemActivity.class);
                                        startActivity(intent);
                                        snackbar1.dismiss();
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                            snackbar.show();
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
            Toast.makeText(getActivity(), "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });

    }



    private void sendUserToCheckOut() {
        Intent intent = new Intent(getActivity(), CartItemActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0,0);
    }

    private void sendUserToEmptyCart() {
        Intent intent = new Intent(getActivity(), EmptyCartActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0,0);
    }

}