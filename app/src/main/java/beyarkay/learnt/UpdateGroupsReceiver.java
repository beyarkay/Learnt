package beyarkay.learnt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class UpdateGroupsReceiver extends BroadcastReceiver {
    private ActivityMicroView rootActivity;

    public UpdateGroupsReceiver(ActivityMicroView groupViewerActivity) {
        this.rootActivity = groupViewerActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String CUSTOM_INTENT = "com.beyarkay.learnt.group_deleted";
        if (intent.getAction().equals(CUSTOM_INTENT)) {
//            rootActivity.deleteGroupView();
            rootActivity.updateDynamicsFromDB();
            rootActivity.updateUIFromDynamics(false);
        }
    }
}
