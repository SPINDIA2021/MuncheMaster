package chat.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import chat.MessageActivity;
import utils.Preferences;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented=remoteMessage.getData().get("sented");
        String user=remoteMessage.getData().get("user");



       /// String currentUser= Preferences.getString(Preferences.CURRENT_RUID);

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null && sented.equals(firebaseUser.getUid()))
        {
            if (!firebaseUser.equals(user))
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    sendNotificationOreo(remoteMessage);
                }else {
                    sendNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNotificationOreo(RemoteMessage remoteMessage) {

        String user=remoteMessage.getData().get("user");
        String username=remoteMessage.getData().get("userName");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");


        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        intent.putExtra("id",user);
        intent.putExtra("name",username);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification=new OreoNotification(this);
        Notification.Builder builder=oreoNotification.getOreoNotification(title,body,pendingIntent,defaultSound,icon);

        int i=0;
        if (j>0)
        {
            i=j;
        }

        oreoNotification.getManager().notify(i,builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user=remoteMessage.getData().get("user");
        String username=remoteMessage.getData().get("userName");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");


        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        intent.putExtra("id",user);
        intent.putExtra("name",username);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager noti= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        int i=0;
        if (j>0)
        {
            i=j;
        }

        noti.notify(i,builder.build());
    }
}
