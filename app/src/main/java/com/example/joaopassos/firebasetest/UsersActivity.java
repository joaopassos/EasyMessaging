package com.example.joaopassos.firebasetest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Usuários");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                        Users.class,
                        R.layout.users_single_layout,
                        UsersViewHolder.class,
                        mUsersDatabase

                ){

                    @Override
                    protected void populateViewHolder(UsersViewHolder usersViewHolder, Users model, int position) {

                        usersViewHolder.setDisplayName(model.getName());
                        usersViewHolder.setUserStatus(model.getStatus());
                        usersViewHolder.setUserImage(model.getThumb_image(), getApplicationContext());
                        usersViewHolder.setUserReq();

                        final String user_id = getRef(position).getKey();

                        usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);

                            }
                        });

                    }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDisplayName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserStatus(String status){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context cxt){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(cxt).load(thumb_image).placeholder(R.drawable.defaultuser).into(userImageView);


        }

        public void setUserReq(){

            TextView userReq = (TextView) mView.findViewById(R.id.user_single_req);
            userReq.setTextColor(Color.BLACK);

        }

    }
}
