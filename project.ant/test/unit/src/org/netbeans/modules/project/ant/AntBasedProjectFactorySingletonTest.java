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

package org.netbeans.modules.project.ant;

import java.lang.reflect.Method;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelperTest;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class AntBasedProjectFactorySingletonTest extends NbTestCase {
    
    public AntBasedProjectFactorySingletonTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    
    protected void setUp() throws Exception {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.xml"), projdir, "nbproject/private/private.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.properties"), projdir, "nbproject/project.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.properties"), projdir, "nbproject/private/private.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/global.properties"), scratch, "userdir/build.properties");
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
        });
    }

    /**Test for second part of #42738.
     */
    public void testAntBasedProjectTypesChanged() throws Exception {
        AntBasedProjectFactorySingleton factory = new AntBasedProjectFactorySingleton();
        AntBasedProjectType type1 = AntBasedTestUtil.testAntBasedProjectType();
        AntBasedProjectType type2 = AntBasedTestUtil.testAntBasedProjectType();
        
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            factory,
            type1,
            type2,
        }));
        
        Method getAntBasedProjectTypeMethod = AntProjectHelper.class.getDeclaredMethod("getType", new Class[0]);
        
        getAntBasedProjectTypeMethod.setAccessible(true);
        
        Project p = ProjectManager.getDefault().findProject(projdir);
        AntProjectHelper helper = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper, null) == type2);
        
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            factory,
            type1,
        }));
        
        p = ProjectManager.getDefault().findProject(projdir);
        helper = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper, null) == type1);
        
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            factory,
            type2,
        }));
        
        p = ProjectManager.getDefault().findProject(projdir);
        helper = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper, null) == type2);
        
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            factory,
        }));
        
        assertNull(ProjectManager.getDefault().findProject(projdir));

        TestUtil.setLookup(Lookups.fixed(new Object[] {
            factory,
            type1,
            type2,
        }));
        
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper, null) == type2);
    }
    
}
