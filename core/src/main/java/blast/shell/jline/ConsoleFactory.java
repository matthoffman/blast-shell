package blast.shell.jline;

import jline.Terminal;
import org.apache.karaf.shell.console.jline.Console;

import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 *
 */
public interface ConsoleFactory {

    public Console createConsole(InputStream in, PrintStream out, PrintStream err, Terminal terminal, Runnable closeCallback) throws Exception;
}
