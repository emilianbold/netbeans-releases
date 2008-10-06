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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.MasterURLMapper;

// XXX needs to test listening as well
import org.openide.util.test.MockLookup;

/**
 * JavadocForBinaryQueryPlatformImpl test
 *
 * @author  David Konecny
 */
public class JavadocForBinaryQueryPlatformImplTest extends NbTestCase {
    
    public JavadocForBinaryQueryPlatformImplTest(java.lang.String testName) {
        super(testName);
        MockLookup.setInstances(
                new JavaPlatformProviderImpl(),
                new ArchiveURLMapper(),
                new JavadocForBinaryQueryPlatformImpl(),
                new MasterURLMapper());
    }
    
    protected @Override void setUp() throws Exception {
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

    @RandomlyFails
    public void testQuery() throws Exception {
        JavaPlatform platform = JavaPlatform.getDefault();
        
        ClassPath cp = platform.getBootstrapLibraries();
        ClassPath.Entry entry = cp.entries().iterator().next();
        URL url = entry.getURL();
        if (FileUtil.getArchiveFile(url) != null) {
            url = FileUtil.getArchiveFile(url);
        }
        File root = new File(url.getFile());
        
        FileObject pfo = cp.getRoots()[0];
        URL u = URLMapper.findURL(pfo, URLMapper.EXTERNAL);
        URL urls[] = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(0, urls.length);

        List<URL> l = new ArrayList<URL>();
        File javadocFile = getBaseDir();
        File api = new File (javadocFile,"api");
        File index = new File (api,"index-files");
        FileUtil.toFileObject(index);
        index.mkdirs();
        l.add(javadocFile.toURI().toURL());
        J2SEPlatformImpl platformImpl = (J2SEPlatformImpl)platform;
        platformImpl.setJavadocFolders(l);
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals(api.toURI().toURL(), urls[0]);
    }
    
    public void testJavadocFolders () throws Exception {
        final File wd = this.getWorkDir();
        final FileObject wdfo = FileUtil.toFileObject(wd);
        final FileObject golden1 = FileUtil.createFolder(wdfo,"test1/docs/api/index-files").getParent();        //NOI18N
        final FileObject golden2 = FileUtil.createFolder(wdfo,"test2/docs/ja/api/index-files").getParent();     //NOI18N
        FileObject testFo = wdfo.getFileObject("test1");                                                        //NOI18N
        FileObject res = JavadocForBinaryQueryPlatformImpl.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test1/docs");                                                              //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test2");                                                                   //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs");                                                              //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs/ja");                                                           //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.findIndexFolder(testFo);
        assertEquals(res, golden2);        
    }
    
}
