package com.projet.fitapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseProfileReference;
    String id;
    List<String> infos;
    ArrayAdapter<String> adapter;
    private TextView bilan;
    public double bmi;
    public double fat;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        View myView =  inflater.inflate(R.layout.fragment_stats,container,false);
        TextView txEmail = getActivity().findViewById(R.id.nav_email);
        id = Integer.toString(txEmail.getText().toString().hashCode());
        mDatabaseProfileReference = mDatabaseReference.child("profiles").child(id);

        infos = new ArrayList<>();
        infos.add("BMI : ");
        infos.add("Lean Body Mass : ");
        infos.add("Ideal Calorie Intake :");
        infos.add("Ideal Body Weight : ");


        getData();

        ListView listView = myView.findViewById(R.id.list);
        bilan = myView.findViewById(R.id.bilan);

        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,infos);
        listView.setAdapter(adapter);
        return myView;
    }

    public void getData(){

        mDatabaseProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    double h = 0;
                    String height = dataSnapshot.child("height").getValue().toString();
                    if (!height.isEmpty()){
                        h = Double.parseDouble(height);
                    }

                    double w = 0;
                    String weight = dataSnapshot.child("weight").getValue().toString();
                    if (!weight.isEmpty()) w = Double.parseDouble(weight);

                    double a= 0;
                    String age = dataSnapshot.child("age").getValue().toString();
                    if (!age.isEmpty()) a = Double.parseDouble(age);

                    //BMI
                    double bmi =  Math.ceil(w/((h*0.01)*(h*0.01)));
                    infos.set(0,infos.get(0) +" "+ bmi);

                    //LEAN BODY MASS AND CALORIES
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    if (!gender.isEmpty()){
                        if (gender.equals("Male")){
                            double lean =  Math.ceil((0.32810*w)+(0.33929*h)-29.5336);
                            infos.set(1,infos.get(1) +" "+lean );

                            double cal = Math.ceil(13.397*w+4.799*h-5.677*a+88.362);
                            infos.set(2,infos.get(2) +" "+cal + " kcal");

                            double ideal = h-100-((h-150)/4);
                            infos.set(3,infos.get(3) +" "+ideal + " kg");
                        }
                        else{
                            double lean=  Math.ceil((0.29569*w)+(0.41813*h)-43.2933);
                            infos.set(1,infos.get(1) +" "+lean );

                            double cal= Math.ceil(  9.248*w+3.098*h-4.330*a+447.593);
                            infos.set(2,infos.get(2) +" "+cal+ " kcal");

                            double ideal = h-100-((h-150)/2.5);
                            infos.set(3,infos.get(3) +" "+ideal + " kg");
                        }
                    }

                    String s = "";

                    if(bmi <18.5) s = "You are \nunderweight";
                    else if(bmi <=29.9 && bmi>=25) s = "You are \noverweight";
                    else if (bmi >= 30) s  = "You are \nobese";
                    else s = "Congratulations,\n you are perfectly\n normal";

                    bilan.setText(s);

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //nothing
            }
        });
    }


}
