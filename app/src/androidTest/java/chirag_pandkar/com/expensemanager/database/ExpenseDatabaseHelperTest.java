package chirag_pandkar.com.expensemanager.database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.model.ExpenseType;
import chirag_pandkar.com.expensemanager.table.ExpenseTable;
import chirag_pandkar.com.expensemanager.table.ExpenseTypeTable;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExpenseDatabaseHelperTest {

  private ExpenseDatabaseHelper database;

  @Before
  public void setUp() throws Exception {
    getTargetContext().deleteDatabase(ExpenseDatabaseHelper.EXPENSE_DB);
    database = new ExpenseDatabaseHelper(getTargetContext());
    database.truncate(ExpenseTypeTable.TABLE_NAME);
    database.truncate(ExpenseTable.TABLE_NAME);

    freezeDate("2015-10-02");
  }

  @After
  public void tearDown() throws Exception {
    database.close();
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void shouldAddExpenseType() throws Exception {
    database.addExpenseType(new ExpenseType("Food"));

    List<String> expenseTypes = database.getExpenseTypes();
    assertThat(expenseTypes.size(), is(1));
    assertTrue(expenseTypes.get(0).equals("Food"));
  }

  @Test
  public void shouldReturnCurrentMonthsExpenses() throws Exception {
    database.addExpense(new Expense(100D, "Food", "31-09-2015"));
    database.addExpense(new Expense(200D, "Food", "02-10-2015"));

    List<Expense> expenses = database.getExpensesForCurrentMonthGroupByCategory();

    assertThat(expenses.size(), is(1));
    assertThat(expenses.get(0).getAmount(), is(200l));
  }



  private void freezeDate(String date) {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(date).getMillis());
  }

  private Context getTargetContext() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }
}