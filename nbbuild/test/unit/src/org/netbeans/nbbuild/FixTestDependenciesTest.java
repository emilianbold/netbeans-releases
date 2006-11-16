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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author pzajac
 */
public class FixTestDependenciesTest extends NbTestCase {

    public FixTestDependenciesTest(String testName) {
        super(testName);
    }

    public void testSimple() throws IOException, Exception {
          File prjFile = copyFile("FixTestDependenciesProject.xml");
          File propertiesFile = copyFile("FixTestDependencies.properties");
          doFixProjectXml(propertiesFile, prjFile);
          doFixProjectXml(propertiesFile, copyFile("FixTestDependenciesProject2.xml"));
    }
    
    public void testStandalone() throws IOException, Exception {
        File prjFile = copyFile("FixTestDependenciesProjectStandalone.xml");
        File propertiesFile = copyFile("FixTestDependencies.properties");
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();
        assertFile(copyFile("FixTestDependenciesProjectStandalonePass.xml"),prjFile);
        assertFile(copyFile("FixTestDependenciesPass.properties"),propertiesFile);
    }

    public void testWrongBuilClassDep() throws IOException {
        FixTestDependencies ftd = newFixTestDependencies();
        Set/*<String>*/ cnb = new HashSet();
        Set/*<String>*/ testCnb = new HashSet();
 
        Properties props = new Properties();
        String PNAME = "cp.extra";
        String PVALUE = "../build/test/unit/classes";
        props.setProperty(PNAME,PVALUE);
        ftd.readCodeNameBases(cnb,testCnb,props,"cp.extra",Collections.EMPTY_SET,Collections.EMPTY_SET);
        assertEquals("No dependency on module.",0,cnb.size());        
        assertEquals("No test dependency on module.",0,testCnb.size()); 
        assertEquals("property value",PVALUE,props.getProperty(PNAME));
    }

    private FixTestDependencies newFixTestDependencies() throws IOException, BuildException {
        Project project = new Project();
        project.setBaseDir(getWorkDir());
        FixTestDependencies ftd = new FixTestDependencies();
        ftd.setProject(project);
        return ftd;
    }
    private void doFixProjectXml(final File propertiesFile, final File prjFile) throws Exception, IOException {
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();
        assertFile(copyFile("FixTestDependenciesProjectPass.xml"),prjFile);
        assertFile(copyFile("FixTestDependenciesPass.properties"),propertiesFile);
    }

    private File copyFile(String resourceName) throws IOException {
       InputStream is = getClass().getResourceAsStream(resourceName);
       byte buf[] = new byte[10000];
       File retFile = new File(getWorkDir(),resourceName);
       FileOutputStream fos = new FileOutputStream(retFile);
       int size;
       while ((size = is.read(buf)) > 0 ) {
           fos.write(buf,0,size);
       }
       is.close();
       fos.close();
       return retFile;
    }

    /**
     *  generates body of method the method creates the content of entries
     */ 
    private void logEntries(HashSet cachedCNB, Set entries) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream("/tmp/test",true);
       try {
        PrintStream ps = new PrintStream(fos);
        ps.println("Set getEntris() {");
        ps.println("   Set entries = new HashSet();");
        for (Iterator it = cachedCNB.iterator(); it.hasNext();) {
            String cnb = (String) it.next();
            for (Iterator eIt = entries.iterator() ; eIt.hasNext() ;) {
              ModuleListParser.Entry entry = (ModuleListParser.Entry) eIt.next(); 
              if (!cnb.equals(entry.getCnb())) {
                  continue;
              }
              ps.println("entries.add(new ModuleListParser.Entry(\"" + entry.getCnb() + "\"," + 
                                                                   getLogFileName(entry.getJar()) + ",");
              File files[] = entry.getClassPathExtensions();
              if (files.length == 0) {
                  ps.print("    new File[0],");
              } else {
                  ps.print("    new File[]{");
                  for (int ce = 0 ; ce < files.length ; ce++) {
                      if (ce != 0) {
                          ps.print(",");
                      } 
                      ps.print(getLogFileName(files[ce]));
                  }
                  ps.println("},");
              }
              ps.println("    null,\"" + entry.getNetbeansOrgPath() + "\",");
              
              
              String bp[] = entry.getBuildPrerequisites();
              if (bp.length == 0) {
                  ps.println("   new String[0],");
              } else {
                  ps.print("    new String[]{");
                  for (int ce = 0 ; ce < bp.length ; ce++) {
                      if (ce != 0) {
                          ps.print(",");
                      } 
                      ps.print("\"" + bp[ce] + "\"");
                  }
                  ps.println("},");
              }
              ps.println("    \"" + entry.getClusterName() + "\",");
              bp = entry.getRuntimeDependencies();
              if (bp.length == 0) {
                  ps.print("   new String[0]");
              } else {
                  ps.print("    new String[]{");
                  for (int ce = 0 ; ce < bp.length ; ce++) {
                      if (ce != 0) {
                          ps.print(",");
                      } 
                      ps.print("\"" + bp[ce] + "\"");
                  }
                  ps.print("}");
              }
              ps.println("));");
            } 
        }
        ps.println("}");
       } finally {
           fos.close();
       }
    }

    private String getLogFileName(File file) {
        String path = file.getAbsolutePath();
        String NB = "/netbeans/";
        int idx = path.indexOf(NB);
        String name = (idx == -1) ? "nonsence:" + file.getPath() : path.substring(idx + NB.length());
        return "new File(\"" + name + "\")"; 
  
    }    

     private Set getEntries() {
           Set entries = new HashSet();
        entries.add(new ModuleListParser.Entry("org.openide.io",new File("extra/modules/org-openide-io.jar"),
            new File[0],    null,"openide/io",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.compat",new File("extra/modules/org-openide-compat.jar"),
            new File[0],    null,"openide/compat",
            new String[]{"org.netbeans.core","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.core","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"}));
        entries.add(new ModuleListParser.Entry("org.netbeans.modules.projectapi",new File("extra/modules/org-netbeans-modules-projectapi.jar"),
            new File[0],    null,"projects/projectapi",
            new String[]{"org.netbeans.modules.queries","org.openide.filesystems","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.modules.queries","org.openide.filesystems","org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.loaders",new File("extra/modules/org-openide-loaders.jar"),
            new File[0],    null,"openide/loaders",
            new String[]{"org.netbeans.api.progress","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.modules","org.openide.nodes","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.api.progress","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.modules","org.openide.nodes","org.openide.text","org.openide.util","org.openide.windows"}));
        entries.add(new ModuleListParser.Entry("org.netbeans.core",new File("extra/modules/org-netbeans-core.jar"),
            new File[0],    null,"core",
            new String[]{"org.netbeans.bootstrap","org.netbeans.core.startup","org.netbeans.swing.plaf","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.loaders","org.openide.modules","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.bootstrap","org.netbeans.core.startup","org.netbeans.swing.plaf","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.loaders","org.openide.modules","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"}));
        entries.add(new ModuleListParser.Entry("org.netbeans.modules.masterfs",new File("extra/modules/org-netbeans-modules-masterfs.jar"),
            new File[0],    null,"openide/masterfs",
            new String[]{"org.openide.filesystems","org.openide.util","org.openide.options","org.netbeans.modules.queries"},
            "extra",
            new String[]{"org.openide.filesystems","org.openide.util","org.openide.options","org.netbeans.modules.queries"}));
        entries.add(new ModuleListParser.Entry("org.netbeans.bootstrap",new File("extra/lib/boot.jar"),
            new File[0],    null,"core/bootstrap",
            new String[]{"org.openide.modules","org.openide.util"},
            "extra",
            new String[]{"org.openide.modules","org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.netbeans.libs.xerces",new File("extra/modules/org-netbeans-libs-xerces.jar"),
            new File[]{new File("nonsence:/home/pzajac/cvss/freshtrunk/libs/external/xerces-2.8.0.jar"),new File("nonsence:/home/pzajac/cvss/freshtrunk/libs/external/xml-commons-dom-ranges-1.0.b2.jar")},
            null,"libs/xerces",
           new String[0],
            "extra",
           new String[0]));
        entries.add(new ModuleListParser.Entry("org.netbeans.api.progress",new File("extra/modules/org-netbeans-api-progress.jar"),
            new File[0],    null,"core/progress",
            new String[]{"org.openide.util","org.openide.awt"},
            "extra",
            new String[]{"org.openide.util","org.openide.awt"}));
        entries.add(new ModuleListParser.Entry("org.openide.options",new File("extra/modules/org-openide-options.jar"),
            new File[0],    null,"openide/options",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.explorer",new File("extra/modules/org-openide-explorer.jar"),
            new File[0],    null,"openide/explorer",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.dialogs","org.openide.options"},
            "extra",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.dialogs","org.openide.options"}));
        entries.add(new ModuleListParser.Entry("org.openide.dialogs",new File("extra/modules/org-openide-dialogs.jar"),
            new File[0],    null,"openide/dialogs",
            new String[]{"org.netbeans.api.progress","org.openide.awt","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.api.progress","org.openide.awt","org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.nodes",new File("extra/modules/org-openide-nodes.jar"),
            new File[0],    null,"openide/nodes",
            new String[]{"org.openide.util","org.openide.awt","org.openide.dialogs"},
            "extra",
            new String[]{"org.openide.util","org.openide.awt","org.openide.dialogs"}));
        entries.add(new ModuleListParser.Entry("org.openide.awt",new File("extra/modules/org-openide-awt.jar"),
            new File[0],    null,"openide/awt",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.text",new File("extra/modules/org-openide-text.jar"),
            new File[0],    null,"openide/text",
            new String[]{"org.netbeans.modules.editor.mimelookup","org.openide.awt","org.openide.dialogs","org.openide.nodes","org.openide.options","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.modules.editor.mimelookup","org.openide.awt","org.openide.dialogs","org.openide.nodes","org.openide.options","org.openide.util","org.openide.windows"}));
        entries.add(new ModuleListParser.Entry("org.openide.actions",new File("extra/modules/org-openide-actions.jar"),
            new File[0],    null,"openide/actions",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.options","org.openide.text","org.openide.explorer","org.openide.dialogs","org.openide.windows"},
            "extra",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.options","org.openide.text","org.openide.explorer","org.openide.dialogs","org.openide.windows"}));
        entries.add(new ModuleListParser.Entry("org.openide.util",new File("extra/lib/org-openide-util.jar"),
            new File[0],    null,"openide/util",
           new String[0],
            "extra",
           new String[0]));
        entries.add(new ModuleListParser.Entry("org.netbeans.core.startup",new File("extra/core/core.jar"),
            new File[0],    null,"core/startup",
            new String[]{"org.netbeans.bootstrap","org.openide.filesystems","org.openide.modules","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.bootstrap","org.openide.filesystems","org.openide.modules","org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.modules",new File("extra/lib/org-openide-modules.jar"),
            new File[0],    null,"openide/modules",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"}));
        entries.add(new ModuleListParser.Entry("org.openide.filesystems",new File("extra/core/org-openide-filesystems.jar"),
            new File[0],    null,"openide/fs",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"}));
        return entries;
    } 
}
    