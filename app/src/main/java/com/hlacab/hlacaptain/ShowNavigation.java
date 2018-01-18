package com.hlacab.hlacaptain;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.commons.models.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gopinath on 17/01/18.
 */

public class ShowNavigation extends AppCompatActivity {
    Point origin, destination;
    String awsPoolId;
    boolean simulateRoute;
    NavigationView navigationView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      //  setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_show_navigation);
        Mapbox.getInstance(getApplicationContext(),"pk.eyJ1IjoiZ29waW5hdGhzd2FtaW5hdGhhbiIsImEiOiJjamNpcDlteG8xeGtoMndwYXBqOXh1bzhsIn0.KpNkoJdYU3IW9ubd_MB5oQ");
        MapboxNavigation navigation = new MapboxNavigation(this, "pk.eyJ1IjoiZ29waW5hdGhzd2FtaW5hdGhhbiIsImEiOiJjamNpcDlteG8xeGtoMndwYXBqOXh1bzhsIn0.KpNkoJdYU3IW9ubd_MB5oQ");
    Point origin=Point.fromLngLat(-77.03613,38.90992);
    Point destination=Point.fromLngLat(-77.0365,38.8977);
    NavigationRoute.builder().accessToken(Mapbox.getAccessToken()).origin(origin).destination(destination).build().getRoute(new Callback<DirectionsResponse>() {
        @Override
        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

        }

        @Override
        public void onFailure(Call<DirectionsResponse> call, Throwable t) {

        }
    });

    }

}
