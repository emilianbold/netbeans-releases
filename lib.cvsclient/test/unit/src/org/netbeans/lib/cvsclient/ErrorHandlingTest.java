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

import java.io.File;
import java.io.InputStream;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.status.StatusCommand;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;

/**
 * Tescase covering handling network unreliability and
 * known server protocol errors (library contains workarounds).
 *
 * @author Petr Kuzel
 */
public class ErrorHandlingTest extends TestCase {

    /**
     * Test how client workarounds [server abort] signals.
     * <p>
     * Uses fake PseudoCvsServer.
     */
    public void test56552() throws Exception {

        File tmpDir = TestKit.createTmpFolder("serverAbortTest");
        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
        System.out.println(protocolLog);

        final PseudoCvsServer cvss = new PseudoCvsServer("protocol/iz56552.in");
        new Thread(cvss).start();

        String cvsRoot = cvss.getCvsRoot();
        CVSRoot root = CVSRoot.parse(cvsRoot);
        final GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        Connection connection = new PServerConnection(root);
        final Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(tmpDir.getAbsolutePath());

        final StatusCommand status = new StatusCommand();
        File[] files = new File[] {new File(tmpDir, "placeholder")};
        status.setFiles(files);

        final Exception testException[] = new Exception[1];
        final boolean completedFlag[] = new boolean[] {false};
        Runnable run = new Runnable() {
            public void run() {
                try {
                    try {
                        client.executeCommand(status, gtx);
                        synchronized(completedFlag) {
                            completedFlag[0] = true;
                            completedFlag.notifyAll();
                        }
                    } finally {
                        cvss.stop();
                    }
                } catch (Exception ex) {
                    testException[0] = ex;
                }
            }
        };

        // test mus finish in reasonable time (compare to blocking forever that'd be a bug)

        Thread t = new Thread(run);
        t.start();
        synchronized(completedFlag) {
            if (completedFlag[0] == false) {
                completedFlag.wait(1000); // 'INFINITE' TIMEOUT
            }
        }
        t.interrupt();
        assertTrue(testException[0] instanceof CommandAbortedException);

        TestKit.deleteRecursively(tmpDir);
    }


}
