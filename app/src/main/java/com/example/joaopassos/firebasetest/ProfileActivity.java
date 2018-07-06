package com.example.joaopassos.firebasetest;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mDeclinebtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclinebtn = (Button) findViewById(R.id.profile_decline_btn);

        mDeclinebtn.setVisibility(View.INVISIBLE);
        mDeclinebtn.setEnabled(true);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Carregando dados do usuário");
        mProgressDialog.setMessage("Por favor aguarde enquanto carregamos os dados do usuário");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                getSupportActionBar().setTitle(display_name);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultuser).into(mProfileImage);

                //-------------- LISTA AMIGOS / SOLICITACOES

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Aceitar Solicitação");

                                mDeclinebtn.setVisibility(View.VISIBLE);
                                mDeclinebtn.setEnabled(true);



                            } else if(req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancelar Solicitação");

                                mDeclinebtn.setVisibility(View.INVISIBLE);
                                mDeclinebtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Deixar de ser amigo");

                                        mDeclinebtn.setVisibility(View.INVISIBLE);
                                        mDeclinebtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //----------------------ENVIAR SOLICITACAO------------

                if(mCurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrent_user.getUid());
                                        notificationData.put("type", "request");

                                        //mProfileSendReqBtn.setEnabled(true);
                                        mCurrent_state = "req_sent";
                                        mProfileSendReqBtn.setText("Cancelar Solicitação");

                                        mDeclinebtn.setVisibility(View.INVISIBLE);
                                        mDeclinebtn.setEnabled(false);

                                        Toast.makeText(ProfileActivity.this, "Solicitação enviada com sucesso.", Toast.LENGTH_SHORT).show();

                                        //NOTIFICATIONS

                                        /*mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    //mProfileSendReqBtn.setEnabled(true);
                                                    mCurrent_state = "req_sent";
                                                    mProfileSendReqBtn.setText("Cancelar Solicitação");

                                                    mDeclinebtn.setVisibility(View.INVISIBLE);
                                                    mDeclinebtn.setEnabled(false);

                                                    Toast.makeText(ProfileActivity.this, "Solicitação enviada com sucesso.", Toast.LENGTH_SHORT).show();
                                                }else{

                                                    Toast.makeText(ProfileActivity.this, "Algo deu errado", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });*/

                                    }
                                });


                            } else {

                                Toast.makeText(ProfileActivity.this, "Não foi possível enviar solicitação.", Toast.LENGTH_LONG).show();
                            }

                            //dentro do if ou aqui
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });



                }
                //----------------------CANCELAR SOLICITACAO------------

                if (mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Enviar Solicitação");

                                    mDeclinebtn.setVisibility(View.INVISIBLE);
                                    mDeclinebtn.setEnabled(false);

                                }
                            });
                        }
                    });

                }

                //------------------DEIXAR DE SER AMIGO--------------

                if (mCurrent_state.equals("friends")){

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Enviar Solicitação");

                                    mDeclinebtn.setVisibility(View.INVISIBLE);
                                    mDeclinebtn.setEnabled(false);

                                }
                            });
                        }
                    });

                }


                //------------------SOLICIATACAO RECEBIDA----------

                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    mCurrent_state = "friends";
                                                                    mProfileSendReqBtn.setText("Deixar de ser amigo");
                                                                    mDeclinebtn.setVisibility(View.INVISIBLE);
                                                                    mDeclinebtn.setEnabled(false);

                                                                }
                                                            });
                                                        }
                                                    });


                                                }
                                            });
                                }
                            });
                }
            }
        });

        //-------------REJEITAR SOLICITACAO----------------

        mDeclinebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Enviar Solicitação");

                                mDeclinebtn.setVisibility(View.INVISIBLE);
                                mDeclinebtn.setEnabled(false);

                            }
                        });
                    }
                });



            }
        });
               /* mDeclinebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Map declineMap = new HashMap();

                        declineMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                        declineMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                        mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError == null)
                                {

                                    mCurrent_state = "not friends";
                                    mProfileSendReqBtn.setText("Enviar Solicitação");

                                    //mDiclineBnt.setVisibility(View.INVISIBLE);
                                    //mDiclineBnt.setEnabled(false);
                                }else{
                                    String error = databaseError.getMessage();
                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                                }

                                mProfileSendReqBtn.setEnabled(true);
                            }
                        });

                    }
                });*/

    }
}
