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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.modules.masterfs.MasterURLMapper;

// XXX needs to test listening as well

/**
 * JavadocForBinaryQueryPlatformImpl test
 *
 * @author  David Konecny
 */
public class JavadocForBinaryQueryPlatformImplTest extends NbTestCase implements Lookup.Provider {
    
    
    public JavadocForBinaryQueryPlatformImplTest(java.lang.String testName) {
        super(testName);
        TestUtil.setLookup (Lookups.proxy(this));
    }
    
    private Lookup lookup;
    
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath()); 
        super.setUp();
        clearWorkDir();                
    }
    
    private File getBaseDir() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir;
    }
    
    public void testQuery() throws Exception {
        JavaPlatform platform = JavaPlatform.getDefault();
        
        ClassPath cp = platform.getBootstrapLibraries();
        ClassPath.Entry entry = (ClassPath.Entry)cp.entries().iterator().next();
        URL url = entry.getURL();
        if (FileUtil.getArchiveFile(url) != null) {
            url = FileUtil.getArchiveFile(url);
        }
        File root = new File(url.getFile());
        
        FileObject pfo = cp.getRoots()[0];
        URL u = URLMapper.findURL(pfo, URLMapper.EXTERNAL);
        URL urls[] = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(0, urls.length);

        ArrayList l = new ArrayList();
        File javadocFile = getBaseDir();
        l.add(javadocFile.toURI().toURL());
        J2SEPlatformImpl platformImpl = (J2SEPlatformImpl)platform;
        platformImpl.setJavadocFolders(l);
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals(javadocFile.toURI().toURL(), urls[0]);
    }
    
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(new Object[] {
                new JavaPlatformProviderImpl (),
                new ArchiveURLMapper(),
                new JavadocForBinaryQueryPlatformImpl(),
                new MasterURLMapper(),
            });
        }
        return lookup;
    }        
    
}
