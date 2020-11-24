package co.edu.unal.gpsandroid;

import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.EditTextPreference;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;

    Button button;
    EditText editTextRadius;
    EditText editTextLimit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        button = (Button) findViewById(R.id.button);
        editTextRadius = (EditText) findViewById(R.id.editTextRadius);
        editTextLimit = (EditText) findViewById(R.id.editTextLimit);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getDataMarker(Integer.parseInt(editTextRadius.getText().toString()), Integer.parseInt(editTextLimit.getText().toString()));
            }

        });

        //getDataMarker(500,2);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void getDataMarker(int radius, int limit){

        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .tilesetIds("mapbox.mapbox-streets-v8")
                .query(Point.fromLngLat(-74.234325,4.5804972))
                .radius(radius)
                .limit(limit)
                .geometry("polygon") // "point", "linestring", or "polygon"
                .dedupe(true)
                //.layers("building") // layer name within a tileset, not a style
                .build();

        System.out.println("tilequery "+tilequery);

        tilequery.enqueueCall(new Callback<FeatureCollection>() {
            @Override public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {

                // The FeatureCollection that is inside the API response
                List<Feature> featureList = response.body().features();
                System.out.println("featureList " + featureList);

                List<Double> coordinates = new ArrayList<>();

                System.out.println("Pos0 " + featureList.get(0).geometry().toJson());
                int i =0;
                for (Feature feature:featureList
                ) {
                    // The Feature's GeoJSON geometry type
                    String type = featureList.get(i).getProperty("type").toString();
                    System.out.println("type "+type);

                    // The id of the map layer which the Feature is a part of
                    String layerId = featureList.get(i).getProperty("tilequery").getAsJsonObject().get("layer").toString();
                    System.out.println("layerId " + layerId);


                    Object obj = JsonParser.parseString(featureList.get(i).geometry().toJson());
                    JsonObject jo = (JsonObject) obj;
                    System.out.println("*******************coordinates************");
                    System.out.println(jo.get("coordinates"));

                    JsonArray ja = jo.get("coordinates").getAsJsonArray();
                    System.out.println(ja.get(0).getAsDouble());
                    System.out.println(ja.get(1).getAsDouble());
                    coordinates.add(ja.get(0).getAsDouble());

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(ja.get(1).getAsDouble(), ja.get(0).getAsDouble()))
                            .title(type)
                            .snippet(layerId));
                    i++;
                }
            }

            @Override public void onFailure(Call<FeatureCollection> call, Throwable throwable) {

                Log.d("Request failed: %s", throwable.getMessage());
            }
        });


    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.TRAFFIC_NIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                enableLocationComponent(style);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            locationEngine = LocationEngineProvider.getBestLocationEngine(this);
            System.out.println("locationEngine "+locationEngine);

        // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            System.out.println("locationComponent "+locationComponent);

        // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

        // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

        // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("results "+requestCode+permissions+grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}