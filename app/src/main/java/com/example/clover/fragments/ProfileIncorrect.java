package com.example.clover.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clover.R;
import com.example.clover.popups.ProfileProgress;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Locale;

public class ProfileIncorrect extends Fragment implements GameAdapter.OnItemClickListener {

    public static int NUMBER;

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);
    private DocumentReference progressRef = fStore.collection("users").document(userId);

    private TextToSpeech mTTS;
    private int age, pitch, speed;

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View view;

    private int code;
    private ArrayList<GameItem> incorrectWords = new ArrayList<GameItem>();

    public ProfileIncorrect() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_incorrect, container, false);

        code = ProfileProgress.CODE;

        // declare if text to speech is being used
        mTTS = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        readProgress(new ProfileIncorrect.ProgressCallback() {
            @Override
            public void onCallback(ArrayList<GameItem> spellingList) { //switches to correct spelling words
                buildRecyclerView(spellingList);
            }
        });

        return view;
    }

    @Override
    public void onItemClick(int position) {
        final String currentWord = incorrectWords.get(position).getItemWord();
        readData(new ProfileIncorrect.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                SettingsPreferences.speak(mTTS, currentWord, pitch, speed);
            }
        });
    }

    public void buildRecyclerView(ArrayList<GameItem> savedList) {
        mRecyclerView = view.findViewById(R.id.incorrectRecycler);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new GameAdapter(savedList); //passes to adapter, then presents to viewholder
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    //get list for the incorrect words of indicated game
    public void readProgress(final ProfileIncorrect.ProgressCallback vCallback){
        progressRef = fStore.collection("users")
                .document(userId);

        int icon = R.drawable.cross;
        String path = "";
        if(code==1){
            path = "spellingprogress";
        } else if (code == 0){
            path = "voiceprogress";
        }

        incorrectWords = new ArrayList<GameItem>();
        progressRef.collection(path)
                .whereEqualTo("itemIcon", icon)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("correct words", document.getId() + " => " + document.getData());
                                GameItem correct = document.toObject(GameItem.class);
                                incorrectWords.add(correct);
                            }
                            if(incorrectWords!=null) {
                                Log.d("Load correct words", "Success");
                                vCallback.onCallback(incorrectWords);
                            }
                        } else {
                            Log.d("Load correct words", "Error getting documents: ", task.getException());
                        }
                    }
                });
        NUMBER = incorrectWords.size();
    }

    public interface ProgressCallback {
        void onCallback(ArrayList<GameItem> progressList);
    }

    //get the right age, pitch, and speed from firebase
    private void readData(final ProfileIncorrect.FirebaseCallback f){
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    age = Integer.parseInt(documentSnapshot.getString("age"));
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    f.onCallback(age, pitch, speed);
                }
            }
        });
    }

    //allows access of variable age, pitch, and speed outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }

    @Override //for the speaker function
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }
}
