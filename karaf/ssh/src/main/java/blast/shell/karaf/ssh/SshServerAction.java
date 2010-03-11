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

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import blast.shell.CommandSupport;
import org.apache.sshd.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start a SSH server.
 *
 * @version $Rev: 720411 $ $Date: 2008-11-25 05:32:43 +0100 (Tue, 25 Nov 2008) $
 */
@Command(scope = "ssh", name = "sshd", description = "Creates a SSH server")
public class SshServerAction extends CommandSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-p", aliases={ "--port" }, description = "The port to setup the SSH server (Default: 8101)", required = false, multiValued = false)
    private int port = 8101;

    @Option(name="-b", aliases={ "--background"}, description = "The service will run in the background (Default: true)", required = false, multiValued = false)
    private boolean background = true;

    SshServer server; 
    
    public void setSshServer(SshServer sshServer) {
        this.server = sshServer;
    }

    public Object doExecute() throws Exception {
        log.debug("Created server: {}", server);

        server.setPort(port);

        server.start();

        System.out.println("SSH server listening on port " + port);

        if (!background) {
            synchronized (this) {
                log.debug("Waiting for server to shutdown");

                wait();
            }

            server.stop();
        }

        return null;
    }
}
