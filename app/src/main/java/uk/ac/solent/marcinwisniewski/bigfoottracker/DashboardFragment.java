package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.StepsDB;
import uk.ac.solent.marcinwisniewski.bigfoottracker.repositories.DateTimeRepository;

public class DashboardFragment extends Fragment {
    private TextView todayTotal, todayDistance, todayCalories, total, distance, calories;
    private static final String METRIC_TYPE = " m";
    private StepsDB stepsDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);
        init(view);
        updateDashboardFields();
        return view;
    }

    private void init(View view) {
        todayTotal = view.findViewById(R.id.todayTotal);
        todayDistance = view.findViewById(R.id.todayDistance);
        todayCalories = view.findViewById(R.id.todayCalories);
        total = view.findViewById(R.id.total);
        distance = view.findViewById(R.id.distance);
        calories = view.findViewById(R.id.calories);
        stepsDB = ((MainActivity) getActivity()).stepsDB;
    }

    public void setTodayTotal(long todayTotal) {
        String value = todayTotal + " steps";
        this.todayTotal.setText(value);
    }

    public void setTodayDistance(double todayDistance) {
        String value = todayDistance + METRIC_TYPE;
        this.todayDistance.setText(value);
    }

    public void setTodayCalories(int todayCalories) {
        String value = todayCalories + " kcal";
        this.todayCalories.setText(value);
    }

    public void setTotal(long total) {
        String value = total + " steps";
        this.total.setText(value);
    }

    public void setDistance(double distance) {
        String value = distance + METRIC_TYPE;
        this.distance.setText(value);
    }

    public void setCalories(int calories) {
        String value = calories + " kcal";
        this.calories.setText(value);
    }

    public void updateDashboardFields()
    {
        // today's fields
        DateTimeRepository dtr = new DateTimeRepository();
        String today = dtr.parseDate(new Date());
        long todaySteps = stepsDB.countAllStepsByDay(today);
        setTodayTotal(todaySteps);

        double todayDist = stepsDB.countDistanceByDay(today);
        setTodayDistance(todayDist);

        int todayKcal = calculateCalories(todaySteps);
        setTodayCalories(todayKcal);

        // total fields
        long steps = stepsDB.countAllSteps();
        setTotal(steps);

        double totalDistance = stepsDB.countDistance();
        setDistance(totalDistance);

        int totalCalories = calculateCalories(steps);
        setCalories(totalCalories);
    }

    /**
     * Calculates calories based on number of steps.
     * We burn about kcal per every 20 steps.
     */
    private int calculateCalories(long steps) {
        return (int)steps/20;
    }
}
