package adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spindia.food.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import models.SearchItemDetails;
import ui.cart.CartItemActivity;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyHolder> {


        private Context context;
        private ArrayList<SearchItemDetails> searchList=new ArrayList<>();


        public SearchAdapter(Context context, ArrayList<SearchItemDetails> searchList) {
            this.context = context;
            this.searchList = searchList;

        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.custom_search_item_layout,parent,false);
                return new MyHolder(view);



        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int i) {
            SearchItemDetails searchModel = searchList.get(i);
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            holder.searchResName.setText(searchModel.getName());
            int netprice = Integer.parseInt(searchModel.getPrice()) - Integer.parseInt(searchModel.getDiscount());
            holder.average_price.setText("\u20B9" + netprice);
            holder.actual_price.setText("\u20B9" + searchModel.getPrice());
            holder.discount_price.setText("( \u20B9" + searchModel.getDiscount() + " off)");


            Glide.with(context).load(searchModel.getMenu_spot_image()).apply(new RequestOptions().override(152,152)).into(holder.searchImageRes);
            holder.search_addMenuItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedItemName = holder.searchResName.getText().toString();
                    addItemToCart(selectedItemName, searchModel, uid,netprice);
                    holder.search_addMenuItemBtn.setVisibility(View.GONE);
                    holder.mQtyPicker.setVisibility(View.VISIBLE);
                    holder.mQtyPicker.setNumber("1");

                }
            });



            holder.mQtyPicker.setOnValueChangeListener((view, oldValue, newValue) -> {



                HashMap<String, String> cartItemMap = new HashMap<>();
                cartItemMap.put("select_name", holder.searchResName.getText().toString());
                cartItemMap.put("select_price", String.valueOf(netprice));
                cartItemMap.put("discount", searchModel.getDiscount());
                cartItemMap.put("select_specification", searchModel.getSpecification());
                cartItemMap.put("item_count", String.valueOf(newValue));


                String mRestName = "R K Restuarant";
                String mResUid = "fBG9p6jlfbSu1HEaV7vs9LphJ3g1";
                String mResDeliveryTime = "5";
                String mResImage =
                        "https://firebasestorage.googleapis.com/v0/b/food-393d3.appspot.com/o/restaurant_spot_image%2FfBG9p6jlfbSu1HEaV7vs9LphJ3g1.jpg?alt=media&token=9a1de45c-2e4e-4ea3-b1bc-8b3cef59ab15";

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("UserList").document(uid).collection("CartItems")
                        .document(holder.searchResName.getText().toString()).set(cartItemMap)
                        .addOnSuccessListener(aVoid -> {

                            db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task -> {

                                if (task.isSuccessful()){
                                    int count = 0;
                                    for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                                        count++;
                                    }
                                    Toast.makeText(context,"Added $count items in  your cart",Toast.LENGTH_LONG);

                                }
                            });
                        }).addOnFailureListener(e -> {
                        });




                HashMap<String, Object> resNameMap = new HashMap<>();
                resNameMap.put("restaurant_cart_name", mRestName);
                resNameMap.put("restaurant_cart_uid", mResUid);
                resNameMap.put("restaurant_delivery_time", mResDeliveryTime);
                resNameMap.put("restaurant_cart_spotimage", mResImage);
                resNameMap.put("item_count", "1");

                db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added Item Successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Adding Item Failed", Toast.LENGTH_SHORT).show();
                });



                if (newValue == 0){
                    String selectedItemName = holder.searchResName.getText().toString();
                    removeItemFromCart(selectedItemName, uid);
                    holder.mQtyPicker.setVisibility(View.GONE);
                    holder.search_addMenuItemBtn.setVisibility(View.VISIBLE);
                }
            });


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef1 =
                    db.collection("UserList").document(uid).collection("CartItems")
                    .document(holder.searchResName.getText().toString());
            docRef1.get().addOnCompleteListener(task1 -> {

                if (task1.isSuccessful()) {
                    DocumentSnapshot documentSnapshot1 = task1.getResult();
                    if (Objects.requireNonNull(
                            documentSnapshot1
                    ).exists()
                    ) {
                        holder.search_addMenuItemBtn.setVisibility(View.GONE);
                        holder.search_removeMenuItemBtn.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

    private void removeItemFromCart(String selectedItemName, String uid) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserList").document(uid).collection("CartItems").document(selectedItemName).delete().addOnCompleteListener(task ->
                Toast.makeText(context, "Item Removed From Cart", Toast.LENGTH_SHORT).show());
    }

    private void addItemToCart(String selectedItemName, SearchItemDetails searchModel, String uid, int netprice) {
        String mRestName = "R K Restuarant";
        String mResUid = "fBG9p6jlfbSu1HEaV7vs9LphJ3g1";
        String mResDeliveryTime = "5";
        String mResImage =
                "https://firebasestorage.googleapis.com/v0/b/food-393d3.appspot.com/o/restaurant_spot_image%2FfBG9p6jlfbSu1HEaV7vs9LphJ3g1.jpg?alt=media&token=9a1de45c-2e4e-4ea3-b1bc-8b3cef59ab15";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, String> cartItemMap = new HashMap<>();
        cartItemMap.put("select_name", selectedItemName);
        cartItemMap.put("select_price", String.valueOf(netprice));
        cartItemMap.put("discount", searchModel.getDiscount());
        cartItemMap.put("select_specification", searchModel.getSpecification());
        cartItemMap.put("item_count", "1");


        db.collection("UserList").document(uid).collection("CartItems")
                .document(selectedItemName).set(cartItemMap)
                .addOnSuccessListener(aVoid -> {

                    db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()){
                            int count = 0;
                            for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                                count++;
                            }
                            Toast.makeText(context,"Added $count items in  your cart",Toast.LENGTH_LONG);

                        }
                    });
                }).addOnFailureListener(e -> {
                });




        HashMap<String, Object> resNameMap = new HashMap<>();
        resNameMap.put("restaurant_cart_name", mRestName);
        resNameMap.put("restaurant_cart_uid", mResUid);
        resNameMap.put("restaurant_delivery_time", mResDeliveryTime);
        resNameMap.put("restaurant_cart_spotimage", mResImage);
        resNameMap.put("item_count", "1");

        db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });


    }

    @Override
        public int getItemCount() {
            return searchList.size();
        }



    public class MyHolder extends RecyclerView.ViewHolder  {
            public TextView searchResName, average_price,discount_price;
            public ImageView searchImageRes,imgMsg;
        TextView actual_price;
        Button search_addMenuItemBtn,search_removeMenuItemBtn;

        ElegantNumberButton mQtyPicker;

            public MyHolder(final View itemView) {
                super(itemView);
                searchResName = itemView.findViewById(R.id.searchResName);
                average_price = itemView.findViewById(R.id.average_price);
                searchImageRes=itemView.findViewById(R.id.searchImageRes);
                actual_price=itemView.findViewById(R.id.actual_price);
                discount_price=itemView.findViewById(R.id.discount_price);

                search_addMenuItemBtn=itemView.findViewById(R.id.search_addMenuItemBtn);
                mQtyPicker=itemView.findViewById(R.id.quantityPicker);


            }



    }




}


