package co.edu.unal.apiandroidfrutas.interfaces;

import java.util.List;

import co.edu.unal.apiandroidfrutas.models.Producto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ProductoAPI {
    @GET("cb3g-6x28.json")
    //public Call<Producto> find(@Path("name") String name);
    Call<List<Producto>> find(@Query("cultivos") String cultivos,
                              @Query("a_o") String a_o);
}
