package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;

public class GetConfigCommand extends ShellCommand {
    @Override
    public void execute(HarmonyClient harmonyClient) {
        println(harmonyClient.getConfig().toJson());
    }
}
