package android.example.donationapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.donationapp.Model.UserClass;
import android.example.donationapp.R;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class Splasscreen extends AppCompatActivity {

    SharedPreferences onBoardingScreen;
    FirebaseUser currentUser;
    private FirebaseFirestore db;
    String Designation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splasscreen);
        db = FirebaseFirestore.getInstance();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Thread t= new Thread(){
            public void run(){
                try{
                    sleep(2000);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{

                    onBoardingScreen = getSharedPreferences("onBoardingScreen", MODE_PRIVATE);
                    boolean isFirstTime = onBoardingScreen.getBoolean("firstTime", true);

                    if(isFirstTime){

                        SharedPreferences.Editor editor = onBoardingScreen.edit();
                        editor.putBoolean("firstTime", false);
                        editor.commit();

                        Intent i=new Intent(getApplicationContext(), OnBoarding.class);
                        startActivity(i);
                        finish();

                    }

                    else{
                        if(currentUser != null){
                            Task<DocumentSnapshot> userDetails = db.collection("UserInformation").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get();
                            userDetails.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    UserClass userClass = documentSnapshot.toObject(UserClass.class);
                                    if (!userClass.getDesignation().equals(null)) {
                                        Designation = userClass.getDesignation();

                                        if (Designation.equalsIgnoreCase("user")) {
                                            startActivity(new Intent(Splasscreen.this, UserFragmentContainer.class));
                                            finish();
                                        } else {
                                            startActivity(new Intent(Splasscreen.this, NGOHomeActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });

                        }
                        else{
                            startActivity( new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }

                    }

                }
            }

        };t.start();
    }
}