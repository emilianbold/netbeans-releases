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
package org.netbeans.api.java.source;

import com.sun.source.util.JavacTask;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import junit.framework.Assert;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan Lahoda
 */
public final class SourceUtilsTestUtil extends ProxyLookup {
    
    private static SourceUtilsTestUtil DEFAULT_LOOKUP = null;
    
    public SourceUtilsTestUtil() {
//        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    private static Object[] extraLookupContent = null;
    
    public static void prepareTest(String[] additionalLayers, Object[] additionalLookupContent) throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[additionalLayers.length];
        
        for (int cntr = 0; cntr < additionalLayers.length; cntr++) {
            layers[cntr] = Thread.currentThread().getContextClassLoader().getResource(additionalLayers[cntr]);
        }
        
        XMLFileSystem xmlFS = new XMLFileSystem();
        xmlFS.setXmlUrls(layers);
        
        FileSystem system = new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), xmlFS});
        
        Repository repository = new Repository(system);
        extraLookupContent = new Object[additionalLookupContent.length + 1];
        
        System.arraycopy(additionalLookupContent, 0, extraLookupContent, 1, additionalLookupContent.length);
        
        extraLookupContent[0] = repository;
        
        SourceUtilsTestUtil.setLookup(extraLookupContent, SourceUtilsTestUtil.class.getClassLoader());
        
        SourceUtilsTestUtil2.disableLocks();
    }
    
    static {
        SourceUtilsTestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", SourceUtilsTestUtil.class.getName());
        Assert.assertEquals(SourceUtilsTestUtil.class, Lookup.getDefault().getClass());
    }
    
    public static void prepareTest(FileObject sourceRoot, FileObject buildRoot, FileObject cache) throws Exception {
        prepareTest(sourceRoot, buildRoot, cache, new FileObject[0]);
    }
    
    public static void prepareTest(FileObject sourceRoot, FileObject buildRoot, FileObject cache, FileObject[] classPathElements) throws Exception {
        if (extraLookupContent == null)
            prepareTest(new String[0], new Object[0]);
        
        Object[] lookupContent = new Object[extraLookupContent.length + 4];
        
        System.arraycopy(extraLookupContent, 0, lookupContent, 4, extraLookupContent.length);
        
        lookupContent[0] = new TestProxyClassPathProvider(sourceRoot, buildRoot, classPathElements);
        lookupContent[1] = new TestSourceForBinaryQuery(sourceRoot, classPathElements);
        lookupContent[2] = new TestSourceLevelQueryImplementation();
        lookupContent[3] = JavaDataLoader.findObject(JavaDataLoader.class, true);
        
        setLookup(lookupContent, SourceUtilsTestUtil.class.getClassLoader());

        IndexUtil.setCacheFolder(FileUtil.toFile(cache));
    }

    private static Map<FileObject,  String> file2SourceLevel = new WeakHashMap<FileObject, String>();
    
    public static void setSourceLevel(FileObject file, String level) {
        file2SourceLevel.put(file, level);
    }

    /**This method assures that all java classes under sourceRoot are compiled,
     * and the caches are created for them.
     */
    public static void compileRecursively(FileObject sourceRoot) throws Exception {
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);
    }

    private static List<URL> bootClassPath;

    private static Logger log = Logger.getLogger(SourceUtilsTestUtil.class.getName());
    
    public static synchronized List<URL> getBootClassPath() {
        if (bootClassPath == null) {
            try {
                String cp = System.getProperty("sun.boot.class.path");
                List<URL> urls = new ArrayList<URL>();
                String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
                
                for (String path : paths) {
                    File f = new File(path);
                    
                    if (!f.canRead())
                        continue;
                    
                    FileObject fo = FileUtil.toFileObject(f);
                    
                    if (FileUtil.isArchiveFile(fo)) {
                        fo = FileUtil.getArchiveRoot(fo);
                    }
                    
                    if (fo != null) {
                        urls.add(fo.getURL());
                    }
                }
                
                bootClassPath = urls;
            } catch (FileStateInvalidException e) {
//                ErrorManager.getDefault().notify(e);
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return bootClassPath;
    }

    private static class TestSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
        
        private FileObject sourceRoot;
        private List<FileObject> classPathElements;
        
        public TestSourceForBinaryQuery(FileObject sourceRoot, FileObject[] classPathElements) {
            this.sourceRoot = sourceRoot;
            this.classPathElements = Arrays.asList(classPathElements);
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (getBootClassPath().contains(binaryRoot))
                return null;

            if (classPathElements.contains(URLMapper.findFileObject(binaryRoot)))
                return null;

            return new SourceForBinaryQuery.Result() {
                public FileObject[] getRoots() {
                    return new FileObject[] {
                        sourceRoot,
                    };
                }
                
                public void addChangeListener(ChangeListener l) {
                }
                
                public void removeChangeListener(ChangeListener l) {
                }
            };
        }
        
    }
    
    private static class TestProxyClassPathProvider implements ClassPathProvider {
        
        private FileObject sourceRoot;
        private FileObject buildRoot;
        private FileObject[] classPathElements;
        
        public TestProxyClassPathProvider(FileObject sourceRoot, FileObject buildRoot, FileObject[] classPathElements) {
            this.sourceRoot = sourceRoot;
            this.buildRoot = buildRoot;
            this.classPathElements = classPathElements;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            try {
            if (ClassPath.BOOT == type) {
                return ClassPathSupport.createClassPath(getBootClassPath().toArray(new URL[0]));
            }
            
            if (ClassPath.SOURCE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    sourceRoot
                });
            }
            
            if (ClassPath.COMPILE == type) {
                return ClassPathSupport.createClassPath(classPathElements);
            }
            
            if (ClassPath.EXECUTE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    buildRoot
                });
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
    }

    public static class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {
        
        public String getSourceLevel(FileObject javaFile) {
            String level = file2SourceLevel.get(javaFile);
            
            if (level == null) {
                if (javaFile.isFolder()) {
                    for (FileObject data : file2SourceLevel.keySet()) {
                        if (FileUtil.isParentOf(javaFile, data)) {
                            return file2SourceLevel.get(data);
                        }
                    }
                }
                return "1.5";
            } else
                return level;
        }
        
    }

    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    public static JavacTask getJavacTaskFor(CompilationInfo info) {
        return info.impl.getJavacTask();
    }
    
    /** Blocking call for CompilationInfo after given phase is reached.
     *  @param phase to be reached
     *  @return CompilationInfo or null
     *  XXX: REMOVE ME!!!!!!!
     */
    public static CompilationInfo getCompilationInfo(JavaSource js, Phase phase ) throws IOException {        
        if (phase == null || phase == Phase.MODIFIED) { 
            throw new IllegalArgumentException (String.format("The %s is not a legal value of phase",phase));   //NOI18N
        }
        final DeadlockTask bt = new DeadlockTask(phase);
        js.runUserActionTask(bt,true);
        return bt.info;
    }
    
    
    private static class DeadlockTask implements Task<CompilationController> {
        
        private final Phase phase;
        private CompilationInfo info;
        
        public DeadlockTask(Phase phase) {
            assert phase != null;
            this.phase = phase;
        }
        
        public void run( CompilationController info ) {
            try {
                info.toPhase(this.phase);
                this.info = info;
            } catch (IOException ioe) {
//                ErrorManager.getDefault().notify(ioe);
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }                
        
    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavacParserProvider implements MimeDataProvider {

        private Lookup javaLookup = Lookups.fixed(new JavacParserFactory());

        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
            }

            return Lookup.EMPTY;
        }
        
    }

    @ServiceProvider(service=MIMEResolver.class)
    public static final class JavaMimeResolver extends MIMEResolver {

        public JavaMimeResolver() {
            super(JavacParser.MIME_TYPE);
        }

        @Override
        public String findMIMEType(FileObject fo) {
            if ("java".equals(fo.getExt())) {
                return JavacParser.MIME_TYPE;
            }

            return null;
        }

    }
    
}
