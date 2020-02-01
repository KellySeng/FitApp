package com.projet.fitapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joooonho.SelectableRoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseProfileReference;
    private String id;
    private TextView textNickname;
    private TextView textAge;
    private TextView textGender;
    private TextView textHeight;
    private TextView textWeight;
    private TextView textActivity;
    private ImageView photo;
    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_GALLERY = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView txEmail = getActivity().findViewById(R.id.nav_email);
        id = Integer.toString(txEmail.getText().toString().hashCode());
        mDatabaseProfileReference = mDatabaseReference.child("profiles").child(id);

        ImageView buttonPhoto = v.findViewById(R.id.button_photo);
        ImageView buttonNickname = v.findViewById(R.id.button_nickname);
        ImageView buttonAge = v.findViewById(R.id.button_age);
        ImageView buttonGender = v.findViewById(R.id.button_gender);
        ImageView buttonHeight = v.findViewById(R.id.button_height);
        ImageView buttonWeight = v.findViewById(R.id.button_weight);
        ImageView buttonActivity = v.findViewById(R.id.button_activity);


        photo = v.findViewById(R.id.photo);
        textNickname = v.findViewById(R.id.edit_nickname);
        textAge = v.findViewById(R.id.edit_age);
        textGender = v.findViewById(R.id.edit_gender);
        textHeight = v.findViewById(R.id.edit_height);
        textWeight = v.findViewById(R.id.edit_weight);
        textActivity = v.findViewById(R.id.edit_activity);

        mDatabaseProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String nickname = dataSnapshot.child("nickname").getValue().toString();
                    if (!nickname.isEmpty()) textNickname.setText(nickname);

                    String age = dataSnapshot.child("age").getValue().toString();
                    if (!age.isEmpty()) textAge.setText(age);

                    String gender = dataSnapshot.child("gender").getValue().toString();
                    if (!gender.isEmpty()) textGender.setText(gender);

                    String height = dataSnapshot.child("height").getValue().toString();
                    if (!height.isEmpty()) textHeight.setText(height);

                    String weight = dataSnapshot.child("weight").getValue().toString();
                    if (!weight.isEmpty()) textWeight.setText(weight);

                    String activity = dataSnapshot.child("activityLevel").getValue().toString();
                    if (!activity.isEmpty()) textActivity.setText(activity);

                    String imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
                    if (!imageUrl.isEmpty() && !imageUrl.contains("http")) {
                        try {
                            Bitmap imageBitmap = decodeFromFirebaseBase64(imageUrl);
                            photo.setImageBitmap(imageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //nothing
            }
        });


        buttonNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditNickname(v);
            }
        });

        buttonAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditAge(v);
            }
        });

        buttonGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditGender(v);
            }
        });

        buttonHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditHeight(v);
            }
        });

        buttonWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditWeight(v);
            }
        });

        buttonActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditActivity(v);
            }
        });

        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditPhoto(v);
            }
        });


        return v;
    }


    private void onClickEditNickname(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_nickname, null);
        mBuilder.setView(dialogView);
        mBuilder.setMessage(R.string.profile_edit_nickname).setTitle(R.string.profile_edit_title);

        final EditText edit = dialogView.findViewById(R.id.edit1);
        mBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newNickName = edit.getText().toString();

                if (!newNickName.isEmpty()) {
                    mDatabaseProfileReference.child("nickname").setValue(newNickName);
                    textNickname.setText(newNickName);
                }
            }
        });

        mBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }


    private void onClickEditAge(View v) {
        DialogFragment newFragment = new DatePickerFragment(textAge, mDatabaseProfileReference);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Class used implementing a date picker and calculate the age
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        final Calendar c = Calendar.getInstance();
        TextView tx;
        DatabaseReference mDatabaseProfileReference;

        public DatePickerFragment(TextView tx, DatabaseReference mDatabaseProfileReference) {
            this.tx = tx;
            this.mDatabaseProfileReference = mDatabaseProfileReference;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                    year, month, day);

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar birthDate = Calendar.getInstance();
            birthDate.set(year, month, day);
            int todayYear = c.get(Calendar.YEAR);
            int birthDateYear = birthDate.get(Calendar.YEAR);
            int todayDayOfYear = c.get(Calendar.DAY_OF_YEAR);
            int birthDateDayOfYear = birthDate.get(Calendar.DAY_OF_YEAR);
            int todayMonth = c.get(Calendar.MONTH);
            int birthDateMonth = birthDate.get(Calendar.MONTH);
            int todayDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            int birthDateDayOfMonth = birthDate.get(Calendar.DAY_OF_MONTH);
            int age = todayYear - birthDateYear;

            if ((birthDateDayOfYear - todayDayOfYear > 3) || (birthDateMonth > todayMonth)) {
                age--;

            } else if ((birthDateMonth == todayMonth) && (birthDateDayOfMonth > todayDayOfMonth)) {
                age--;
            }

            String res = Integer.toString(age);

            this.mDatabaseProfileReference.child("age").setValue(res);
            this.tx.setText(res);


        }
    }

    public void onClickEditGender(View v) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(R.string.profile_edit_title);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Male");
        arrayAdapter.add("Female");


        builderSingle.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String gender = arrayAdapter.getItem(which);
                mDatabaseProfileReference.child("gender").setValue(gender);
                textGender.setText(gender);

            }
        });
        builderSingle.show();
    }

    private void onClickEditHeight(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_weight_height, null);
        mBuilder.setView(dialogView);
        mBuilder.setMessage(R.string.profile_edit_height).setTitle(R.string.profile_edit_title);

        final EditText edit = dialogView.findViewById(R.id.edit);
        mBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String height = edit.getText().toString();

                if (!height.isEmpty()) {
                    mDatabaseProfileReference.child("height").setValue(height);
                    textHeight.setText(height);
                }
            }
        });

        mBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }


    private void onClickEditWeight(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_weight_height, null);
        mBuilder.setView(dialogView);
        mBuilder.setMessage(R.string.profile_edit_weight).setTitle(R.string.profile_edit_title);

        final EditText edit = dialogView.findViewById(R.id.edit);
        mBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String weight = edit.getText().toString();

                if (!weight.isEmpty()) {
                    mDatabaseProfileReference.child("weight").setValue(weight);
                    textWeight.setText(weight);
                }
            }
        });

        mBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }


    public void onClickEditActivity(View v) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(R.string.profile_edit_title);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Sedentary");
        arrayAdapter.add("Moderately active");
        arrayAdapter.add("Vigorously active");
        arrayAdapter.add("Extremely active");


        builderSingle.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String activity = arrayAdapter.getItem(which);
                mDatabaseProfileReference.child("activity").setValue(activity);
                textActivity.setText(activity);

            }
        });
        builderSingle.show();
    }

    public void onClickEditPhoto(View v) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose or take a picture");
        // add a list
        String[] actions = {"Camera", "Image Gallery"};
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                        }
                        break;
                    case 1:
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
                        break;
                }
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photo.setImageBitmap(imageBitmap);
            encodeBitmapAndSaveToFirebase(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == getActivity().RESULT_OK) {
            Uri selectedImageURI = data.getData();
            photo.setImageURI(selectedImageURI);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                        selectedImageURI);
                encodeBitmapAndSaveToFirebase(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        mDatabaseProfileReference.child("imageUrl").setValue(imageEncoded);
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
