package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;
import static java.lang.String.format;
import static java.lang.System.out;

public class GetConfigCommand extends ShellCommand {
    @Override
    public void execute(HarmonyClient harmonyClient) {
        println(harmonyClient.getConfig());
    }

    private void println(String fmt, Object... args) {
        out.println(format(fmt, args));
    }
}
