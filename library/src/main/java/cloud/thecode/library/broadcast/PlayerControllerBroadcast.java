package cloud.thecode.library.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cloud.thecode.library.radio.RadioManager;
import cloud.thecode.library.radio.RadioPlayerService;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class PlayerControllerBroadcast extends BroadcastReceiver{

    /**
     * This receiver receives STOP controlling between player services
     * For instances, If MediaPlayerService is running and playing stream
     * and then RadioPlayerService requested play radio stream, Service sends broadcast
     * that stop MediaPlayerService. And If RadioPlayerService is running and
     * playing radio stream, RadioPlayerService will be stopped here when MediaPlayerService
     * requested play media. This is the way we communicate between services to stop
     * each other.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isRadioServiceBinded = RadioManager.getService() == null ? false : true;

        String action = intent.getAction();

        if(action.equals(RadioPlayerService.NOTIFICATION_INTENT_PLAY_PAUSE)
                && isRadioServiceBinded){
            if(RadioManager.getService().isPlaying())
                RadioManager.getService().stop();
            else
                RadioManager.getService().resume();
        }else if(action.equals(RadioPlayerService.NOTIFICATION_INTENT_CANCEL)
                && isRadioServiceBinded){
            RadioManager.getService().stopFromNotification();
        }

    }
}
