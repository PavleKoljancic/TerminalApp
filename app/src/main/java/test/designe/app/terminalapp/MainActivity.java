package test.designe.app.terminalapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONException;

import java.io.IOException;

import retrofit2.Response;
import test.designe.app.terminalapp.models.RouteHistory;
import test.designe.app.terminalapp.models.User;
import test.designe.app.terminalapp.nfc.IdReadSubscriber;
import test.designe.app.terminalapp.nfc.ReaderNFC;
import test.designe.app.terminalapp.retrofit.DriverAPI;
import test.designe.app.terminalapp.retrofit.RetrofitService;
import test.designe.app.terminalapp.sigeltons.DriverSingleton;
import test.designe.app.terminalapp.sigeltons.RouteSingelton;
import test.designe.app.terminalapp.sigeltons.TerminalSingelton;
import test.designe.app.terminalapp.sigeltons.TokenManager;

public class MainActivity extends AppCompatActivity implements IdReadSubscriber {


    HandlerThread handlerThread;

    RouteHistory currentRouteHistory;

    CircularProgressIndicator mainProgress;

    Button exitButton;
    TextView routText;
    Handler handler;
    DriverAPI api;


    TextView canDriveTextView;
    ImageView aprovalImageView;
    TextView UserNametext;
    ShapeableImageView userImage;
    TextView errorText;
    Button retryBtn;



    ReaderNFC readerNFC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Moram srediti ROUTE HISTORY PRIJE SVEGA
        //Moram reci da se nesto uradi sa slikom korinka ako se ne desi next load
        //Da necu imapti isti problem ko sto sam imo sa terminalom zbog praznog body-a response sad sa RouteHistory
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.api = RetrofitService.getApi();
        this.handlerThread = new HandlerThread("API Call Thread");
        this.handlerThread.start();


        errorText = findViewById(R.id.errorText);
        retryBtn = findViewById(R.id.retryBtnLoadRH);


        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler(handlerThread.getLooper());
                errorText.setVisibility(View.INVISIBLE);
                retryBtn.setVisibility(View.INVISIBLE);
                handler.post(() -> setUpRouteHistory());

            }
        });

        exitButton = (Button) findViewById(R.id.exitButton);
        routText = (TextView) findViewById(R.id.routeText);
        routText.setText(RouteSingelton.getRoute().getName());
        canDriveTextView = (TextView) findViewById(R.id.canDriveText);
        UserNametext = findViewById(R.id.UserName);
        userImage = findViewById(R.id.userImage);
        aprovalImageView = (ImageView) findViewById(R.id.statusImage);
        mainProgress = (CircularProgressIndicator) findViewById(R.id.progressMain);
        handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {

            readerNFC = new ReaderNFC(handlerThread, this);
            readerNFC.subscribeToIdRead(this);
            setUpRouteHistory();

        });
        final  Activity parent =this;

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                final View customLayout = getLayoutInflater().inflate(R.layout.logout_dialog, null);
                builder.setView(customLayout)
                        .setPositiveButton("Potvrdi PIN", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText PinEnter = customLayout.findViewById(R.id.PINEnterDialog);
                                String PINEntred =PinEnter.getText().toString();
                                if(PINEntred.equals(DriverSingleton.getDriver().getPin()))
                                {
                                   clearUserUI();
                                    mainProgress.setVisibility(View.VISIBLE);
                                    Handler handler1 =new Handler(handlerThread.getLooper());
                                    handler1.post(
                                            ()->{

                                                try {
                                                    RouteHistory current =  api.getRouteHistory(TerminalSingelton.getTerminal().getId(), TokenManager.bearer()+TokenManager.getInstance().getToken()).execute().body();
                                                    String closeResult = api.closeRouteHistory(current, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
                                                    if("RouteHistory successufully closed".equals(closeResult))
                                                    {
                                                        runOnUiThread(()-> {
                                                           mainProgress.setVisibility(View.INVISIBLE);
                                                           Intent intent = new Intent(parent,InitActivity.class);
                                                           startActivity(intent);
                                                           parent.finish();
                                                        });
                                                    }
                                                } catch (IOException e) {
                                                    runOnUiThread(()-> {
                                                        mainProgress.setVisibility(View.INVISIBLE);
                                                    });
                                                }
                                                
                                            }
                                    );
                                   
                                }

                               
                            }
                        }).setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TextView infoText = customLayout.findViewById(R.id.infoDialogText);
                                    
                            }
                        });
                builder.setCancelable(false);
                builder.show();

            }
        });


    }

    private void setUpRouteHistory() {
        try {
            RouteHistory routeHistory = api.getRouteHistory(TerminalSingelton.getTerminal().getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();

            if (routeHistory != null) {
                Response<String> response = api.closeRouteHistory(routeHistory, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute();
            }

            String result = null;
            try {
                result = api.startRouteHistory(TerminalSingelton.getTerminal().getId(), RouteSingelton.getRoute().getId(), TokenManager.getInstance().getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (result != null && result.equals(RouteSingelton.getRoute().getName())) {

                runOnUiThread(() -> clearUserUI());
                readerNFC.enableReaderMode();
            } else {
                runOnUiThread(() -> {

                    errorText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);

                });
            }
        } catch (IOException e) {
            runOnUiThread(() -> {
                errorText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            });
        }
    }

    private void clearUserUI() {



        userImage.setVisibility(View.INVISIBLE);
        canDriveTextView.setVisibility(View.INVISIBLE);
        aprovalImageView.setVisibility(View.INVISIBLE);
        UserNametext.setText("");


    }


    private void checkUser(String UserId) {


        User user = null;
        try {
            user = api.getUserById(UserId, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
        } catch (IOException e) {

        }
        byte[] pictureBytes = null;
        if (user != null && user.getPictureHash() != null) {

            try {
                pictureBytes = api.getUserProfilePicture(user.getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body().bytes();

            } catch (IOException e) {

            }
        }
        String ticketNameResult = null;
        if (user != null) {
            try {
                ticketNameResult = api.scanCall(TerminalSingelton.getTerminal().getId(), user.getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
            } catch (IOException e) {

            }
        }
        final User result = user;
        final byte[] pBytes = pictureBytes;
        final String ticketName = ticketNameResult;

        runOnUiThread(() ->
        {
            mainProgress.setVisibility(View.INVISIBLE);

            displayUserData(result, pBytes, ticketName);


        });
    }

    public void displayUserData(User result, byte[] pBytes, String ticketName) {
        if (result == null) {
            this.userNotFound();
        } else {
            userFound(result, pBytes, ticketName);
        }
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            readerNFC.enableReaderMode();

        });
    }

    private void userFound(User result, byte[] pBytes, String ticketName) {
        UserNametext.setText(result.getFirstName() + " " + result.getLastName());

        if (pBytes != null) {
            //Found and has picture
            userImage.setImageBitmap(BitmapFactory.decodeByteArray(pBytes, 0, pBytes.length));
        } else {
            //Found doesnt have picture
            userImage.setImageDrawable(getResources().getDrawable(R.drawable.no_profile_picture));
            userImage.setVisibility(View.VISIBLE);
        }

        userImage.setVisibility(View.VISIBLE);
        canDriveTextView.setVisibility(View.VISIBLE);
        if (ticketName != null && (!ticketName.isEmpty())) {

            canDriveTextView.setTextColor(Color.GREEN);
            canDriveTextView.setText(ticketName);

            aprovalImageView.setImageDrawable(getResources().getDrawable(R.drawable.aproved));

        } else {

            canDriveTextView.setTextColor(Color.RED);
            canDriveTextView.setText("Nedovoljno kredita");

            aprovalImageView.setImageDrawable(getResources().getDrawable(R.drawable.rejected));

        }
        aprovalImageView.setVisibility(View.VISIBLE);
    }

    public void userNotFound() {
        UserNametext.setText("Korisnik nije pronađen");
        canDriveTextView.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onIdRead(Integer id) {

        if (id > 0){
            readerNFC.disableReaderMode();
            runOnUiThread(() ->{
                clearUserUI();
                mainProgress.setVisibility(View.VISIBLE);
            } );
            handler.post(() -> {

                checkUser("" + id);
            });}
    }


    @Override
    public void onDestroy() {
        this.handlerThread.quit();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}