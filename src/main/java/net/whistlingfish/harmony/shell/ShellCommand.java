package net.whistlingfish.harmony.shell;

import net.whistlingfish.harmony.HarmonyClient;


public abstract class ShellCommand {
    public abstract void execute(HarmonyClient harmonyClient);
}
