package com.example.quiz_app_java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionsActivity extends AppCompatActivity {
     public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();


    private  TextView question;
    private TextView noIndicator;
    private LinearLayout optionsContainer;

    private Button nextBtn;
    private  int count =0;
    private List<QuestionModel> list;
    private int position =0;
    private int score = 0;
    private Dialog loadingDialog;


    private List<QuestionModel> bookmarksList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private  int matchedQuestionPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        question = findViewById(R.id.question);
        noIndicator = findViewById(R.id.no_indicator);
        FloatingActionButton bookmarkBtn = findViewById(R.id.bookmark_btn);
        optionsContainer = findViewById(R.id.options_container);
        Button shareBtn = findViewById(R.id.share_btn);
        nextBtn = findViewById(R.id.next_btn);

        preferences= getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

       getBookmarks();
        bookmarkBtn.setOnClickListener(v -> {
             if(modelMatch()){
                 bookmarksList.remove(matchedQuestionPosition);
                 bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
             }else{
                 bookmarksList.add(list.get(position));
                 bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
             }
        });

        String category = getIntent().getStringExtra("category");
        int setNo = getIntent().getIntExtra("setNo", 1);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);


        list = new ArrayList<>();

        loadingDialog.show();
        myRef.child("SETS").child(category).child("questions").orderByChild("setNo").equalTo(setNo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    list.add(snapshot1.getValue(QuestionModel.class));
                }
                if(list.size() > 0){

                    for(int i=0; i<4; i++) {
                        optionsContainer.getChildAt(i).setOnClickListener(v -> checkAnswer((Button)v));
                    }

                    playAnim(question,0,list.get(position).getQuestion());
                    nextBtn.setOnClickListener(v -> {
                        nextBtn.setEnabled(false);
                        nextBtn.setAlpha(0.7f);
                        enableOption(true);
                        position++;
                        if(position==list.size()){
                            Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                            scoreIntent.putExtra("score",score);
                            scoreIntent.putExtra("total",list.size());
                            startActivity(scoreIntent);
                            finish();
                            return;
                        }
                        count =0;
                        playAnim(question, 0, list.get(position).getQuestion());
                    });

                    shareBtn.setOnClickListener(v -> {
                        String body = list.get(position).getQuestion() + "\n" +
                                list.get(position).getOptionA() + "\n" +
                                list.get(position).getOptionB() + "\n" +
                                list.get(position).getOptionC() + "\n" +
                                list.get(position).getOptionD();

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("plain/text");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer challenge");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,body);
                        startActivity(Intent.createChooser(shareIntent,"Share via"));
                    });

                }else{
                    finish();
                    Toast.makeText(QuestionsActivity.this, "no questions", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnim(View view, int value, String data){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {


                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {
                        if (value == 0 && count < 4) {
                            String option = "";
                            if (count == 0) {
                                option = list.get(position).getOptionA();
                            } else if (count == 1) {
                                option = list.get(position).getOptionB();
                            } else if (count == 2) {
                                option = list.get(position).getOptionC();
                            } else if (count == 3) {
                                option = list.get(position).getOptionD();
                            }
                            playAnim(optionsContainer.getChildAt(count), 0, option);
                            count++;
                        }
                    }

                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        if (value == 0) {
                            try {
                                ((TextView) view).setText(data);
                                noIndicator.setText((position + 1) + "/" + list.size());

                                ImageSwitcher bookmarkBtn =null;
                                if (bookmarkBtn != null) {
                                    if (modelMatch()) {
                                        bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
                                    } else {
                                        bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                                    }
                                }
                            }catch (ClassCastException ex){
                                ((Button) view).setText(data);
                            }
                            view.setTag(data);
                            playAnim(view, 1, data);
                        }
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {

                    }
                });

    }
    private void checkAnswer(Button selectedOption){
        enableOption(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1);
        if(selectedOption.getText().toString().equals(list.get(position).getCorrectANS())){
            //Correct
            score++;
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

        }
        else {
            //inCorrect
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            Button correctoption= optionsContainer.findViewWithTag(list.get(position).getCorrectANS());
            correctoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }

    private void enableOption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            optionsContainer.getChildAt(i).setEnabled(enable);
            if(enable){
                optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
            }
        }
    }

    private  void getBookmarks(){
        String json = preferences.getString(KEY_NAME,"");
        Type type = new TypeToken<List<QuestionModel>>(){}.getType();
        bookmarksList = gson.fromJson(json,type);

        if(bookmarksList == null){
            bookmarksList = new ArrayList<>();
        }
    }

    private boolean modelMatch(){
        boolean matched = false;
        int i=0;
        for(QuestionModel model : bookmarksList ){

            if(model.getQuestion().equals(list.get(position).getQuestion())
            && model.getCorrectANS().equals(list.get(position).getCorrectANS())
            && model.getSetNo() == list.get(position).getSetNo()){
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return  matched;
    }

    private void storeBookmarks(){
        String json = gson.toJson(bookmarksList);
        editor.putString(KEY_NAME, json);
        editor.commit();
    }

}