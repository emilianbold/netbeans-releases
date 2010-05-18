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
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

/**
 * Add command test suite.
 *
 * @author Petr Kuzel
 */
public class AddTest extends TestCase {

    /**
     * Tests cvswrappers compliance.
     * <p>
     * Uses fake PseudoCvsServer.
     */
    public void test36289() throws Exception {

        File tmpDir = TestKit.createTmpFolder("serverAbortTest");
        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
        System.setProperty("Env-CVSWRAPPERS", "*.wrap -k 'b'");
        System.out.println(protocolLog);

        final PseudoCvsServer cvss = new PseudoCvsServer("protocol/iz36289.in");
        File requestsLog = File.createTempFile("requests", null, tmpDir);
        cvss.logRequests(new FileOutputStream(requestsLog));
        Thread cvssThread = new Thread(cvss);
        cvssThread.start();

        String cvsRoot = cvss.getCvsRoot();
        CVSRoot root = CVSRoot.parse(cvsRoot);
        final GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        Connection connection = new PServerConnection(root);
        final Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(tmpDir.getAbsolutePath());

        // prepare working directory
        File CVSdir = new File(tmpDir, "CVS");
        CVSdir.mkdirs();

        OutputStream out;
        File rootFile = new File(CVSdir, "Root");
        out = new FileOutputStream(rootFile);
        out.write(cvsRoot.getBytes("utf8"));
        out.flush();
        out.close();

        File repo = new File(CVSdir, "Repository");
        out = new FileOutputStream(repo);
        out.write("/cvs".getBytes("utf8"));
        out.flush();
        out.close();

        // execute the command

        AddCommand add = new AddCommand();
        File wrap = new File(tmpDir, "test.wrap");
        File txt = new File(tmpDir, "test.txt");
        if (wrap.createNewFile() == false) {
            throw new IOException("Can not create " + wrap);
        }
        if (txt.createNewFile() == false) {
            throw new IOException("Can not create " + txt);
        }
        File[] files = new File[] {wrap, txt};
        add.setFiles(files);

        client.executeCommand(add, gtx);
        cvss.stop();
        cvssThread.join();

        // check test matching golden file (here critical line from issue #36289)

        InputStream actual = new FileInputStream(requestsLog);
        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(actual, "utf8"));
        String line = lineReader.readLine();
        StringBuffer sb = new StringBuffer();
        while (line != null) {
            sb.append(line + "\n");
            line = lineReader.readLine();
        }
        String requests = sb.toString();
        assertTrue(requests, requests.indexOf("Kopt -kb\n" +
                "Is-modified test.wrap") != -1);

        TestKit.deleteRecursively(tmpDir);

    }

}
