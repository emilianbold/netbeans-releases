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
