package blast.shell.jline;

import jline.Terminal;
import org.apache.karaf.shell.console.jline.Console;
import org.osgi.service.command.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 *
 *
 */
public class BlastConsole extends Console {
    private static final Logger log = LoggerFactory.getLogger(Console.class);

    public BlastConsole(CommandProcessor processor, InputStream in, PrintStream out, PrintStream err, Terminal term, Runnable closeCallback) throws Exception {
        super(processor, in, out, err, term, closeCallback);
    }

    Properties brandingProperties = null;

    @Override
    protected Properties loadBrandingProperties() {
        if (brandingProperties == null) {
            return super.loadBrandingProperties();
        } else {
            return brandingProperties;
        }
    }

    /**
     * Do we want to override the prompt?
     *
     * @return
     */
    @Override
    protected String getPrompt() {
        return super.getPrompt();
    }

    public void setBrandingProperties(Properties brandingProperties) {
        this.brandingProperties = brandingProperties;
    }
}
