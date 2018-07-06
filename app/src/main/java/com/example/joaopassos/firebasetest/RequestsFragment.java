package com.example.joaopassos.firebasetest;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mReqList;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mReqList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendReqDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(

                Requests.class,
                R.layout.users_single_layout,
                RequestsViewHolder.class,
                mFriendReqDatabase

        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder requestsViewHolder, Requests requests, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        requestsViewHolder.setName(userName);
                        requestsViewHolder.setUserImage(userThumb, getContext());
                        requestsViewHolder.setStatus(userStatus);

                        requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);
                            }

                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mReqList.setAdapter(requestsRecyclerViewAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }

        public void setUserImage(String thumb_image, Context cxt){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(cxt).load(thumb_image).placeholder(R.drawable.defaultuser).into(userImageView);

        }
    }
}
