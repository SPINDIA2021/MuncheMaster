package com.spindia.food;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import chat.MessageActivity;
import fragments.FavouriteFragment;
import fragments.MyProfileFragment;
import fragments.RestaurantFragment;
import fragments.SearchFragment;
import ui.auth.LoginActivity;
import ui.order.OrdersActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser mCurrentUser;
    private BottomNavigationView bottomNav;
    private ImageView imgChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changestatusbarcolor();
        init();
        bottomNav = findViewById(R.id.bottom_navigation);
        imgChat=findViewById(R.id.img_chat);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new RestaurantFragment())
                .commit();

        imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MessageActivity.class));
            }
        });
    }

    String token;
    private void init() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();



        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        HashMap<String,Object> tokenData = new HashMap<>();
                        tokenData.put("token", token);
                        db.collection("Tokens").document(  mCurrentUser.getUid()).set(tokenData)
                                .addOnSuccessListener(aVoid -> {
                                }).addOnFailureListener(e -> {
                                });


                       // Toast.makeText(MainActivity.this, "msg", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void changestatusbarcolor() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.nav_restaurants:
                        selectedFragment = new RestaurantFragment();
                        break;
                    case R.id.nav_search:
                        selectedFragment = new SearchFragment();
                        break;
                    case R.id.nav_favourites:
                        selectedFragment = new FavouriteFragment();
                        break;
                    case R.id.nav_profile:
                       selectedFragment = new MyProfileFragment();
                        break;
                    case R.id.imageBadgeView:
                        bottomNav.setVisibility(View.GONE);
                        break;

                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        Objects.requireNonNull(selectedFragment)).commit();
                return true;
            };

    private void sendUserToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof MyProfileFragment) {
                f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser == null) {
            sendUserToLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
