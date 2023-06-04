package chirag_pandkar.com.expensemanager.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.presenter.ExpensePresenter;
import chirag_pandkar.com.expensemanager.utils.ExpenseCollection;
import chirag_pandkar.com.expensemanager.view.ExpenseView;

public class ExpenseFragment extends Fragment implements ExpenseView, View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_expense, container, false);
    }

    private SeekBar seekBar;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this.getActivity());
        ExpensePresenter expensePresenter = new ExpensePresenter(expenseDatabaseHelper, this);
        expensePresenter.setExpenseTypes();
        expenseDatabaseHelper.close();

        Button addExpenseButton = getActivity().findViewById(R.id.add_expense);
        addExpenseButton.setOnClickListener(this);
        Double expenseLimit = expenseDatabaseHelper.getExpenseLimit();
        if (expenseLimit == null) {
            Toast.makeText(this.getActivity(), "Monthly Limit Not Set, Please Set The Limit", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Set Monthly Limit");
            builder.setMessage("Please set the monthly limit:");
            final EditText limitInput = new EditText(getActivity());
            builder.setView(limitInput);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String limitString = limitInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(limitString)) {
                        Double limit = Double.valueOf(limitString);
                        expenseDatabaseHelper.setExpenseLimit(limit);
                        Toast.makeText(getActivity(), "Monthly Limit Set Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Invalid limit. Please enter a valid value.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        seekBar = getActivity().findViewById(R.id.seekBar);
        double currentExpense = new ExpenseCollection(expenseDatabaseHelper.getExpenses()).getTotalExpense(); // Replace with your current expense value
        double monthlyLimit = expenseDatabaseHelper.getExpenseLimit();
        // Calculate the percentage of the limit reached
        int limitReached = (int) ((currentExpense / monthlyLimit) * 100);

        // Set the progress value
        seekBar.setProgress(limitReached);
        seekBar.setEnabled(false);
    }
    @Override
    public String getAmount() {
        TextView view = getActivity().findViewById(R.id.amount);
        return view.getText().toString();
    }

    @Override
    public String getType() {
        Spinner spinner = getActivity().findViewById(R.id.expense_type);
        return (String) spinner.getSelectedItem();
    }

    @Override
    public void renderExpenseTypes(List<String> expenseTypes) {
        Spinner spinner = getActivity().findViewById(R.id.expense_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, expenseTypes);
        spinner.setAdapter(adapter);
    }

    @Override
    public void displayError() {
        TextView view = getActivity().findViewById(R.id.amount);
        view.setError(getActivity().getString(R.string.amount_empty_error));
    }

    @Override
    public void onClick(View view) {
        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(this.getActivity());
        ExpensePresenter expensePresenter = new ExpensePresenter(expenseDatabaseHelper, this);
        ExpenseCollection expenseCollection = new ExpenseCollection(expenseDatabaseHelper.getExpenses());
        Double totalExpense = expenseCollection.getTotalExpense();
        Double monthlyExpense = expenseDatabaseHelper.getExpenseLimit();
        BigDecimal precc = new BigDecimal(totalExpense / monthlyExpense * 100);
        BigDecimal rounded = precc.setScale(1, RoundingMode.FLOOR);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Warning");
        if (precc.doubleValue() < 100D) {
            builder.setMessage("You Have Reached " + rounded.doubleValue() + " % of your monthly limit");
        } else builder.setMessage("You Have Exceeded your monthly limit.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        if (expensePresenter.addExpense()) {
            MainActivity activity = (MainActivity) getActivity();
            Toast.makeText(activity, R.string.expense_add_successfully, Toast.LENGTH_LONG).show();
            activity.onExpenseAdded();
        }
        expenseDatabaseHelper.close();
    }


}
