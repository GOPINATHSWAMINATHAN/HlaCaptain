package com.hlacab.hlacaptain;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mCarField;

    private Button mBack, mConfirm;

    private CircularImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mCar;
    private String mService;
    private String mProfileImageUrl;

    private Uri resultUri;

    private RadioGroup mRadioGroup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);


        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCarField = (EditText) findViewById(R.id.car);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "Nunito-Regular.ttf");
        mProfileImage = (CircularImageView) findViewById(R.id.profileImage);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        //mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mNameField.setTypeface(custom_font);
        mPhoneField.setTypeface(custom_font);
        mCarField.setTypeface(custom_font);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);

        getUserInfo();

        //Newly added methods
       // getCompleteCarDetails();


        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveUserInformation();
            }
        });

//        mBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//                return;
//            }
//        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                this.finish();
               // onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void getUserInfo() {
        /**
         * Name, phoneno,car,servicetype,profileimageurl
         */
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                       // mNameField.setText(mName);
                    }
                    if (map.get("phone") != null) {
                        mPhone = map.get("phone").toString();
                        //mPhoneField.setText(mPhone);
                    }
                    if (map.get("car") != null) {
                        mCar = map.get("car").toString();
                        //mCarField.setText(mCar);
                    }
                    if (map.get("service") != null) {
                        mService = map.get("service").toString();
//                        switch (mService) {
//                            case "Economy":
//                                mRadioGroup.check(R.id.UberX);
//                                break;
//                            case "Deluxe":
//                                mRadioGroup.check(R.id.UberBlack);
//                                break;
//                            case "Business":
//                                mRadioGroup.check(R.id.UberXl);
//                                break;
//                        }
                    }
                    if (map.get("profileImageUrl") != null) {
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                       // Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("FromMobilyDriver").child(userID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("username") != null)
                        mName = map.get("username").toString();
                    mNameField.setText(mName);
                    if (map.get("mobno") != null)
                        mPhone = map.get("mobno").toString();
                    mPhoneField.setText(mPhone);
                    if(map.get("drpic")!=null)
                        mProfileImageUrl=map.get("drpic").toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
getCompleteCarDetails();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


            void getCompleteCarDetails() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("FromMobilyDriver").child(userID).child("cardet");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("vhtype") != null)
                        mService = map.get("vhtype").toString();
                    Log.e("MSERVICE", "" + mService);
                    switch (mService) {
                        case "Economy":
                            mRadioGroup.check(R.id.UberX);
                            break;
                        case "Deluxe":
                            mRadioGroup.check(R.id.UberBlack);
                            break;
                        case "Business":
                            mRadioGroup.check(R.id.UberXl);
                            break;


                    }
                    if (map.get("carmodel") != null)
                        mCar = map.get("carmodel").toString();
                    mCarField.setText(mCar);
                    if(map.get("refid")!=null)

                    saveUserInformation();
                    //getCompleteCaptainDetails();
//                    Toast.makeText(getApplicationContext(), "" + map, Toast.LENGTH_LONG).show();
//                    Log.e("CAPTAIN DETAILS", "" + map);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


//    void getCompleteCaptainDetails() {
//        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("FromMobilyDriver").child(userID).child("cardet");
//        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if (map.get("username") != null)
//                        mName = map.get("username").toString();
//                    mNameField.setText(mName);
//                    if (map.get("mobno") != null)
//                        mPhone = map.get("mobno").toString();
//                    mPhoneField.setText(mPhone);
//                    if(map.get("drpic")!=null)
//                        mProfileImageUrl=map.get("drpic").toString();
//                    Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void saveUserInformation() {
//        mName = mNameField.getText().toString();
//        mPhone = mPhoneField.getText().toString();
//        mCar = mCarField.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText() == null) {
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("car", mCar);
        userInfo.put("service", mService);
        userInfo.put("profileImageUrl",mProfileImageUrl);
        mDriverDatabase.updateChildren(userInfo);

        if (resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", mProfileImageUrl);
                    mDriverDatabase.updateChildren(newImage);

                    return;
                }
            });
        } else {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}
