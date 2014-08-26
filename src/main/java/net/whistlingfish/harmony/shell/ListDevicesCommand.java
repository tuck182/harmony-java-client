package net.whistlingfish.harmony.shell;

import java.util.Map.Entry;

import net.whistlingfish.harmony.HarmonyClient;

public class ListDevicesCommand extends ShellCommand {
    @Override
    public void execute(HarmonyClient harmonyClient) {
        for (Entry<Integer, String> e : harmonyClient.getDeviceLabels().entrySet()) {
            println("%d: %s", e.getKey(), e.getValue());
        }
    }
}
