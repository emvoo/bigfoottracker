package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.DatabaseHelper;
import uk.ac.solent.marcinwisniewski.bigfoottracker.repositories.DateTimeRepository;

/**
 * Dashboard fragment.
 */
public class DashboardFragment extends Fragment {
    private TextView todayTotal, todayDistance, todayCalories, total, distance, calories;
    private static final String METRIC_TYPE = " m";
    private DatabaseHelper db;
    private int screenHeight;
    private int initialCardHeight;

    // constructor
    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);
        setHasOptionsMenu(true);
        setWindowHeight();

        LinearLayout dashboard_view = view.findViewById(R.id.dashboard_view);
        final List<CardView> cardViews = new ArrayList<>();
        for (int i = 0; i < dashboard_view.getChildCount(); i++) {
            final CardView cardView = (CardView) dashboard_view.getChildAt(i);

            cardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    cardView.getViewTreeObserver().removeOnPreDrawListener(this);
                    initialCardHeight = cardView.getHeight();
                    ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                    layoutParams.height = initialCardHeight;
                    cardView.setLayoutParams(layoutParams);

                    return true;
                }
            });
            cardViews.add(cardView);
        }

        for (CardView cardView:cardViews) {
            cardView.setOnClickListener(new View.OnClickListener() {
                boolean visible;
                @Override
                public void onClick(final View v) {
                    int cardViewId = 0;
                    int summaryContentId = 0;
                    int headerSummaryId = 0;

                    switch (v.getId()) {
                        case R.id.card_view:
                            cardViewId = R.id.card_view;
                            summaryContentId = R.id.day_summary_content;
                            headerSummaryId = R.id.day_summary_header;
                            break;
                        case R.id.card_view1:
                            summaryContentId = R.id.total_summary_content;
                            headerSummaryId = R.id.total_summary_header;
                            cardViewId = R.id.card_view1;
                            break;
                        default:
                            break;
                    }
                    final CardView cardView = v.findViewById(cardViewId);

                    if (cardView.getHeight() == initialCardHeight) {
                        expand(cardView, screenHeight);

                        for (CardView cv:cardViews) {
                            if (cv.getId() != cardView.getId()) {
                                collapse(cv, 0);
                            }
                            else {
                                final ViewGroup transitionsContainer = cardView;
                                final LinearLayout summaryContent = transitionsContainer.findViewById(summaryContentId);
                                final TextView headerSummary = transitionsContainer.findViewById(headerSummaryId);
                                TransitionManager.beginDelayedTransition(transitionsContainer);
                                headerSummary.getLayoutParams().height = 0;
                                visible = !visible;
                                summaryContent.setVisibility(visible ? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                    else {
                        collapse(cardView, initialCardHeight);
                        for (CardView cv:cardViews) {
                            if (cv.getId() != cardView.getId()) {
                                expand(cv, initialCardHeight);
                            } else {
                                final ViewGroup transitionsContainer = cardView;
                                final LinearLayout summaryContent = transitionsContainer.findViewById(summaryContentId);
                                final TextView headerSummary = transitionsContainer.findViewById(headerSummaryId);
                                headerSummary.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                                TransitionManager.beginDelayedTransition(transitionsContainer);
                                visible = !visible;
                                summaryContent.setVisibility(visible ? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                }
            });
        }

        init(view);
        updateDashboardFields();

        setWindowHeight();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Expands card view to full screen.
     * @param cardView
     * @param desiredHeight
     */
    private void expand(final CardView cardView, int desiredHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(cardView.getMeasuredHeightAndState(),
                desiredHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                layoutParams.height = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        animator.start();
    }

    /**
     * Collapses card view to its initial height.
     * @param cardView
     * @param desiredHeight
     */
    private void collapse(final CardView cardView, int desiredHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(cardView.getMeasuredHeightAndState(),
                desiredHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                layoutParams.height = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        animator.start();
    }

    /**
     * Sets window height
     */
    private void setWindowHeight() {
        WindowManager windowmanager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dimension = new DisplayMetrics();
        windowmanager.getDefaultDisplay().getMetrics(dimension);
        screenHeight = (int) (0.78*dimension.heightPixels);
    }

    /**
     * Initiates view values.
     * @param view
     */
    private void init(View view) {
        todayTotal = view.findViewById(R.id.todayTotal);
        todayDistance = view.findViewById(R.id.todayDistance);
        todayCalories = view.findViewById(R.id.todayCalories);
        total = view.findViewById(R.id.total);
        distance = view.findViewById(R.id.distance);
        calories = view.findViewById(R.id.calories);
        db = ((MainActivity) getActivity()).db;
    }

    /**
     * Sets today's steps.
     *
     * @param todayTotal
     */
    public void setTodayTotal(long todayTotal) {
        String value = todayTotal + " steps";
        this.todayTotal.setText(value);
    }

    /**
     * Sets today's distance
     *
     * @param todayDistance
     */
    public void setTodayDistance(double todayDistance) {
        String value = todayDistance + METRIC_TYPE;
        this.todayDistance.setText(value);
    }

    /**
     * Sets today's calories burnt
     *
     * @param todayCalories
     */
    public void setTodayCalories(int todayCalories) {
        String value = todayCalories + " kcal";
        this.todayCalories.setText(value);
    }

    /**
     * Sets total steps.
     *
     * @param total
     */
    public void setTotal(long total) {
        String value = total + " steps";
        this.total.setText(value);
    }

    /**
     * Set total distance.
     *
     * @param distance
     */
    public void setDistance(double distance) {
        String value = distance + METRIC_TYPE;
        this.distance.setText(value);
    }

    /**
     * Set total calories.
     *
     * @param calories
     */
    public void setCalories(int calories) {
        String value = calories + " kcal";
        this.calories.setText(value);
    }

    /**
     * Updates dashboards fields.
     */
    public void updateDashboardFields() {
        // today's fields
        DateTimeRepository dtr = new DateTimeRepository();
        String today = dtr.parseDate(new Date());

        long todaySteps = db.countAllStepsByDay(today);
        setTodayTotal(todaySteps);

        double todayDist = db.countDistanceByDay(today);
        setTodayDistance(todayDist);

        int todayKcal = calculateCalories(todaySteps);
        setTodayCalories(todayKcal);

        // total fields
        long steps = db.countAllSteps();
        setTotal(steps);

        double totalDistance = db.countDistance();
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
