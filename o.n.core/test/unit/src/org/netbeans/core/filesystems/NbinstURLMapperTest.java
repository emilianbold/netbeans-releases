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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.filesystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import org.netbeans.core.startup.layers.NbinstURLMapper;
import org.netbeans.core.startup.layers.NbinstURLStreamHandlerFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

public class NbinstURLMapperTest extends NbTestCase {

    private static final String FILE_NAME = "test.txt";     //NOI18N
    private static final String FOLDER_NAME = "modules";    //NOI18N
    
    private File testFile;
    private int expectedLength;

    public NbinstURLMapperTest (String testName) throws IOException {
        super (testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        MockServices.setServices(
                TestInstalledFileLocator.class,
                NbinstURLStreamHandlerFactory.class,
                NbinstURLMapper.class,
                MasterURLMapper.class);

        org.netbeans.core.startup.Main.initializeURLFactory ();
        
        File f = this.getWorkDir();
        this.clearWorkDir();
        Lookup.Result result = Lookup.getDefault().lookupResult(InstalledFileLocator.class);
        boolean found = false;
        for (java.util.Iterator it = result.allInstances().iterator(); it.hasNext();) {
            Object locator = it.next();
            if (locator instanceof TestInstalledFileLocator) {
                ((TestInstalledFileLocator)locator).setRoot(f);
                found = true;
            }
        }
        assertTrue("No TestInstalledFileLocator can be found in " + Lookup.getDefault(), found);
        f = new File (f,FOLDER_NAME);
        f.mkdir();
        f = new File (f,FILE_NAME);
        f.createNewFile();
        testFile = f;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            pw.println(FILE_NAME);
        } finally {
            if (pw!=null) {
                pw.close ();
            }
        }
        this.expectedLength = (int) f.length();
    }

    public void testFindFileObject () throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");  //NOI18N
        FileObject fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://test-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://foo-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNull ("The nbinst URL was resolved.",fo);
    }

    public void testURLConnection() throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");                //NOI18N
        URLConnection connection = url.openConnection();
        assertEquals ("URLConnection returned wrong content length.",connection.getContentLength(),expectedLength);
        BufferedReader in = null;
        try {
            in = new BufferedReader  ( new InputStreamReader (connection.getInputStream()));
            String line = in.readLine();
            assertTrue("URLConnection returned invalid InputStream",line.equals(FILE_NAME));
        } finally {
            if (in != null) {
                in.close ();
            }
        }
    }

    public static class TestInstalledFileLocator extends InstalledFileLocator {

        private File root;

        public TestInstalledFileLocator() {}

        public void setRoot (File root) {
            this.root = root;
        }

        public File locate(String relativePath, String codeNameBase, boolean localized) {
            assert relativePath != null;
            if (root == null) {
                return null;
            }
            if (codeNameBase!= null && !"test-module".equals(codeNameBase)) {
                return null;
            }
            StringTokenizer tk = new StringTokenizer(relativePath,"/");
            File f = this.root;
            while (tk.hasMoreTokens()) {
                String part = tk.nextToken();
                f = new File (f,part);
                if (!f.exists()) {
                    return null;
                }
            }
            return f;
        }
    }

}
