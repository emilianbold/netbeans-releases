/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.connection;

import java.io.*;

import org.netbeans.lib.cvsclient.util.*;
// import org.openide.util.RequestProcessor;

/**
 * Implements a connection to a local server. See the cvs documents for more
 * information about different connection methods. Local is popular where
 * the CVS repository exists on the machine where the client library is
 * running.<p>
 * Because this library implements just the client part, it can not operate
 * directly on the repository. It needs a server to talk to. Therefore
 * it needs to execute the server process on the local machine.
 *
 * @author  Robert Greig
 */
public class LocalConnection extends AbstractConnection {

    private static final String CVS_EXE_COMMAND = System.getenv("CVS_EXE") != null?
        System.getenv("CVS_EXE") + " server": "cvs server";  // NOI18N    

    /**
     * The CVS process that is being run.
     */
    protected Process process;

    /**
     * Creates a instance of ServerConnection.
     */
    public LocalConnection() {
        reset();
    }

    /**
     * Authenticate a connection with the server.
     *
     * @throws AuthenticationException if an error occurred
     */
    private void openConnection()
            throws AuthenticationException {
        try {
            process = Runtime.getRuntime().exec(CVS_EXE_COMMAND);
            setOutputStream(new LoggedDataOutputStream(process.
                                                      getOutputStream()));
            setInputStream(new LoggedDataInputStream(process.getInputStream()));
        }
        catch (IOException t) {
            reset();
            String locMessage = AuthenticationException.getBundleString(
                    "AuthenticationException.ServerConnection"); //NOI18N
            throw new AuthenticationException("Connection error", t, locMessage); //NOI18N
        }
    }

    private void reset() {
        process = null;
        setInputStream(null);
        setOutputStream(null);
    }

    /**
     * Authenticate with the server. Closes the connection immediately.
     * Clients can use this method to ensure that they are capable of
     * authenticating with the server. If no exception is thrown, you can
     * assume that authentication was successful
     *
     * @throws AuthenticationException if the connection with the server
     *                                cannot be established
     */
    public void verify() throws AuthenticationException {
        try {
            openConnection();
            verifyProtocol();
            process.destroy();
        }
        catch (Exception e) {
            String locMessage = AuthenticationException.getBundleString(
                    "AuthenticationException.ServerVerification"); //NOI18N
            throw new AuthenticationException("Verification error", e, locMessage); //NOI18N
        }
        finally {
            reset();
        }
    }

    /**
     * Authenticate with the server and open a channel of communication
     * with the server. This Client will
     * call this method before interacting with the server. It is up to
     * implementing classes to ensure that they are configured to
     * talk to the server (e.g. port number etc.)
     * @throws AuthenticationException if the connection with the server
     * cannot be established
     */
    public void open() throws AuthenticationException {
        openConnection();
    }

    /**
     * Returns true to indicate that the connection was successfully established.
     */
    public boolean isOpen() {
        return process != null;
    }

    /**
     * Close the connection with the server.
     */
    public void close() throws IOException {
        try {
            if (process != null) {
                process.destroy();
            }
        }
        finally {
            reset();
        }
    }
    
    /**
     * @return 0, no port is used by the local connection.
     */
    public int getPort() {
        return 0; // No port
    }
    
    /**
     * Modify the underlying inputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyInputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    /**
     * Modify the underlying outputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyOutputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }
    
}
