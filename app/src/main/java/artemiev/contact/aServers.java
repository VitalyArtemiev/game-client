package artemiev.contact;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class aServers extends AppCompatActivity
        implements MyEventListener {

    private LinearLayout serverList;
    private ProgressBar progressBar;
    private EditText textConnection;
    private Button bNewServer;

    public ArrayList<Room> rooms;

    private CoordinatorClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_servers);
        serverList = (LinearLayout) findViewById(R.id.LLServers);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        textConnection = (EditText) findViewById(R.id.textConnection);
        textConnection.setVisibility(View.INVISIBLE);

        bNewServer = (Button) findViewById(R.id.BNewServer);
        bNewServer.setOnClickListener(bServerClick);

        WebSocketClient.setEventListener(this);

        if (WebSocketClient.getMode() != WebSocketClient.ClientMode.mCoordinator) {
            try {
                WebSocketClient.changeMode(WebSocketClient.ClientMode.mCoordinator);
            }
            catch (Exception e) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(aServers.this);

                dlgAlert.setMessage(e.getMessage());

                dlgAlert.setTitle("Error while contacting server");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

                e.printStackTrace();
            }
        }

        client = (CoordinatorClient) WebSocketClient.client;
        client.fetchRoomList();
        //ArrayList ButtonArray = new ArrayList(8);
        //(Button) ButtonArray.get(0)
        //ButtonArray.add(btn)
    }

    private final View.OnClickListener bServerClick = new View.OnClickListener() {
        public void onClick(View v){
            //setContentView(R.layout.activity_a_servers);
            //Intent i = new Intent(this, aServers.class);
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(aServers.this);

            switch (v.getId()) {
                case R.id.BNewServer: {
                    try {
                        client.createRoom();
                        WebSocketClient.pingServer();
                        //WebSocketClient.changeMode(WebSocketClient.ClientMode.mGameServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case R.id.scrollView: {
                    progressBar.setVisibility(View.VISIBLE);
                    textConnection.setVisibility(View.INVISIBLE);
                    break;
                }
                case 500: dlgAlert.setMessage("1"); break;
                case 501: dlgAlert.setMessage("2"); break;
                case 502: Intent i = new Intent(aServers.this, aGameRoom.class);
                          startActivity(i);
                          dlgAlert.setMessage("3"); break;
                default:  dlgAlert.setMessage(">3"); break;
            }

            dlgAlert.setTitle("App Title");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }
    };

    @Override
    public void onEventCompleted(final Object obj) {
        runOnUiThread(new Runnable() {//Todo: maybe use handler/messages instead
            @Override
            public void run() {
                if (obj instanceof ArrayList) {
                    try {
                        rooms = (ArrayList<Room>) obj;
                        int ID = 500;
                        for (Room r : rooms) {
                            Button btn = new Button(aServers.this);
                            btn.setId(ID);
                            btn.setText(r.Name + "  " + Integer.toString(r.playerCount) + '/' + Integer.toString(r.playerLimit));

                            btn.setOnClickListener(bServerClick);
                            serverList.addView(btn);

                            ID++;
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        textConnection.setVisibility(View.INVISIBLE);

                    } catch (ClassCastException e) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(aServers.this);

                        dlgAlert.setMessage("ArrayList generic is not of type <Room>");

                        dlgAlert.setTitle("Error while passing server list");
                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    }
                }
            }
        });
    }

    @Override
    public void onEventFailed(Object obj) {
        textConnection.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        String err;

        if (obj instanceof String) {
            err = (String) obj;
        }
        else {
            return;
        }

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(aServers.this);

        dlgAlert.setMessage(err);

        dlgAlert.setTitle("Error while contacting server");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
