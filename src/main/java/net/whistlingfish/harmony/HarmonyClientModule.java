package net.whistlingfish.harmony;

import java.io.IOException;

import org.jivesoftware.smack.provider.ProviderFileLoader;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.FileUtils;

import com.google.inject.Binder;
import com.google.inject.Module;

public class HarmonyClientModule implements Module {
    @Override
    public void configure(Binder binder) {
        try {
            ProviderManager.addLoader(new ProviderFileLoader(FileUtils.getStreamForUrl(
                    "classpath:net/whistlingfish/harmony/smack-providers.xml", null)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize smack providers", e);
        }
    }
}
