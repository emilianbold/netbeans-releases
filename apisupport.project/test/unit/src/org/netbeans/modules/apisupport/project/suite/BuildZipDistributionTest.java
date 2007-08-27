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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * Checks building of ZIP support.
 * @author Jaroslav Tulach
 */
public class BuildZipDistributionTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    private SuiteProject suite;
    private Logger LOG;
    
    public BuildZipDistributionTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    
    protected @Override void setUp() throws Exception {
        clearWorkDir();

        LOG = Logger.getLogger("test." + getName());
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        suite.open();
        proj.open();
        
        LOG.info("Workdir " + getWorkDirPath());
    }
    
    public void testBuildTheZipAppWhenAppNamePropIsNotSet() throws Exception {
        SuiteActions p = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here", p);
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-zip: " + l, l.contains("build-zip"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        ExecutorTask task = p.invokeActionImpl("build-zip", suite.getLookup());
        assertNull("did not even run task", task);
    }
    
    public void testBuildTheZipAppWhenAppNamePropIsSet() throws Exception {
        EditableProperties ep = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "fakeapp");
        
        ep.setProperty("enabled.clusters", TestBase.CLUSTER_PLATFORM);
        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
            "org.openide.compat," +
            "org.netbeans.api.progress," +
            "org.netbeans.core.multiview," +
            "org.openide.util.enumerations" +
            "");
        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(suite);
        
        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here", p);
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-zip: " + l, l.contains("build-zip"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        ExecutorTask task = p.invokeActionImpl("build-zip", suite.getLookup());
        
        assertNotNull("Task was started", task);
        assertEquals("Finished ok", 0, task.result());
        
        FileObject[] arr = suite.getProjectDirectory().getChildren();
        List<FileObject> subobj = new ArrayList<FileObject>(Arrays.asList(arr));
        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
        assertNotNull("dist created", dist);
        subobj.remove(dist);
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
        
        FileObject zip = dist.getFileObject("fakeapp.zip");
        assertNotNull("ZIP file created: " + zip, zip);
        
        File zipF = FileUtil.toFile(zip);
        JarFile zipJ = new JarFile(zipF);
        Enumeration en = zipJ.entries();
        int cntzip = 0;
        
        StringBuffer sb = new StringBuffer();
        StringBuffer hidden = new StringBuffer();
        while (en.hasMoreElements()) {
            JarEntry entry = (JarEntry)en.nextElement();
            sb.append("\n");
            sb.append(entry.getName());
            cntzip++;
            
            if (entry.getName().endsWith("_hidden")) {
                hidden.append("\n");
                hidden.append(entry.getName());
            }
        }
        
        if (cntzip == 0) {
            fail("There should be at least one zip entry: " + sb);
        }
        
        if (hidden.length() != 0) {
            fail("There should be no hidden files in the zip file: " + hidden);
        }
        
        File expand = new File(getWorkDir(), "expand");
        JarFile f = new JarFile(FileUtil.toFile(zip));
        for (JarEntry jarEntry : NbCollections.iterable(f.entries())) {
            String path = jarEntry.getName().replace('/', File.separatorChar);
            if (path.endsWith("/")) {
                continue;
            }
            File entry = new File(expand, path);
            entry.getParentFile().mkdirs();
            FileOutputStream os = new FileOutputStream(entry);
            FileUtil.copy(f.getInputStream(jarEntry), os);
            os.close();
        }

        File root = new File(expand, "fakeapp");
        File launch;
        if (Utilities.isWindows()) {
            launch = new File(new File(root, "bin"), "fakeapp.exe");
        } else {
            launch = new File(new File(root, "bin"), "fakeapp");
        }
        
        assertTrue("Launcher exists " + launch, launch.canRead());
        
        run(launch, "*");
        
        String[] args = MainCallback.getArgs(getWorkDir());
        
        if (!Arrays.asList(args).contains("*")) {
            fail("There should be a * in\n" + Arrays.asList(args));
        }
    }
    
    private File createNewJarFile (String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) {
                f.createNewFile();
                return f;
            }
        }
    }
    
    private void run(File nbexec, String... args) throws Exception {

        URL tu = MainCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = new File(tu.toURI());
        assertTrue("file found: " + testf, testf.exists());
        
        LinkedList<String> allArgs = new LinkedList<String>(Arrays.asList(args));
        allArgs.addFirst("-J-Dnetbeans.mainclass=" + MainCallback.class.getName());
        allArgs.addFirst(getWorkDirPath());
        allArgs.addFirst("--userdir");
        allArgs.addFirst(testf.getPath());
        allArgs.addFirst("-cp:p");
        
        if (!Utilities.isWindows()) {
            allArgs.addFirst(nbexec.getPath());
            allArgs.addFirst("-x");
            allArgs.addFirst("/bin/sh");
        } else {
            allArgs.addFirst(nbexec.getPath());
        }

        String[] envp = {
            "jdkhome=" + System.getProperty("java.home") 
        };
        
        StringBuffer sb = new StringBuffer();
        Process p = Runtime.getRuntime().exec(allArgs.toArray(new String[0]), envp, nbexec.getParentFile());
        int res = readOutput(sb, p);
        
        String output = sb.toString();
        
        assertEquals("Execution is ok: " + output, 0, res);
    }
    
    private static int readOutput(final StringBuffer sb, Process p) throws Exception {
        class Read extends Thread {
            private InputStream is;

            public Read(String name, InputStream is) {
                super(name);
                this.is = is;
                setDaemon(true);
            }

            @Override
            public void run() {
                byte[] arr = new byte[4096];
                try {
                    for(;;) {
                        int len = is.read(arr);
                        if (len == -1) {
                            return;
                        }
                        sb.append(new String(arr, 0, len));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Read out = new Read("out", p.getInputStream());
        Read err = new Read("err", p.getErrorStream());
        out.start();
        err.start();

        int res = p.waitFor();

        out.interrupt();
        err.interrupt();
        out.join();
        err.join();

        return res;
    }
    
}
