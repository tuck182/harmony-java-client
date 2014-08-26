package net.whistlingfish.harmony;

import net.whistlingfish.harmony.config.Activity;

public abstract class ActivityChangeListener implements HarmonyHubListener {
    public abstract void activityStarted(Activity activity);

    @Override
    public void addTo(HarmonyClient harmonyClient) {
        harmonyClient.addListener(this);
    }

    @Override
    public void removeFrom(HarmonyClient harmonyClient) {
        harmonyClient.removeListener(this);
    }
}
