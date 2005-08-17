/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;

/**
 * Basic setup for all the tests.
 * @author Jesse Glick
 */
public abstract class TestBase extends NbTestCase {
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected static String EEP = "apisupport/project/test/unit/data/example-external-projects";
    
    /** Represents netbeans.org CVS tree this test is run in. */
    protected File nbrootF;

    /** Represents netbeans.org CVS tree this test is run in. */
    protected FileObject nbroot;

    protected File extexamplesF;
    protected FileObject extexamples;
    protected File apisZip;
    protected void setUp() throws Exception {
        super.setUp();
        nbrootF = new File(System.getProperty("test.nbroot"));
        assertTrue("there is a dir " + nbrootF, nbrootF.isDirectory());
        assertTrue("nbbuild exists", new File(nbrootF, "nbbuild").isDirectory());
        nbroot = FileUtil.toFileObject(nbrootF);
        assertNotNull("have a file object for nbroot", nbroot);
        extexamplesF = file(nbrootF, EEP);
        assertTrue("there is a dir " + extexamplesF, extexamplesF.isDirectory());
        extexamples = FileUtil.toFileObject(extexamplesF);
        assertNotNull("have a file object for extexamples", extexamples);
        // Need to set up private locations in extexamples, as if they were opened in the IDE.
        clearWorkDir();
        // Nonexistent path, just for JavadocForBuiltModuleTest:
        apisZip = new File(getWorkDir(), "apis.zip");
        File userPropertiesFile = initializeBuildProperties(getWorkDir(), apisZip);
        String[] suites = {
            // Suite projects:
            "suite1",
            "suite2",
            // Standalone module projects:
            "suite3/dummy-project",
        };
        for (int i = 0; i < suites.length; i++) {
            File platformPrivate = file(extexamplesF, suites[i] + "/nbproject/private/platform-private.properties");
            Properties p = new Properties();
            p.setProperty("user.properties.file", userPropertiesFile.getAbsolutePath());
            platformPrivate.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(platformPrivate);
            try {
                p.store(os, null);
            } finally {
                os.close();
            }
        }
        NbPlatform.reset();
    }
    
    /**
     * Sets up global build.properties for the default platform.
     * For {@link PropertyUtils#userBuildProperties()}.
     * Called automatically by {@link #setUp}.
     * @param workDir use getWorkDir()
     */
    public static File initializeBuildProperties(File workDir) throws Exception {
        return initializeBuildProperties(workDir, null);
    }
    private static File initializeBuildProperties(File workDir, File apisZip) throws Exception {
        File nbrootF = new File(System.getProperty("test.nbroot"));
        assertTrue("there is a dir " + nbrootF, nbrootF.isDirectory());
        assertTrue("nbbuild exists", new File(nbrootF, "nbbuild").isDirectory());
        System.setProperty("netbeans.user", workDir.getAbsolutePath());
        File userPropertiesFile = new File(workDir, "build.properties");
        Properties p = new Properties();
        p.setProperty("nbplatform.default.netbeans.dest.dir", file(nbrootF, "nbbuild/netbeans").getAbsolutePath());
        p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness");
        p.setProperty("nbplatform.custom.netbeans.dest.dir", file(nbrootF, EEP + "/suite3/nbplatform").getAbsolutePath());
        if (apisZip != null) {
            p.setProperty("nbplatform.default.javadoc", apisZip.getAbsolutePath());
        }
        // Make source association work to find misc-project from its binary:
        p.setProperty("nbplatform.default.sources", nbrootF.getAbsolutePath() + ":" + file(nbrootF, EEP + "/suite2").getAbsolutePath());
        OutputStream os = new FileOutputStream(userPropertiesFile);
        try {
            p.store(os, null);
        } finally {
            os.close();
        }
        return userPropertiesFile;
    }
    
    /**
     * Just calls <code>File(root, path.replace('/', File.separatorChar));</code>
     */
    protected static File file(File root, String path) {
        return new File(root, path.replace('/', File.separatorChar));
    }
    
    /**
     * Calls in turn {@link #file(File, String)} with {@link #nbrootF} as the
     * first parameter. So the returned path will be actually relative to the
     * netbeans.org CVS tree this test is run in.
     */
    protected File file(String path) {
        return file(nbrootF, path);
    }
    
    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.
     * Stolen from ant/freeform.
     */
    protected File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = workdir.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }
    
    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (from.getName().equals("CVS")) {
                return;
            }
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }
    
    public static String slurp(FileObject fileObject) throws IOException {
        InputStream is = fileObject.getInputStream();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }
    
    // XXX copied from TestBase in ant/freeform
    protected static final class TestPCL implements PropertyChangeListener {
        
        public final Set/*<String>*/ changed = new HashSet();
        public final Map/*<String,String*/ newvals = new HashMap();
        public final Map/*<String,String*/ oldvals = new HashMap();
        
        public TestPCL() {}
        
        public void reset() {
            changed.clear();
            newvals.clear();
            oldvals.clear();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            String nue = (String)evt.getNewValue();
            String old = (String)evt.getOldValue();
            changed.add(prop);
            if (prop != null) {
                newvals.put(prop, nue);
                oldvals.put(prop, old);
            } else {
                assert nue == null : "null prop name -> null new value";
                assert old == null : "null prop name -> null old value";
            }
        }
        
    }
    
    /**
     * Returns {@link NbModuleProject} created in the {@link
     * #getWorkDir()}/prjDir with code name base default to <em>org.example +
     * dotted prjDir</em> which is also used as the <em>default</em> package so
     * the layer and bundle are generated accordingly. Default module's display
     * name is set to <em>Testing Module</em>. So final set of generated files
     * for <em>module1</em> as the parameter may look like:
     *
     * <ul>
     *   <li>module1/manifest.mf
     *   <li>module1/nbproject/platform.properties
     *   <li>module1/nbproject/project.xml
     *   <li>module1/src/org/example/module1/resources/Bundle.properties
     *   <li>module1/src/org/example/module1/resources/layer.xml
     * </ul>
     * Do not forget to first call {@link #initializeBuildProperties} if you are not a TestBase subclass!
     */
    public static NbModuleProject generateStandaloneModule(File workDir, String prjDir) throws IOException {
        String prjDirDotted = prjDir.replace('/', '.');
        File prjDirF = file(workDir, prjDir);
        NbModuleProjectGenerator.createStandAloneModule(
                prjDirF,
                "org.example." + prjDirDotted, // cnb
                "Testing Module", // display name
                "org/example/" + prjDir + "/resources/Bundle.properties",
                "org/example/" + prjDir + "/resources/layer.xml",
                "default"); // platform id
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        return (NbModuleProject) ProjectManager.getDefault().findProject(prjDirFO);
    }

}
