package chat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spindia.food.R;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import chat.notification.APIService;
import chat.notification.Client;
import chat.notification.Data;
import chat.notification.MyResponse;
import chat.notification.Sender;
import chat.notification.Token;
import models.OrderedItemDetail;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ui.order.CurrentOrderActivity;
import ui.order.OrdersActivity;
import utils.GenerateRandomNum;
import utils.Preferences;

public class MessageActivity extends AppCompatActivity {

    private static final String LOG_TAG ="RECORDTAG" ;
    TextView tvInitial,tvUserName,tvStatus;
    String userId,userName,firstInitial,status;
    EditText editMsg;
    RelativeLayout laySend, layMic;
    RecyclerView recyclerViewChat;
    ImageView imgBack, imgAttach, imgSend;
    RelativeLayout layPDf;
    LinearLayout layDocument,layGallery;

    BottomSheetDialog bottomDialogAttachment;

    MessageAdapter messageAdapter;
   // ArrayList<ChatResponse> chatResponseArrayList=new ArrayList<>();

    FirebaseUser fUser;
    DatabaseReference reference;
    ValueEventListener seenListener;

    APIService apiService;
    boolean notify=false;

    String checker="text";
    Uri fileUri;

    boolean mic=true;
    MediaRecorder mRecorder;

    String mFileName=null;
    String sendDate, sendTime,datetime;
    TextView tvNoDataFound;
    String chatID ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        tvInitial=findViewById(R.id.text_initial);
        tvUserName=findViewById(R.id.text_username);
        editMsg=findViewById(R.id.edit_message);
        laySend=findViewById(R.id.lay_send);
        tvStatus=findViewById(R.id.text_status);
        imgBack=findViewById(R.id.img_back);
        recyclerViewChat=findViewById(R.id.recyclerview_chat);
        imgAttach=findViewById(R.id.img_attachment);
        imgSend=findViewById(R.id.img_send);
        layMic=findViewById(R.id.lay_mic);
        tvNoDataFound=findViewById(R.id.text_nodatafound);
        initView();
    }

    private void initView() {

        userId="fBG9p6jlfbSu1HEaV7vs9LphJ3g1";
        userName="R K Restaurant";



        tvUserName.setText(userName);




        tvInitial.setText("RK");


        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendDate=new SimpleDateFormat("dd MMMM,yyyy", Locale.US).format(new Date());
                sendTime=new SimpleDateFormat("hh:mm aa", Locale.US).format(new Date());
                datetime=new SimpleDateFormat("dd MMMM,yyyy hh:mm:ss aa", Locale.US).format(new Date());
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        progressDialog=new ProgressDialog(this);
        progressDialogRecord=new ProgressDialog(this);
        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        mFileName=  getExternalCacheDir()+"recorded_audio.3gp";




        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);
        recyclerViewChat.setHasFixedSize(true);

        readMessage();

        editMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editMsg.getText().toString().length()==0)
                {
                    mic=true;
                   laySend.setVisibility(View.GONE);
                   layMic.setVisibility(View.VISIBLE);
                    typingStatus("no");
                }else {
                    laySend.setVisibility(View.VISIBLE);
                    layMic.setVisibility(View.GONE);
                    typingStatus("typing");
                    mic=false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        fUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("UserList").child(userId);


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgAttach.setVisibility(View.GONE);
       /* imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogAttachment();
            }
        });*/


        layMic.setVisibility(View.GONE);

       /* layMic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                notify=true;
                checker="audio";
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    startRecordAudio();
                }else if (motionEvent.getAction()==MotionEvent.ACTION_UP){
                    stopRecordAudio();
                }
                return false;
            }
        });*/

        laySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;

                    if (editMsg.getText().toString().isEmpty())
                    {
                        Toast.makeText(MessageActivity.this, "Please enter message first!!", Toast.LENGTH_SHORT).show();
                    }else {
                        sendMessage(fUser.getUid(),userId,editMsg.getText().toString().trim());
                    }

                    editMsg.setText("");
            }
        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
/*
                UserModel userModel=snapshot.getValue(UserModel.class);


                if (userModel.getTyping().equals("typing"))
               {
                    tvStatus.setText("typing...");
                    tvStatus.setVisibility(View.VISIBLE);
                }else if (userModel.getStatus().equals("online"))
                {
                    tvStatus.setText("online");
                    tvStatus.setVisibility(View.VISIBLE);
                }else {
                    String today=new SimpleDateFormat("dd MMM,yyyy", Locale.US).format(new Date());
                    if (userModel.getDate().equals(today))
                    {
                        tvStatus.setText("last seen today at "+userModel.getTime());
                    }else {
                        tvStatus.setText("last seen at "+userModel.getDate()+" "+userModel.getTime());
                    }
                }*/

                //(fUser.getUid(),userId)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }


    private void startRecordAudio() {
        progressDialogRecord.setMessage("Recording Audio..");
        progressDialogRecord.show();

        mRecorder=new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();


    }

    private void stopRecordAudio()
    {
        try{
            mRecorder.stop();
            mRecorder.release();
            mRecorder=null;
            progressDialogRecord.dismiss();
            uploadAudio();
        }catch (Exception e){}


    }



    /*private void openDialogAttachment() {
        bottomDialogAttachment = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        bottomDialogAttachment.setContentView(R.layout.dailog_attachment);
        layDocument=bottomDialogAttachment.findViewById(R.id.lay_document);
        layGallery= bottomDialogAttachment.findViewById(R.id.lay_gallery);
        layPDf= bottomDialogAttachment.findViewById(R.id.lay_pdf);

        layPDf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker="pdf";
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent,"Select PDF"),1);

                bottomDialogAttachment.dismiss();
            }
        });

        layGallery.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                checker="image";
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent,"Select Image"),1);

                bottomDialogAttachment.dismiss();
            }
        });

        layDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker="docx";
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/msword");
                startActivityForResult(intent.createChooser(intent,"Select Document"),1);

                bottomDialogAttachment.dismiss();
            }
        });


        bottomDialogAttachment.show();
    }*/

    ProgressDialog progressDialog,progressDialogRecord;
    private void uploadAudio() {

        progressDialog.setMessage("Uploading Audio..");
        progressDialog.show();

        Uri uri=Uri.fromFile(new File(mFileName));
        DatabaseReference pushRef= FirebaseDatabase.getInstance().getReference("Chats").child(fUser.getUid()).child(userId).push();
        String pushId=pushRef.getKey();

        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("message_audio").child(pushId+".3gp");

        storageReference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                progressDialog.dismiss();
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    downloadUrl = downloadUri.toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("sender",fUser.getUid());
                    hashMap.put("date",sendDate);
                    hashMap.put("time",sendTime);
                    hashMap.put("type","audio");
                    hashMap.put("reciever",userId);
                    hashMap.put("message",downloadUrl);
                    hashMap.put("isseen",false);

                    reference.child("Chats").push().setValue(hashMap);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

       /* reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                if (notify)
                {
                    sendNotification(userId,userModel.getUsername(),checker);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
*/
    }

    String downloadUrl="";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 1 && resultCode==RESULT_OK && data !=null)
        {
            if (checker.equals("image"))
            {
                progressDialogRecord.setMessage("Sending image..");
                progressDialogRecord.show();
                fileUri= data.getData();

                DatabaseReference pushRef= FirebaseDatabase.getInstance().getReference("Chats").child(fUser.getUid()).child(userId).push();
                String pushId=pushRef.getKey();

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("message_images").child(pushId+".jpg");


                storageReference.putFile(fileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            downloadUrl = downloadUri.toString();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender",fUser.getUid());
                            hashMap.put("date",sendDate);
                            hashMap.put("time",sendTime);
                            hashMap.put("type","image");
                            hashMap.put("reciever",userId);
                            hashMap.put("message",downloadUrl);
                            hashMap.put("isseen",false);

                            reference.child("Chats").push().setValue(hashMap);
                            progressDialogRecord.dismiss();
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            }else if (checker.equals("pdf")){

                progressDialogRecord.setMessage("Sending pdf..");
                progressDialogRecord.show();
                fileUri= data.getData();
                DatabaseReference pushRef= FirebaseDatabase.getInstance().getReference("Chats").child(fUser.getUid()).child(userId).push();
                String pushId=pushRef.getKey();

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("message_documents").child(pushId+"."+checker);

                storageReference.putFile(fileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            downloadUrl = downloadUri.toString();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender",fUser.getUid());
                            hashMap.put("date",sendDate);
                            hashMap.put("time",sendTime);
                            hashMap.put("type","pdf");
                            hashMap.put("reciever",userId);
                            hashMap.put("message",downloadUrl);
                            hashMap.put("isseen",false);

                            reference.child("Chats").push().setValue(hashMap);
                            progressDialogRecord.dismiss();
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });
            }else if (checker.equals("docx")){
                progressDialogRecord.setMessage("Sending document..");
                progressDialogRecord.show();
                fileUri= data.getData();
                DatabaseReference pushRef= FirebaseDatabase.getInstance().getReference("Chats").child(fUser.getUid()).child(userId).push();
                String pushId=pushRef.getKey();

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("message_documents").child(pushId+"."+checker);

                storageReference.putFile(fileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            downloadUrl = downloadUri.toString();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender",fUser.getUid());
                            hashMap.put("date",sendDate);
                            hashMap.put("time",sendTime);
                            hashMap.put("type","docx");
                            hashMap.put("reciever",userId);
                            hashMap.put("message",downloadUrl);
                            hashMap.put("isseen",false);

                            reference.child("Chats").push().setValue(hashMap);
                            progressDialogRecord.dismiss();
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });
            }

         /*   reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    UserModel userModel=snapshot.getValue(UserModel.class);
                    if (notify)
                    {
                        sendNotification(userId,userModel.getUsername(),checker);
                    }

                    notify=false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });*/
        }
    }


    private FirebaseFirestore db;
    String uid;
    private DocumentReference mUserRef;
    public void sendMessage(String sender, String reciever, String message)
    {
        chatID =  GenerateRandomNum.Companion.generateRandNum();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("date",sendDate);
        hashMap.put("time",sendTime);
        hashMap.put("create_date",datetime);
        hashMap.put("type","text");
        hashMap.put("reciever",reciever);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        hashMap.put("id",chatID);

        db = FirebaseFirestore.getInstance();

        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        final String msg=message;

        db.collection("UserList").document(uid).collection("UserChat").document(chatID).set(hashMap).addOnCompleteListener(task -> {

            if (notify)
            {
                sendNotification("fBG9p6jlfbSu1HEaV7vs9LphJ3g1","R K Restuarant",msg);
            }
            notify=false;


        });


        HashMap<String,Object> hashMapRes=new HashMap<>();
        hashMapRes.put("sender",sender);
        hashMapRes.put("date",sendDate);
        hashMapRes.put("time",sendTime);
        hashMapRes.put("create_date",datetime);
        hashMapRes.put("type","text");
        hashMapRes.put("reciever",reciever);
        hashMapRes.put("message",message);
        hashMapRes.put("isseen",false);
        hashMapRes.put("id",chatID);
        db.collection("RestaurantList").document("fBG9p6jlfbSu1HEaV7vs9LphJ3g1").collection("UserChat").document(chatID).set(hashMapRes).addOnCompleteListener(task -> {

  Toast.makeText(getApplicationContext(),"Message send successfully",Toast.LENGTH_LONG).show();

        });





  /*      reference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("ChatList").child(fUser.getUid()).child(userId);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        final String msg=message;*/

        /*reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                if (notify)
                {
                    sendNotification(reciever,userModel.getUsername(),msg);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });*/

    }


    Data data;
    private void sendNotification(String reciever, String username, String msg) {

        DocumentReference mTokenRef;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mTokenRef = db.collection("Tokens").document(reciever);

        mTokenRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                String token = String.valueOf(documentSnapshot.get("token"));
                if (checker.equals("text"))
                {
                    data=new Data(fUser.getUid(),R.mipmap.ic_launcher,username+": "+msg,"New Message",userId, username);
                }else
                {
                    data=new Data(fUser.getUid(),R.mipmap.ic_launcher,username+": "+checker,"New Message",userId, username);
                }

                Sender sender= new Sender(data,token);

                apiService.sendNotification(sender)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.code()==200)
                                {
                                    if (response.body().success!=1)
                                    {
                                        Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {}
                        });
            }
        });
    }


    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private Context context;
   // private ArrayList<ChatResponse> chatResponses=new ArrayList<>();
    private static final int MSG_UPDATE_SEEK_BAR = 1845;

    private MediaPlayer mediaPlayer;
    private int playingPosition;
    private Handler uiUpdateHandler;
    private MessageAdapter.MyHolder playingHolder;

    public void readMessage()
    {
        //chatResponseArrayList=new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


      com.google.firebase.firestore.Query query = db.collection("UserList").document(uid).collection("UserChat").orderBy("create_date");
        FirestoreRecyclerOptions<ChatResponse> chatItemModel = new FirestoreRecyclerOptions.Builder<ChatResponse>()
                .setQuery(query, ChatResponse.class)
                .build();


        FirestoreRecyclerAdapter<ChatResponse, ChatItemHolder> chatAdapter = new FirestoreRecyclerAdapter<ChatResponse, ChatItemHolder>(chatItemModel) {
            @Override
            protected void onBindViewHolder(@NonNull ChatItemHolder holder, int i, @NonNull ChatResponse model) {

                ChatResponse chatResponse = model;
                 chatID=model.id;
                seenMsg(chatID,model.reciever,model.sender);

                holder.tvSeen.setText(chatResponse.getTime());

                DateFormat dateFormat = new SimpleDateFormat("dd MMMM,yyyy");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String yesterdayDate= dateFormat.format(cal.getTime());

                String  todayDate=new SimpleDateFormat("dd MMMM,yyyy", Locale.US).format(new Date());
                if (chatResponse.getDate().equals(todayDate))
                {
                    holder.tvDate.setText("TODAY");
                }else if (chatResponse.getDate().equals(yesterdayDate))
                {
                    holder.tvDate.setText("YESTERDAY");
                }else
                {
                    holder.tvDate.setText(chatResponse.getDate());
                }

               // holder.layDate.setVisibility(View.GONE);

                if (i > 0) {
                    if (chatItemModel.getSnapshots().get(i).getDate().equalsIgnoreCase(chatItemModel.getSnapshots().get(i - 1).getDate())) {
                        holder.tvDate.setVisibility(View.GONE);
                    } else {
                        holder.tvDate.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.tvDate.setVisibility(View.VISIBLE);
                }

                if (chatResponse.getType().equals("image"))
                {
                    holder.tvMessage.setVisibility(View.GONE);

                    holder.layImg.setVisibility(View.VISIBLE);
                    //  holder.layAudio.setVisibility(View.GONE);


                    Glide.with(context)
                            .load(chatResponse.getMessage())
                            .placeholder(R.drawable.restaurant_image_placeholder)
                            .into(holder.imgMsg);


                }else if (chatResponse.getType().equals("audio"))
                {
                    holder.tvMessage.setVisibility(View.GONE);

                    holder.layImg.setVisibility(View.GONE);
                    //  holder.layAudio.setVisibility(View.VISIBLE);

                }else {
                    holder.tvMessage.setVisibility(View.VISIBLE);

                    holder.layImg.setVisibility(View.GONE);
                    //  holder.layAudio.setVisibility(View.GONE);
                    holder.tvMessage.setText(chatResponse.getMessage());
                }

               /* if (i == playingPosition) {
                    playingHolder = holder;
                    // this view holder corresponds to the currently playing audio cell
                    // update its view to show playing progress
                    updatePlayingView();
                } else {
                    // and this one corresponds to non playing
                    updateNonPlayingView(holder);
                }*/


                if (i== chatItemModel.getSnapshots().size()-1)
                {
                    if (model.isseen)
                    {
                        holder.imgSeen.setImageResource(R.drawable.doubletick);
                        holder.imgSeen.setColorFilter(getResources().getColor(R.color.colorPrimary));

                        //holder.tvSeen.setText("Seen");
                    }else {
                        holder.imgSeen.setImageResource(R.drawable.singletick);
                        holder.imgSeen.getLayoutParams().height = 26;
                        holder.imgSeen.getLayoutParams().width = 50;
                        holder.imgSeen.setColorFilter(getResources().getColor(R.color.grey));
                        //  holder.tvSeen.setText("Delivered");
                    }

                }else {
                    holder.imgSeen.setVisibility(View.GONE);
                }

            }


            @Override
            public int getItemViewType(int position) {
                fUser= FirebaseAuth.getInstance().getCurrentUser();

                if (chatItemModel.getSnapshots().get(position).getSender().equals(fUser.getUid()))
                {
                    return MSG_TYPE_RIGHT;
                }else {
                    return MSG_TYPE_LEFT;
                }
                //return super.getItemViewType(position);
            }

            @NonNull
            @Override
            public ChatItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                if (viewType==MSG_TYPE_RIGHT)
                {
                    View view= LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.row_chat_right,parent,false);
                    return new ChatItemHolder(view);
                }else {
                    View view= LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.row_chat_left,parent,false);
                    return new ChatItemHolder(view);
                }


            }
        };
        chatAdapter.startListening();
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.setAdapter(chatAdapter);




       /* reference=FirebaseDatabase.getInstance().getReference().child("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatResponseArrayList.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    ChatResponse chatResponse=snapshot.getValue(ChatResponse.class);

                    if (chatResponse.getReciever().equals(myId)&&chatResponse.getSender().equals(userId)||
                    chatResponse.getReciever().equals(userId)&&chatResponse.getSender().equals(myId))
                    {
                        chatResponseArrayList.add(chatResponse);
                    }
                }


                if (chatResponseArrayList.size()!=0)
                {
                    recyclerViewChat.setVisibility(View.VISIBLE);
                    tvNoDataFound.setVisibility(View.GONE);
                    messageAdapter=new MessageAdapter(MessageActivity.this,chatResponseArrayList);
                    recyclerViewChat.setAdapter(messageAdapter);
                }else {
                    recyclerViewChat.setVisibility(View.GONE);
                    tvNoDataFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });*/
    }



    public static class ChatItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_msg)
        TextView tvMessage;
        @BindView(R.id.text_seen)
        TextView tvSeen;
        @BindView(R.id.img_seen)
        ImageView imgSeen;
        @BindView(R.id.img_message)
        ImageView imgMsg;
        @BindView(R.id.lay_img)
        RelativeLayout layImg;
        @BindView(R.id.text_date)
        TextView tvDate;
        @BindView(R.id.lay_date)
        RelativeLayout layDate;

        public ChatItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private void status(String status)
    {
       String currentDate=new SimpleDateFormat("dd MMM,yyyy", Locale.US).format(new Date());
       String currentTime=new SimpleDateFormat("hh:mm aa", Locale.US).format(new Date());

        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("UserList").child(fUser.getUid());

        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("time",currentTime);
        hashMap.put("date",currentDate);
        hashMap.put("status",status);
        hashMap.put("typing","no");
        reference.updateChildren(hashMap);
    }

    private void typingStatus(String status)
    {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("UserList").child(fUser.getUid());

        HashMap<String, Object> hashMap= new HashMap<>();

        hashMap.put("typing",status);
        reference.updateChildren(hashMap);
    }

    private void seenMsg(String chatid, String reciever, String sender)
    {
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db = FirebaseFirestore.getInstance();



        if (reciever.equals(uid) && sender.equals("fBG9p6jlfbSu1HEaV7vs9LphJ3g1"))
        {
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("isseen",true);

            db.collection("UserList").document(uid).collection("UserChat").document(chatid).update(hashMap).addOnCompleteListener(task -> {

            });
        }



    }

    private void currentUser(String userId)
    {
        SharedPreferences.Editor editor= getSharedPreferences("PREF",MODE_PRIVATE).edit();
        editor.putString("currentuser",userId);
        editor.apply();


    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
       /* FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("UserList").child(fUser.getUid());
        reference.removeEventListener(seenListener);*/
        status("offline");
        currentUser("none");
    }

}
