package javarepl.console.commands;

import javarepl.Evaluator;
import javarepl.console.ConsoleLogger;
import jline.console.completer.StringsCompleter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import static com.googlecode.totallylazy.Streams.copyAndClose;
import static com.googlecode.totallylazy.Strings.startsWith;
import static java.lang.String.format;
import static javarepl.Utils.*;

public final class AddToClasspath extends Command {
    private static final String COMMAND = ":cp";
    private final Evaluator evaluator;
    private final ConsoleLogger logger;

    public AddToClasspath(Evaluator evaluator, ConsoleLogger logger) {
        super(COMMAND + " <path> - includes given file or directory in the classpath", startsWith(COMMAND), new StringsCompleter(COMMAND));
        this.evaluator = evaluator;
        this.logger = logger;
    }

    public void execute(String expression) {
        String path = parseStringCommand(expression).second().getOrNull();
        try {
            URL url = resolveClasspath(path);

            if (isWebUrl(url)) {
                logger.info(format("Downloading %s...", path));

                File outputFile = new File(evaluator.outputDirectory(), randomIdentifier("external"));
                copyAndClose(url.openStream(), new FileOutputStream(outputFile));

                evaluator.addClasspathUrl(outputFile.toURI().toURL());
            } else {
                evaluator.addClasspathUrl(url);
            }

            logger.info(format("Added %s to classpath.", path));
        } catch (Exception e) {
            logger.error(format("Could not add %s to classpath. %s", path, e.getLocalizedMessage()));
        }
    }
}
