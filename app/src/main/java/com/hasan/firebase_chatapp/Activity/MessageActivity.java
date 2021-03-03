package com.hasan.firebase_chatapp.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hasan.firebase_chatapp.Adapter.MessageAdapter;
import com.hasan.firebase_chatapp.Fragments.APIService;
import com.hasan.firebase_chatapp.Fragments.view.ViewProfileFragment;
import com.hasan.firebase_chatapp.Model.Chat;
import com.hasan.firebase_chatapp.Model.Chatlist;
import com.hasan.firebase_chatapp.Model.User;

import com.hasan.firebase_chatapp.Notifications.Client;
import com.hasan.firebase_chatapp.Notifications.Data;
import com.hasan.firebase_chatapp.Notifications.MyResponse;
import com.hasan.firebase_chatapp.Notifications.Sender;
import com.hasan.firebase_chatapp.Notifications.Token;
import com.hasan.firebase_chatapp.R;
import com.hasan.firebase_chatapp.Util.OnLoadMoreListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    TextView status;

    FirebaseUser fuser;
    User cuser;
    DatabaseReference reference;
    DatabaseReference referencew;
    DatabaseReference referenceM;
    ImageButton btn_send;
    ImageButton foto_send;
    ImageButton record;
    ImageButton cancel;
    ImageButton copy;
    ImageButton imoje;
    EmojiconEditText text_send;
    ProgressBar load;

    MessageAdapter messageAdapter;
    List<Chat> mchat = new ArrayList<>();
    List<Chat> mchatL = new ArrayList<>();
    RelativeLayout relativeLayout;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;
    ValueEventListener isWritingListener;

    String userid;
    String user_url;

    APIService apiService;
    Handler myHandler;
    boolean notify = false;
    Context context = this;
    StorageReference storageReference;

    private Bitmap Sbitmap;
    private static final int PICK_IMAGE = 100;
    private static final int IMAGE_REQUEST = 1;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private MediaRecorder mediaRecorder;
    private String recordFile;
    private Uri imageUri;
    private StorageTask uploadTask;
    private boolean bir = true;
    private String copyM;
    ClipData myClip;
    ClipboardManager myClipboard;

    private boolean recording = false;
    EmojIconActions emojIcon;
    protected Handler handler;
    Handler handle = new Handler();
    Runnable  runa = new Runnable() {
        @Override
        public void run() {
            isWriting(false);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        init();
        status.setVisibility(View.GONE);
        foto_send.setVisibility(View.VISIBLE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));

        copyViev();
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = sifrele(text_send.getText().toString());
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg, "null", "null");
                }
                text_send.setText("");
                isWriting(false);
            }
        });

        foto_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MessageActivity.this);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTask.cancel();
                cancel.setVisibility(View.GONE);
                load.setVisibility(View.GONE);

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                cuser = user;
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                if (bir) {
                    readMesagges(fuser.getUid(), userid, user.getImageURL());
                    user_url = user.getImageURL();
                    bir = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vievProfile();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("text", copyM);
                myClipboard.setPrimaryClip(myClip);
                copy.setVisibility(View.GONE);
                readMesagges(fuser.getUid(), userid, user_url);
            }
        });



        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.equals("")) {
                    handler.removeCallbacks(runa);
                    isWriting(true);
                    handler.postDelayed(runa,1000);

                    // btn_send.setVisibility(View.VISIBLE);
                    // record.setVisibility(View.GONE);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        text_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojIcon.closeEmojIcon();
            }
        });


        Statusw();

        imoje();

        LinearLayoutManager Layout = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(Layout);
        messageAdapter = new MessageAdapter(MessageActivity.this, getApplicationContext(), mchatL, recyclerView);
        recyclerView.setAdapter(messageAdapter);

        messageAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //add null , so the adapter will check view_type and show progress bar at bottom
                mchatL.add(null);
                messageAdapter.notifyItemInserted(mchatL.size() - 1);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //   remove progress item
                        if (mchatL.size() != 0) {
                            mchatL.remove(mchatL.size() - 1);
                            messageAdapter.notifyItemRemoved(mchatL.size());
                            //add items one by one
                            int start = mchatL.size();
                            int end = start + 10;
                            for (int i = start; i <= end; i++) {
                                if (mchat.size() > i) {
                                    mchatL.add(mchat.get(i));
                                    messageAdapter.notifyItemInserted(mchatL.size());
                                }
                            }

                            messageAdapter.setLoaded();
                            //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                        }
                    }
                }, 2000);

            }
        });
    }


    private Uri compres(Bitmap file) {

        int nh = (int) (file.getHeight() * (1024.0 / file.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(file, 1024, nh, true);
        Bitmap bitmap = scaled;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), bitmap, "IMG_" + Calendar.getInstance().getTime(), null);
        Uri uri = Uri.parse(path);
        return uri;
    }

    public void imoje() {
        emojIcon = new EmojIconActions(this, relativeLayout, text_send, imoje);
        emojIcon.setIconsIds(R.drawable.klavye, R.drawable.imoje);
        emojIcon.ShowEmojIcon();


        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {


            }

            @Override
            public void onKeyboardClose() {

            }
        });
    }

    private void vievProfile() {
        Fragment fragmentk = getSupportFragmentManager().findFragmentByTag(getString(R.string.vievProfil));
        if (fragmentk != null)
            getSupportFragmentManager().beginTransaction().remove(fragmentk).commit();
        else {
            ViewProfileFragment fragment = new ViewProfileFragment();
            Bundle args = new Bundle();
            args.putString(getString(R.string.intent_user), cuser.getId());
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.Main, fragment, getString(R.string.vievProfil));
            transaction.commit();
        }
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        handler = new Handler();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        record = findViewById(R.id.record);
        copy = findViewById(R.id.copy);
        copy.setVisibility(View.GONE);
        imoje = findViewById(R.id.imoje);
        load = findViewById(R.id.fotoup);
        load.setVisibility(View.GONE);
        cancel = findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        myHandler = new Handler();
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        foto_send = findViewById(R.id.foto_send);
        text_send = findViewById(R.id.text_send);
        status = findViewById(R.id.status);
        relativeLayout = findViewById(R.id.Main);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        NotifyCancel(userid);
        seenMessage(userid);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

    }


    public void copyViev() {
        if (copyM != null && copyM != "null")
            copy.setVisibility(View.VISIBLE);
        else if (copyM != null && copyM.equals("null"))
            copy.setVisibility(View.GONE);

    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            copyM = intent.getStringExtra("item");
            copyViev();
        }
    };

    private void NotifyCancel(String id) {
        String s = "";
        String s0 = id.replaceAll("[\\D]", "");
        for (int i = 0; i < 8; i++) {
            if (s0.length() > i) {
                s += s0.charAt(i);
            }
        }
        int NOTIFICATION_ID = Integer.parseInt(s);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void seenMessage(final String userid) {
        referenceM = FirebaseDatabase.getInstance().getReference("Chats").child(userid).child(fuser.getUid());
        seenListener = referenceM.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message, String sndimage, String sndr) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String zaman = df.format("HH:mm", new java.util.Date()).toString();
        String mzaman = df.format("yyMMddHHmmss", new java.util.Date()).toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("date", zaman);
        hashMap.put("isseen", false);
        hashMap.put("image", sndimage);
        hashMap.put("record", sndr);

        reference.child("Chats").child(userid).child(fuser.getUid()).push().setValue(hashMap);
        reference.child("Chats").child(fuser.getUid()).child(userid).push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist");
        Chatlist B = new Chatlist(userid, mzaman);
        Chatlist O = new Chatlist(fuser.getUid(), mzaman);
        chatRef.child(fuser.getUid()).child(userid).setValue(B);
        chatRef.child(userid).child(fuser.getUid()).setValue(O);


        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.icon, message, username,
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Hata!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(myid).child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        chat.setMessage(sifrecoz(chat.getMessage()));
                        if (!mchat.equals(chat))
                            mchat.add(chat);
                    }

                }
                Collections.reverse(mchat);

                yansit();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void yansit() {
        if (mchatL != null) {
            mchatL.clear();
            messageAdapter.notifyDataSetChanged();
        }
        for (int x = 0; x < 20; x++) {
            if (mchat.size() > x) {
                mchatL.add(mchat.get(x));

                messageAdapter.notifyItemInserted(mchatL.size());

                messageAdapter.setLoaded();
            }
        }
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    private void Statusw() {
        try {
            referencew = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            isWritingListener = referencew.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    status.setVisibility(View.VISIBLE);
                    if (user.getIsWriting() == null) {
                        status.setVisibility(View.GONE);
                    } else if (user.getIsWriting().equals(fuser.getUid())) {
                        status.setVisibility(View.VISIBLE);
                        status.setText("yazıyor");
                    } else {
                        status.setVisibility(View.VISIBLE);
                        status.setText(user.getStatus());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            status.setText("");
        }

    }

    private void isWriting(Boolean Wstatus) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();

        if (Wstatus) {
            hashMap.put("isWriting", userid);
        } else {
            hashMap.put("isWriting", "0");
        }
        reference.updateChildren(hashMap);

    }

    private static String sifrele(String s) {
        String tkar = "ıüğşİÜĞŞ";
        String sifreli = "";
        for (int i = 0; i < s.length(); i++) {
            boolean sayac = true;
            for (int b = 0; b < tkar.length(); b++) {
                if (tkar.charAt(b) == s.charAt(i)) {
                    sifreli += s.charAt(i);
                    sayac = false;
                    break;
                }
            }

            if (sayac) {
                int a = (int) s.charAt(i);
                if (a != 32)
                    sifreli += Character.valueOf((char) (a - 32));
                else
                    sifreli += Character.valueOf((char) (a));
            }
        }


        return sifreli;


    }

    private static String sifrecoz(String s) {

        String tkar = "ıüğşİÜĞŞ";
        String sifreli = "";
        for (int i = 0; i < s.length(); i++) {
            boolean sayac = true;
            for (int b = 0; b < tkar.length(); b++) {
                if (tkar.charAt(b) == s.charAt(i)) {
                    sifreli += s.charAt(i);
                    sayac = false;
                    break;
                }
            }

            if (sayac) {
                int a = (int) s.charAt(i);
                if (a != 32)
                    sifreli += Character.valueOf((char) (a + 32));
                else
                    sifreli += Character.valueOf((char) (a));
            }
        }


        return sifreli;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                uploadImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void uploadImage() {
        if (imageUri != null) {
            load.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            final StorageReference fileReference = storageReference.child(fuser.getUid()).child(userid).child("galery").child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri uri = compres(bitmap);

            uploadTask = fileReference.putFile(uri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        load.setVisibility(View.GONE);
                        cancel.setVisibility(View.GONE);
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        String s = sifrele("Fotograf !!!");
                        notify = true;
                        sendMessage(fuser.getUid(), userid, s, mUri, "null");

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), "hata", Toast.LENGTH_LONG).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    load.setProgress((int) progress);
                }
            });
        }
    }

    public void uploadRecord() {
        mediaRecorder.stop();
        mediaRecorder.release();

        String path = getApplication().getExternalFilesDir("/").getAbsolutePath();
        Uri recordUri = Uri.fromFile(new File(path + "/" + recordFile));

        load.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        final StorageReference fileReference = storageReference.child(fuser.getUid()).child(userid).child("Record").child(recordFile);

        uploadTask = fileReference.putFile(recordUri);
        uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            return fileReference.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    load.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                    String s = sifrele("ses !!!");
                    notify = true;
                    sendMessage(fuser.getUid(), userid, s, "null", mUri);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "hata", Toast.LENGTH_LONG).show();
            }
        });

    }



    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        status("Aktif");
        currentUser(userid);
    }

    @Override
    public void onPause() {
        super.onPause();
        referenceM.removeEventListener(seenListener);
        referencew.removeEventListener(isWritingListener);
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String zaman = df.format("yyyy/MM/dd HH:mm", new java.util.Date()).toString();
        isWriting(false);
        status(getString(R.string.sg) + zaman);
        currentUser("none");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        referenceM.removeEventListener(seenListener);
        referencew.removeEventListener(isWritingListener);
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String zaman = df.format("yyyy/MM/dd HH:mm", new java.util.Date()).toString();
        isWriting(false);
        status(getString(R.string.sg) + zaman);
        currentUser("none");
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isWriting(false);
            Intent intent = new Intent(MessageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return false;
    }
}
