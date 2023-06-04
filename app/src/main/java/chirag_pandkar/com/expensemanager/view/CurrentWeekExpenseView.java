package chirag_pandkar.com.expensemanager.view;

import java.util.List;
import java.util.Map;

import chirag_pandkar.com.expensemanager.model.Expense;

public interface CurrentWeekExpenseView {
    void displayCurrentWeeksExpenses(Map<String, List<Expense>> expensesByDate);

    void displayTotalExpenses(Double totalExpense);
}
