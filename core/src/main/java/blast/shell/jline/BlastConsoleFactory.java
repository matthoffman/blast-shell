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
import org.apache.karaf.shell.console.jline.Console;
import org.apache.log4j.Logger;
import org.osgi.service.command.CommandProcessor;
import org.osgi.service.command.CommandSession;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * This is NOT a copy of Karaf's ConsoleFactory... it's a more traditional Factory.  It just returns a
 * Console implementation of our choice.
 */
public class BlastConsoleFactory implements ConsoleFactory {
    private static final Logger log = Logger.getLogger(BlastConsoleFactory.class);

    private CommandRegistry commandRegistry;
    private CommandProcessor commandProcessor;

    String welcomeMessage = null;
    String welcomeMessageFile = null;

    @Override
    public Console createConsole(InputStream in, PrintStream out, PrintStream err, Terminal terminal, Runnable closeCallback) throws Exception {
        BlastConsole console = new BlastConsole(commandProcessor,
                in,
                out,
                err,
                terminal,
                closeCallback);
        // "Branding" properties are things like the welcome message, the prompt, and other places where it might say
        // the application name.
        console.setBrandingProperties(loadBrandingProperties());

        final CommandSession session = console.getSession();
        session.put("APPLICATION", System.getProperty("karaf.name", "root"));
        session.put("LINES", Integer.toString(terminal.getTerminalHeight()));
        session.put("COLUMNS", Integer.toString(terminal.getTerminalWidth()));
        session.put(".jline.terminal", terminal);
        commandRegistry.registerCommandsInSession(session);
        return console;
    }

    @Required
    public void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Required
    public void setCommandProcessor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    /**
     * This takes precedence over welcomeMessageFile.
     *
     * @param welcomeMessage
     */
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    /**
     * If welcomeMessage is set directly, we'll use that instead.  Otherwise, we'll load this file, which we'll expect
     * contains a property named "welcome":
     * <p/>
     * welcome=<some welcome message>.
     * <p/>
     * Any other properties will be ignored.
     *
     * @param welcomeMessageFile
     */
    public void setWelcomeMessageFile(String welcomeMessageFile) {
        this.welcomeMessageFile = welcomeMessageFile;
    }

    /**
     * Tries to load from welcomeMessage and then welcomeMessageFile, in that order... failing that, it will return
     * an empty Properties object.
     *
     * @return
     */
    protected Properties loadBrandingProperties() {
        if (welcomeMessage != null) {
            Properties props = new Properties();
            props.put("welcome", welcomeMessage);
            return props;
        } else if (welcomeMessageFile != null) {
            try {
                ResourceUtils.getFile(welcomeMessageFile);
            } catch (FileNotFoundException e) {
                log.error("Could not find file " + welcomeMessageFile + ": " + e.getMessage());
            }
        }
        // failed... no welcome message for you.
        return new Properties();
    }
}

