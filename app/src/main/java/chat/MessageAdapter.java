package chat;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spindia.food.R;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> implements Handler.Callback{

        public static final int MSG_TYPE_LEFT=0;
        public static final int MSG_TYPE_RIGHT=1;
        private Context context;
        private ArrayList<ChatResponse> chatResponses=new ArrayList<>();
        FirebaseUser fUser;
        private static final int MSG_UPDATE_SEEK_BAR = 1845;

        private MediaPlayer mediaPlayer;
        private int playingPosition;
        private Handler uiUpdateHandler;
        private MyHolder playingHolder;

        public MessageAdapter(Context context, ArrayList<ChatResponse> chatResponses) {
            this.context = context;
            this.chatResponses = chatResponses;
            this.playingPosition = -1;
            uiUpdateHandler = new Handler(this);
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType==MSG_TYPE_RIGHT)
            {
                View view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chat_right,parent,false);
                return new MyHolder(view);
            }else {
                View view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chat_left,parent,false);
                return new MyHolder(view);
            }


        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int i) {
           ChatResponse chatResponse = chatResponses.get(i);

            holder.tvSeen.setText(chatResponse.getTime());

            DateFormat dateFormat = new SimpleDateFormat("dd MMMM,yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate= dateFormat.format(cal.getTime());

           String  todayDate=new SimpleDateFormat("dd MMMM,yyyy", Locale.US).format(new Date());
           if (chatResponse.getDate().equals(todayDate))
           {
               holder.tvDate.setText("TODAY");
           }else if (chatResponse.getDate().equals(yesterdayDate))
           {
               holder.tvDate.setText("YESTERDAY");
           }else
           {
               holder.tvDate.setText(chatResponse.getDate());
           }

            if (i > 0) {
                if (chatResponses.get(i).getDate().equalsIgnoreCase(chatResponses.get(i - 1).getDate())) {
                    holder.tvDate.setVisibility(View.GONE);
                } else {
                    holder.tvDate.setVisibility(View.VISIBLE);
                }
            } else {
                holder.tvDate.setVisibility(View.VISIBLE);
            }


            if (chatResponse.getType().equals("image"))
            {
                holder.tvMessage.setVisibility(View.GONE);

                holder.layImg.setVisibility(View.VISIBLE);
              //  holder.layAudio.setVisibility(View.GONE);


                Glide.with(context)
                        .load(chatResponse.getMessage())
                        .placeholder(R.drawable.restaurant_image_placeholder)
                        .into(holder.imgMsg);


            }else if (chatResponse.getType().equals("audio"))
            {
                holder.tvMessage.setVisibility(View.GONE);

                holder.layImg.setVisibility(View.GONE);
              //  holder.layAudio.setVisibility(View.VISIBLE);

            }else {
                holder.tvMessage.setVisibility(View.VISIBLE);

                holder.layImg.setVisibility(View.GONE);
              //  holder.layAudio.setVisibility(View.GONE);
                holder.tvMessage.setText(chatResponse.getMessage());
            }

            if (i == playingPosition) {
                playingHolder = holder;
                // this view holder corresponds to the currently playing audio cell
                // update its view to show playing progress
                updatePlayingView();
            } else {
                // and this one corresponds to non playing
                updateNonPlayingView(holder);
            }


            if (i== chatResponses.size()-1)
            {
                if (chatResponse.isseen)
                {
                    holder.imgSeen.setImageResource(R.drawable.doubletick);
                    holder.imgSeen.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    //holder.tvSeen.setText("Seen");
                }else {
                    holder.imgSeen.setImageResource(R.drawable.singletick);
                    holder.imgSeen.getLayoutParams().height = 26;
                    holder.imgSeen.getLayoutParams().width = 50;
                    holder.imgSeen.setColorFilter(context.getResources().getColor(R.color.grey));
                  //  holder.tvSeen.setText("Delivered");
                }

            }else {
                holder.imgSeen.setVisibility(View.GONE);
            }



        }

        @Override
        public int getItemCount() {
            return chatResponses.size();
        }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case MSG_UPDATE_SEEK_BAR: {
                //playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
            public TextView tvMessage, tvSeen,tvDate;
            public ImageView imgSeen,imgMsg;
            RelativeLayout layImg/* ,layAudio*/;
         // SeekBar sbProgress;
          // ImageView ivPlayPause;

            public MyHolder(final View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.text_msg);
                tvSeen = itemView.findViewById(R.id.text_seen);
                imgSeen=itemView.findViewById(R.id.img_seen);
                imgMsg=itemView.findViewById(R.id.img_message);

                layImg=itemView.findViewById(R.id.lay_img);
                //layAudio=itemView.findViewById(R.id.lay_audio);
                tvDate=itemView.findViewById(R.id.text_date);
               /* ivPlayPause = (ImageView) itemView.findViewById(R.id.ivPlayPause);
                ivPlayPause.setOnClickListener(this);
                sbProgress = (SeekBar) itemView.findViewById(R.id.sbProgress);
                sbProgress.setOnSeekBarChangeListener(this);*/
            }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == playingPosition) {
                // toggle between play/pause of audio
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            } else {
                // start another audio playback
                playingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder);
                    }
                    mediaPlayer.release();
                }
                playingHolder = this;
                startMediaPlayer(chatResponses.get(playingPosition).getMessage());
            }
            updatePlayingView();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                mediaPlayer.seekTo(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    public int getItemViewType(int position) {
            fUser= FirebaseAuth.getInstance().getCurrentUser();

            if (chatResponses.get(position).getSender().equals(fUser.getUid()))
            {
                return MSG_TYPE_RIGHT;
            }else {
                return MSG_TYPE_LEFT;
            }
    }

    private void updatePlayingView() {
      /*  playingHolder.sbProgress.setMax(mediaPlayer.getDuration());
        playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
        playingHolder.sbProgress.setEnabled(true);
        if (mediaPlayer.isPlaying()) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            playingHolder.ivPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            playingHolder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }*/
    }

    private void updateNonPlayingView(MyHolder holder) {
        if (holder == playingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
    /*    holder.sbProgress.setEnabled(false);
        holder.sbProgress.setProgress(0);
        holder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);*/
    }

    private void startMediaPlayer(String audioResId) {

        Uri uri = Uri.parse(audioResId);
        mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
        mediaPlayer.start();
    }

    private void releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder);
        }
        mediaPlayer.release();
        mediaPlayer = null;
        playingPosition = -1;
    }


}


