package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.DatabaseHelper;
import uk.ac.solent.marcinwisniewski.bigfoottracker.db.Step;

// TODO comment and check graph functionality

public class MapFragment extends Fragment {
    private MapView mapView;
    private View view;
    private DatabaseHelper db;
    private SharedPreferences prefs;

    private boolean follow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_fragment, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mapView = view.findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(14);
        ((MainActivity) getActivity()).requestLocUpdates();
        double lat = ((MainActivity) getActivity()).getLatitude();
        double lon = ((MainActivity) getActivity()).getLongitude();
        centerMapView(lat, lon);
        db = ((MainActivity) getActivity()).db;
        drawHistoryPaths();
    }

    public void centerMapView(double lat, double lon) {
        follow = prefs.getBoolean("follow", true);
        if (mapView != null && follow)
            mapView.getController().setCenter(new GeoPoint(lat, lon));
    }

    private void drawHistoryPaths() {
        List<Step> steps = db.getAllSteps();
        if (steps.size() > 0) {
            List<IGeoPoint> points = new ArrayList<>();
            for (Step step:steps) {
                double lat = step.getLatitude();
                double lon = step.getLongitude();
                if (lat != 0 && lon != 0) {
                    IGeoPoint igp = new LabelledGeoPoint(lat, lon);
                    points.add(igp);
                }
            }

            if (points.size() > 0) {
                // wrap them in a theme
                SimplePointTheme pt = new SimplePointTheme(points, true);

                // set some visual options for the overlay
                // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
                SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                        .setRadius(7).setCellSize(15);

                // create the overlay with the theme
                final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

                // add overlay
                mapView.getOverlays().add(sfpo);
            }
        }
    }
}
