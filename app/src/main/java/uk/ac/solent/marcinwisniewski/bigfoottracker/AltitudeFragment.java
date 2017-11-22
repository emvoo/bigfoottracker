package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.StepsDB;

public class AltitudeFragment extends Fragment {
    private GraphView graph;
    private StepsDB stepsDB;
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
        stepsDB = ((MainActivity) getActivity()).stepsDB;
    }

    private void fillGraph()
    {
        Cursor steps = stepsDB.getAllSteps();
        int noOfSteps = (int) stepsDB.countAllSteps();
        dataSeries = new DataPoint[noOfSteps];
        if (steps.moveToFirst()) {
            for (steps.moveToFirst(); !steps.isAfterLast(); steps.moveToNext()) {
                int id = steps.getInt(steps.getColumnIndex(StepsDB.ID));
                double altitude = steps.getDouble(steps.getColumnIndex(StepsDB.ALTITUDE));
                id--;
                DataPoint dataPoint = new DataPoint(id, altitude);
                dataSeries[id] = dataPoint;
            }

            series = new LineGraphSeries<>(dataSeries);
            graph.addSeries(series);
        }
    }
}
