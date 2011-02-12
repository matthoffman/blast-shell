/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package blast.shell.jline;

import blast.shell.CommandRegistry;
import jline.Terminal;
import org.apache.karaf.shell.console.jline.TerminalFactory;
import org.apache.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;
import org.osgi.service.command.CommandProcessor;
import org.osgi.service.command.CommandSession;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

public class ConsoleFactory {
    private static final Logger log = Logger.getLogger(ConsoleFactory.class);

    private TerminalFactory terminalFactory;

    private CommandProcessor commandProcessor;
    private CommandRegistry commandRegistry;
    private org.apache.karaf.shell.console.jline.Console console;
    private boolean start;

    public synchronized void registerCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        this.commandProcessor = commandProcessor;
        start();
    }

    public synchronized void unregisterCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        this.commandProcessor = null;
        stop();
    }

    public void setTerminalFactory(TerminalFactory terminalFactory) {
        this.terminalFactory = terminalFactory;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    protected void start() throws Exception {
        if (start) {
            InputStream in = unwrap(System.in);
            PrintStream out = unwrap(System.out);
            PrintStream err = unwrap(System.err);
            // TODO: this is a callback used to close the server. I'm going to disable it for now.
            Runnable callback = new Runnable() {
                public void run() {
                    try {
                        log.info("Shutdown invoked.  But not implemented.  Sorry.");
                        //bundleContext.getBundle(0).stop();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            };
            Terminal terminal = terminalFactory.getTerminal();
            this.console = new org.apache.karaf.shell.console.jline.Console(commandProcessor,
                    in,
                    wrap(out),
                    wrap(err),
                    terminal,
                    callback);
            CommandSession session = console.getSession();
            session.put("USER", "karaf");
            session.put("APPLICATION", System.getProperty("karaf.name", "root"));
            session.put("LINES", Integer.toString(terminal.getTerminalHeight()));
            session.put("COLUMNS", Integer.toString(terminal.getTerminalWidth()));
            session.put(".jline.terminal", terminal);
            commandRegistry.registerCommandsInSession(session);

            new Thread(console, "Karaf Shell Console Thread").start();
        }
    }

    public void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    protected void stop() throws Exception {
        if (console != null) {
            console.close();
        }
    }

    private static PrintStream wrap(PrintStream stream) {
        OutputStream o = AnsiConsole.wrapOutputStream(stream);
        if (o instanceof PrintStream) {
            return ((PrintStream) o);
        } else {
            return new PrintStream(o);
        }
    }

    private static <T> T unwrap(T stream) {
        try {
            Method mth = stream.getClass().getMethod("getRoot");
            return (T) mth.invoke(stream);
        } catch (Throwable t) {
            return stream;
        }
    }
}
