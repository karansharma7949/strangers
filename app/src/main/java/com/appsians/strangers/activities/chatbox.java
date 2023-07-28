package com.appsians.strangers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsians.strangers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class chatbox extends AppCompatActivity {


    EditText mgetmessage;
    Button msendmessagebutton;
    private String enteredmessage;
    Intent intent;
    String mrecievername, sendername , mrecieveruid, msenderuid;
    private FirebaseAuth firebaseAuth ;
    private FirebaseDatabase firebaseDatabase;
    String senderroom, recieverroom;

    RecyclerView mmessagerecyclerview;

    String currenttime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessageAdapter messageAdapter;
    ArrayList<Messages> messagesArrayList;



    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        mgetmessage= findViewById(R.id.getmessage);
        msendmessagebutton = findViewById(R.id.sendbuttonofchatbox);
        messagesArrayList = new ArrayList<>();
        mmessagerecyclerview =findViewById(R.id.recycleviewofchatbox);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mmessagerecyclerview.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(chatbox.this, messagesArrayList);
        mmessagerecyclerview.setAdapter(messageAdapter);

        intent= getIntent();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        msenderuid = firebaseAuth.getUid();
        mrecieveruid = getIntent().getStringExtra("receiveruid");
        mrecievername = getIntent().getStringExtra("username");
        senderroom = msenderuid + mrecieveruid;
        recieverroom = mrecieveruid + msenderuid;


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(senderroom).child("messages");
        messageAdapter = new MessageAdapter(chatbox.this , messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });







        msendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredmessage = mgetmessage.getText().toString();
                if (enteredmessage.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter massage", Toast.LENGTH_SHORT).show();
                }else{
                    Date date = new Date();
                    currenttime = simpleDateFormat.format(calendar.getTime());
                    Messages messages = new Messages(enteredmessage, firebaseAuth.getUid(), date.getTime(), currenttime );
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats").child(senderroom).child("messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference().child("chats").child(recieverroom).child("messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });
                    mgetmessage.setText(null);
                }
            }
        });




    }




}
