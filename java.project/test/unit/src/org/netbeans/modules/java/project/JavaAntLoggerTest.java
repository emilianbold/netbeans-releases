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

package org.netbeans.modules.java.project;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Test hyperlinking functionality of {@link JavaAntLogger}.
 * @author Jesse Glick
 */
public final class JavaAntLoggerTest extends NbTestCase {
    
    public JavaAntLoggerTest(String name) {
        super(name);
    }
    
    private File simpleAppDir;
    private Properties props;
    
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {IOP.class, IFL.class, SFBQ.class});
        simpleAppDir = new File(getDataDir(), "simple-app");
        assertTrue("have dir " + simpleAppDir, simpleAppDir.isDirectory());
        ((SFBQ) Lookup.getDefault().lookup(SFBQ.class)).setSimpleAppDir(simpleAppDir);
        nonhyperlinkedOut.clear();
        nonhyperlinkedErr.clear();
        hyperlinkedOut.clear();
        hyperlinkedErr.clear();
        String junitJarS = System.getProperty("test.junit.jar");
        assertNotNull("defined test.junit.jar", junitJarS);
        File junitJar = new File(junitJarS);
        assertTrue("file " + junitJar + " exists", junitJar.isFile());
        props = new Properties();
        props.setProperty("libs.junit.classpath", junitJar.getAbsolutePath()); // #50261
    }
    
    public void testHyperlinkRun() throws Exception {
        FileObject buildXml = FileUtil.toFileObject(new File(simpleAppDir, "build.xml"));
        assertNotNull("have build.xml as a FileObject", buildXml);
        ActionUtils.runTarget(buildXml, new String[] {"clean", "run"}, props).result();
        //System.out.println("nonhyperlinkedOut=" + nonhyperlinkedOut + " nonhyperlinkedErr=" + nonhyperlinkedErr + " hyperlinkedOut=" + hyperlinkedOut + " hyperlinkedErr=" + hyperlinkedErr);
        assertTrue("got a hyperlink for Clazz.run NPE", hyperlinkedErr.contains("\tat simpleapp.Clazz.run(Clazz.java:4)"));
    }
    
    /** See #44328. */
    public void testHyperlinkTest() throws Exception {
        FileObject buildXml = FileUtil.toFileObject(new File(simpleAppDir, "build.xml"));
        assertNotNull("have build.xml as a FileObject", buildXml);
        ActionUtils.runTarget(buildXml, new String[] {"clean", "test"}, props).result();
        //System.out.println("nonhyperlinkedOut=" + nonhyperlinkedOut + " nonhyperlinkedErr=" + nonhyperlinkedErr + " hyperlinkedOut=" + hyperlinkedOut + " hyperlinkedErr=" + hyperlinkedErr);
        assertTrue("got a hyperlink for Clazz.run NPE in " + hyperlinkedErr, hyperlinkedErr.contains("\tat simpleapp.Clazz.run(Clazz.java:4)"));
    }
    
    public static final class SFBQ implements SourceForBinaryQueryImplementation {
        
        private URL buildClasses, buildTestClasses;
        private FileObject src, testSrc;
        
        public void setSimpleAppDir(File simpleAppDir) throws Exception {
            buildClasses = slashify(new File(simpleAppDir, "build" + File.separatorChar + "classes").toURI().toURL());
            buildTestClasses = slashify(new File(simpleAppDir, "build" + File.separatorChar + "test" + File.separatorChar + "classes").toURI().toURL());
            src = FileUtil.toFileObject(new File(simpleAppDir, "src"));
            testSrc = FileUtil.toFileObject(new File(simpleAppDir, "test"));
        }
        
        private static URL slashify(URL u) throws Exception {
            String s = u.toExternalForm();
            if (s.endsWith("/")) {
                return u;
            } else {
                return new URL(s + "/");
            }
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (binaryRoot.equals(buildClasses)) {
                return new FixedResult(src);
            } else if (binaryRoot.equals(buildTestClasses)) {
                return new FixedResult(testSrc);
            } else {
                return null;
            }
        }
        
        private static final class FixedResult implements SourceForBinaryQuery.Result {
            
            private final FileObject dir;
            
            public FixedResult(FileObject dir) {
                this.dir = dir;
            }

            public FileObject[] getRoots() {
                return new FileObject[] {dir};
            }

            public void addChangeListener(ChangeListener l) {}

            public void removeChangeListener(ChangeListener l) {}
            
        }
        
    }
    
    public static final class IOP extends IOProvider implements InputOutput {
        
        public IOP() {}

        public InputOutput getIO(String name, boolean newIO) {
            return this;
        }

        public OutputWriter getStdOut() {
            throw new UnsupportedOperationException();
        }

        public OutputWriter getOut() {
            return new OW(false);
        }

        public OutputWriter getErr() {
            return new OW(true);
        }

        public Reader getIn() {
            return new StringReader("");
        }

        public Reader flushReader() {
            return getIn();
        }

        public void closeInputOutput() {}

        public boolean isClosed() {
            return false;
        }

        public boolean isErrSeparated() {
            return false;
        }

        public boolean isFocusTaken() {
            return false;
        }

        public void select() {}

        public void setErrSeparated(boolean value) {}

        public void setErrVisible(boolean value) {}

        public void setFocusTaken(boolean value) {}

        public void setInputVisible(boolean value) {}

        public void setOutputVisible(boolean value) {}
        
    }
    
    private static final List/*<String>*/ nonhyperlinkedOut = new ArrayList();
    private static final List/*<String>*/ nonhyperlinkedErr = new ArrayList();
    private static final List/*<String>*/ hyperlinkedOut = new ArrayList();
    private static final List/*<String>*/ hyperlinkedErr = new ArrayList();
    
    private static final class OW extends OutputWriter {
        
        private final boolean err;
        
        public OW(boolean err) {
            super(new StringWriter());
            this.err = err;
        }

        public void println(String s, OutputListener l) throws IOException {
            message(s, l != null);
        }

        public void println(String x) {
            message(x, false);
        }
        
        private void message(String msg, boolean hyperlinked) {
            List/*<String>*/ messages = hyperlinked ?
                (err ? hyperlinkedErr : hyperlinkedOut) :
                (err ? nonhyperlinkedErr : nonhyperlinkedOut);
            messages.add(msg);
        }
        
        public void reset() throws IOException {}

    }

    /** Copied from AntLoggerTest. */
    public static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
