package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;

public class StartActivityCommand extends ShellCommand {
    private String activity;

    @Override
    public void execute(HarmonyClient harmonyClient) {
        try {
            harmonyClient.startActivity(Integer.parseInt(activity));
        } catch (NumberFormatException e) {
            harmonyClient.startActivityByName(activity);
        }
    }
}
