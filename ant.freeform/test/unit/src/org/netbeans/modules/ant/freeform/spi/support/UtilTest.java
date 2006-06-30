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

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * @author David Konecny
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testAuxiliaryConfiguration() throws Exception {
        File proj = new File(getWorkDir(), "aux_proj");
        proj.mkdir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(proj, proj, "proj1", null);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("project was created", p);
        assertEquals("expected project folder", base, p.getProjectDirectory());
        
        AuxiliaryConfiguration au = Util.getAuxiliaryConfiguration(helper);
        assertNotNull("project has AuxiliaryConfiguration", au);
    }
    
    public void testRelativizeLocation() throws Exception {
        File srcApp = Utilities.isWindows() ? new File("c:\\src\\app") : new File("/src/app");
        File srcAppFooBar = new File(srcApp, "foo" + File.separatorChar + "bar");
        File projApp = Utilities.isWindows() ? new File("c:\\proj\\app") : new File("/proj/app");
        File otherFooBar = Utilities.isWindows() ? new File("c:\\other\\foo\\bar") : new File("/other/foo/bar");
        assertEquals("foo/bar", Util.relativizeLocation(srcApp, srcApp, srcAppFooBar));
        assertEquals("${project.dir}/foo/bar", Util.relativizeLocation(srcApp, projApp, srcAppFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, srcApp, otherFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, projApp, otherFooBar));
        // Mentioned incidentally in #54428:
        assertEquals(".", Util.relativizeLocation(srcApp, srcApp, srcApp));
        assertEquals("${project.dir}", Util.relativizeLocation(srcApp, projApp, srcApp));
    }

    public void testGetDefaultAntScript() throws Exception {
        assertNull("no default ant script", Util.getDefaultAntScript(extsrcroot));
        assertEquals("found build.xml", simple.getProjectDirectory().getFileObject("build.xml"), Util.getDefaultAntScript(simple));
        assertEquals("found build.xml", extbuildscript.getProjectDirectory().getFileObject("scripts/build.xml"), Util.getDefaultAntScript(extbuildscript));
    }
    
}
