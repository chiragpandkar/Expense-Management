package chirag_pandkar.com.expensemanager.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Map;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.adapter.CurrentWeeksExpenseAdapter;
import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.presenter.CurrentWeekExpensePresenter;
import chirag_pandkar.com.expensemanager.view.CurrentWeekExpenseView;

public class CurrentWeekExpenseFragment extends Fragment implements CurrentWeekExpenseView {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.current_week_expenses, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(getActivity());
        CurrentWeekExpensePresenter presenter = new CurrentWeekExpensePresenter(expenseDatabaseHelper, this);
        presenter.renderTotalExpenses();
        presenter.renderCurrentWeeksExpenses();
        expenseDatabaseHelper.close();
    }

    @Override
    public void displayCurrentWeeksExpenses(Map<String, List<Expense>> expensesByDate) {
        ExpandableListView listView = getActivity().findViewById(R.id.current_week_expenses_list);
        listView.setAdapter(new CurrentWeeksExpenseAdapter(getActivity(), expensesByDate));
    }

    @Override
    public void displayTotalExpenses(Double totalExpense) {
        TextView totalExpenseTextBox = getActivity().findViewById(R.id.current_week_expense);
        totalExpenseTextBox.setText(getString(R.string.total_expense) + " " + getString(R.string.rupee_sym) + totalExpense);
    }
}
