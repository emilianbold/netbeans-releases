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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ClassIndexAdapter;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public abstract class JavaSourceTestCase extends NbTestCase {

    // XXX make thread-safe

    private ClassPathProviderImpl cpProvider;

    protected FileObject srcFO;
    protected List<FileObject> roots;

    protected ClassPathImpl srcCPImpl;
    protected ClassPathImpl compileCPImpl;

    protected ClassPath srcCP;
    protected ClassPath compileCP;
    protected ClassPath bootCP;

    private CountDownLatch blockLatch;

    public JavaSourceTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockLookup.init();
        clearWorkDir();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        srcFO = FileUtil.toFileObject(getWorkDir()).createFolder("src");
        srcCPImpl = new ClassPathImpl(URLMapper.findURL(srcFO, URLMapper.INTERNAL));
        srcCP = ClassPathFactory.createClassPath(srcCPImpl);
        compileCPImpl = new ClassPathImpl();
        compileCP = ClassPathFactory.createClassPath(compileCPImpl);
        bootCP = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        roots = new ArrayList<FileObject>();
        roots.add(srcFO);
        cpProvider = new ClassPathProviderImpl();
        MockLookup.setInstances(cpProvider);
    }

    protected void tearDown() {
        if (blockLatch != null) {
            resumeClassPathScan();
        }
        MockLookup.setInstances();
    }

    protected void addSourceRoots(List<FileObject> roots) {
        this.roots.addAll(roots);
        List<URL> urls = new ArrayList<URL>(roots.size());
        for (FileObject root : roots) {
            urls.add(URLMapper.findURL(root, URLMapper.INTERNAL));
        }
        srcCPImpl.addResources(urls);
    }

    protected void removeSourceRoots(List<FileObject> roots) {
        this.roots.removeAll(roots);
        List<URL> urls = new ArrayList<URL>(roots.size());
        for (FileObject root : roots) {
            urls.add(URLMapper.findURL(root, URLMapper.INTERNAL));
        }
        srcCPImpl.removeResources(urls);
    }

    protected void addCompileRoots(List<URL> roots) {
        compileCPImpl.addResources(roots);
    }

    protected void startAndBlockClassPathScan() throws IOException, InterruptedException {
        if (blockLatch != null) {
            resumeClassPathScan();
        }
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject blockFO = FileUtil.createFolder(workDir, FileUtil.findFreeFolderName(workDir, "cp-scan-block-root"));
        MockLookup.setInstances(cpProvider, new SimpleClassPathProvider(blockFO));
        System.out.println(System.identityHashCode(ClassPath.getClassPath(blockFO, ClassPath.SOURCE)));
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(blockFO, blockFO).await();
        final CountDownLatch waitLatch = new CountDownLatch(1);
        blockLatch = new CountDownLatch(1);
        ClassIndex index = ClasspathInfo.create(blockFO).getClassIndex();
        index.addClassIndexListener(new ClassIndexAdapter() {
            public void typesAdded(TypesEvent event) {
                waitLatch.countDown();
                try {
                    blockLatch.await();
                } catch (InterruptedException e) {
                    throw new Error(e);
                }
            }
        });
        TestUtilities.copyStringToFileObject(blockFO, "Dummy.java", "public class Dummy {}");
        waitLatch.await();
    }

    protected void resumeClassPathScan() {
        blockLatch.countDown();
        blockLatch = null;
    }

    public final class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            boolean found = false;
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    found = true;
                }
            }
            if (!found) {
                return null;
            }
            if (ClassPath.SOURCE.equals(type)) {
                return srcCP;
            } else if (ClassPath.COMPILE.equals(type)) {
                return compileCP;
            } else if (ClassPath.BOOT.equals(type)) {
                return bootCP;
            }
            return null;
        }
    }

    private static final class ClassPathImpl implements ClassPathImplementation {

        private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
        private final List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();

        public ClassPathImpl() {}

        public ClassPathImpl(URL url) {
            addResource(url);
        }

        public void addResources(List<URL> urls) {
            for (URL url : urls) {
                addResource(url);
            }
            propSupport.firePropertyChange(PROP_RESOURCES, null, null);
        }

        public void removeResources(List<URL> urls) {
            boolean modified = false;
            main: for (URL url : urls) {
                for (PathResourceImplementation resource : resources) {
                    for (URL resourceRoot : resource.getRoots()) {
                        if (resourceRoot.equals(url)) {
                            resources.remove(resource);
                            modified = true;
                            break main;
                        }
                    }
                }
            }
            if (modified) {
                propSupport.firePropertyChange(PROP_RESOURCES, null, null);
            }
        }

        private void addResource(URL url) {
            resources.add(ClassPathSupport.createResource(url));
        }

        public List<? extends PathResourceImplementation> getResources() {
            return resources;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propSupport.removePropertyChangeListener(listener);
        }
    }
}
