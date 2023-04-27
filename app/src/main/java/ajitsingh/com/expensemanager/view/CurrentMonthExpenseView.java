package ajitsingh.com.expensemanager.view;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.PieSlice;

import java.util.List;

public interface CurrentMonthExpenseView {
  void displayGraph(List<Bar> points);

  void displayTotalExpense(Long totalExpense);

  int getGraphColor();
}
