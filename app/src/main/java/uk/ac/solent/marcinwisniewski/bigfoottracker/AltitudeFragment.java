package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.DatabaseHelper;
import uk.ac.solent.marcinwisniewski.bigfoottracker.db.Step;

public class AltitudeFragment extends Fragment {
    private GraphView graph;
    private DatabaseHelper db;
    private LineGraphSeries<DataPoint> series;
    private DataPoint[] dataSeries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.altitude_fragment, container, false);
        init(view);
        fillGraph();
        return view;
    }

    private void init(View view) {
        graph = view.findViewById(R.id.graph);
        db = ((MainActivity) getActivity()).db;
    }

    private void fillGraph()
    {
        List<Step> steps = db.getAllSteps();
        int noOfSteps = (int) db.countAllSteps();
        dataSeries = new DataPoint[noOfSteps];
        if (steps != null) {
            for (Step step:steps) {
                int id = step.getId();
                double altitude = step.getAltitude();
                id--;
                DataPoint dataPoint = new DataPoint(id, altitude);
                dataSeries[id] = dataPoint;
            }

            series = new LineGraphSeries<>(dataSeries);
            graph.addSeries(series);
        }
    }
}
