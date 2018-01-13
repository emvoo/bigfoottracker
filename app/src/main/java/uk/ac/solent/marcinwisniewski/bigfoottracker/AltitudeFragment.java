package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.DatabaseHelper;
import uk.ac.solent.marcinwisniewski.bigfoottracker.db.Step;

/**
 * Class to display taken steps altitudes.
 */
public class AltitudeFragment extends Fragment {
    private DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.altitude_fragment, container, false);
        init();
        fillGraph(view);
        return view;
    }

    private void init() {
        db = ((MainActivity) getActivity()).db;
    }

    private void fillGraph(View view)
    {
        GraphView graph = view.findViewById(R.id.graph);
        GridLabelRenderer glr = graph.getGridLabelRenderer();
        glr.setPadding(32);

        List<Step> steps = db.getAllSteps();
        int noOfSteps = steps.size();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        if (steps.size() > 0) {
            for (int i = 1; i < noOfSteps; i++) {
                double altitude = steps.get(i).getAltitude();
                series.appendData(new DataPoint(i, altitude), true, noOfSteps);
            }
            graph.addSeries(series);
        }
    }
}
