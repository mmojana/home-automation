package ch.mojanam.shadowtoinflux.clients;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface InfluxClient {

    @POST("/api/v2/write")
    Call<String> write(@Query("org") String organization,
                       @Query("bucket") String bucket,
                       @Query("precision") String precision,
                       @Header("Authorization") String authorization,
                       @Body String metrics);

}
