package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;

import org.kohsuke.args4j.Argument;

public class StartActivityCommand extends ShellCommand {
    @Argument(required = true)
    private String activity;

    @Override
    public void execute(HarmonyClient harmonyClient) {
        try {
            harmonyClient.startActivity(Integer.parseInt(activity));
        } catch (NumberFormatException e) {
            harmonyClient.startActivityByName(activity);
        }
        println("Activity %s started", activity);
    }
}
