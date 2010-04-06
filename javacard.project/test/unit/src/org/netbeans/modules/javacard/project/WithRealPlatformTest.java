/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.spi.AbstractCard;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class WithRealPlatformTest extends AbstractJCProjectTest {
    public static final String SYSTEM_PROP_UNIT_TEST = "JCProject.test";
    private static final String ext = "testplatform";
    private static final String FAKE_PLATFORM_NAME = "javacard_default";
    private FileObject pform;
    public WithRealPlatformTest() {
        super ("JCProjectTest");
        System.setProperty (SYSTEM_PROP_UNIT_TEST, "true");
    }

    JCProject project;
    private static final DL dl = new DL();

    @Before
    @Override
    public void setUp() throws Exception {
        MockServices.setServices(DL.class);
        super.setUp();
        FileObject fo = FileUtil.getConfigFile("Templates/Project/javacard/capproject.properties");
        project = createProject(fo, "CapProject", ProjectKind.CLASSIC_APPLET, "com.foo.bar.baz", "Cap Project", "Bob", FAKE_PLATFORM_NAME, "card");
        setUpPlatform();
    }

    private void setUpPlatform() {
        FileObject pformsFolder = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER);
        assertNotNull (pformsFolder);
        pform = pformsFolder.getFileObject(FAKE_PLATFORM_NAME, ext);
        if (pform == null) {
            try {
                pform = pformsFolder.createData(FAKE_PLATFORM_NAME, ext);
            } catch (IOException ex) {
                throw new Error(ex);
            }
            assertNotNull (pform);
        }
        assertNotNull (dl);
        try {
            DataLoaderPool.setPreferredLoader(pform, dl);
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    @Test
    public void testSanity() throws Exception {
        DataObject pfDo = DataObject.find(pform);
        assertNotNull (pfDo.getLookup().lookup(DOB.class));
        assertTrue (pfDo instanceof DOB);
        InstanceCookie ck = pfDo.getCookie(InstanceCookie.class);
        assertNotNull (ck);
        final JavacardPlatform pf = (JavacardPlatform) ck.instanceCreate();
        assertNotNull (pf);
        assertTrue (pf instanceof DOB.PForm);
        DOB.PForm pform = (DOB.PForm) pf;
        final CountDownLatch latch = new CountDownLatch(1);
        class PCL implements PropertyChangeListener {
            boolean fired;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fired = true;
                latch.countDown();
            }
        };
        File f = new File (System.getProperty("java.io.tmpdir"));
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        FileObject fo = fileObject.createData(System.currentTimeMillis() + getClass().getName());
        URL url = fo.getURL();
        ClassPath pth = pform.getBootstrapLibraries();
        PCL pcl = new PCL();
        pth.addPropertyChangeListener(pcl);
        pform.setBootURLs(url);
        assertTrue (pcl.fired);
        pth.removePropertyChangeListener(pcl);
    }

    @Test
    public void testPlatformClasspathChangesArePropagated() throws Exception {
        assertNotNull (project.getPlatform());
        assertTrue (project.getPlatform() instanceof DOB.PForm);
        assertTrue (project.getCard() instanceof DOB.PForm.C.CD);
        System.err.println("PROJECT PLATFORM " + project.getPlatform());
        System.err.println("PROJECT CARD " + project.getCard());
        DOB.PForm pform = (DOB.PForm) project.getPlatform();
        ClassPath processorPath = project.getProcessorClassPath();
        assertNotNull (processorPath);
        assertEquals (0, processorPath.getRoots().length);
        final CountDownLatch latch = new CountDownLatch(1);
        class PCL implements PropertyChangeListener {
            boolean fired;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fired = true;
                latch.countDown();
            }

        };
        PCL pcl = new PCL();
        processorPath.addPropertyChangeListener(pcl);
        assertFalse (pcl.fired);
        File f = new File (System.getProperty("java.io.tmpdir"));
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        pform.setProcessorURLs(fileObject.getURL());
        latch.await(20000, TimeUnit.MILLISECONDS);
        assertEquals (1, pform.getProcessorClasspath(ProjectKind.CLASSIC_APPLET).getRoots().length);
        assertEquals (1, processorPath.getRoots().length);
        FileObject fo = fileObject.createData(System.currentTimeMillis() + getClass().getName());
        try {
            assertNotNull (fileObject);
            assertTrue (processorPath.contains(fo));
        } finally {
            fo.delete();
        }
        assertTrue (pcl.fired);
        pcl.fired = false;
        pform.setProcessorURLs();
        assertTrue (pcl.fired);
        processorPath.removePropertyChangeListener(pcl);
    }

    public static final class DL extends DataLoader {
        public DL() {
            super (DOB.class);
        }

        @Override
        protected DataObject handleFindDataObject(FileObject fo, RecognizedFiles recognized) throws IOException {
            System.err.println("Find data object " + fo.getPath());
            if (ext.equals (fo.getExt())) {
                DataObject result = new DOB (fo, this);
                recognized.markRecognized(fo);
                return result;
            }
            return null;
        }
    }

    private static final class DOB extends DataObject implements InstanceCookie, InstanceCookie.Of {
        DOB(FileObject pf, DataLoader ldr) throws DataObjectExistsException {
            super (pf, ldr);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> c) {
            return getLookup().lookup(c);
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(this, pform);
        }

        @Override
        public boolean isDeleteAllowed() {
            return false;
        }

        @Override
        public boolean isCopyAllowed() {
            return false;
        }

        @Override
        public boolean isMoveAllowed() {
            return false;
        }

        @Override
        public boolean isRenameAllowed() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected DataObject handleCopy(DataFolder f) throws IOException {
            return this;
        }

        @Override
        protected void handleDelete() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected FileObject handleRename(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected FileObject handleMove(DataFolder df) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String instanceName() {
            return getName();
        }

        @Override
        public Class<?> instanceClass() throws IOException, ClassNotFoundException {
            return PForm.class;
        }

        @Override
        public Object instanceCreate() throws IOException, ClassNotFoundException {
            return pform;
        }

        @Override
        public boolean instanceOf(Class<?> type) {
            return type.isInstance(pform);
        }

        private final PForm pform = new PForm();

        private final CPImpl procCpImpl = new CPImpl();
        private final CPImpl bootCpImpl = new CPImpl();
        private final ClassPath procClassPath = ClassPathFactory.createClassPath(procCpImpl);
        private final ClassPath bootClassPath = ClassPathFactory.createClassPath(bootCpImpl);

        private static class CPImpl implements ClassPathImplementation {
            private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
            private List<URL> urls = new ArrayList<URL>();
            private List<PathResourceImplementation> pris = null;

            public synchronized void setURLs (URL... urls) {
                List<PathResourceImplementation> old = new ArrayList<PathResourceImplementation>();
                if (pris != null) {
                    old.addAll(pris);
                }
                this.urls.clear();
                this.pris = null;
                this.urls.addAll(Arrays.asList(urls));
                List<? extends PathResourceImplementation> nue = getResources();
                if (!old.equals(nue)) {
                    supp.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, old, nue);
                }
            }

            @Override
            public synchronized List<? extends PathResourceImplementation> getResources() {
                if (pris == null) {
                    pris = new ArrayList<PathResourceImplementation>();
                    for (URL u : urls) {
                        pris.add (new PRI(u));
                    }
                }
                return pris;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                supp.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                supp.removePropertyChangeListener(listener);
            }

            private final class PRI implements PathResourceImplementation {
                private final URL url;
                PRI(URL url) {
                    this.url = url;
                }

                @Override
                public URL[] getRoots() {
                    return new URL[] { url };
                }

                @Override
                public ClassPathImplementation getContent() {
                    return CPImpl.this;
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                    //do nothing
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                    //do nothing
                }

                public boolean equals (Object o) {
                    return o instanceof PRI && ((PRI) o).url.toString().equals(url.toString());
                }

                public int hashCode() {
                    return url.toString().hashCode();
                }
            }
        }

        private class PForm extends JavacardPlatform {
            public void setProcessorURLs (URL... urls) {
                procCpImpl.setURLs(urls);
            }

            public void setBootURLs (URL... urls) {
                bootCpImpl.setURLs(urls);
            }
            
            @Override
            public String getSystemName() {
                return DOB.this.getName();
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public SpecificationVersion getJavacardVersion() {
                return new SpecificationVersion ("3.0.2");
            }

            @Override
            public boolean isVersionSupported(SpecificationVersion javacardVersion) {
                return true;
            }

            @Override
            public ClassPath getBootstrapLibraries(ProjectKind kind) {
                return getBootstrapLibraries();
            }

            @Override
            public ClassPath getProcessorClasspath(ProjectKind kind) {
                return procClassPath;
            }

            @Override
            public String getDisplayName() {
                return getSystemName();
            }

            @Override
            public Cards getCards() {
                return new C();
            }

            private final class C extends Cards implements Lookup.Provider {
                private final CD cd = new CD();
                @Override
                public List<? extends Provider> getCardSources() {
                    return Collections.singletonList(this);
                }

                @Override
                public Lookup getLookup() {
                    return Lookups.singleton(cd);
                }

                private class CD extends AbstractCard {
                    CD() {
                        super (PForm.this, "card");
                    }
                }
            }

            @Override
            public Properties toProperties() {
                Properties result = new Properties();
                result.setProperty (JavacardPlatformKeyNames.PLATFORM_ID, getName());
                return result;
            }

            @Override
            public String getPlatformKind() {
                return JavacardPlatformKeyNames.PLATFORM_KIND_RI;
            }

            @Override
            public Set<ProjectKind> supportedProjectKinds() {
                return EnumSet.allOf(ProjectKind.class);
            }

            @Override
            public Map<String, String> getProperties() {
                return new HashMap<String,String>();
            }

            @Override
            public ClassPath getBootstrapLibraries() {
                return bootClassPath;
            }

            @Override
            public ClassPath getStandardLibraries() {
                return ClassPathSupport.createClassPath("");
            }

            @Override
            public String getVendor() {
                return "Joe Blow";
            }

            @Override
            public Specification getSpecification() {
                return new Specification ("jcre", new SpecificationVersion("3.0.2"));
            }

            @Override
            public Collection<FileObject> getInstallFolders() {
                return Collections.singleton(FileUtil.createMemoryFileSystem().getRoot());
            }

            @Override
            public FileObject findTool(String toolName) {
                return null;
            }

            @Override
            public ClassPath getSourceFolders() {
                return ClassPathSupport.createClassPath("");
            }

            @Override
            public List<URL> getJavadocFolders() {
                return new ArrayList<URL>();
            }
        }
    }
}