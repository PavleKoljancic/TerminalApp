package test.designe.app.terminalapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONException;

import java.io.IOException;

import retrofit2.Response;
import test.designe.app.terminalapp.models.Driver;
import test.designe.app.terminalapp.models.PinUser;
import test.designe.app.terminalapp.retrofit.DriverAPI;
import test.designe.app.terminalapp.retrofit.RetrofitService;
import test.designe.app.terminalapp.sigeltons.DriverSingleton;
import test.designe.app.terminalapp.sigeltons.TerminalSingelton;
import test.designe.app.terminalapp.sigeltons.TokenManager;


public class LoginActivity extends AppCompatActivity {


    HandlerThread handlerThread = new HandlerThread("API Call Handler Thread");
    Button enterButton;
    TextView outPut;
    EditText pinInput;
    CircularProgressIndicator loginProgress;

    DriverAPI api;

    TextView informText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        handlerThread.start();
        enterButton = (Button) findViewById(R.id.PinButton);
        outPut = (TextView) findViewById(R.id.output);
        pinInput = (EditText) findViewById(R.id.enterPin);
        loginProgress = (CircularProgressIndicator) findViewById(R.id.progressLogin);
        informText = (TextView) findViewById(R.id.informtext);


        api = RetrofitService.getApi();
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButtonClick();

            }
        });
    }

    @Override
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }

    public void loginButtonClick() {
        enterButton.setEnabled(false);
        String PIN = pinInput.getText().toString();
        if (PIN.isEmpty() || PIN.length() != 6) {

            informText.setText("PIN mora da sadrži 6 brojeva!");
            enterButton.setEnabled(true);
            return;
        }


        Handler handler = new Handler(handlerThread.getLooper());
        loginProgress.setVisibility(View.VISIBLE);
        handler.post(()->loginCall(PIN));
    }


    public void loginCall(final String PIN){
        try {
            PinUser pinUser = new PinUser();
            pinUser.setPin(PIN);
            Response<String> response = api.loginUser(pinUser).execute();
            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                TokenManager.setToken(response.body());
               final Driver driver;
                if("DRIVER".equals(TokenManager.getInstance().getRole())) {
                    Response<Driver> res = api.getDriverByPIN(PIN, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute();
                    driver = res.body();
                }else driver = null;
                runOnUiThread(() -> {

                        if(driver!=null&& TerminalSingelton.getTerminal().getTransporterId()==driver.getTransporterId()) {
                            DriverSingleton.setDriver(driver);
                            Intent i = new Intent(this,ChooseRouteActivity.class);
                            startActivity(i);
                            this.finish();
                        }
                        else {informText.setText("Neuspješna prijava");}


                });
            } else
                runOnUiThread(() ->
                {
                    informText.setText("Neuspješna prijava");

                });
        } catch (IOException | JSONException e) {
            runOnUiThread(() ->
            {
                informText.setText("Grška pri povezivanju");

            });

        } finally {
            runOnUiThread(() -> {
                loginProgress.setVisibility(View.INVISIBLE);
                enterButton.setEnabled(true);
            });
        }
    }
}