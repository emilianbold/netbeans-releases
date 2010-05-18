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
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

/**
 * Checkout test suite. Works at C/S protocol wire level.
 *
 * @author Petr Kuzel
 */
public class CheckoutTest extends TestCase {

    /**
     * Test how client handles unxpected IOException caused
     * by line drop from checkout command.
     * <p>
     * Uses fake PseudoCvsServer.
     */
    public void test56552_21126() throws Exception {

        final File tmpDir = TestKit.createTmpFolder("checkoutDropTest");
        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
        System.out.println(protocolLog);

        // command exception expected
        // infinite *1s) blocking is a failure
        // other exception is test failre

        final Exception expectedException[] = new Exception [1];
        final Exception testException[] = new Exception[1];
        Runnable run = new Runnable() {
            public void run() {
                try {
                    PseudoCvsServer cvss = new PseudoCvsServer("protocol/iz56552_21126.in");
                    try {
                        cvss.simulateNetworkFailure(1000, -1);
                        new Thread(cvss).start();

                        String cvsRoot = cvss.getCvsRoot();
                        CVSRoot root = CVSRoot.parse(cvsRoot);
                        GlobalOptions gtx = new GlobalOptions();
                        gtx.setCVSRoot(cvsRoot);
                        Connection connection = new PServerConnection(root);
                        Client client = new Client(connection, new StandardAdminHandler());
                        client.setLocalPath(tmpDir.getAbsolutePath());

                        CheckoutCommand checkout = new CheckoutCommand();
                        checkout.setModule("a11y");

                        client.executeCommand(checkout, gtx);
                        expectedException.notify();
                    } catch (CommandException ex) {
                        // expected expected, infinite wait would be a bug
                        synchronized(expectedException) {
                            expectedException[0] = ex;
                            expectedException.notify();
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
        synchronized(expectedException) {
            if (expectedException[0] == null) {
                expectedException.wait(1000); // 'INFINITE' TIMEOUT
            }
        }
        t.interrupt();
        if (testException[0] != null) {
            throw testException[0];
        }
        assertTrue(expectedException[0] != null);
        TestKit.deleteRecursively(tmpDir);

    }



}
