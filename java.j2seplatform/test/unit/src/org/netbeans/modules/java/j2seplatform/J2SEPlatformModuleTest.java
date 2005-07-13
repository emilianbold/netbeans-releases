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

package org.netbeans.modules.java.j2seplatform;

import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Test that module restore works as expected.
 *
 * @author David Konecny
 */
public class J2SEPlatformModuleTest extends NbTestCase {
    
    static {
        Class c = TestUtil.class; // force lookup init before anyone else
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public J2SEPlatformModuleTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        System.setProperty("netbeans.user", FileUtil.normalizeFile(getWorkDir()).getPath());
        TestUtil.setLookup(new Object[] {
            new JavaPlatformProviderImpl(),
        });
    }
    
    public void testRestored() throws Exception {
        J2SEPlatformModule.updateBuildProperties();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.source"));
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.target"));
    }
    
}
