package test.designe.app.terminalapp.retrofit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import test.designe.app.terminalapp.models.Driver;
import test.designe.app.terminalapp.models.PinUser;
import test.designe.app.terminalapp.models.Route;
import test.designe.app.terminalapp.models.RouteHistory;
import test.designe.app.terminalapp.models.ScanInteractionResponse;
import test.designe.app.terminalapp.models.Terminal;
import test.designe.app.terminalapp.models.TerminalActivationRequest;
import test.designe.app.terminalapp.models.Transporter;
import test.designe.app.terminalapp.models.User;


public interface DriverAPI {





    @POST("/api/pinusers/login")
    Call<String> loginUser(@Body PinUser PinUser);

    @GET("/api/routesHistory/checkRouteHistoryByTerminalId={TerminalId}")
    Call<RouteHistory> getRouteHistory(@Path("TerminalId") Integer terminalId, @Header("Authorization") String BarerToken);


    @GET("api/users/getUserById={Id}")
    public Call<User> getUserById(@Path("Id") String UserId, @Header("Authorization") String BarerToken);

    @GET("api/user/files/get/profilepicture&userId={UserId}")
    Call<ResponseBody> getUserProfilePicture(@Path("UserId") Integer UserId, @Header("Authorization") String BarerToken);

    @GET("/api/terminals/getTerminalBySerialNumber={SerialNumber}")
    Call<Terminal> getTerminalBySerialNumber(@Path("SerialNumber") String SerialNumber);

    @POST("/api/terminals/add/activationrequest")
    Call<Integer> sendTerminalActivationRequest(@Body TerminalActivationRequest terminalActivationRequest);

    @GET("/api/transporters/getTransporters")
    Call<List<Transporter>> getAllTransporters();

    @GET("/api/pinusers/getDriverByPIN={PIN}")
    Call<Driver> getDriverByPIN(@Path("PIN") String PIN, @Header("Authorization") String BarerToken);

    @GET("api/routes/getAllRoutesByTransporterId={transporterID}")
    Call<List<Route>> getRoutesByTransporterId(@Path("transporterID") Integer transporterId,@Header("Authorization") String BarerToken);

    @POST("/api/terminals/updateTerminalId={TerminalId}andRouteId={RouteId}andDriverId={DriverId}")
    Call<String> startRouteHistory(@Path("TerminalId") Integer TerminalId, @Path("RouteId") Integer RouteId, @Path("DriverId") Integer DriverId,
                                    @Header("Authorization") String BarerToken);
    @POST("/api/terminals/CloseTerminalRouteHistory")
    Call<String> closeRouteHistory(@Body RouteHistory routeHistory,@Header("Authorization") String BarerToken);


    @GET("/api/routesHistory/scanInteractionTerminalId={TerminalId}&UserString={UserString}")
    Call<ScanInteractionResponse> scanCall(@Path("TerminalId") Integer terminalId, @Path("UserString") String userString, @Header("Authorization") String BarerToken);

    @GET("/api/terminals/getIsTerminalActivationProcessed/{SerialNumber}")
    Call<Boolean> checkIfActivationRequestIsPending(@Path("SerialNumber") String SerialNumber);
}
