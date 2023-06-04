package chirag_pandkar.com.expensemanager.view;

import java.util.List;

import chirag_pandkar.com.expensemanager.model.Expense;

public interface TodaysExpenseView {
    void displayTotalExpense(Double totalExpense);

    void displayTodaysExpenses(List<Expense> expenses);
}
