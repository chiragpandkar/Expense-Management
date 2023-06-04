package chirag_pandkar.com.expensemanager.presenter;

import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.utils.ExpenseCollection;
import chirag_pandkar.com.expensemanager.view.CurrentWeekExpenseView;

public class CurrentWeekExpensePresenter {

    private final CurrentWeekExpenseView view;
    private final ExpenseDatabaseHelper database;
    private final ExpenseCollection expenseCollection;

    public CurrentWeekExpensePresenter(ExpenseDatabaseHelper database, CurrentWeekExpenseView view) {
        this.database = database;
        this.view = view;
        expenseCollection = new ExpenseCollection(this.database.getCurrentWeeksExpenses());
    }

    public void renderTotalExpenses() {
        view.displayTotalExpenses(expenseCollection.getTotalExpense());
    }

    public void renderCurrentWeeksExpenses() {
        view.displayCurrentWeeksExpenses(expenseCollection.groupByDate());
    }
}
