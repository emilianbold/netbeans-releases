/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform;

import java.io.File;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test that module restore works as expected.
 *
 * @author David Konecny
 */
public class J2SEPlatformModuleTest extends NbTestCase implements Lookup.Provider {
    
    private Lookup lookup;
    
    public J2SEPlatformModuleTest(java.lang.String testName) {
        super(testName);
        TestUtil.setLookup (Lookups.proxy(this));
    }
    
    protected void setUp() throws Exception {
        this.clearWorkDir();
        System.setProperty("netbeans.user", FileUtil.normalizeFile(getWorkDir()).getPath());
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testRestored() throws Exception {
        J2SEPlatformModule.updateBuildProperties();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.source"));
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.target"));
    }
    
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(new Object[] {
                new JavaPlatformProviderImpl (),
                new MasterURLMapper(),
            });
        }
        return lookup;
    }
    
}
