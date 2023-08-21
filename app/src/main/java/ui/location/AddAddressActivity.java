package ui.location;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.spindia.food.MainActivity;
import com.spindia.food.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import ui.cart.CartItemActivity;
import utils.GenerateRandomNum;

public class AddAddressActivity extends AppCompatActivity  {

    Spinner spinnerAddresstype;
    EditText edName,edMobile,edAddress1,edAddress2,edCity,edPincode,edState,edCountry;
    RelativeLayout layAddAddress;
    String addresstype="Home";
    String name="";
    String mobile="";
    String address1="";
    String address2="";
    String city="";
    String pincode="";
    String state="";
    String country="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_newaddress);


        init();

    }

    private void init() {

        edName=findViewById(R.id.edit_name);
        edMobile=findViewById(R.id.edit_mobile);
        edAddress1=findViewById(R.id.edit_address1);
        edAddress2=findViewById(R.id.edit_address2);
        edCity=findViewById(R.id.edit_city);
        edPincode=findViewById(R.id.edit_pincode);
        edState=findViewById(R.id.edit_state);
        edCountry=findViewById(R.id.edit_country);
        spinnerAddresstype=findViewById(R.id.spinner_addresstype);
        layAddAddress=findViewById(R.id.lay_addaddress);


        try {

            /*setting appointmentResponse list data on spinner..*/
            String[] itemsLocation = getResources().getStringArray(R.array.addresstype);


            ArrayAdapter<String> arrayAdapter;
            arrayAdapter= new ArrayAdapter<String>(this, R.layout.spinneritem_address,itemsLocation);
            arrayAdapter.setDropDownViewResource(R.layout.row_simple_spinner_dropdown_item_address);

            //setting adapter to spinner
            spinnerAddresstype.setAdapter(arrayAdapter);

            spinnerAddresstype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addresstype=itemsLocation[position];

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }catch (Exception e){
            e.printStackTrace();
        }



        layAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });
    }


    private void saveAddress() {
         FirebaseFirestore db=FirebaseFirestore.getInstance();
         String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

         name=edName.getText().toString();
         mobile=edMobile.getText().toString();
         address1=edAddress1.getText().toString();
         address2=edAddress2.getText().toString();
         city=edCity.getText().toString();
         pincode=edPincode.getText().toString();
         state=edState.getText().toString();
         country=edCountry.getText().toString();


        String addressID = GenerateRandomNum.Companion.generateRandNum();

        Map<String, Object> addressItemsMap = new HashMap<>();
        addressItemsMap.put("address_id", addressID);
        addressItemsMap.put("address_name", name);
        addressItemsMap.put("address_mobile",  mobile);
        addressItemsMap.put("address_line1", address1);
        addressItemsMap.put("address_line2", address2);
        addressItemsMap.put("address_city", city);
        addressItemsMap.put("address_pincode", pincode);
        addressItemsMap.put("address_state", state);
        addressItemsMap.put("address_country",country);
        addressItemsMap.put("address_type", addresstype);
        addressItemsMap.put("isChecked", false);
        db.collection("UserList").document(uid).collection("Addresses").document(addressID).set(addressItemsMap).addOnCompleteListener(task -> {

            Toast.makeText(getApplicationContext(),"Address Saved Successfully",Toast.LENGTH_LONG).show();

        });
    }


}
