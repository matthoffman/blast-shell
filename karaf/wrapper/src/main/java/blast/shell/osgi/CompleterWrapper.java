package blast.shell.osgi;

import blast.shell.Completer;

import java.util.List;

/**
 *
 *
 */
public class CompleterWrapper implements org.apache.felix.karaf.shell.console.Completer {

    blast.shell.Completer blastCompleter;

    public CompleterWrapper(blast.shell.Completer blastCompleter) {
        this.blastCompleter = blastCompleter;
    }

    public CompleterWrapper() {
    }


    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        return blastCompleter.complete(buffer, cursor, candidates);
    }

    public void setBlastCompleter(Completer blastCompleter) {
        this.blastCompleter = blastCompleter;
    }
}
