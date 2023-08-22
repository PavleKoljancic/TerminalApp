package test.designe.app.terminalapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import test.designe.app.terminalapp.adapters.RouteViewAdapter;
import test.designe.app.terminalapp.models.Route;
import test.designe.app.terminalapp.retrofit.DriverAPI;
import test.designe.app.terminalapp.retrofit.RetrofitService;
import test.designe.app.terminalapp.sigeltons.TerminalSingelton;
import test.designe.app.terminalapp.sigeltons.TokenManager;

public class ChooseRouteActivity extends AppCompatActivity {

    DriverAPI api;
    HandlerThread handlerThread;
    RecyclerView recyclerView;
    CircularProgressIndicator progressIndicator;

    Button retryBtn;
    TextView infoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);
        api = RetrofitService.getApi();
         handlerThread = new HandlerThread("API CALL HANDLER THREAD");
         handlerThread.start();

         recyclerView = findViewById(R.id.routeRV);
         recyclerView.setVisibility(View.INVISIBLE);

         retryBtn = findViewById(R.id.retryBtnRRoute);
         infoText = findViewById(R.id.infoTextRoute);

         progressIndicator = findViewById(R.id.loadRoutes);
        loadRoutes();
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRoutes();
            }
        });



    }

    public void loadRoutes() {
        progressIndicator.setVisibility(View.VISIBLE);
        retryBtn.setVisibility(View.INVISIBLE);
        infoText.setText("Učitavanje podataka");
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(()-> {
            try {
                List<Route> routes = api.getRoutesByTransporterId(TerminalSingelton.getTerminal().getTransporterId(), TokenManager.bearer()+TokenManager.getInstance().getToken()).execute().body();
                if(routes==null)
                    routes = new ArrayList<Route>();
                final List<Route> routesRes = routes;
                runOnUiThread(()->{
                    infoText.setVisibility(View.INVISIBLE);
                    progressIndicator.setVisibility(View.INVISIBLE);

                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new RouteViewAdapter(routesRes,this));
                });
            } catch (IOException e) {
                runOnUiThread(()->{
                    infoText.setText("Greška pri učitavanju");
                    progressIndicator.setVisibility(View.INVISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                });
            }

        });

    }

    @Override
    public void onBackPressed() {

    }



        @Override
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }
}