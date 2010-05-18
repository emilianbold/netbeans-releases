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

package org.netbeans.lib.cvsclient;

import junit.framework.TestCase;

import java.io.*;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

/**
 * Rlog command testcase.
 *
 * @author Petr Kuzel
 */
public class RlogTest extends TestCase {

    /**
     * Tests rlog wire format.
     */
    public void test57365() throws Exception {

        final File tmpDir = TestKit.createTmpFolder("rlogTest");
        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
        System.out.println(protocolLog);

        final File requestsLog = File.createTempFile("requests", null, tmpDir);

        final Exception testException[] = new Exception[1];
        final boolean completedFlag[] = new boolean[] {false};
        Runnable run = new Runnable() {
            public void run() {
                try {
                    PseudoCvsServer cvss = new PseudoCvsServer("protocol/iz57365.in");
                    cvss.logRequests(new FileOutputStream(requestsLog));
                    try {
                        new Thread(cvss).start();

                        String cvsRoot = cvss.getCvsRoot();
                        CVSRoot root = CVSRoot.parse(cvsRoot);
                        GlobalOptions gtx = new GlobalOptions();
                        gtx.setCVSRoot(cvsRoot);
                        Connection connection = new PServerConnection(root);
                        Client client = new Client(connection, new StandardAdminHandler());
                        client.setLocalPath(tmpDir.getAbsolutePath());

                        RlogCommand rlog = new RlogCommand();
                        rlog.setModule("folder/file");

                        client.executeCommand(rlog, gtx);
                        synchronized(completedFlag) {
                            completedFlag[0] = true;
                            completedFlag.notifyAll();
                        }

                    } finally {
                        cvss.stop();;
                    }
                } catch (Exception ex) {
                    testException[0] = ex;
                }
            }
        };

        Thread t = new Thread(run);
        t.start();
        synchronized(completedFlag) {
            if (completedFlag[0] == false) {
                completedFlag.wait(1000); // 'INFINITE' TIMEOUT
            }
        }
        t.interrupt();
        if (testException[0] != null) {
            throw testException[0];
        }

        // check send data

        InputStream actual = new FileInputStream(requestsLog);
        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(actual, "utf8"));
        boolean foundDirectoryLine = false;
        boolean foundRlog = false;
        String line = lineReader.readLine();
        StringBuffer sb = new StringBuffer();
        while (line != null) {
            sb.append(line + "\n");
            foundDirectoryLine |= line.startsWith("Directory");
            foundRlog |= line.equals("rlog");
            line = lineReader.readLine();
        }
        assertFalse("Local state is irrelevant in:\n" + sb.toString(), foundDirectoryLine);
        assertTrue("Where is rlog? in:" + sb.toString(), foundRlog);
        TestKit.deleteRecursively(tmpDir);

    }
    
}
