package artemiev.contact;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
//import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
//import com.google.android.gms.games.achievement.Achievements;
//import com.google.android.gms.games.multiplayer.realtime.Room;
//import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;

public class aMainMenu extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private SignInButton bSignIn;
    private Button bSignOut;
    private Button bSettings;
    private Button bPlay;

    //public WebSocketClient gameServerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast.makeText(context, text, duration).show();

       mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        bPlay = (Button) findViewById(R.id.BPlay);
        //bPlay.setOnClickListener(this);

        bSettings = (Button) findViewById(R.id.BSettings);
        //bSettings.setOnClickListener(this);

        bSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        //bSignIn.setOnClickListener(this);

        bSignOut = (Button) findViewById(R.id.sign_out_button);
        //bSignOut.setOnClickListener(this);

        //gameServerClient = new WebSocketClient();

        try {
            WebSocketClient.startup();
        } catch (Exception e) {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(aMainMenu.this);

            dlgAlert.setMessage(e.getMessage());

            dlgAlert.setTitle("Error while contacting server");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            e.printStackTrace();
        }
    }

    public int UnlockAchievement(int A) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Call a Play Games services API method, for example:
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(A));
            return 0;
        } else {
            // Alternative implementation (or warn user that they must
            // sign in to use this feature)
            return -1;
        }
    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        //Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);
       //com.google.android.gms.games.multiplayer.realtime.
        // build the room config:
        //RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        //roomConfigBuilder.setAutoMatchCriteria(am);
        //RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        //Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }

    @Override
    public void onClick(View view) {
        //Toast.makeText(getApplicationContext(), "OnClick", Toast.LENGTH_SHORT).show();
        switch (view.getId()) {
            case R.id.sign_in_button: {
                // start the asynchronous sign in flow
                mSignInClicked = true;
                mGoogleApiClient.reconnect();
                break;
            }

            case R.id.sign_out_button: {
                // sign out.
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                //mGoogleApiClient.disconnect();

                // show sign-in button, hide the sign-out button
                bSignIn.setVisibility(View.VISIBLE);
                bSignOut.setVisibility(View.GONE);
                break;
            }

            case R.id.BPlay: {
                UnlockAchievement(R.string.achievement_hello_world);
                Intent i = new Intent(aMainMenu.this, aServers.class);
                startActivity(i);
                break;
            }

            case R.id.BSettings: {
                Intent i = new Intent(aMainMenu.this, aSettings.class);
                startActivity(i);
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect(); //TODO: SIGN_IN_MODE_OPTIONAL
        Toast.makeText(getApplicationContext(), "OnStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        WebSocketClient.shutdown();
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Toast.makeText(getApplicationContext(), "OnDestroy", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        bSignIn.setVisibility(View.GONE);
        bSignOut.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "OnConnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, R.string.sign_in_other_error)) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.sign_in_failed);
            }
        }
    }
}
