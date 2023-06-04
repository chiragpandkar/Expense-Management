package chirag_pandkar.com.expensemanager.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.ExpenseType;
import chirag_pandkar.com.expensemanager.table.ExpenseTypeTable;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class DatabaseResetRule implements TestRule {

  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          clearDatabase();
          seedData();
          base.evaluate();
        } finally {
          clearDatabase();
        }
      }
    };
  }

  private void clearDatabase() {
    final ExpenseDatabaseHelper database = new ExpenseDatabaseHelper(getInstrumentation().getTargetContext());
    database.deleteAll();
    database.close();
  }

  private void seedData() {
    final ExpenseDatabaseHelper database = new ExpenseDatabaseHelper(getInstrumentation().getTargetContext());
    List<ExpenseType> expenseTypes = ExpenseTypeTable.seedData();
    for (ExpenseType expenseType : expenseTypes) {
      database.addExpenseType(expenseType);
    }
    database.close();
  }
}
