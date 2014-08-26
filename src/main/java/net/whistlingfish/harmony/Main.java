package net.whistlingfish.harmony;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.inject.Inject;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.shell.ShellCommandWrapper;

import org.kohsuke.args4j.CmdLineParser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.martiansoftware.jsap.CommandLineTokenizer;

import static java.lang.String.format;

public class Main {
    @Inject
    private HarmonyClient harmonyClient;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new HarmonyClientModule());
        Main mainObject = new Main();
        injector.injectMembers(mainObject);
        System.exit(mainObject.execute(args));
    }

    public int execute(String[] args) throws Exception {
        harmonyClient.addListener(new ActivityChangeListener() {
            @Override
            public void activityStarted(Activity activity) {
                System.out.println(format("activity changed: [%d] %s", activity.getId(), activity.getLabel()));
            }
        });
        harmonyClient.connect(args[0], args[1], args[2]);

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

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
}
