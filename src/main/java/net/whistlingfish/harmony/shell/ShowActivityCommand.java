package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.config.Activity;

public class ShowActivityCommand extends ShellCommand {
    @Override
    public void execute(HarmonyClient harmonyClient) {
        Activity activity = harmonyClient.getCurrentActivity();
        println("%d: %s", activity.getId(), activity.getLabel());
    }
}
