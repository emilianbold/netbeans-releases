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
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.status.StatusCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.PipedFileInformation;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.event.*;

import java.io.*;
import java.util.Date;

/**
 * Compares output and input streams.
 * <p>
 * NON PORTABLE
 *
 * @author Petr Kuzel
 */
public class ClientTest extends TestCase {

    /**
     * Tests single file status command for local file that is
     * in repository Attic. It's should be Unknown = unversioned.
     */
    public void test50963() throws Exception {
        File tmpDir = TestKit.createTmpFolder("localFileStatusTest");
        File localCheckout = tmpDir;
        String relativePath = "javacvs" + File.separator + "test" + File.separator + "data" +
                File.separator + "iz50963" + File.separator + "removed.txt";
        String cvsRoot = ":pserver:anoncvs@netbeans.org:/cvs";

        System.setProperty("cvsClientLog", new File(tmpDir, "protocol").getAbsolutePath());
//        System.setProperty("socksProxyHost", "icsocks.holland.sun.com");

        CVSRoot root = CVSRoot.parse(cvsRoot);
        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);

        // prepare local environment, checkout and create a local file

        Connection connection = new PServerConnection(root);
        Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(tmpDir.getAbsolutePath());

        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule("javacvs/test/data/iz50963");
        checkout.setPruneDirectories(true);
        checkout.setNotShortenPaths(true);
        checkout.setRecursive(true);

        client.executeCommand(checkout, gtx);

        File localFile = new File(localCheckout, relativePath);
        if (localFile.createNewFile() == false) {
            throw new IOException("Cannot create " + localFile);
        }

        // run status on *single* local file that is Attic in repository

        connection = new PServerConnection(root);
        client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(tmpDir.getAbsolutePath());


        StatusCommand status = new StatusCommand();
        File[] files = new File[] {new File(localCheckout,  relativePath)};
        status.setFiles(files);

        client.getEventManager().addCVSListener(new CVSListener() {

            public void messageSent(MessageEvent e) {
            }

            public void messageSent(BinaryMessageEvent e) {
            }

            public void fileAdded(FileAddedEvent e) {
            }

            public void fileToRemove(FileToRemoveEvent e) {
            }

            public void fileRemoved(FileRemovedEvent e) {
            }

            public void fileUpdated(FileUpdatedEvent e) {
            }

            public void fileInfoGenerated(FileInfoEvent e) {
                FileInfoContainer fic = e.getInfoContainer();
                System.err.println("Fic: " + fic);
            }

            public void commandTerminated(TerminationEvent e) {
            }

            public void moduleExpanded(ModuleExpansionEvent e) {
            }
        });
        client.executeCommand(status, gtx);

        // check result "Unknown" expected

        assertTrue(false);

        // all OK clean up

        TestKit.deleteRecursively(tmpDir);

    }

    /**
     * Tests checkou command prune empty directories option.
     * It must eliminate pruned folder from Entries file.
     */
    public void test53239() throws Exception {

        String checkoutModule = "javacvs/test/data";
        File tmpDir = TestKit.createTmpFolder("pruneTest");
        String localCheckout = tmpDir.getAbsolutePath();
        String cvsRoot = ":pserver:anoncvs@netbeans.org/cvs";

        System.setProperty("cvsClientLog", new File(tmpDir, "protocol").getAbsolutePath());
//        System.setProperty("socksProxyHost", "icsocks.holland.sun.com");

        CVSRoot root = CVSRoot.parse(cvsRoot);
        Connection connection = new PServerConnection(root);
        Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(localCheckout);

        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule(checkoutModule);
        checkout.setPruneDirectories(true);
        checkout.setNotShortenPaths(true);
        checkout.setRecursive(true);

        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        client.executeCommand(checkout, gtx);

        // check results, there must not be iz53239 entry

        String relativePath = "javacvs" + File.separator + "test" + File.separator + "data" +
                File.separator + "CVS" + File.separator + "Entries";
        FileReader reader = new FileReader(new File(localCheckout, relativePath));
        BufferedReader buffy = new BufferedReader(reader);
        String line = buffy.readLine();
        do {
            assertTrue(line.indexOf("iz53239") == -1);
            line = buffy.readLine();
        } while (line != null);

        // all OK clean up

        TestKit.deleteRecursively(tmpDir);
    }

    /**
     * Test handling of binary file piped out to standard output
     * (these are sent as as pure <tt>M</tt> responses instead of <tt>Mbinary</tt> responses)
     */
    public void test56710() throws Exception {
        File tmpDir = TestKit.createTmpFolder("binaryToStdoutTest");
        String relativePath = "javacvs" + File.separator + "test" + File.separator + "data" +
                File.separator + "iz56710" + File.separator + "binary.out";

        String localCheckout = tmpDir.getAbsolutePath();
        String cvsRoot = ":pserver:anoncvs@netbeans.org/cvs";

        String protocolLog = new File(tmpDir, "protocol").getAbsolutePath();
        System.setProperty("cvsClientLog", protocolLog);
//        System.setProperty("socksProxyHost", "icsocks.holland.sun.com");

        CVSRoot root = CVSRoot.parse(cvsRoot);
        Connection connection = new PServerConnection(root);
        Client client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(localCheckout);

        UpdateCommand update = new UpdateCommand();
        update.setPipeToOutput(true);
        File[] files = new File[] {new File(tmpDir, relativePath)};
        update.setFiles(files);

        final File[] pipedTmpFile = new File[1];
        client.getEventManager().addCVSListener(new CVSListener() {

            public void messageSent(MessageEvent e) {
            }

            public void messageSent(BinaryMessageEvent e) {
            }

            public void fileAdded(FileAddedEvent e) {
            }

            public void fileToRemove(FileToRemoveEvent e) {
            }

            public void fileRemoved(FileRemovedEvent e) {
            }

            public void fileUpdated(FileUpdatedEvent e) {
            }

            public void fileInfoGenerated(FileInfoEvent e) {
                PipedFileInformation pfi = (PipedFileInformation) e.getInfoContainer();
                pipedTmpFile[0] = pfi.getTempFile();
            }

            public void commandTerminated(TerminationEvent e) {
            }

            public void moduleExpanded(ModuleExpansionEvent e) {
            }
        });
        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        client.executeCommand(update, gtx);

        FileInputStream in = new FileInputStream(pipedTmpFile[0]);
        byte[] bytes = new byte[257];
        int len = in.read(bytes);
        assertEquals(len, 256);

        for (int i = 0; i<256; i++) {
            assertEquals(bytes[i], (byte)i);
        }

        TestKit.deleteRecursively(tmpDir);
    }


}
