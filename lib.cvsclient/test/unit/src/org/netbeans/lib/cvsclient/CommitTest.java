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
import java.util.Date;

import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;

/**
 * Commit command test suite.
 *
 * @author Petr Kuzel
 */
public class CommitTest extends TestCase {

    /**
     * Client must checks conflicted files timestamps. Until
     * it changes it should not commit the file (it actually
     * decides server by testing sent entry).
     * <p>
     * Uses fake PseudoCvsServer.
     */
    public void test36288() throws Exception {
        File tmpDir = TestKit.createTmpFolder("commitConflictTest");
        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
        System.out.println(protocolLog);

        // prepare working directory
        File CVSdir = new File(tmpDir, "CVS");
        CVSdir.mkdirs();
        File entries = new File(CVSdir, "Entries");
        OutputStream out = new FileOutputStream(entries);
        String dateString = "Thu Mar 24 15:14:27 2005";
        String data = "/conflict.txt/1.2/Result of merge+" + dateString + "//\nD";
        out.write(data.getBytes("utf8"));
        out.flush();
        out.close();

        File conflict_txt = new File(tmpDir, "conflict.txt");
        out = new FileOutputStream(conflict_txt);
        data = "AAA\n" +
                "BBB\n" +
                "<<<<<<< conflict.txt\n" +
                "YYY <= fix\n" +
                "=======\n" +
                "222 <= fix\n" +
                ">>>>>>> 1.2\n" +
                "DDD\n" +
                "EEE\n";
        out.write(data.getBytes("utf8"));
        out.flush();
        out.close();
        Date date = Entry.getLastModifiedDateFormatter().parse(dateString);
        conflict_txt.setLastModified(date.getTime());

        PseudoCvsServer cvss = new PseudoCvsServer("protocol/iz36288.in");

        File requestsLog = File.createTempFile("requests", null, tmpDir);
        cvss.logRequests(new FileOutputStream(requestsLog));
        Thread cvssThread = new Thread(cvss);
        cvssThread.start();
        String cvsRoot = cvss.getCvsRoot();

        File root = new File(CVSdir, "Root");
        out = new FileOutputStream(root);
        out.write(cvsRoot.getBytes("utf8"));
        out.flush();
        out.close();

        File repo = new File(CVSdir, "Repository");
        out = new FileOutputStream(repo);
        out.write("/cvs".getBytes("utf8"));
        out.flush();
        out.close();

        // commit command
        CVSRoot CvsRoot = CVSRoot.parse(cvsRoot);
        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        Connection connection = new PServerConnection(CvsRoot);
        Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(tmpDir.getAbsolutePath());

        CommitCommand commit = new CommitCommand();
        File[] files = new File[] {new File(tmpDir, "conflict.txt")};
        commit.setFiles(files);

        client.executeCommand(commit, gtx);
        cvss.stop();
        cvssThread.join();

        // check test matching golden file (here critical line from iz36288.out)

        InputStream actual = new FileInputStream(requestsLog);
        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(actual, "utf8"));
        boolean foundConflictLine = false;
        String line = lineReader.readLine();
        StringBuffer sb = new StringBuffer();
        while (foundConflictLine == false && line != null) {
            sb.append(line + "\n");
            foundConflictLine |= "Entry /conflict.txt/1.2/+=//".equals(line);
            line = lineReader.readLine();
        }
        assertTrue("Missing 'Entry /conflict.txt/1.2/+=//' in:\n" + sb.toString(), foundConflictLine);

        TestKit.deleteRecursively(tmpDir);
    }

}
