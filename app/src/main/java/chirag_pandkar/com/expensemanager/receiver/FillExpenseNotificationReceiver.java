package chirag_pandkar.com.expensemanager.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.activity.MainActivity;

public class FillExpenseNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("asas", "FillExpenseNotificationReceiver");
        // Create a notification channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Expense Manager Channel";
            String channelId = "expense_manager_channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            // Customize the notification channel (optional)
            channel.setDescription("Expense Manager Notifications");

            // Register the notification channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

// Create the notification intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

// Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "expense_manager_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Fill Today's Expense")
                .setContentText("Never miss an expense, fill it now.")
                .setTicker("Every expense is important!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

// Show the notification
        Log.d("asas", "working");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());

    }
}
