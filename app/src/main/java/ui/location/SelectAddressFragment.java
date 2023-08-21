package ui.location;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mapbox.mapboxsdk.Mapbox;
import com.spindia.food.MainActivity;
import com.spindia.food.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import fragments.RestaurantFragment;
import models.FavoriteRestaurantDetails;
import utils.Preferences;

public class SelectAddressFragment extends AppCompatActivity {

    private FirebaseFirestore db;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mFavoriteResRecyclerView;
    private String uid;

    public SelectAddressFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_favourite);
        init();
        getFavoriteRes();


        init();

    }



    @SuppressLint("SetTextI18n")
    private void init() {
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        ImageView mGoBackBtn = findViewById(R.id.cartBackBtn);
        mGoBackBtn.setOnClickListener(view -> {
           onBackPressed();
        });
        TextView mFavoriteResToolBarText = findViewById(R.id.confirmOrderText);
        mFavoriteResToolBarText.setText("Select Address");
        db = FirebaseFirestore.getInstance();
        mFavoriteResRecyclerView = findViewById(R.id.favoriteResRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mFavoriteResRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void getFavoriteRes() {
        Query query = db.collection("UserList").document(uid).collection("Addresses");
        FirestoreRecyclerOptions<AddressDetails> favResModel = new FirestoreRecyclerOptions.Builder<AddressDetails>()
                .setQuery(query, AddressDetails.class)
                .build();
        FirestoreRecyclerAdapter<AddressDetails, FavoriteResMenuHolder> adapter = new FirestoreRecyclerAdapter<AddressDetails, FavoriteResMenuHolder>(favResModel) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull FavoriteResMenuHolder holder, int i, @NonNull AddressDetails model) {

                holder.tvName.setText(model.getAddress_name());
                holder.tvAddress.setText( model.getAddress_line1()+" , " + model.getAddress_line2()
                        +" , " + model.getAddress_city()+" , " + model.getAddress_state()+" (" + model.getAddress_country()+" ), " + model.getAddress_pincode());
                holder.tvMobile.setText(model.getAddress_mobile());

                holder.laySelectAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Preferences.saveValue(Preferences.CurrentAddress,model.getAddress_line1()+" , " + model.getAddress_line2()
                                +" , " + model.getAddress_city()+" , " + model.getAddress_state()+" (" + model.getAddress_country()+" ), " + model.getAddress_pincode());
                    startActivity(new Intent(SelectAddressFragment.this, MainActivity.class));
                    }

                });
            }

            @NonNull
            @Override
            public FavoriteResMenuHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.row_address, group, false);
                return new FavoriteResMenuHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        mFavoriteResRecyclerView.setAdapter(adapter);

    }

    public static class FavoriteResMenuHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_mobile)
        TextView tvMobile;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.lay_select_address)
        RelativeLayout laySelectAddress;


        public FavoriteResMenuHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}