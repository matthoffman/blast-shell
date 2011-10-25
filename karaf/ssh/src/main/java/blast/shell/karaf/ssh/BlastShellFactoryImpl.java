/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package blast.shell.karaf.ssh;

import blast.shell.jline.ConsoleFactory;
import jline.Terminal;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.jline.Console;
import org.apache.karaf.shell.ssh.SshTerminal;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.*;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.util.Map;

/**
 * This is copied from Karaf, and needs to be here *only* because we want to override the branding.
 * Branding isn't set in this file, but in the {@link Console}; the Console class is easy to extend to override the
 * branding, but unfortunately this class doesn't let us use an overridden Console.
 * Ideally, Karaf's ShellFactoryImpl would take a ConsoleFactory (instead of creating its own Console) and then this
 * wouldn't be necessary.
 */
public class BlastShellFactoryImpl implements Factory<Command> {
    private ConsoleFactory consoleFactory;

    @Required
    public void setConsoleFactory(ConsoleFactory consoleFactory) {
        this.consoleFactory = consoleFactory;
    }

    public Command create() {
        return new ShellImpl();
    }

    public class ShellImpl implements Command {
        private InputStream in;

        private OutputStream out;

        private OutputStream err;

        private ExitCallback callback;

        private boolean closed;

        public void setInputStream(final InputStream in) {
            this.in = in;
        }

        public void setOutputStream(final OutputStream out) {
            this.out = out;
        }

        public void setErrorStream(final OutputStream err) {
            this.err = err;
        }

        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        public void start(final Environment env) throws IOException {
            try {
                final Terminal terminal = new SshTerminal(env);
                Console console = consoleFactory.createConsole(in,
                        new PrintStream(new LfToCrLfFilterOutputStream(out), true),
                        new PrintStream(new LfToCrLfFilterOutputStream(err), true),
                        terminal,
                        new Runnable() {
                            public void run() {
                                destroy();
                            }
                        });
                // The consoleFactory already sets a lot of things in this console's session.
                // All we need to do here is set up some SSH-specific things.
                final CommandSession session = console.getSession();
                for (Map.Entry<String, String> e : env.getEnv().entrySet()) {
                    session.put(e.getKey(), e.getValue());
                }
                env.addSignalListener(new SignalListener() {
                    public void signal(Signal signal) {
                        session.put("LINES", Integer.toString(terminal.getHeight()));
                        session.put("COLUMNS", Integer.toString(terminal.getWidth()));
                    }
                }, Signal.WINCH);
                new Thread(console).start();

            } catch (Exception e) {
                throw (IOException) new IOException("Unable to start shell").initCause(e);
            }
        }

        public void destroy() {
            if (!closed) {
                closed = true;
                flush(out, err);
                close(in, out, err);
                callback.onExit(0);
            }
        }

    }

    private static void flush(OutputStream... streams) {
        for (OutputStream s : streams) {
            try {
                s.flush();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }


    // TODO: remove this class when sshd use lf->crlf conversion by default
    public class LfToCrLfFilterOutputStream extends FilterOutputStream {

        private boolean lastWasCr;

        public LfToCrLfFilterOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            if (!lastWasCr && b == '\n') {
                out.write('\r');
                out.write('\n');
            } else {
                out.write(b);
            }
            lastWasCr = b == '\r';
        }

    }


}
