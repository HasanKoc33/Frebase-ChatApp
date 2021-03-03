package com.hasan.firebase_chatapp.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import static java.lang.System.currentTimeMillis;

public class Foto_Viev extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    FirebaseUser fuser;
    ProgressBar load;
    private Uri imageUri;
    ImageButton indir;
    ImageView resim , update;
    String uri;
    String username;
    DatabaseReference reference;
    private static final int IMAGE_REQUEST = 1;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    StorageReference storageReference;

    Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto__viev);
        init();

        uri=getIntent().getStringExtra(getString(R.string.foto_viev));
        String Uid= getIntent().getStringExtra(getString(R.string.foto_name));
        isim(Uid);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (uri.equals("default")){

            resim.setImageResource(R.drawable.user);
        }else if (!uri.isEmpty()){
            Glide.with(context).load(uri).into(resim);
        }
        else {
            Toast.makeText(getApplicationContext(), "bir hata oldu !!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Foto_Viev.this, MessageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        if (fuser.getUid().equals(Uid)){
            indir.setVisibility(View.GONE);
            update.setVisibility(View.VISIBLE);
        }

        indir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               Foto_indir(username);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile(fuser.getUid());
             /*   CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Foto_Viev.this);*/
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTask.cancel();
                Glide.with(context).load(uri).into(resim);
                load.setVisibility(View.GONE);
                resim.setVisibility(View.VISIBLE);
            }
        });

    }

    private void init(){
        indir = findViewById(R.id.indir);
        resim = findViewById(R.id.resim);
        update = findViewById(R.id.duzen);
        load = findViewById(R.id.bekle);
        Toolbar toolbar = findViewById(R.id.toolbarF);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void Foto_indir(String name){
        try {
            File root = Environment.getExternalStorageDirectory();
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "/Selefkos/");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File folder1 = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "/Selefkos/" + name);

            if (!folder1.exists()) {
                folder1.mkdir();
            }

            File cachePath = new File(root.getAbsolutePath() + "/Selefkos/" + name+"/" + currentTimeMillis() + ".jpg");
            try {
                allahin_izni_ile(this);
                resim.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) resim.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                cachePath.createNewFile();
                FileOutputStream ostream = new FileOutputStream(cachePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();
                Toast.makeText(getApplicationContext(), "kaydedildi", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){

        }

    }


    public static void allahin_izni_ile(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void isim(String id){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                username =user.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private Uri compres(Bitmap file){

        int nh = (int) ( file.getHeight() * (1024.0 / file.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(file, 1024, nh, true);
        Bitmap bitmap = scaled;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), bitmap, "IMG_" + Calendar.getInstance().getTime(), null);
        Uri uri = Uri.parse(path);
        return uri;
    }

    private void uploadImage(){
        
        if (imageUri != null){
            load.setVisibility(View.VISIBLE);
            resim.setVisibility(View.GONE);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri uri = compres(bitmap);

            final StorageReference fileReference = storageReference.child(fuser.getUid()).child("profile").child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            profile(fuser.getUid());

            uploadTask = fileReference.putFile(uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){

                    load.setVisibility(View.GONE);
                    throw  task.getException();
                }

                return  fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", ""+mUri);
                    reference.updateChildren(map);
                    Glide.with(context).load(downloadUri).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            load.setVisibility(View.GONE);
                            resim.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(resim);


                } else {
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    load.setVisibility(View.GONE);

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void profile(String id){
        try {
            File root = Environment.getExternalStorageDirectory();
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "/Selefkos/");
            if (!folder.exists()) {
                folder.mkdir();
            }
            allahin_izni_ile(this);
            File cachePath = new File(root.getAbsolutePath() + "/Selefkos/" + id + ".jpg");
            try {

                resim.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) resim.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                cachePath.createNewFile();
                FileOutputStream ostream = new FileOutputStream(cachePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();
                Toast.makeText(getApplicationContext(), "kaydedildi", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){

        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(getApplicationContext(), "Upload in preogress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}