package net.whistlingfish.harmony;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Activity.Status;

public abstract class ActivityStatusListener implements HarmonyHubListener {  
    public abstract void activityStatusChanged(Activity activity, Status status);

    @Override
    public void addTo(HarmonyClient harmonyClient) {
        harmonyClient.addListener(this);
    }

    @Override
    public void removeFrom(HarmonyClient harmonyClient) {
        harmonyClient.removeListener(this);
    }
}
