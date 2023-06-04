package chirag_pandkar.com.expensemanager.table;

import static android.provider.BaseColumns._ID;

public class ExpenseLimitTable {

    public static final String TABLE_NAME = "expense_limit";

    public static final String CREATE_TABLE_QUERY = "create table " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "monthly_limit " + " float)";
    public static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
}
