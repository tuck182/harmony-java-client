package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;
import static java.lang.String.format;
import static java.lang.System.out;


public abstract class ShellCommand {
    public abstract void execute(HarmonyClient harmonyClient);

    protected void println(String fmt, Object... args) {
        out.println(format(fmt, args));
    }
}
