package net.whistlingfish.harmony;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import net.whistlingfish.harmony.shell.ListActivitiesCommand;
import net.whistlingfish.harmony.shell.ShellCommandWrapper;
import net.whistlingfish.harmony.shell.ShowActivityCommand;

import org.jivesoftware.smack.provider.ProviderFileLoader;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.FileUtils;
import org.kohsuke.args4j.CmdLineParser;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.martiansoftware.jsap.CommandLineTokenizer;

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

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        new ListActivitiesCommand().execute(harmonyClient);
        new ShowActivityCommand().execute(harmonyClient);

        String line;
        while (true) {
            line = br.readLine();
            if (line == null || line.equals("q"))
                break;

            try {
                String[] lineArgs = CommandLineTokenizer.tokenize(line);
                ShellCommandWrapper command = new ShellCommandWrapper();
                new CmdLineParser(command).parseArgument(lineArgs);
                command.execute(harmonyClient);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("\n");
            }
        }

        br.close();

        return 0;
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
