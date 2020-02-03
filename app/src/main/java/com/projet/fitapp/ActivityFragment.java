package com.projet.fitapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


public class ActivityFragment extends Fragment {

    private static String g;

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private TextView stepTextView;
    private TextView totalView;
    private TextView goalView;
    private Button setGoalButton;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseGoalReference;
    String id;
    String goalStep;
    private long dailyStep;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        TextView txEmail = getActivity().findViewById(R.id.nav_email);
        id = Integer.toString(txEmail.getText().toString().hashCode());
        mDatabaseGoalReference = mDatabaseReference.child("goals").child(id);
        View myView = inflater.inflate(R.layout.fragment_activity, container, false);


        stepTextView = myView.findViewById(R.id.step_count);
        totalView = myView.findViewById(R.id.total);
        goalView = myView.findViewById(R.id.goal);
        setGoalButton = myView.findViewById(R.id.setGoal);

        if(mDatabaseGoalReference!= null) {
            mDatabaseGoalReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        goalStep = dataSnapshot.child("goalStep").getValue().toString();
                        if (!goalStep.isEmpty()) {
                            goalView.setText("Current goal : " +
                                    goalStep + " steps");
                            g = goalStep;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //nothing
                }
            });
        }

        setGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal(v);
            }
        });


        checkAuthorization(createFitnessOptions());
        readData();


        return myView;
    }

    public FitnessOptions createFitnessOptions(){
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        return fitnessOptions;
    }


    public void checkAuthorization(FitnessOptions fitnessOptions){
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        } else {
            subscribe();
        }
    }

    public void subscribe(){
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Read the number of steps in a day
     */
    private void readData() {
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
                                stepTextView.setText("Daily steps");
                                dailyStep = total;
                                totalView.setText(Long.toString(dailyStep)+" / "+ g);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }


    public void setGoal(View v){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_edit_weight_height, null);
            mBuilder.setView(dialogView);
            mBuilder.setMessage("Enter a new goal").setTitle("Editing goal");

            final EditText edit = dialogView.findViewById(R.id.edit);
            mBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String goalStep = edit.getText().toString();

                    if (!goalStep.isEmpty()) {
                        mDatabaseGoalReference.child("goalStep").setValue(goalStep);
                        goalView.setText("Current goal : "+
                                goalStep+ " steps");
                        String s = totalView.getText().toString();
                        String [] arr = s.split("/");
                        totalView.setText(arr[0]+" / "+goalStep);
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


}
