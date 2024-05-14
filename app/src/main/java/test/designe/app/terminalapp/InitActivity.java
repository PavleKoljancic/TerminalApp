package test.designe.app.terminalapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.IOException;

import test.designe.app.terminalapp.models.Terminal;
import test.designe.app.terminalapp.retrofit.DriverAPI;
import test.designe.app.terminalapp.retrofit.RetrofitService;
import test.designe.app.terminalapp.sigeltons.TerminalSingelton;

public class InitActivity extends AppCompatActivity {

    HandlerThread handlerThread = new HandlerThread("API Call Handler Thread");

    CircularProgressIndicator loadingProgress;
    DriverAPI api;
    TextView infoText;
    Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        loadingProgress = findViewById(R.id.progressloading);
        infoText = findViewById(R.id.loadingtext);
        handlerThread.start();
        api = RetrofitService.getApi();
        retry = (Button) findViewById(R.id.retryBtn);
        getTerminalData();
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retry.setEnabled(false);
                loadingProgress.setVisibility(View.VISIBLE);
                infoText.setText("Učitavanje podataka");
                getTerminalData();
            }
        });
    }


    private void getTerminalData()
    {
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(()-> {
            String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            Terminal terminal=null;
            try {
                terminal = api.getTerminalBySerialNumber(androidId).execute().body();
                Activity thisActivity = this;
                if(terminal==null) {
                    runOnUiThread(() -> {
                        infoText.setText("Desila se greška pri povezivanju.");
                    });
                    return;
                }
                if(terminal.getId()==null)
                {    final boolean isPending = api.checkIfActivationRequestIsPending(androidId).execute().body();
                    runOnUiThread(()->{
                        if(isPending)
                        {
                            infoText.setText("Zahtjev za aktivaciju\n ovog terminal čeka obradu");
                            loadingProgress.setVisibility(View.INVISIBLE);
                            retry.setVisibility(View.VISIBLE);
                            retry.setEnabled(true);
                        }
                            else {requestActivation(thisActivity);}

                    });





                }
                else if (terminal.getIsActive())
                {   TerminalSingelton.setTerminal(terminal);
                    runOnUiThread(()-> {

                        Intent intent = new Intent(thisActivity, LoginActivity.class);
                        startActivity(intent);
                        this.finish();

                    });
                }
                else
                {
                    runOnUiThread(()->{
                        loadingProgress.setVisibility(View.INVISIBLE);
                        retry.setVisibility(View.INVISIBLE);
                        this.infoText.setText("OVAJ TERMINAL JE DEAKTIVIRAN");
                        this.infoText.setTextSize(25);
                        this.infoText.setTextColor(Color.RED);


                    });

                }
            } catch (IOException e) {
                runOnUiThread(()-> {this.infoText.setText("Greška pri povezivanju");
                    loadingProgress.setVisibility(View.INVISIBLE);
                    retry.setVisibility(View.VISIBLE);
                    retry.setEnabled(true);});

            }
        });
    }

    private void requestActivation(Activity thisActivity) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);


            builder.setMessage("Da li želite da pošaljete zahtjev za aktivaciju?")
                    .setTitle("Ovaj terminal nije aktiviran");
            builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(thisActivity, RegisterTerminalActivity.class);
                    startActivity(intent);
                    thisActivity.finish();
                }
            });
            builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    runOnUiThread(()-> {infoText.setText("Terminal nije aktiviran");
                        loadingProgress.setVisibility(View.INVISIBLE);
                        retry.setVisibility(View.VISIBLE);
                        retry.setEnabled(true);});
                }
            });
            builder.setCancelable(false);
            builder.show();

    }

    @Override
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }
}