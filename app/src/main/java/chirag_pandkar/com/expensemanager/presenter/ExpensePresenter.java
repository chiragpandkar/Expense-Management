package chirag_pandkar.com.expensemanager.presenter;

import static chirag_pandkar.com.expensemanager.utils.DateUtil.getCurrentDate;

import android.util.Log;

import java.util.List;

import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.view.ExpenseView;

public class ExpensePresenter {

    private final ExpenseDatabaseHelper database;
    private final ExpenseView view;

    public ExpensePresenter(ExpenseDatabaseHelper expenseDatabaseHelper, ExpenseView view) {
        this.database = expenseDatabaseHelper;
        this.view = view;
    }

    public boolean addExpense() {
        String amount = view.getAmount();

        if (amount.isEmpty()) {
            view.displayError();
            return false;
        }

        Expense expense = new Expense(Double.valueOf(amount), view.getType(), getCurrentDate());
        database.addExpense(expense);
        return true;
    }

    public boolean addExpenseHH(String amount) {
        if (amount.isEmpty()) {
            view.displayError();
            return false;
        }

        Expense expense = new Expense(Double.valueOf(amount), view.getType(), getCurrentDate());
        database.addExpense(expense);
        return true;
    }

    public void setExpenseTypes() {
        List<String> expenseTypes = database.getExpenseTypes();
        try {
            //Log.d("BBB","inPresenter" + expenseTypes);
            view.renderExpenseTypes(expenseTypes);

        } catch (Exception e) {
            Log.d("EEE", "E" + e);
        }
    }
}
