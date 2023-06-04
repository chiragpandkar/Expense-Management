package chirag_pandkar.com.expensemanager.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.activity.MainActivity;
import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.utils.ExpenseCollection;

public class RemainderlExpenseNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Expense Manager Channel";
            String channelId = "expense_manager_channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Expense Manager Notifications");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(context);
        ExpenseCollection expenseCollection = new ExpenseCollection(expenseDatabaseHelper.getExpenses());
        Double totalExpense = expenseCollection.getTotalExpense();
        Double monthlyExpense = expenseDatabaseHelper.getExpenseLimit();
        BigDecimal precc = new BigDecimal(totalExpense / monthlyExpense * 100);
        BigDecimal rounded = precc.setScale(1, RoundingMode.FLOOR);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "expense_manager_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Expense Limit Remainder")
                .setContentText("You have reached " + rounded.doubleValue() + " % Of Your Monthly Limit")
                .setTicker("Every expense is important!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());

    }
}
