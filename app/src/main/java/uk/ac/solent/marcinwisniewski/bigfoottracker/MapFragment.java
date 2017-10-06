package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapFragment extends Fragment {
    private MapView mapView;
    View view;
    LocationManager locationManager;
    String provider;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mapView = new MapView(inflater.getContext());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        view = inflater.inflate(R.layout.map_fragment, container, false);

        init();
        // Inflate the layout for this fragment
        return view;
    }

    private void init() {
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());
        mapView = view.findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(14);
        mapView.getController().setCenter(new GeoPoint(51.05,-0.72));
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }
}
