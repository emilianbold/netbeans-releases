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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.connection;

import java.io.*;

import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Provides a method for accessing a connection, in order to be able to
 * communicate using the CVS Protocol. Instances of this interface are used
 * by the Client class to communicate with the server without being too
 * concerned with how the communication is taking place or how it was
 * set up.
 * @see org.netbeans.lib.cvsclient.Client
 * @author  Robert Greig
 */
public interface Connection {
    /**
     * Get a data inputstream for reading data
     * @return an input stream
     **/
    LoggedDataInputStream getInputStream();

    /**
     * Get an output stream for sending data to the server
     * @return an output stream
     **/
    LoggedDataOutputStream getOutputStream();

    /**
     * Open a connection with the server. Until this method is called, no
     * communication with the server can take place. This Client will
     * call this method before interacting with the server. It is up to
     * implementing classes to ensure that they are configured to
     * talk to the server (e.g. port number etc.)
     * @throws AutenticationException if the connection with the server
     * cannot be established
     **/
    void open() throws AuthenticationException, CommandAbortedException;

    /**
     * Verify a cnnection with the server. Simply verifies that a connection
     * could be made, for example that the user name and password are both
     * acceptable. Does not create input and output stream. For that, use
     * the open() method.
     */
    void verify() throws AuthenticationException;

    /**
     * Close the connection with the server
     */
    void close() throws IOException;

    /**
     * Returns true to indicate that the connection was successfully established.
     */
    boolean isOpen();

    /**
     * Get the repository
     */
    String getRepository();
    
    /**
     * Get the port number, which this connection is actually using.
     * @return The port number or zero, when the port number does not have sense.
     */
    int getPort();

    /**
     * Modify the underlying inputstream
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    void modifyInputStream(ConnectionModifier modifier) throws IOException;

    /**
     * Modify the underlying outputstream
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    void modifyOutputStream(ConnectionModifier modifier) throws IOException;
}
