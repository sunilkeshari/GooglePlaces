package bank.sunil.com.bank;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Sonu on 3/10/2017.
 */

public interface RetrofitInterface {
    @GET("/maps/api/place/nearbysearch/json?sensor=true&key=AIzaSyDVmWHF5P1WsbiNpQJXTg7CWMzgXNSjG-I")
    Call<Example> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
}
