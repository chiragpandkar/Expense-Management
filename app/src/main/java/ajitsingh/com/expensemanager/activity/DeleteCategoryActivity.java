package ajitsingh.com.expensemanager.activity;

import static ajitsingh.com.expensemanager.activity.MainActivity.ADD_NEW_CAT;
import static ajitsingh.com.expensemanager.activity.MainActivity.DELETE_CAT;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

import ajitsingh.com.expensemanager.R;
import ajitsingh.com.expensemanager.database.ExpenseDatabaseHelper;
import ajitsingh.com.expensemanager.presenter.CategoryPresenter;
import ajitsingh.com.expensemanager.presenter.ExpensePresenter;
import ajitsingh.com.expensemanager.view.AddCategoryView;
import ajitsingh.com.expensemanager.view.ExpenseView;


public class DeleteCategoryActivity extends FragmentActivity implements AddCategoryView, View.OnClickListener, ExpenseView {
  private ExpenseDatabaseHelper database;
  private ExpenseView view;

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this);
    ExpensePresenter expensePresenter = new ExpensePresenter(expenseDatabaseHelper, this);
//    expensePresenter.setExpenseTypes();
    expenseDatabaseHelper.close();
    setContentView(R.layout.delete_category);
  }


  public void setExpenseTypes() {
    view.renderExpenseTypes(database.getExpenseTypes());
  }


  @Override
  public String getAmount() {
    return null;
  }

  @Override
  public String getType() {
    Spinner spinner = (Spinner) this.findViewById(R.id.expense_type);
    return (String) spinner.getSelectedItem();
  }

  public void renderExpenseTypes(List<String> expenseTypes) {
    Spinner spinner = (Spinner) this.findViewById(R.id.expense_type);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, expenseTypes);
    spinner.setAdapter(adapter);
  }
  public void deleteCategory(View view) {
    ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this);
    CategoryPresenter categoryPresenter = new CategoryPresenter(this, expenseDatabaseHelper);
    expenseDatabaseHelper.close();
    if(categoryPresenter.deleteCategory())
      Toast.makeText(this, "Category Deleted Successfully", Toast.LENGTH_LONG).show();

    expenseDatabaseHelper.close();
    finishActivity(DELETE_CAT);
  }

  @Override
  public String getCategory() {
    TextView categoryInput = (TextView) findViewById(R.id.category);
    return categoryInput.getText().toString();
  }

  @Override
  public void displayError() {
    TextView view = (TextView) this.findViewById(R.id.category);
    view.setError(this.getString(R.string.category_empty_error));
  }

  @Override
  public void onClick(View view) {
    ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this);
    ExpensePresenter expensePresenter = new ExpensePresenter(expenseDatabaseHelper, this);
    if(expensePresenter.addExpense()){
      DeleteCategoryActivity activity = (DeleteCategoryActivity) this;
      Toast.makeText(activity, "Category Deleted Successfully", Toast.LENGTH_LONG).show();
      activity.deleteCategory(view);
    }
    expenseDatabaseHelper.close();
  }
}
