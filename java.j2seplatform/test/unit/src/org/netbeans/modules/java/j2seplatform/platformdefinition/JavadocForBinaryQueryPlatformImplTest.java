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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

// XXX needs to test listening as well

/**
 * JavadocForBinaryQueryPlatformImpl test
 *
 * @author  David Konecny
 */
public class JavadocForBinaryQueryPlatformImplTest extends NbTestCase {
    
    public JavadocForBinaryQueryPlatformImplTest(java.lang.String testName) {
        super(testName);
        MockServices.setServices(
                ArchiveURLMapper.class,
                JavadocForBinaryQueryPlatformImpl.class,
                MasterURLMapper.class,
                JavaPlatformProviderImpl.class);
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


    public void testQuery() throws Exception {
        JavaPlatform platform = JavaPlatform.getDefault();
        ClassPath cp = platform.getBootstrapLibraries();
        FileObject pfo = cp.getRoots()[0];
        URL u = URLMapper.findURL(pfo, URLMapper.EXTERNAL);
        URL urls[] = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertTrue(urls[0].toString(), urls[0].toString().startsWith("http://download.oracle.com/"));

        List<URL> l = new ArrayList<URL>();
        File javadocFile = getBaseDir();
        File api = new File (javadocFile,"api");
        File index = new File (api,"index-files");
        FileUtil.toFileObject(index);
        index.mkdirs();
        l.add(Utilities.toURI(javadocFile).toURL());
        J2SEPlatformImpl platformImpl = (J2SEPlatformImpl)platform;
        platformImpl.setJavadocFolders(l);
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals(Utilities.toURI(api).toURL(), urls[0]);
    }
    
    public void testJavadocFolders () throws Exception {
        final File wd = this.getWorkDir();
        final FileObject wdfo = FileUtil.toFileObject(wd);
        final FileObject golden1 = FileUtil.createFolder(wdfo,"test1/docs/api/index-files").getParent();        //NOI18N
        final FileObject golden2 = FileUtil.createFolder(wdfo,"test2/docs/ja/api/index-files").getParent();     //NOI18N
        FileObject testFo = wdfo.getFileObject("test1");                                                        //NOI18N
        FileObject res = JavadocForBinaryQueryPlatformImpl.R.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test1/docs");                                                              //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.R.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test2");                                                                   //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.R.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs");                                                              //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.R.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs/ja");                                                           //NOI18N
        res = JavadocForBinaryQueryPlatformImpl.R.findIndexFolder(testFo);
        assertEquals(res, golden2);        
    }

    public void testTwoPlatformsoverSameSDK() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"jdoc1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"jdoc2");  //NOI18N
        jdocFile2.mkdir();
        JavaPlatformProviderImpl provider = Lookup.getDefault().lookup(JavaPlatformProviderImpl.class);
        final URL binRoot = Utilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final List<URL> javadoc1 = Collections.singletonList(Utilities.toURI(jdocFile1).toURL());
        final List<URL> javadoc2 = Collections.singletonList(Utilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform2.setJavadoc(javadoc2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final JavadocForBinaryQuery.Result result1 = JavadocForBinaryQuery.findJavadoc(binRoot);
        assertEquals(javadoc2, Arrays.asList(result1.getRoots()));

        platform1.setJavadoc(javadoc1);
        assertEquals(javadoc1, Arrays.asList(result1.getRoots()));

        final JavadocForBinaryQuery.Result result2 = JavadocForBinaryQuery.findJavadoc(binRoot);
        assertEquals(javadoc1, Arrays.asList(result2.getRoots()));

        platform1.setJavadoc(Collections.<URL>emptyList());
        assertEquals(javadoc2, Arrays.asList(result1.getRoots()));
        assertEquals(javadoc2, Arrays.asList(result2.getRoots()));
    }

    private static final class TestJavaPlatform extends JavaPlatform {

        private final String name;
        private final ClassPath bootCp;
        private List<URL> javadoc;

        TestJavaPlatform(
                final String name,
                final ClassPath bootCp) {
            this.name = name;
            this.bootCp = bootCp;
            this.javadoc = Collections.<URL>emptyList();
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.<String,String>emptyMap();
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return bootCp;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public String getVendor() {
            return "Oracle";    //NOI18N
        }

        @Override
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("1.5"));  //NOI18N
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.emptySet();
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return javadoc;
        }

        void setJavadoc(final List<URL> javadoc) {
            this.javadoc = javadoc;
            firePropertyChange(PROP_JAVADOC_FOLDER, null, null);
        }

    }
    
}
