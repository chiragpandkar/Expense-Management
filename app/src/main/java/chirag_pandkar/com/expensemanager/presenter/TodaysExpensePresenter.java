package chirag_pandkar.com.expensemanager.presenter;

import java.util.List;

import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.view.TodaysExpenseView;

public class TodaysExpensePresenter {

    private final List<Expense> expenses;
    private final TodaysExpenseView view;

    public TodaysExpensePresenter(TodaysExpenseView view, ExpenseDatabaseHelper expenseDatabaseHelper) {
        this.view = view;
        expenses = expenseDatabaseHelper.getTodaysExpenses();
    }

    public void renderTotalExpense() {
        Double totalExpense = 0D;
        for (Expense expense : expenses)
            totalExpense += expense.getAmount();

        view.displayTotalExpense(totalExpense);
    }

    public void renderTodaysExpenses() {
        view.displayTodaysExpenses(expenses);
    }
}
