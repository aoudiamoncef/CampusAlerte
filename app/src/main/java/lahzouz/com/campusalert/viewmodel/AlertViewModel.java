package lahzouz.com.campusalert.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lahzouz.com.campusalert.service.database.AppDatabase;
import lahzouz.com.campusalert.service.localisation.LocationProvider;
import lahzouz.com.campusalert.service.model.Alert;


public class AlertViewModel extends AndroidViewModel {
    private final LiveData<Alert> alertObservable;
    private final long alertId;
    public ObservableField<Alert> alert = new ObservableField<>();
    private AppDatabase appDatabase;


    private MutableLiveData<Location> locationLiveData = new MutableLiveData<>();
    private double longitude;
    private double latitude;
    private String address;
    private Geocoder geocoder;

    public AlertViewModel(@NonNull Application application,
                          final long alertId) {
        super(application);
        this.alertId = alertId;
        appDatabase = AppDatabase.getAppDatabase(application.getApplicationContext());

        // uncomment to switch to Database mode
        alertObservable = appDatabase.AlertModel().findOneAlert(alertId);
        // uncomment to switch to Remote repository mode
//        alertObservable = AlertRepository.getInstance().getProjectDetails("Apolline-Lille", alertId);
        this.geocoder = new Geocoder(application, Locale.getDefault());

    }

    public LiveData<Alert> getObservableProject() {
        return alertObservable;
    }


    public void setAlert(Alert alert) {
        this.alert.set(alert);
    }


    public void deleteAlert(Alert alert) {
        appDatabase.AlertModel().delete(alert);
    }

    public void insertAlert(Alert alert) {
        appDatabase.AlertModel().insertOne(alert);
    }

    public Alert getAlert(long alertId) {
        return appDatabase.AlertModel().getAlert(alertId);
    }


    public void getCurrentLocation(){
        Log.e("location","je passe ici");
        LocationListener locationListener= new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                try {
                    List<Address> results = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    StringBuilder sb = new StringBuilder("");
                    sb.append(location.getLatitude());
                    sb.append(",");
                    sb.append(location.getLongitude());
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                    Log.e("location",sb.toString() );
                    address=results.get(0).getAddressLine(0);
                    locationLiveData.postValue(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        LocationProvider locationProvider = new LocationProvider();
        locationProvider.locationUpdate(this.getApplication(),locationListener);
    }

    public LiveData<Location> getLocationLiveData() {
        return locationLiveData;
    }



    /**
     * A creator is used to inject the alert ID into the ViewModel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application application;

        private final long alertId;

        public Factory(@NonNull Application application, long alertId) {
            this.application = application;
            this.alertId = alertId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AlertViewModel(application, alertId);
        }


    }


    public String getAddress() {
        return address;
    }
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
