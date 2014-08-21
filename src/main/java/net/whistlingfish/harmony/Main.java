package net.whistlingfish.harmony;

import java.io.IOException;

import javax.inject.Inject;

import org.jivesoftware.smack.provider.ProviderFileLoader;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.FileUtils;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import static java.lang.String.format;
import static java.lang.System.out;

public class Main {
    @Inject
    private HarmonyClient harmonyClient;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new MainModule());
        Main mainObject = new Main();
        injector.injectMembers(mainObject);
        System.exit(mainObject.execute(args));
    }

    public int execute(String[] args) throws Exception {
        harmonyClient.connect(args[0], args[1], args[2]);
        println(harmonyClient.getConfig());
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private void println(String fmt, Object... args) {
        out.println(format(fmt, args));
    }

    public static class MainModule implements Module {
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
}
