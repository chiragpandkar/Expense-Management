package chirag_pandkar.com.expensemanager.database;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static chirag_pandkar.com.expensemanager.utils.DateUtil.getCurrentDate;
import static chirag_pandkar.com.expensemanager.utils.DateUtil.getCurrentWeeksDates;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.model.ExpenseType;
import chirag_pandkar.com.expensemanager.table.ExpenseLimitTable;
import chirag_pandkar.com.expensemanager.table.ExpenseTable;
import chirag_pandkar.com.expensemanager.table.ExpenseTypeTable;
import chirag_pandkar.com.expensemanager.utils.DateUtil;

public class ExpenseDatabaseHelper extends SQLiteOpenHelper {
    public static final String EXPENSE_DB = "expense";

    public ExpenseDatabaseHelper(Context context) {
        super(context, EXPENSE_DB, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("aaaa", "Executed");
        sqLiteDatabase.execSQL(ExpenseTable.CREATE_TABLE_QUERY);
        sqLiteDatabase.execSQL(ExpenseTypeTable.CREATE_TABLE_QUERY);
        sqLiteDatabase.execSQL(ExpenseLimitTable.CREATE_TABLE_QUERY);
        seedExpenseTypes(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public List<String> getExpenseTypes() {
        ArrayList<String> expenseTypes = new ArrayList<>();

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTypeTable.SELECT_ALL, null);

        if (isCursorPopulated(cursor)) {
            do {
                String type = cursor.getString(cursor.getColumnIndex(ExpenseTypeTable.TYPE));
                expenseTypes.add(type);
            } while (cursor.moveToNext());
        }

        return expenseTypes;
    }

    public Double getExpenseLimit() {
        String query = "SELECT monthly_limit FROM expense_limit";
        Cursor cursor = this.getReadableDatabase().rawQuery(query, null);

        String expenseLimit = null;
        if (cursor.moveToFirst()) {
            expenseLimit = cursor.getString(cursor.getColumnIndex("monthly_limit"));
        }

        cursor.close();
        if (expenseLimit != null) {
            return Double.parseDouble(expenseLimit);
        }
        return null;
    }

    public void setExpenseLimit(Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("monthly_limit", limit.toString());

        db.insert("expense_limit", null, values);
        db.close();
    }

    public void updateExpenseLimit(Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("monthly_limit", limit.toString());

        db.update("expense_limit", values, null, null);
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(ExpenseTypeTable.TABLE_NAME, "", new String[]{});
        database.delete(ExpenseTable.TABLE_NAME, "", new String[]{});
        database.close();
    }

    public void addExpense(Expense expense) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseTable.AMOUNT, expense.getAmount());
        values.put(ExpenseTable.TYPE, expense.getType());
        values.put(ExpenseTable.DATE, expense.getDate());
        database.insert(ExpenseTable.TABLE_NAME, null, values);
    }

    public List<Expense> getExpenses() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTable.SELECT_ALL, null);

        return buildExpenses(cursor);
    }

    public List<Expense> getTodaysExpenses() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTable.getExpensesForDate(getCurrentDate()), null);

        return buildExpenses(cursor);
    }

    public List<Expense> getCurrentWeeksExpenses() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTable.getConsolidatedExpensesForDates(getCurrentWeeksDates()), null);
        return buildExpenses(cursor);
    }

    public List<Expense> getExpensesGroupByCategory() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTable.SELECT_ALL_GROUP_BY_CATEGORY, null);
        return buildExpenses(cursor);
    }

    public List<Expense> getExpensesForCurrentMonthGroupByCategory() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(ExpenseTable.getExpenseForCurrentMonth(DateUtil.currentMonthOfYear()), null);
        return buildExpenses(cursor);
    }

    public void addExpenseType(ExpenseType type) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseTable.TYPE, type.getType());

        database.insert(ExpenseTypeTable.TABLE_NAME, null, values);
    }

    public void deleteExpenseType(ExpenseType type) {
        Log.d("ggg", type.getType());
        SQLiteDatabase database = this.getWritableDatabase();
        database.rawQuery(ExpenseTable.deleteExpenseType(type), null);
    }


    public void truncate(String tableName) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from " + tableName);
    }

    private List<Expense> buildExpenses(Cursor cursor) {
        List<Expense> expenses = new ArrayList<>();
        if (isCursorPopulated(cursor)) {
            do {
                String type = cursor.getString(cursor.getColumnIndex(ExpenseTable.TYPE));
                String amount = cursor.getString(cursor.getColumnIndex(ExpenseTable.AMOUNT));
                String date = cursor.getString(cursor.getColumnIndex(ExpenseTable.DATE));
                String id = cursor.getString(cursor.getColumnIndex(ExpenseTable._ID));

                Expense expense = id == null ? new Expense(parseDouble(amount), type, date) : new Expense(parseInt(id), parseDouble(amount), type, date);
                expenses.add(expense);
            } while (cursor.moveToNext());
        }

        return expenses;
    }

    private boolean isCursorPopulated(Cursor cursor) {
        return cursor != null && cursor.moveToFirst();
    }

    private void seedExpenseTypes(SQLiteDatabase sqLiteDatabase) {
        List<ExpenseType> expenseTypes = ExpenseTypeTable.seedData();
        for (ExpenseType expenseType : expenseTypes) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ExpenseTypeTable.TYPE, expenseType.getType());

            sqLiteDatabase.insert(ExpenseTypeTable.TABLE_NAME, null, contentValues);
        }
    }
}
