/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.common.dbgp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 *
 * @author jdeva
 */
public class DebuggerServer {
    private String sessionID;
    private ServerSocket serverSocket;

    public DebuggerServer(String sessionID) {
        this.sessionID = sessionID;
    }

    public int createSocket() throws IOException {
        return createSocket(0);
    }

    public int createSocket(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        return serverSocket.getLocalPort();
    }

    public void cancelGetDebuggerProxy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Log.getLogger().log(Level.INFO, "Unexpected exception while closing socket", ex);
            }
        }
    }
    
    public DebuggerProxy getDebuggerProxy() throws IOException{
        Socket sessionSocket = serverSocket.accept();
        serverSocket.close();
        return new DebuggerProxy(sessionSocket, sessionID);
    }

    public int findFreePort(int port, int range) {
        for (int testPort = port  ; testPort < port + range; testPort++) {
            Socket testClient = null;
            try {
                //If connection to a port fails, then that port is a free port
                testClient = new Socket("127.0.0.1", testPort); //NOI18N
            } catch (ConnectException ce) {
                // connection failed , so it is a free port, return it
                return testPort;
            } catch (IOException ioe) {
            } finally {
                if (testClient != null) {
                    try {
                        testClient.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return -1;
    }
}
