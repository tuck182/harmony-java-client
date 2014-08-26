package net.whistlingfish.harmony;

/**
 * Marker interface for Harmony Hub notifications
 */
public interface HarmonyHubListener {
    void addTo(HarmonyClient harmonyClient);
    void removeFrom(HarmonyClient harmonyClient);
}
