package demo.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback {

    private boolean flag; //是否请求了权限标志，没用
    private GoogleMap mMap;
    public volatile static LatLng latLng;
    private Button drawPoint, drawLine, drawCar, drawCarWithText;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        drawPoint = findViewById(R.id.drawPoint);
        drawLine = findViewById(R.id.drawLine);
        drawCar = findViewById(R.id.drawCar);
        drawCarWithText = findViewById(R.id.drawCarWithText);

        drawLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<LatLng> list = new ArrayList<>();
                list.add(new LatLng(31.92873, 138.59995));
                list.add(new LatLng(31, 121.7932231));
                list.add(new LatLng(31.81319, 144.96298));
                list.add(new LatLng(30, 115.85734));
                drawLine(list);

            }
        });
        drawPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marker(new LatLng(31, 121));
            }
        });

        drawCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                carMarker(new LatLng(29, 121), 105);
            }
        });

        drawCarWithText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carMarkerWithTest(new LatLng(29.5 ,121), 75);
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        initPermission();
        enableMyLocation();
        LocationUtils.addLocationListener(this, LocationManager.NETWORK_PROVIDER, new LocationUtils.ILocationListener() {
            @Override
            public void onSuccessLocation(Location location) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(MapsActivity.this, "network location: lat==" + latLng.latitude + "  lng==" + latLng.longitude, Toast.LENGTH_SHORT).show();
                if (isFirst) {
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(14)); //zoom 的方式
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //地图移动到某个位置
                    isFirst = false;
                }
            }
        });

    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }

    /**
     * 权限的结果回调函数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            flag = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        getNetworkLocation();
        return false;
    }

    /**
     * 通过网络等获取定位信息
     */
    private void getNetworkLocation() {
        Location net = LocationUtils.getNetWorkLocation(this);
        if (net == null) {
            Toast.makeText(this, "net location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "network location: lat==" + net.getLatitude() + "  lng==" + net.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        //会自动zoom,并定位。 需要点击才能拿到Location。所以用android的定位方式 LocationUtils去拿Latlng
    }

    private void marker(LatLng latLng) {

        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    //画汽车
    private void carMarker(LatLng latLng, float rotation) {
        //没有text图层，可以用title（需要点一下）
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("car")
                .rotation(rotation) //方向
                .snippet("提示信息")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                .infoWindowAnchor(0.5f, 0.5f);
        mMap.addMarker(options);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //一样 使用 popwindow
                Toast.makeText(MapsActivity.this, "点击了Marker", Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    private void carMarkerWithTest(LatLng latLng,float rotation) {
        View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.car_with_text, null);
        TextView carNoTv = (TextView) view.findViewById(R.id.car_no_tv);
        carNoTv.setText("沪C 88888");
        ImageView carImgIv = (ImageView) view.findViewById(R.id.car_img_iv);
        carImgIv.setRotation(rotation);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("car")
                .snippet("提示信息")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, view)))
                .infoWindowAnchor(0.5f, 0.5f);
        mMap.addMarker(options);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //一样 使用 popwindow
                Toast.makeText(MapsActivity.this, "点击了Marker", Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    private void drawLine(List<LatLng> latLngs) {
        mMap.addPolyline(new PolylineOptions()
                .addAll(latLngs) //添加所有点
                .width(5)
                .color(Color.BLUE)
                .geodesic(true));
    }


    //view to bitmap 项目中有
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
}
