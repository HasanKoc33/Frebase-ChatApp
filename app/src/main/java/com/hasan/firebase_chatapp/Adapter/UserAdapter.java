package com.hasan.firebase_chatapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Activity.MessageActivity;
import com.hasan.firebase_chatapp.Model.Chat;
import com.hasan.firebase_chatapp.Model.Chatlist;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;
import com.hasan.firebase_chatapp.Util.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context mContext;
    private List<User> mUsers;
    private List<Chatlist> mUsers1;
    private boolean ischat;
    private String date = null;
    private String message;
    FirebaseUser fuser;
    DatabaseReference reference;
    String theLastMessage;
    Boolean ben = true;

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat , RecyclerView recyclerView) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading
                                    && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                if (onLoadMoreListener != null && totalItemCount>8) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }

    public UserAdapter(Context mContext, List<Chatlist> mUsers1, boolean ischat, int i, RecyclerView recyclerView) {
        this.mUsers1 = mUsers1;
        this.mContext = mContext;
        this.ischat = ischat;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading
                                    && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                if (onLoadMoreListener != null && totalItemCount>8) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mUsers.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder  onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v =  LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

            vh = new UViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UViewHolder) {
            final User user = mUsers.get(position);
            ((UViewHolder) holder).username.setText(user.getUsername());
            ((UViewHolder) holder).gelen.setVisibility(View.GONE);
            if (user.getImageURL().equals("default")) {
                ((UViewHolder) holder).profile_image.setImageResource(R.drawable.user);
            } else {
                Glide.with(mContext).load(user.getImageURL()).into(((UViewHolder) holder).profile_image);
            }

            if (ischat) {
                lastMessage(user.getId(), ((UViewHolder) holder).last_msg, ((UViewHolder) holder).gelen);

            } else {
                ((UViewHolder) holder).last_msg.setVisibility(View.GONE);
                ((UViewHolder) holder).gelen.setVisibility(View.GONE);
            }

            reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getId()).child("status");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue().toString().equals("Aktif")) {
                        ((UViewHolder) holder).img_on.setVisibility(View.VISIBLE);
                        ((UViewHolder) holder).img_off.setVisibility(View.GONE);
                    } else {
                        ((UViewHolder) holder).img_on.setVisibility(View.GONE);
                        ((UViewHolder) holder).img_off.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if (ischat) {
                if (user.getStatus().equals("Aktif")) {
                    ((UViewHolder) holder).img_on.setVisibility(View.VISIBLE);
                    ((UViewHolder) holder).img_off.setVisibility(View.GONE);
                } else {
                    ((UViewHolder) holder).img_on.setVisibility(View.GONE);
                    ((UViewHolder) holder).img_off.setVisibility(View.VISIBLE);
                }
            } else {
                ((UViewHolder) holder).gelen.setVisibility(View.GONE);
                ((UViewHolder) holder).img_on.setVisibility(View.GONE);
                ((UViewHolder) holder).img_off.setVisibility(View.GONE);
            }


            ((UViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    final Dialog builder = new Dialog(mContext);
                    builder.setContentView(R.layout.uyari);
                    builder.setTitle(R.string.app_name);

                    TextView mesaj = (TextView) builder.findViewById(R.id.mesaj);
                    TextView evet = (TextView) builder.findViewById(R.id.evet);
                    TextView iptal = (TextView) builder.findViewById(R.id.iptal);
                    mesaj.setText("Mesajlarınız silinecek \nEmin misin?");

                    evet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reference = FirebaseDatabase.getInstance().getReference();
                            fuser = FirebaseAuth.getInstance().getCurrentUser();
                            reference.child("Chats").child(fuser.getUid()).child(user.getId()).removeValue();
                            reference.child("Chatlist").child(fuser.getUid()).child(user.getId()).removeValue();
                            builder.dismiss();
                        }
                    });

                    iptal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            builder.dismiss();
                        }
                    });

                    builder.show();


                    return false;
                }
            });
            ((UViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });

            ((UViewHolder) holder).profile_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }
    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public class UViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;
        private TextView gelen;

        public UViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            gelen = itemView.findViewById(R.id.mesadet);

        }

    }
    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }
    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final TextView gelen) {
        theLastMessage = "default";
        final ArrayList<Boolean> seen = new ArrayList();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid).child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seen.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                            theLastMessage = sifrecoz(chat.getMessage());
                            ben = false;
                            if (!chat.isIsseen())
                                seen.add(chat.isIsseen());
                        }
                        if (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = sifrecoz(chat.getMessage());
                            ben = true;

                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("Karşı taraf mesajları sildi !!!");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                if (ben) {
                    last_msg.setTextColor(mContext.getColor(R.color.giden_son_mes));
                    gelen.setVisibility(View.GONE);

                } else {
                    gelen.setVisibility(View.VISIBLE);
                    gelen.setText(String.valueOf(seen.size()));
                    if (seen.size() != 0 && !seen.get(seen.size() - 1)) {
                        last_msg.setTextColor(mContext.getColor(R.color.Y_son_mes));
                        last_msg.setTextSize(18);
                    } else {
                        gelen.setVisibility(View.GONE);
                        last_msg.setTextColor(mContext.getColor(R.color.O_son_mes));
                        last_msg.setTextSize(14);
                    }

                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private static String sifrecoz(String s) {

        String tkar = "ıüğşİÜĞŞ";
        String sifreli = "";
        for (int i = 0; i < s.length(); i++) {
            boolean sayac = true;
            for (int b = 0; b < tkar.length(); b++) {
                if (tkar.charAt(b) == s.charAt(i)) {
                    sifreli += s.charAt(i);
                    sayac = false;
                    break;
                }
            }

            if (sayac) {
                int a = (int) s.charAt(i);
                if (a != 32)
                    sifreli += Character.valueOf((char) (a + 32));
                else
                    sifreli += Character.valueOf((char) (a));
            }
        }


        return sifreli;


    }


}
