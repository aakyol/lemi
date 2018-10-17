package aa_wearable.aa_apps.lemi;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Created by aakyo on 10/10/2016.
 */

public class MessageService extends WearableListenerService {

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate()
    {
        GoogleApiClient.Builder clientBuilder = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API);
        mApiClient = clientBuilder.build();
        mApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                System.out.println("Connection failed. " + connectionResult.toString() + ". Retrying...");
                mApiClient.connect();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                mApiClient.connect();
                System.out.println();
            }
        }).start();
        sendMessage("","Hello");

        /*
        new Thread()
        {

            Application launcher code.
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                System.out.println("Main UI (onActivityResult): current package name: "
                        + packageInfo.loadLabel(pm).toString().toLowerCase());
                if(packageInfo.loadLabel(pm).toString().toLowerCase().equals(recognizedAppName.toLowerCase()))
                {
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                    startActivity( LaunchIntent );
                }
            }

        }.start(); */
    }

    private void sendMessage(final String path, final String text) {
        new Thread()
        {
            public void run()
            {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                Node connectedNode = nodes.getNodes().get(0);
                if (connectedNode != null && mApiClient != null && mApiClient.isConnected())

                {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, connectedNode.getId(), path, text.getBytes()).await();
                }
            }
        }.start();
    }

    @Override
    public void onConnectedNodes (List<Node> connectedNodes)
    {
        System.out.println("Connection succesful. First node: " + connectedNodes.get(0).getDisplayName().toString());
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        System.out.println("Message received: " + messageEvent.getPath());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid)
    {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mApiClient.disconnect();
    }
}

