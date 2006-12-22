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

package org.apache.tools.ant.module.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;

// XXX testSubElements
// XXX testSpecials

/**
 * Test functionality of IntrospectedInfo.
 * @author Jesse Glick
 */
public class IntrospectedInfoTest extends NbTestCase {

    public IntrospectedInfoTest(String name) {
        super(name);
    }

    private IntrospectedInfo ii;

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(IFL.class);
        ii = IntrospectedInfo.getDefaults();
    }
    
    public void testBasicDefinitions() throws Exception {
        Map<String,String> tasks = ii.getDefs("task");
        assertEquals("binding for javac", "org.apache.tools.ant.taskdefs.Javac", tasks.get("javac"));
        assertEquals("binding for sql", "org.apache.tools.ant.taskdefs.SQLExec", tasks.get("sql"));
        Map<String,String> types = ii.getDefs("type");
        assertEquals("binding for path", "org.apache.tools.ant.types.FileSet", types.get("fileset"));
        assertEquals("binding for path", "org.apache.tools.ant.types.Path", types.get("path"));
    }
    
    public void testBasicAttributes() throws Exception {
        Map<String,String> attrs = ii.getAttributes("org.apache.tools.ant.taskdefs.Javac");
        assertEquals("right type for destdir", "java.io.File", attrs.get("destdir"));
        // XXX sometimes this line fails - when run from inside the IDE, but not on the command line!
        // (related to #50160?)
        // Debugger shows that IntrospectionHelper.createAttributeSetter is calling
        //     Path.class.getConstructor(new Class[] {Project.class, String.class})
        // and this is (for some reason) throwing a NoSuchMethodException.
        // Seems to be that Path.class has a matching constructor but with a different
        // version of Project.class - some sort of class loader snafu perhaps.
        // The code sources appear correct.
        /*
        assertEquals("right type for srcdir", "org.apache.tools.ant.types.Path", attrs.get("srcdir"));
         */
        /* This however works:
        ClassLoader l = org.apache.tools.ant.module.bridge.AntBridge.getMainClassLoader();
        Class prj = l.loadClass("org.apache.tools.ant.Project");
        Class path = l.loadClass("org.apache.tools.ant.types.Path");
        System.out.println("constructor: " + path.getConstructor(new Class[] {prj, String.class}));
         */
    }
    
    public void testEnumeratedAttributes() throws Exception {
        ii.register("enumtask", EnumTask.class, "task");
        String k1 = EnumTask.class.getName();
        assertEquals(k1, ii.getDefs("task").get("enumtask"));
        String k2 = EnumTask.E.class.getName();
        assertEquals(Collections.singletonMap("attr", k2), ii.getAttributes(k1));
        assertEquals("[chocolate, vanilla, strawberry]", Arrays.toString(ii.getTags(k2)));
    }
    
    public static class EnumTask {
        public enum E {chocolate, vanilla, strawberry}
        public void setAttr(E e) {}
    }
    
    public static final class IFL extends InstalledFileLocator {
        public IFL() {
            //System.err.println("ant.home=" + System.getProperty("test.ant.home"));
        }
        @Override
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
