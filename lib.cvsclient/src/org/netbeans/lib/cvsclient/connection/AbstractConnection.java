/*
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
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.cvsclient.connection;

import java.io.IOException;

import org.netbeans.lib.cvsclient.util.*;
import org.netbeans.lib.cvsclient.request.*;


/**
 * This class abstracts the common features and functionality that all connection protocols to CVS
 * share
 *
 * @author Sriram Seshan
 */
public abstract class AbstractConnection implements Connection {


    /**
     * The name of the repository this connection is made to
     */
    private String repository = null;

    /**
     * The socket's input stream.
     */
    private LoggedDataInputStream inputStream;

    /**
     * The socket's output stream.
     */
    private LoggedDataOutputStream outputStream;

    /** Creates a new instance of AbstractConnection */
    public AbstractConnection() {
    }
    
    /**
     * Get an input stream for receiving data from the server.
     * @return a data input stream
     */
    public LoggedDataInputStream getInputStream() {
        return inputStream;
    }
    
    /**
     * Set an input stream for receiving data from the server.
     * The old stream (if any) is closed.
     * @param inputStream The data input stream
     */
    protected final void setInputStream(LoggedDataInputStream inputStream) {
        if (this.inputStream == inputStream) return ;
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException ioex) {/*Ignore*/}
        }
        this.inputStream = inputStream;
    }

    /**
     * Get an output stream for sending data to the server.
     * @return an output stream
     */
    public LoggedDataOutputStream getOutputStream() {
        return outputStream;
    }    
 
    /**
     * Set an output stream for sending data to the server.
     * The old stream (if any) is closed.
     * @param outputStream The data output stream
     */
    protected final void setOutputStream(LoggedDataOutputStream outputStream) {
        if (this.outputStream == outputStream) return ;
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException ioex) {/*Ignore*/}
        }
        this.outputStream = outputStream;
    }

    /**
     * Get the repository path.
     * @return the repository path, e.g. /home/banana/foo/cvs
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Set the repository path.
     * @param repository the repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }    
    
    /**
     * Verifies that this open connection is a connetion to a working CVS server.
     * Clients should close this connection after verifying.
     */ 
    protected void verifyProtocol() throws IOException {
        try {
            outputStream.writeBytes(new RootRequest(repository).getRequestString(), "US-ASCII");
            outputStream.writeBytes(new UseUnchangedRequest().getRequestString(), "US-ASCII");
            outputStream.writeBytes(new ValidRequestsRequest().getRequestString(), "US-ASCII");
            outputStream.writeBytes("noop \n", "US-ASCII");
        } catch (UnconfiguredRequestException e) {
            throw new RuntimeException("Internal error verifying CVS protocol: " + e.getMessage());
        }
        outputStream.flush();

        StringBuffer responseNameBuffer = new StringBuffer();
        int c;
        while ((c = inputStream.read()) != -1) {
            responseNameBuffer.append((char)c);
            if (c == '\n') break;
        }

        String response = responseNameBuffer.toString();
        if (!response.startsWith("Valid-requests")) {
            throw new IOException("Unexpected server response: " + response);
        }
    }
}
