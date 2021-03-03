package com.hasan.firebase_chatapp.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hasan.firebase_chatapp.Activity.Foto_Viev;
import com.hasan.firebase_chatapp.Model.Chat;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;
import com.hasan.firebase_chatapp.Util.OnLoadMoreListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private static final int VIEW_PROG = 2;


    private Context mContext;
    private List<Chat> mChat = new ArrayList<>();
    private List<Chat> oChat = new ArrayList<>();
    Activity activity;
    FirebaseUser fuser;
    DatabaseReference reference;
    StorageReference islandRef;
    FirebaseStorage storage;
    ArrayList<String> mesajlar = new ArrayList<>();
    ArrayList<String> Smesajlar = new ArrayList<>();
    int totalTime;
    boolean State = false;
    boolean yaz = false;
    TextView text;


    private int visibleThreshold = 5;
    private int FirstVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public MessageAdapter(Activity activity, Context mContext, List<Chat> mChat, RecyclerView recyclerView) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.activity = activity;

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
                            FirstVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                            if (!loading &&  totalItemCount  <= (FirstVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                if (onLoadMoreListener != null && totalItemCount >= 5) {
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
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position) == null) {
            return VIEW_PROG;
        } else if (mChat.get(position).getSender().equals(fuser.getUid())) {

            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;

        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        } else if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            vh = new MViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            vh = new MViewHolder(view);
        }


        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MViewHolder) {
            final Chat chat = mChat.get(position);

            if (!chat.getImage().toString().equals("null")) {
                ((MViewHolder) holder).show_message.setVisibility(View.GONE);
                ((MViewHolder) holder).send_image.setVisibility(View.VISIBLE);
                ((MViewHolder) holder).bekle.setVisibility(View.VISIBLE);
                ((MViewHolder) holder).ses.setVisibility(View.GONE);
                ((MViewHolder) holder).ff.setVisibility(View.VISIBLE);

                Glide.with(mContext).load(chat.getImage()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ((MViewHolder) holder).bekle.setVisibility(View.GONE);
                        return false;
                    }
                }).into(((MViewHolder) holder).send_image);


            } else if (!chat.getRecord().equals("null")) {
                ((MViewHolder) holder).ses.setVisibility(View.VISIBLE);
                ((MViewHolder) holder).ff.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer = MediaPlayer.create(mContext, Uri.parse(chat.getRecord()));

                        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                                String s = String.valueOf(createTimeLabel(mp.getDuration()));
                                totalTime = mp.getDuration();
                                ((MViewHolder) holder).sure.setText(s);
                                ((MViewHolder) holder).akis.setMax(totalTime);

                                ((MViewHolder) holder).play.setOnClickListener(new View.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void onClick(View v) {

                                        if (!State) {
                                            ((MViewHolder) holder).play.setBackground(mContext.getDrawable(R.drawable.pause));
                                            mp.start();
                                            State = true;
                                            yaz(((MViewHolder) holder).sure, mp, ((MViewHolder) holder).akis);
                                        } else {
                                            ((MViewHolder) holder).play.setBackground(mContext.getDrawable(R.drawable.play));
                                            mp.pause();
                                            State = false;
                                            String s = "" + createTimeLabel(mp.getDuration());
                                            ((MViewHolder) holder).sure.setText(s);
                                        }
                                    }
                                });


                            }
                        });


                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                ((MViewHolder) holder).play.setBackground(mContext.getDrawable(R.drawable.play));
                                ((MViewHolder) holder).sure.setText(String.valueOf(createTimeLabel(mp.getDuration())));
                                State = false;


                            }
                        });

                    }
                }).start();


            } else {
                ((MViewHolder) holder).show_message.setText(chat.getMessage());
                ((MViewHolder) holder).show_message.setVisibility(View.VISIBLE);
                ((MViewHolder) holder).send_image.setVisibility(View.GONE);
            }

            ((MViewHolder) holder).send_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(activity, Foto_Viev.class);
                    i.putExtra("8090", chat.getImage());
                    i.putExtra("8091", chat.getSender());
                    activity.startActivity(i);
                }
            });

            ((MViewHolder) holder).date.setText(chat.getDate());

            ((MViewHolder) holder).root.setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public boolean onLongClick(View view) {

                    if (mesajlar.size() == 0) {
                        ((MViewHolder) holder).root.setBackgroundColor(R.color.colorPrimary);
                        selectItem(chat.getMessage(), chat.getSender(), chat.getDate());
                    }
                    return true;
                }
            });


            ((MViewHolder) holder).root.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View view) {

                    if (mesajlar.size() >= 1) {
                        if (Smesajlar.contains(chat.getMessage())) {
                            ((MViewHolder) holder).root.setBackgroundColor(Color.TRANSPARENT);
                            int i = Smesajlar.indexOf(chat.getMessage());
                            Smesajlar.remove(i);
                            mesajlar.remove(i);
                            copyy();

                        } else {
                            ((MViewHolder) holder).root.setBackgroundColor(R.color.colorPrimary);
                            selectItem(chat.getMessage(), chat.getSender(), chat.getDate());

                        }
                    }


                }
            });


            if (chat.isIsseen()) {
                ((MViewHolder) holder).date.setTextColor(Color.CYAN);
            }else{
                ((MViewHolder) holder).date.setTextColor(Color.DKGRAY);
            }


        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void yaz(TextView t, MediaPlayer mp, ProgressBar pb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (State) {
                    try {
                        Thread.sleep(1000);
                        pb.setProgress(mp.getCurrentPosition());
                        String remainingTime = "" + createTimeLabel(mp.getCurrentPosition());
                        if (mp.getCurrentPosition() == mp.getDuration())
                            State = false;
                        t.setText(remainingTime);

                    } catch (InterruptedException ignored) {
                    }

                }
            }
        });
    }


    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public class MViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView send_image;
        public TextView date;
        public RelativeLayout root;
        public RelativeLayout ff;
        public RelativeLayout ses;
        public ProgressBar bekle;
        public ProgressBar akis;
        public ImageButton play;
        public TextView sure;


        public MViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            send_image = itemView.findViewById(R.id.fotosnd);
            show_message = itemView.findViewById(R.id.show_message);
            date = itemView.findViewById(R.id.date);
            bekle = itemView.findViewById(R.id.bekle);
            sure = itemView.findViewById(R.id.dater);
            play = itemView.findViewById(R.id.play);
            akis = itemView.findViewById(R.id.akis);
            ses = itemView.findViewById(R.id.ses);
            ff = itemView.findViewById(R.id.ff);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }


    private static String sifrele(String s) {
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
                    sifreli += Character.valueOf((char) (a - 32));
                else
                    sifreli += Character.valueOf((char) (a));
            }
        }


        return sifreli;


    }

    private void selectItem(final String mesaj, String userName, final String date) {
        final String[] username = new String[1];
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userName);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username[0] = user.getUsername();
                String sMesaj = "(" + date + ")" + username[0] + " : " + mesaj + "\n";
                mesajlar.add(sMesaj);
                Smesajlar.add(mesaj);
                copyy();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void copyy() {

        String s = "";
        if (mesajlar.size() != 0) {
            for (String i : mesajlar)
                s += i;
        } else
            s = "null";
        Intent intent = new Intent("custom-message");
        intent.putExtra("item", s);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    private File klasor(String name, String id) {
        File cachePath = null;
        try {
            File root = Environment.getExternalStorageDirectory();
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "/Selefkos/");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File folder1 = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "/Selefkos/" + id);

            if (!folder1.exists()) {
                folder1.mkdir();
            }

            cachePath = new File(root.getAbsolutePath() + "/Selefkos/" + id + "/" + name + ".3gp");
            try {
                cachePath.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {

        }
        return cachePath;

    }


}