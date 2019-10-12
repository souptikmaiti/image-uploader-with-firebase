package com.example.firebaseimageuploader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private Button btnFile, btnUpload;
    private EditText etFile;
    private ImageView imageView;
    private TextView tvShow;
    private ProgressBar progressBar;

    public static final int IMAGE_FILE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    //private FirebaseAuth mAuth;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFile = findViewById(R.id.btn_file);
        btnUpload = findViewById(R.id.btn_upload);
        etFile = findViewById(R.id.et_file);
        tvShow = findViewById(R.id.tv_show);
        imageView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);

        /*mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }*/
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");


        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhotoFromFileLocation();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "Upload is in progress", Toast.LENGTH_LONG).show();
                }else{
                    uploadImageToFirebase();
                }
            }
        });

        tvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUploadedImages();
            }
        });
    }

    /*private void signInAnonymously(){
        mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d(TAG, "onSuccess: Authenticated anonymously");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Authentication Failed");
            }
        });
    }*/

    private void getPhotoFromFileLocation(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_FILE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageView);
        }
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadImageToFirebase(){

        //Testing
        String keyId = databaseReference.push().getKey();
        FirebaseDatabase.getInstance().getReference("testing").child(keyId).setValue("Hello");

        if(imageUri!=null){
            StorageReference fileUploadRef = storageReference.child(System.currentTimeMillis() + "."+getExtension(imageUri));

            uploadTask = fileUploadRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                            //save the metadata of the uploaded image is stored in the database with name and download url

                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String photoLink = uri.toString();
                                    String key = databaseReference.push().getKey();
                                    Upload upload = new Upload(etFile.getText().toString().trim(),photoLink);
                                    databaseReference.child(key).setValue(upload);

                                }
                            });

                            progressBar.setProgress(0);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double pr = taskSnapshot.getBytesTransferred()*100/taskSnapshot.getTotalByteCount();
                            progressBar.setProgress((int)pr);
                        }
                    });

        }else{
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUploadedImages(){
        Intent intent = new Intent(this,ImageActivity.class);
        startActivity(intent);
    }
}
