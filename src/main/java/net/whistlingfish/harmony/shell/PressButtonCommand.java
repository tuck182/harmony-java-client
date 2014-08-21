package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;

import org.kohsuke.args4j.Argument;

public class PressButtonCommand extends ShellCommand {
    @Argument(required = true)
    private String button;

    @Override
    public void execute(HarmonyClient harmonyClient) {
        harmonyClient.pressButton(button);
    }

}
