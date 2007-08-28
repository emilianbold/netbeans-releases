/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

public class NewClustersRebootTest extends NbTestCase {

    public NewClustersRebootTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        System.setProperty("netbeans.dirs", getWorkDirPath());
        OutputStream os2 = new FileOutputStream(new File(getWorkDir(),"nbmfortest"));
        NewClustersRebootCallback.copy(DefaultTestCase.class.getResourceAsStream("data/com-sun-testmodule-cluster.nbm"), os2);
        os2.close();
    }

    public void testSelf() throws Exception {
        if (org.openide.util.Utilities.isUnix() || org.openide.util.Utilities.isMac()) { 
            StringBuffer sb = new StringBuffer();
            assertFalse(getNewCluster().exists());
            invokeNbExecAndCreateCluster(getWorkDir(), sb, new String[]{"--clusters", new File(getWorkDir(), "oldcluster").getAbsolutePath()});
            assertTrue(getNewCluster().exists());
            assertTrue(getTestModule().exists());
        }
    }

    private File getNewCluster() throws IOException {
        return new File(getWorkDir(), NewClustersRebootCallback.NAME_OF_NEW_CLUSTER);
    }
    
    private File getTestModule() throws IOException {
        return new File(getNewCluster(),"/modules/com-sun-testmodule-cluster.jar");
    }
    
    private void invokeNbExecAndCreateCluster(File workDir, StringBuffer sb, String... args) throws Exception {
        URL u = Lookup.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(u.toURI());
        assertTrue("file found: " + f, f.exists());
        File nbexec = org.openide.util.Utilities.isWindows() ? new File(f.getParent(), "nbexec.exe") : new File(f.getParent(), "nbexec");
        assertTrue("nbexec found: " + nbexec, nbexec.exists());

        URL tu = NewClustersRebootCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = new File(tu.toURI());
        assertTrue("file found: " + testf, testf.exists());
                
        LinkedList<String> allArgs = new LinkedList<String>(Arrays.asList(args));
        allArgs.addFirst("-J-Dnetbeans.mainclass=" + NewClustersRebootCallback.class.getName());
        allArgs.addFirst(System.getProperty("java.home"));
        allArgs.addFirst("--jdkhome");
        allArgs.addFirst(getWorkDirPath());
        allArgs.addFirst("--userdir");
        allArgs.addFirst(testf.getPath());
        allArgs.addFirst("-cp:p");
        allArgs.addFirst("--nosplash");

        if (!org.openide.util.Utilities.isWindows()) {
            allArgs.addFirst(nbexec.getPath());
            allArgs.addFirst("-x");
            allArgs.addFirst("/bin/sh");
        } else {
            allArgs.addFirst(nbexec.getPath());
        }

        Process p = Runtime.getRuntime().exec(allArgs.toArray(new String[0]), new String[0], workDir);
        int res = readOutput(sb, p);
        String output = sb.toString();
        assertEquals("Execution is ok: " + output, 0, res);
    }

    private static int readOutput(final StringBuffer sb, final Process p) throws Exception {
        class Read extends Thread {

            private InputStream is;

            public Read(String name, InputStream is) {
                super(name);
                this.is = is;
                setDaemon(true);
            }

            @Override
            public void run() {
                byte[] arr = new byte[4096];
                try {
                    for (;;) {
                        int len = is.read(arr);
                        if (len == -1) {
                            return;
                        }
                        sb.append(new String(arr, 0, len));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Read out = new Read("out", p.getInputStream());
        Read err = new Read("err", p.getErrorStream());
        out.start();
        err.start();

        int res = p.waitFor();

        out.interrupt();
        err.interrupt();
        out.join();
        err.join();

        return res;
    }
}
