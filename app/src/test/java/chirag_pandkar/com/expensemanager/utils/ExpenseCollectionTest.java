package chirag_pandkar.com.expensemanager.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import chirag_pandkar.com.expensemanager.model.Expense;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ExpenseCollectionTest {

  private Expense food;
  private Expense travel;
  private ExpenseCollection expenseCollection;

  @Before
  public void setUp() throws Exception {
    food = new Expense(100D, "Food", "31-09-2015");
    travel = new Expense(150D, "Travel", "02-10-2015");
    Expense movie = new Expense(100D, "Movie", "02-10-2015");
    expenseCollection = new ExpenseCollection(asList(food, movie, travel));
  }

  @Test
  public void shouldReturnTotalExpense() throws Exception {
    assertThat(expenseCollection.getTotalExpense(), is(350l));
  }

  @Test
  public void shouldReturnExpensesGroupedByDate() throws Exception {
    Map<String, List<Expense>> expenses = expenseCollection.groupByDate();

    assertThat(expenses.keySet().size(), is(2));
    assertThat(expenses.get(travel.getDate()).size(), is(2));
    assertThat(expenses.get(food.getDate()).size(), is(1));
  }
}