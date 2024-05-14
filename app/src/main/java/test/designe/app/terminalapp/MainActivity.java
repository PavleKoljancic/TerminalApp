package test.designe.app.terminalapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Response;
import test.designe.app.terminalapp.models.RouteHistory;
import test.designe.app.terminalapp.models.ScanInteractionResponse;
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
    TextView UserNametext;
    ShapeableImageView userImage;
    TextView errorText;
    Button retryBtn;

    Button scanQRBtn;


    ReaderNFC readerNFC;

    ScanInteractionResponse lastValidInteraction = null;
    byte [] lastValidPicture = null;

    long lastScanTime = 0;



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

        mainProgress = (CircularProgressIndicator) findViewById(R.id.progressMain);
        scanQRBtn = (Button) findViewById(R.id.qr_code_btn);

        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {

            readerNFC = new ReaderNFC(handlerThread, this);
            readerNFC.subscribeToIdRead(this);
            setUpRouteHistory();

        });
        final Activity parent = this;

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
                                String PINEntred = PinEnter.getText().toString();
                                if (PINEntred.equals(DriverSingleton.getDriver().getPin())) {
                                    clearUserUI();
                                    mainProgress.setVisibility(View.VISIBLE);
                                    Handler handler1 = new Handler(handlerThread.getLooper());
                                    handler1.post(
                                            () -> {

                                                try {
                                                    RouteHistory current = api.getRouteHistory(TerminalSingelton.getTerminal().getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
                                                    String closeResult = api.closeRouteHistory(current, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
                                                    if ("RouteHistory successufully closed".equals(closeResult)) {
                                                        runOnUiThread(() -> {
                                                            mainProgress.setVisibility(View.INVISIBLE);
                                                            Intent intent = new Intent(parent, InitActivity.class);
                                                            startActivity(intent);
                                                            parent.finish();
                                                        });
                                                    }
                                                } catch (IOException e) {
                                                    runOnUiThread(() -> {
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
        UserNametext.setText("");


    }


    private void checkUser(String UserString) {
        if((lastScanTime+300*1000)>System.currentTimeMillis()&&lastValidInteraction!=null && Integer.parseInt(UserString.split("\\.")[0])==lastValidInteraction.getUser().getId())
        {
            runOnUiThread(() ->
            {
                mainProgress.setVisibility(View.INVISIBLE);

                displayUserData(lastValidInteraction, lastValidPicture);


            });
            return;
        }
        ScanInteractionResponse interactionResponse = null;
        User user=null;
        Response<ScanInteractionResponse> responseToCall;
        try {
            responseToCall = api.scanCall(TerminalSingelton.getTerminal().getId(), UserString, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute();
            interactionResponse = responseToCall.body();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(interactionResponse!=null)
            user = interactionResponse.getUser();


        byte[] pictureBytes = null;
        if (user != null && user.getPictureHash() != null) {

            try {
                Response<ResponseBody> responseInner = api.getUserProfilePicture(user.getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute();
                if(responseInner.isSuccessful()&&responseInner.body()!=null)
                    pictureBytes = responseInner.body().bytes();
            } catch (IOException e) {

            }
        }

        final byte[] pBytes = pictureBytes;
        final ScanInteractionResponse response=interactionResponse;
        runOnUiThread(() ->
        {
            mainProgress.setVisibility(View.INVISIBLE);

            displayUserData(response, pBytes);


        });
    }

    public void displayUserData(ScanInteractionResponse response, byte[] pBytes) {
        if (response == null||response.getUser()==null) {
            this.userNotFound();
        }
        else if(!response.isAuthSuccess())
        {
            authFail();
        }
        else {

            userFound(response, pBytes);
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

    private void userFound(ScanInteractionResponse response, byte[] pBytes) {
        UserNametext.setText(response.getUser().getFirstName() + " " + response.getUser().getLastName());

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
        if (response.getTicketName() != null && (!response.getTicketName().isEmpty())) {

            canDriveTextView.setTextColor(Color.parseColor("#4bae4f"));
            userImage.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#4bae4f")));
            canDriveTextView.setText(response.getTicketName());


            lastValidInteraction = response;
            lastValidPicture = pBytes;
            lastScanTime = System.currentTimeMillis();


        } else {

            canDriveTextView.setTextColor(Color.RED);
            canDriveTextView.setTextColor(Color.parseColor("#FF8E0409"));
            userImage.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF8E0409")));
            canDriveTextView.setText("Nedovoljno kredita");



        }

    }

    public void userNotFound() {
        UserNametext.setText("Korisnik nije pronađen");
        canDriveTextView.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }

    public void authFail() {
        UserNametext.setText("Greška pri autentikaciji");
        canDriveTextView.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onUserStringRead(String UserString) {

        if (UserString.split("\\.").length==2) {
            readerNFC.disableReaderMode();
            runOnUiThread(() -> {
                clearUserUI();
                mainProgress.setVisibility(View.VISIBLE);
            });
            handler.post(() -> {

                checkUser(UserString);
            });
        }
    }


    @Override
    public void onDestroy() {
        this.handlerThread.quit();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    private void scanCode() {
        CameraManager cManager = (CameraManager) this.getSystemService(this.CAMERA_SERVICE);

        try {
            for(final String cameraId : cManager.getCameraIdList()){
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT)

                {
                    ScanOptions options = new ScanOptions();
                    options.setBeepEnabled(true);
                    options.setOrientationLocked(true);
                    options.setCameraId(Integer.parseInt(cameraId));
                    options.setCaptureActivity(CaptureAct.class);

                    barLauncher.launch(options);
                    return;

                }
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }




    }


    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult( new ScanContract() , result ->  {



        if(result.getContents()!=null)
        {
            handler.post(() -> {

                checkUser(result.getContents());
            });

        }
        else
        {
            userNotFound();
        }

    });

}