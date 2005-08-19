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
package org.netbeans.nbbuild;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.netbeans.junit.NbTestCase;

/** Check behaviour of ModuleSelector.
 *
 * @author Jaroslav Tulach
 */
public class ModuleSelectorTest extends NbTestCase {
    private ModuleSelector selector;
    
    public ModuleSelectorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        selector = new ModuleSelector();
    }

    protected void tearDown() throws Exception {
    }

    public void testIsSelectedForNotAModule() throws IOException {
        File noModule = generateJar(new String[0], ModuleDependenciesTest.createManifest ());
        assertFalse("Not acceptable", selector.isSelected(getWorkDir(), noModule.toString(), noModule));
    }

    public void testIncludesAllModulesByDefault() throws Exception {
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertTrue("Accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testCanExcludeAModule() throws Exception {
        Parameter p = new Parameter();
        p.setName("excludeModules");
        p.setValue("org.my.module");
        selector.setParameters(new Parameter[] { p });
        
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertFalse("Refused", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testCanExcludeACluster() throws Exception {
        Parameter p = new Parameter();
        p.setName("excludeClusters");
        p.setValue(getWorkDir().getName());
        selector.setParameters(new Parameter[] { p });
        
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), aModule.toString(), aModule));
    }
    
    public void testWhatItDoesOnADirectory() throws Exception {
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), getWorkDir().getName(), getWorkDir()));
    }
    
    public void testNoManifest() throws Exception {
        File aModule = generateJar(new String[] { "some/fake/entry.txt" }, null);
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), aModule.toString(), aModule));
    }

    public void testParsingOfUpdateTrackingFiles() throws Exception {
        doParsingOfUpdateTrackingFiles(1);
    }
    
    public void testParsingOfUpdateTrackingFilesOnMoreDirs() throws Exception {
        doParsingOfUpdateTrackingFiles(2);
    }
    
    
    private void doParsingOfUpdateTrackingFiles(int parents) throws Exception {
        File updateTracking = new File(getWorkDir(), "update-tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        
        File trackingFile = new File(getWorkDir(), "org-my-module.xml");
        FileWriter w = new FileWriter(trackingFile);
        w.write(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<module codename='org.apache.tools.ant.module/3'>\n" +
    "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
        "<file name='ant/bin/ant' crc='1536373800'/>\n" +
        "<file name='ant/bin/ant.bat' crc='3245456472'/>\n" +
        "<file name='ant/bin/ant.cmd' crc='3819623376'/>\n" +
        "<file name='ant/bin/antRun' crc='2103827286'/>\n" +
        "<file name='ant/bin/antRun.bat' crc='2739687679'/>\n" +
        "<file name='ant/bin/antRun.pl' crc='3955456526'/>\n" +
"    </module_version>\n" +
"</module>\n"
        );
        w.close();

        StringBuffer sb = new StringBuffer();
        sb.append(trackingFile.getPath());
        
        while (--parents > 0) {
            File x = new File(getWorkDir(), parents + ".xml");
            FileWriter xw = new FileWriter(x);
            xw.write(
    "<?xml version='1.0' encoding='UTF-8'?>\n" +
    "<module codename='" + x + "/3'>\n" +
        "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
    "    </module_version>\n" +
    "</module>\n"
            );
            xw.close();
            
            sb.insert(0, File.pathSeparator);
            sb.insert(0, x.getPath());
        }
        
        Parameter p = new Parameter();
        p.setName("updateTrackingFiles");
        p.setValue(sb.toString());
        selector.setParameters(new Parameter[] { p });
        
        assertTrue("module accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
        assertTrue("its file as well", selector.isSelected(getWorkDir(), "ant/bin/ant.bat", new File(aModule.getParent(), "ant/bin/ant.bat")));
    }
    
    private final File createNewJarFile () throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }

    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
}
