package blast.shell.jline;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is ugly, but necessary for some of the REPLs. To be honest, I haven't
 * tracked through to figure out exactly why, but I'm assuming it's because the REPLs' JLine
 * implementation isn't properly figuring out how to handle the console, since it's nested
 * inside the SSH's JLine.
 * I did some debugging, but this is the best answer I've come up with; if someone else
 * knows of a way around this, let me know.
 *
 */
public class BackspaceWrappingInputStream extends InputStream {
    public InputStream delegate;

    public BackspaceWrappingInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        int c = delegate.read();
        if (c == 127) return '\b';
        else return c;
    }
}


