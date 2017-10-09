package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.StepsDB;

public class DashboardFragment extends Fragment {
    private TextView todayTotal, todayDistance, todayCalories, total, distance, calories;
    private static final String METRIC_TYPE = " m";
    private StepsDB stepsDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);
        init(view);
        setValues();
        return view;
    }

    private void setValues() {
//        totalToday =
    }

    private void init(View view) {
//        userDb =
        todayTotal = view.findViewById(R.id.todayTotal);
        todayDistance = view.findViewById(R.id.todayDistance);
        todayCalories = view.findViewById(R.id.todayCalories);
        total = view.findViewById(R.id.total);
        distance = view.findViewById(R.id.distance);
        calories = view.findViewById(R.id.calories);
        stepsDB = ((MainActivity) getActivity()).stepsDB;
    }

    public void setTodayTotal(int todayTotal) {
        String value = todayTotal + " steps";
        this.todayTotal.setText(value);
    }

    public void setTodayDistance(int todayDistance) {
        String value = todayDistance + METRIC_TYPE;
        this.todayDistance.setText(value);
    }

    public void setTodayCalories(int todayCalories) {
        String value = todayCalories + " kcal";
        this.todayCalories.setText(value);
    }

    public void setTotal(int total) {
        String value = total + " steps";
        this.total.setText(value);
    }

    public void setDistance(int distance) {
        String value = distance + METRIC_TYPE;
        this.distance.setText(value);
    }

    public void setCalories(int calories) {
        String value = calories + " kcal";
        this.calories.setText(value);
    }

    public void updateDashboardFields()
    {

//        Cursor cursor = stepsDB.countAllStepsByDay();
//        setTodayTotal();
    }
}
