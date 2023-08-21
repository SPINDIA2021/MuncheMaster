package ui.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spindia.food.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

import chat.MessageActivity;
import models.OrderedItemDetail;
import butterknife.BindView;
import butterknife.ButterKnife;
import utils.Preferences;

public class OrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mOrderItemRecyclerView;
    private String uid;
    private String USER_LIST = "UserList";
    private String USER_ORDERS = "UserOrders";
    private ImageView mGoBackBtn, imgChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        init();
        fetchOrderedItems();

    }
    
    private void init() {
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        db = FirebaseFirestore.getInstance();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mOrderItemRecyclerView = findViewById(R.id.orderedItemsRecyclerView);
        mOrderItemRecyclerView.setLayoutManager(linearLayoutManager);
        mGoBackBtn = findViewById(R.id.cartBackBtn);
        imgChat=findViewById(R.id.img_chat);

        imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrdersActivity.this, MessageActivity.class));
            }
        });

        mGoBackBtn.setOnClickListener(view -> this.onBackPressed());

    }

    private void fetchOrderedItems() {
        Query query = db.collection(USER_LIST).document(uid).collection(USER_ORDERS);
        FirestoreRecyclerOptions<OrderedItemDetail> orderedItemModel = new FirestoreRecyclerOptions.Builder<OrderedItemDetail>()
                .setQuery(query, OrderedItemDetail.class)
                .build();

        FirestoreRecyclerAdapter<OrderedItemDetail, OrderedItemHolder> orderAdapter = new FirestoreRecyclerAdapter<OrderedItemDetail, OrderedItemHolder>(orderedItemModel) {
            @Override
            protected void onBindViewHolder(@NonNull OrderedItemHolder holder, int item, @NonNull OrderedItemDetail model) {

                ArrayList<String> orderedItems = model.getOrdered_items();
                for (int i = 0; i < (orderedItems != null ? orderedItems.size() : 0); i++) {
                    TextView tv = new TextView(getApplicationContext());
                    final Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.open_sans);
                    tv.setText(orderedItems.get(i));
                    tv.setTypeface(typeface);
                    tv.setTextColor(getResources().getColor(R.color.colorAccent));
                    tv.setTextSize(15);
                    holder.layout.addView(tv);
                }

                holder.mOrderedResName.setText(model.getOrdered_restaurant_name());
                holder.mOrderedTime.setText(model.getOrdered_time());
                holder.mTotalAmount.setText(model.getTotal_amount());
                holder.tvOrderStatus.setText(model.getOrder_status());
                Glide.with(getApplicationContext())
                        .load(model.getOrdered_restaurant_spotimage())
                        .placeholder(R.drawable.restaurant_image_placeholder)
                        .into(holder.mOrderedResImage);

                if (model.getOrder_status().equals("Completed")||model.getOrder_status().equals("Declined"))
                {
                    holder.layTrackOrder.setVisibility(View.GONE);
                }else {
                    holder.layTrackOrder.setVisibility(View.VISIBLE);
                }

                holder.layTrackOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(OrdersActivity.this, CurrentOrderActivity.class);
                        intent.putExtra("RES_UID", Preferences.getString(Preferences.CURRENT_RUID));
                        intent.putExtra("orderid", model.getOrdered_id());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderedItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ordered_items_layout, parent, false);

                return new OrderedItemHolder(view);
            }
        };
        orderAdapter.startListening();
        orderAdapter.notifyDataSetChanged();
        mOrderItemRecyclerView.setAdapter(orderAdapter);

    }

    public static class OrderedItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.orderedResName)
        TextView mOrderedResName;
        @BindView(R.id.orderedTimeStamp)
        TextView mOrderedTime;
        @BindView(R.id.orderedAmount)
        TextView mTotalAmount;
        @BindView(R.id.orderedItemsLayout)
        LinearLayout layout;
        @BindView(R.id.orderedResImage)
        ImageView mOrderedResImage;
        @BindView(R.id.text_orderstatus)
        TextView tvOrderStatus;
        @BindView(R.id.lay_track_order)
        RelativeLayout layTrackOrder;

        public OrderedItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}