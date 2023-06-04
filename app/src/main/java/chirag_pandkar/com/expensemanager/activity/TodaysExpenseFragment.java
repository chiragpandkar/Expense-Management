package chirag_pandkar.com.expensemanager.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.adapter.TodaysExpenseListViewAdapter;
import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.model.Expense;
import chirag_pandkar.com.expensemanager.presenter.TodaysExpensePresenter;
import chirag_pandkar.com.expensemanager.view.TodaysExpenseView;

public class TodaysExpenseFragment extends Fragment implements TodaysExpenseView {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.todays_expenses, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this.getActivity());
        TodaysExpensePresenter todaysExpensePresenter = new TodaysExpensePresenter(this, expenseDatabaseHelper);

        todaysExpensePresenter.renderTodaysExpenses();
        todaysExpensePresenter.renderTotalExpense();
        expenseDatabaseHelper.close();
    }

    @Override
    public void displayTotalExpense(Double totalExpense) {
        TextView totalExpenseTextBox = getActivity().findViewById(R.id.total_expense);
        totalExpenseTextBox.setText(getActivity().getString(R.string.total_expense) + " " + getActivity().getString(R.string.rupee_sym) + totalExpense.toString());
    }

    @Override
    public void displayTodaysExpenses(List<Expense> expenses) {
        ListView listView = getActivity().findViewById(R.id.todays_expenses_list);
        listView.setAdapter(new TodaysExpenseListViewAdapter(expenses, getActivity(), android.R.layout.simple_list_item_1));
    }
}
