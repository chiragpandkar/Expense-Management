package chirag_pandkar.com.expensemanager.table.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import chirag_pandkar.com.expensemanager.receiver.FillExpenseNotificationReceiver;
import chirag_pandkar.com.expensemanager.receiver.RemainderlExpenseNotificationReceiver;

public class FillExpenseNotificationScheduler {

    public void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(context, FillExpenseNotificationReceiver.class); // Replace `YourBroadcastReceiver` with the actual class name of your broadcast receiver
        notificationIntent.setAction("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6000, broadcast);
    }

    public void scheduleLimitRemainder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(context, RemainderlExpenseNotificationReceiver.class); // Replace `YourBroadcastReceiver` with the actual class name of your broadcast receiver
        notificationIntent.setAction("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Log.d("asas", "scheduled scheduleLimitRemainder");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6000, broadcast);
    }
}
