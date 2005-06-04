/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.filesystems;


import java.beans.PropertyVetoException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.util.StringTokenizer;
import org.netbeans.core.startup.layers.NbinstURLMapper;
import org.netbeans.core.startup.layers.NbinstURLStreamHandlerFactory;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.junit.NbTestCase;


public class NbinstURLMapperTest extends NbTestCase {

    private static final String FILE_NAME = "test.txt";     //NOI18N
    private static final String FOLDER_NAME = "modules";    //NOI18N

    private FileSystem fs;
    private File testFile;
    private int expectedLength;

    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        URLStreamHandlerFactory fact = new NbinstURLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(fact);
    }

    public NbinstURLMapperTest (String testName) throws IOException {
        super (testName);
    }


    protected void setUp() throws Exception {
        super.setUp();
        File f = this.getWorkDir();
        this.clearWorkDir();
        Lookup.Result result = Lookup.getDefault().lookup (new Lookup.Template(InstalledFileLocator.class));
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
        this.fs = this.mountFs ();
        assertNotNull ("Test was not able to mount filesystem.",this.fs);
    }


    protected void tearDown() throws Exception {
        this.umountFs (this.fs);
        super.tearDown();
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





    private FileSystem mountFs () throws IOException {
        File f = FileUtil.normalizeFile(this.getWorkDir());
        String parentName;
        while ((parentName=f.getParent())!=null) {
            f = new File (parentName);
        }
        try {
            LocalFileSystem fs = new LocalFileSystem ();
            fs.setRootDirectory (f);
            Repository.getDefault().addFileSystem(fs);
            return fs;
        } catch (PropertyVetoException pve) {
            return null;
        }
    }

    private void umountFs (FileSystem fs) {
        assertNotNull ("umountFs called with null FileSystem.",fs);
        Repository.getDefault().removeFileSystem(fs);
    }

    public static class Lkp extends ProxyLookup {
        public Lkp () {
            this.setLookups (new Lookup[] {
                Lookups.fixed(new Object[] {new TestInstalledFileLocator(), new NbinstURLStreamHandlerFactory(),
                new NbinstURLMapper()})
            });
        }
    }

    public static class TestInstalledFileLocator extends InstalledFileLocator {

        private File root;

        public TestInstalledFileLocator () {
        }


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
