package aa_wearable.aa_apps.lemi;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class MainActivity extends WearableActivity {

    private final int REQ_CODE_SPEECH_LEMI = 100;
    private final int REQ_CODE_SPEECH_CALL = 101;
    private final int REQ_CODE_SPEECH_TEXT = 102;
    private final int REQ_CODE_SPEECH_LAUNCH = 103;
    private final int REQ_CODE_SPEECH_NOTES = 104;
    String recognizedAppName = null;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    public GoogleApiClient mGoogleApiClient;
    public Node connectedNode;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        //AUTO-GEN Do not delete.
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        //AUTO-GEN Do not delete.

        GoogleApiClient.Builder clientBuilder = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API);
        mGoogleApiClient = clientBuilder.build();
        startService(new Intent(this, MessageCarrier.class));
        //System.out.println("App starts finding node...");
        //new AsyncNodeFetcher().execute("","","");
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.connect();
            }
        }).start();
        */

        //Speech buttons
        Button speechButton = (Button) findViewById(R.id.speechButton);

        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechPrompter("");
                //Start checking result and act accordingly
                //TODO DEBUG START
                sendMessage("TEST","Test message".getBytes());
                //TODO DEBUG END
            }
        });
    }

    /*
    public void findNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Node search begins...");
                System.out.println("Timer start: " + System.currentTimeMillis());
                NodeApi.GetConnectedNodesResult nodeList = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                System.out.println("Timer end: " + System.currentTimeMillis());
                for (Node node : nodeList.getNodes()) {
                        connectedNode = node;
                        Button speakButton = (Button) findViewById(R.id.speechButton);
                        speakButton.setEnabled(true);
                        break;
                }
                System.out.println("Thread for connection establishment to mobile terminated. Exiting...");
            }
        }).start();
    }
    */

    public void speechPrompter(String speechType) //Google APU voice recognition
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //Open intent for recognition
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); //Get the local language for recognition
        switch (speechType) {
            case "": {
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_LEMI);
                } catch (ActivityNotFoundException a) {
                    System.out.println("MainUI (speechPrompter): Exception caught. Details: " + a.getMessage().toString());
                }
            }

            case "call": {
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_CALL);
                } catch (ActivityNotFoundException a) {
                    System.out.println("MainUI (speechPrompter): Exception caught. Details: " + a.getMessage().toString());
                }
            }

            case "text": {
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_TEXT);
                } catch (ActivityNotFoundException a) {
                    System.out.println("MainUI (speechPrompter): Exception caught. Details: " + a.getMessage().toString());
                }
            }

            case "launch": {
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_LAUNCH);
                } catch (ActivityNotFoundException a) {
                    System.out.println("MainUI (speechPrompter): Exception caught. Details: " + a.getMessage().toString());
                }
            }

            case "take notes": {
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_NOTES);
                } catch (ActivityNotFoundException a) {
                    System.out.println("MainUI (speechPrompter): Exception caught. Details: " + a.getMessage().toString());
                }
            }
        }
    }

    private void sendMessage(String path, byte[] data) //TODO: Variable named "Path" Type "String": like a message code. Assign sometime
    {
        if (connectedNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, connectedNode.getId(), path, data).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                System.out.println("Failed to send message. Status code: " + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    public void speechProcessor(String result)
    {
        result = result.replace(" ", "").toLowerCase();
        switch (result){
            case "call":{speechPrompter("call");}
            case "text":{speechPrompter("text");}
            case "launch": {speechPrompter("launch");}
            case "take notes":{speechPrompter("take notes");}
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            /* TODO: Delete after being sure that this code snippet is not required
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            */
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            /* TODO: Delete after being sure that this code snippet is not required
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            */
            mClockView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_LEMI: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    recognizedAppName = result.get(0);

                    System.out.println("MainUI (onActivityResult): result of speech recognition: " + result.get(0));
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText(result.get(0));
                    speechProcessor(result.get(0));
                }
                else
                {
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText("Try Again...");
                }
                break;
            }

            case REQ_CODE_SPEECH_CALL: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    recognizedAppName = result.get(0);

                    System.out.println("MainUI (onActivityResult): result of speech recognition: " + result.get(0));
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText(result.get(0));
                    //Send to WEAR with the message format CALL|<result text here>
                }
                else
                {
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText("Try Again...");
                }
                break;
            }

            case REQ_CODE_SPEECH_TEXT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    recognizedAppName = result.get(0);

                    System.out.println("MainUI (onActivityResult): result of speech recognition: " + result.get(0));
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText(result.get(0));
                    //Send to WEAR with the message format TEXT|<result text here>
                }
                else
                {
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText("Try Again...");
                }
                break;
            }

            case REQ_CODE_SPEECH_LAUNCH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    recognizedAppName = result.get(0);

                    System.out.println("MainUI (onActivityResult): result of speech recognition: " + result.get(0));
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText(result.get(0));
                    //Send to WEAR with the message format LAUNCH|<result text here>
                }
                else
                {
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText("Try Again...");
                }
                break;
            }

            case REQ_CODE_SPEECH_NOTES: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    recognizedAppName = result.get(0);

                    System.out.println("MainUI (onActivityResult): result of speech recognition: " + result.get(0));
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText(result.get(0));
                    //Send to WEAR with the message format NOTES|<result text here>
                }
                else
                {
                    TextView resultText = (TextView) findViewById(R.id.speechResult);
                    resultText.setText("Try Again...");
                }
                break;
            }

        }
    }

    /*
    private class AsyncNodeFetcher extends AsyncTask <String, String, String>
    {
        @Override
        protected String doInBackground(String... params) {
            while(!mGoogleApiClient.isConnected());
            if (mGoogleApiClient.isConnected()) {
                System.out.println("Timer start: " + System.currentTimeMillis());
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                System.out.println("Timer end: " + System.currentTimeMillis());
                for (Node node : nodes.getNodes()) {
                    connectedNode = node;
                    break;
                }
                return "Done";
            } else {
                System.out.println("Connection failed.");
                return "Fail";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Done")) {
                Button speechButton = (Button) findViewById(R.id.speechButton);
                speechButton.setEnabled(true);
            }
            else
            {
                System.out.println("Failed to activate button.");
            }
        }
    }
    */
}
