package chirag_pandkar.com.expensemanager.presenter;

import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.ExpenseType;
import chirag_pandkar.com.expensemanager.view.AddCategoryView;

public class CategoryPresenter {
    private final AddCategoryView view;
    private final ExpenseDatabaseHelper database;

    public CategoryPresenter(AddCategoryView view, ExpenseDatabaseHelper database) {
        this.view = view;
        this.database = database;
    }

    public boolean addCategory() {
        String newCategory = view.getCategory();
        if (newCategory.isEmpty()) {
            view.displayError();
            return false;
        }

        database.addExpenseType(new ExpenseType(newCategory));
        return true;
    }

    public boolean deleteCategory() {
        String newCategory = view.getCategory();
        if (newCategory.isEmpty()) {
            view.displayError();
            return false;
        }

        database.deleteExpenseType(new ExpenseType(newCategory));
        return true;
    }

}
