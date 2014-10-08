/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.client.cmdline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import static org.junit.Assert.assertTrue;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Response;

/**
 *
 * @author Martin Entlicher
 */
abstract class AbstractTestBase {
    
    private static final String NODE_EXE = "node";          // NOI18N
    private static final String NODE_EXE_PROP = "nodeBinary";   // NOI18N
    
    protected String testFilePath;
    protected V8Debug v8dbg;
    protected ResponseHandler responseHandler;
    protected Process nodeProcess;
    
    protected final void startUp(InputStream testSource, String testFileName, String debugArgs) throws IOException {
        int port = startNodeDebug(testSource, testFileName, debugArgs);
        assertTrue("Invalid port: "+port, port > 0);
        responseHandler = new ResponseHandler();
        v8dbg = V8Debug.TestAccess.createV8Debug("localhost", port, responseHandler);
    }
    
    protected final int startNodeDebug(InputStream testSource, String testFileName, String debugArgs) throws IOException {
        File testFile = File.createTempFile(testFileName.substring(0, testFileName.indexOf('.')), ".js");
        testFile.deleteOnExit();
        Files.copy(testSource, testFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ProcessBuilder pb = new ProcessBuilder();
        String nodeBinary = System.getProperty(NODE_EXE_PROP);
        if (nodeBinary == null) {
            nodeBinary = pb.environment().get(NODE_EXE_PROP);
        }
        if (nodeBinary == null) {
            nodeBinary = NODE_EXE;
        }
        this.testFilePath = testFile.getAbsolutePath();
        pb.command(nodeBinary, debugArgs, testFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process node = pb.start();
        nodeProcess = node;
        InputStream stdOut = node.getInputStream();
        BufferedReader bso = new BufferedReader(new InputStreamReader(stdOut));
        String line;
        while ((line = bso.readLine()) != null) {
            int space = line.lastIndexOf(' ');
            if (space > 0) {
                try {
                    int port = Integer.parseInt(line.substring(space).trim());
                    reportPrgOutput(bso);
                    return port;
                } catch (NumberFormatException nfex) {}
            }
            System.err.println(line);
        }
        return -1;
    }
    
    private void reportPrgOutput(final BufferedReader bso) {
        new Thread("Node.js output thread") {
            @Override
            public void run() {
                String line;
                try {
                    while ((line = bso.readLine()) != null) {
                        System.err.println("node.js: '"+line+"'");
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }.start();
    }
    
    protected final class ResponseHandler implements V8Debug.Testeable {
        
        private V8Response lastResponse;
        private V8Event lastEvent;
        private boolean closed;

        @Override
        public synchronized void notifyResponse(V8Response response) {
            this.lastResponse = response;
            this.notifyAll();
        }

        @Override
        public synchronized void notifyEvent(V8Event event) {
            this.lastEvent = event;
            this.notifyAll();
        }

        @Override
        public void notifyClosed() {
            this.closed = true;
        }
        
        public synchronized V8Response getLastResponse() throws InterruptedException {
            while (lastResponse == null) {
                this.wait();
            }
            V8Response response = lastResponse;
            lastResponse = null;
            return response;
        }
        
        public synchronized void clearLastResponse() {
            lastResponse = null;
        }
        
        public synchronized V8Event getLastEvent() throws InterruptedException {
            while (lastEvent == null) {
                this.wait();
            }
            V8Event event = lastEvent;
            lastEvent = null;
            return event;
        }
        
        public boolean isClosed() {
            return closed;
        }
        
    }
}
