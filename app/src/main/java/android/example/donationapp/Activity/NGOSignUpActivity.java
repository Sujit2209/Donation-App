package android.example.donationapp.Activity;

import android.app.Activity;
import android.content.Intent;
import android.example.donationapp.Model.UserClass;
import android.example.donationapp.R;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class NGOSignUpActivity extends AppCompatActivity {

    TextInputLayout nameBox, descriptionBox, addressBox, contactBox, emailBox, passwordBox;
    TextInputEditText ngoName, ngoDescription, ngoAddress, ngoContact, ngoEmail, ngoPassowrd;
    Button submit;
    ImageView ngoPicadd, ngoProfilePic;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    DocumentReference documentReference;
    String imagePath;
    FirebaseUser currentUser;
    UserClass userInfo;
    String sName, sDescription, sAddress, sContact, sEmail, sPassword;
    String uID, sDesignation = "NGO", sImageURL = "null";

    Uri selectedImage;
    Bitmap bitmap;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        selectedImage = Objects.requireNonNull(data.getData());
                        bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImage);
                            ngoProfilePic.setImageBitmap(bitmap);
                            ngoPicadd.setVisibility(View.GONE);

                        } catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_signup);

        nameBox = findViewById(R.id.ngo_signup_namebox);
        descriptionBox = findViewById(R.id.ngo_signup_descriptionbox);
        addressBox = findViewById(R.id.ngo_signup_addressbox);
        contactBox = findViewById(R.id.ngo_signup_contactbox);
        emailBox = findViewById(R.id.ngo_signup_emailbox);
        passwordBox = findViewById(R.id.ngo_signup_passwordbox);

        ngoName = findViewById(R.id.ngo_signup_nameinput);
        ngoDescription = findViewById(R.id.ngo_signup_descriptioninput);
        ngoAddress = findViewById(R.id.ngo_signup_addressinput);
        ngoContact = findViewById(R.id.ngo_signup_contactinput);
        ngoEmail = findViewById(R.id.ngo_signup_emailinput);
        ngoPassowrd = findViewById(R.id.ngo_signup_passwordinput);

        submit = findViewById(R.id.ngo_signup_submit_button);
        ngoPicadd = findViewById(R.id.ngo_profile_pic_add);
        ngoProfilePic = findViewById(R.id.ngo_profile_pic);

        firebaseAuth = FirebaseAuth.getInstance();


        ngoPicadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                someActivityResultLauncher.launch(intent);

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sName = ngoName.getText().toString();
                sDescription = ngoDescription.getText().toString();
                sAddress = ngoAddress.getText().toString();
                sContact = ngoContact.getText().toString();
                sEmail = ngoEmail.getText().toString().trim();
                sPassword = ngoPassowrd.getText().toString().trim();

                if(sName.isEmpty())
                {
                    ngoName.setError("Enter NGO Name");
                }
                if(sDescription.isEmpty())
                {
                    ngoDescription.setError("Enter Description");
                }
                if(sAddress.isEmpty())
                {
                    ngoAddress.setError("Enter Address");
                }
                if(sContact.isEmpty())
                {
                    ngoContact.setError("Enter Contact");
                }
                if(sEmail.isEmpty())
                {
                    ngoEmail.setError("Enter Email");
                }
                if(sPassword.isEmpty())
                {
                    ngoPassowrd.setError("Enter Password");
                }

                firebaseAuth.createUserWithEmailAndPassword(sEmail, sPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert currentUser != null;
                        uID = currentUser.getUid();

                        userInfo = new UserClass(sName, sDescription, sAddress, sContact, sEmail, sDesignation, uID, sImageURL);

                        firebaseFirestore.collection("UserInformation").document(currentUser.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(NGOSignUpActivity.this, "Info Added to Firebase", Toast.LENGTH_SHORT).show();

                                if(selectedImage != null)
                                {
                                    storageReference = firebaseStorage.getReference().child("images").child("ngo").child(currentUser.getUid());
                                    documentReference = FirebaseFirestore.getInstance().collection("UserInformation").document(currentUser.getUid());
                                    storageReference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    imagePath = uri.toString();

                                                    documentReference.update("imageURL", imagePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(NGOSignUpActivity.this, "URL Saved", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(NGOSignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(NGOSignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(NGOSignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NGOSignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NGOSignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

                Intent nRegister = new Intent(NGOSignUpActivity.this, NGOHomeActivity.class);
                startActivity(nRegister);

            }
        });

    }
}
