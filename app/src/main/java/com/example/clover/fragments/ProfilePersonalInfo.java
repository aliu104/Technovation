package com.example.clover.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.activities.Profile;
import com.example.clover.activities.ProfileNameDialog;
import com.example.clover.adapters.PersonalInfoAdapter;
import com.example.clover.pojo.PersonalInfoItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class ProfilePersonalInfo extends Fragment implements PersonalInfoAdapter.OnItemClickListener, ProfileNameDialog.profileInput {
    View view;

    String userId;
    TextView fullname, age;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference progressRef;

    private RecyclerView mRecyclerView;
    private PersonalInfoAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<PersonalInfoItem> savedList = new ArrayList<PersonalInfoItem>();
    private PersonalInfoItem currentItem;

    public ProfilePersonalInfo() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personal_info, container, false);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                savedList.add(0, new PersonalInfoItem("Name", documentSnapshot.getString("name"), R.drawable.name)); //TODO fix bug with logout that occurs here for some reason
                savedList.add(1, new PersonalInfoItem("Age", documentSnapshot.getString("age"), R.drawable.age));
                savedList.add(2, new PersonalInfoItem("Email", documentSnapshot.getString("email"), R.drawable.email));
                buildRecyclerView(savedList);
            }
        });

        return view;
    }

    public void buildRecyclerView(ArrayList<PersonalInfoItem> savedList) {
        mRecyclerView = view.findViewById(R.id.personalInfoRecycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new PersonalInfoAdapter(savedList); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ProfilePersonalInfo.this);
    }

    @Override
    public void onEditClick(PersonalInfoItem item, int position) {
        // Allows you to edit name when edit button is clicked
        currentItem = item;
        ProfileNameDialog profileDialog = new ProfileNameDialog(item.getItemTitle());
        profileDialog.setTargetFragment(ProfilePersonalInfo.this, 1);
        Log.e("edit click", "Button has been clicked: opening dialog");
        profileDialog.show(getFragmentManager(), "profile dialog"); //I can't use getSupportFragmentManager in a fragment, might need to change
    }

    public void applyText(String newField){
        currentItem.setItemText(newField);
    }
}