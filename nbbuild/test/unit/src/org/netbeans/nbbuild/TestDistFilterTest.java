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
 * 'Portions Copyrighted [year] [name of copyright owner]'
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import junit.framework.*;
import org.apache.tools.ant.Project;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class TestDistFilterTest extends NbTestCase {
    private static final String ORG_OPENIDE_UNIT = "unit/platform5/org-openide";
    private static final String ORG_OPENIDE_FS = "unit/platform5/org-openide-fs";
    private static final String ORG_OPENIDE_LOADERS = "unit/platform5/org-openide-loaders";
    private static final String ORG_OPENIDE_FS_QA = "qa-functional/platform5/org-openide-fs";   
    public TestDistFilterTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testDistFilter() throws IOException {
        TestDistFilter filter = new TestDistFilter();
        filter.setTestDistDir(getWorkDir());
        createModule(ORG_OPENIDE_UNIT,createConfig("unstable","code")); 
        filter.setAttribs("stable");
        filter.setHarness("junit");
        Project prj = getProject();
        filter.setProject(prj);
        filter.setTesttype("unit");
        
        filter.setTestListProperty("list.prop");
        filter.execute();
        assertProperty(prj,"list.prop",new String[]{});
        
        createModule(ORG_OPENIDE_UNIT,createConfig("stable","code")); 
        
        filter.setTestListProperty("list.prop1");
        filter.execute();
        assertProperty(prj,"list.prop1",new String[]{ORG_OPENIDE_UNIT});
        
        createModule(ORG_OPENIDE_FS,createConfig("stable","ide")); 
        filter.setTestListProperty("list.prop2");
        filter.execute();
        assertProperty(prj,"list.prop2",new String[]{ORG_OPENIDE_UNIT});
 
        filter.setTestListProperty("list.prop3");
        createModule(ORG_OPENIDE_LOADERS,createConfig("stable","code")); 
        filter.execute();
        assertProperty(prj,"list.prop3",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_LOADERS});

        createModule(ORG_OPENIDE_FS_QA,createConfig("stable","code"));
        filter.setTestListProperty("list.prop4");
        filter.execute();
        assertProperty(prj,"list.prop4",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_LOADERS});

        filter.setTestListProperty("list.prop5");
        createModule(ORG_OPENIDE_LOADERS,createConfig("unstable","code")); 
        filter.execute();
        assertProperty(prj,"list.prop5",new String[]{ORG_OPENIDE_UNIT});
   
        filter.setHarness("xtest");
        filter.setTestListProperty("list.prop6");
        filter.execute();
        assertProperty(prj,"list.prop6",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_FS});

        filter.setTestListProperty("list.prop7");
        filter.setTesttype("qa-functional");
        filter.execute();
        assertProperty(prj,"list.prop7",new String[]{ORG_OPENIDE_FS_QA});        
   }
    
   public void testAllTestTypes() throws Exception {
        TestDistFilter filter = new TestDistFilter();
        filter.setTestDistDir(getWorkDir());
        Project prj = getProject();
        filter.setProject(prj);
        filter.setTesttype("all");
        filter.setHarness("xtest");
        createModule(ORG_OPENIDE_UNIT,createConfig("stable","code")); 
        createModule(ORG_OPENIDE_FS_QA,createConfig("stable","code"));
        filter.setTestListProperty("list.prop");
        filter.execute();
        assertProperty(prj,"list.prop",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_FS_QA});
   }
   
   public void testComplexAttribs() throws Exception {
        TestDistFilter filter = new TestDistFilter();
        filter.setTestDistDir(getWorkDir());
        Project prj = getProject();
        createModule(ORG_OPENIDE_UNIT,createConfig("stable|xx , aa","code")); 
        filter.setProject(prj);
        filter.setTesttype("all");
        filter.setHarness("xtest");
        filter.setAttribs("stable");
        filter.setTestListProperty("list.prop");
        filter.execute();
        assertProperty(prj,"list.prop",new String[]{ORG_OPENIDE_UNIT});
       
        filter.setAttribs("xx");
        filter.setTestListProperty("list.prop2");
        filter.execute();
        assertProperty(prj,"list.prop2",new String[]{ORG_OPENIDE_UNIT});
 
        filter.setAttribs("aa");
        filter.setTestListProperty("list.prop3");
        filter.execute();
        assertProperty(prj,"list.prop3",new String[]{ORG_OPENIDE_UNIT});

        filter.setAttribs("");
        filter.setTestListProperty("list.prop4");
        filter.execute();
        assertProperty(prj,"list.prop4",new String[]{ORG_OPENIDE_UNIT});
        
        filter.setAttribs(null); 
        filter.setTestListProperty("list.prop5");
        filter.execute();
        assertProperty(prj,"list.prop5",new String[]{ORG_OPENIDE_UNIT});

        filter.setAttribs("nonsence");
        filter.setTestListProperty("list.prop6");
        filter.execute();
        assertProperty(prj,"list.prop6",new String[]{});
   }
    private String createConfig(String attribs,String executor) {   
        return "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE mconfig PUBLIC '-//NetBeans//DTD XTest cfg 1.0//EN' 'http://www.netbeans.org/dtds/xtest-cfg-1_0.dtd'>\n" +
                "<mconfig name='Configuration of refactoring unit tests'>\n" +
                    "<testbag testattribs='" + attribs + "' executor='" + executor + "' name='Refactoring unit tests'>\n" +
                        "<testset dir='unit/src'>\n" +
                            "<patternset>\n" +
                                "<include name='org/netbeans/test/refactoring/encapsulate/EncapsulateTest.class'/>\n" +
                            "</patternset>\n" +
                        "</testset>\n" +
                    "</testbag>\n" +
                "</mconfig>\n";
    }

    private void createModule(String path, String xml) throws IOException {
        File dir = new File(getWorkDir(),path);
        dir.mkdirs();
        File cfg = new File(dir,(path.startsWith("unit")) ? "cfg-unit.xml" : "cfg-qa-functional.xml");
        PrintStream ps = new PrintStream(cfg);
        try {
            ps.print(xml);
        } finally {
            ps.close();
        }
    }

    private Project getProject() throws IOException {
        Project project = new Project();
        project.setBaseDir(getWorkDir());
        return project;
    }

    private void assertProperty(Project prj, String propName, String modules[]) throws IOException {
        String listModules = prj.getProperty(propName);
        assertNotNull("prop " + propName + " was not defined",listModules);
        log(" listModules " + listModules);
        String arrayModules[] = (listModules.length() == 0) ? new String[0] :listModules.split(":");
        Set set1 = new HashSet();
        for (int i = 0 ; i < arrayModules.length ; i++) {
            String module = arrayModules[i];
            if (module.length() == 1 && i < arrayModules.length + 1) { 
                // module is e:/dd/dd/ on windows
                module = module + ":" + arrayModules[++i];
            }
            log(i + " = " + module );
            set1.add(new File(module)); 
        }
        Set set2 = new HashSet();
        for (int i = 0 ; i < modules.length ; i++) {
            set2.add(new File(getWorkDir(),modules[i]));
        }
        assertEquals("paths length",set2.size(),set1.size());
        assertEquals("Different paths: ", set2,set1);
    }
}
