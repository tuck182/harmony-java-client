package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.config.Activity;

public class ListActivitiesCommand extends ShellCommand {
    @Override
    public void execute(HarmonyClient harmonyClient) {
        for (Activity activity : harmonyClient.getConfig().getActivities()) {
            println("%s: %s", activity.getId(), activity.getLabel());
        }
    }
}
