package test.designe.app.terminalapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.IOException;
import java.util.List;

import test.designe.app.terminalapp.adapters.TransporterViewAdapter;
import test.designe.app.terminalapp.models.TerminalActivationRequest;
import test.designe.app.terminalapp.models.Transporter;
import test.designe.app.terminalapp.retrofit.DriverAPI;
import test.designe.app.terminalapp.retrofit.RetrofitService;

public class RegisterTerminalActivity extends AppCompatActivity {

    DriverAPI api;
    HandlerThread handlerThread;
    RecyclerView recyclerView;
    CircularProgressIndicator progressIndicator;
    Button cancleBtn;
    Button retryBtn;
    TextView infoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_terminal);
        api = RetrofitService.getApi();
         handlerThread = new HandlerThread("API CALL HANDLER THREAD");
         handlerThread.start();
         recyclerView = findViewById(R.id.transporterRV);
         recyclerView.setVisibility(View.INVISIBLE);
         cancleBtn = findViewById(R.id.cancleRegButton);
         retryBtn = findViewById(R.id.retryBtnReg);
         infoText = findViewById(R.id.infoTextReg);

         progressIndicator = findViewById(R.id.loadTransporters);
        loadTransporters();
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTransporters();
            }
        });
        Activity thisActivity = this ;
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(thisActivity, InitActivity.class);
                startActivity(intent);
                thisActivity.finish();
            }
        });

    }

    public void loadTransporters() {
        progressIndicator.setVisibility(View.VISIBLE);
        retryBtn.setVisibility(View.INVISIBLE);
        infoText.setText("Učitavanje podataka");
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(()-> {
            try {
                List<Transporter> transporters = api.getAllTransporters().execute().body();
                runOnUiThread(()->{
                    infoText.setVisibility(View.INVISIBLE);
                    progressIndicator.setVisibility(View.INVISIBLE);

                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new TransporterViewAdapter(transporters, this));
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

    public void sendTerminalActivationRequets(Transporter transporter)
    { String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);




        TerminalActivationRequest tar = new TerminalActivationRequest();
        tar.setSerialNumber(androidId);
        tar.setTransporterId(transporter.getId());
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(()-> {

            try {
                api.sendTerminalActivationRequest(tar).execute();
                runOnUiThread(()-> {
                    Toast.makeText(this,"Zahtjev uspješno poslat.",Toast.LENGTH_LONG).show();

                });

            } catch (IOException e) {
                runOnUiThread(()->{

                    Toast.makeText(this,"Desila se greška pri slanju zahtjeva.",Toast.LENGTH_LONG).show();
                });

            }


        });

    }
    @Override
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }
}