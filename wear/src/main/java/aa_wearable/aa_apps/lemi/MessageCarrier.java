package aa_wearable.aa_apps.lemi;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Created by aakyo on 08/11/2016.
 */

public class MessageCarrier extends WearableListenerService{

    private Node connectedNode;

    @Override
    public void onConnectedNodes(List<Node> connectedNodes)
    {
        connectedNode = connectedNodes.get(0);
        System.out.println("DEBUG");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        System.out.println(messageEvent.getData().toString());
    }


}
