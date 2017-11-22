package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.StepsDB;

public class MapFragment extends Fragment {
    private MapView mapView;
    private View view;
    private LocationManager locationManager;
    private String provider;
    private StepsDB stepsDB;
    private List<GeoPoint> geoPoints;
    private Polyline path;
    String date = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        view = inflater.inflate(R.layout.map_fragment, container, false);

        init();
        // Inflate the layout for this fragment
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
        drawHistoryPaths();
    }

    private void init() {
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());
        mapView = view.findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(14);
        ((MainActivity) getActivity()).requestLocUpdates();
        double lat = ((MainActivity) getActivity()).getLatitude();
        double lon = ((MainActivity) getActivity()).getLongitude();
        centerMapView(lat, lon);
        stepsDB = ((MainActivity) getActivity()).stepsDB;
        initiatePathVariables();
    }

    public void centerMapView(double lat, double lon) {
        mapView.getController().setCenter(new GeoPoint(lat, lon));
    }

    private void initiatePathVariables() {
        if (geoPoints != null || path != null) {
            geoPoints = null;
            path = null;
        }
        geoPoints = new ArrayList<>();
        path = new Polyline();
    }

    private void drawHistoryPaths() {
        Cursor steps = stepsDB.getAllSteps();
        if (steps.moveToFirst()) {
            date = steps.getString(steps.getColumnIndex(StepsDB.DATE));
            for (steps.moveToFirst(); !steps.isAfterLast(); steps.moveToNext()) {
                if (!date.equals(steps.getString(steps.getColumnIndex(StepsDB.DATE)))) {
                    path.setPoints(geoPoints);
                    path.setColor(Color.BLUE);
                    mapView.getOverlayManager().add(path);
                    date = steps.getString(steps.getColumnIndex(StepsDB.DATE));
                    initiatePathVariables();
                    continue;
                }
                double lat = steps.getDouble(steps.getColumnIndex(StepsDB.LATITUDE));
                double lon = steps.getDouble(steps.getColumnIndex(StepsDB.LONGITUDE));
                if (lat != 0 && lon != 0) {
                    GeoPoint geoPoint = new GeoPoint(lat, lon);
                    geoPoints.add(geoPoint);
                }
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
