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

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 * @author David Konecny
 */
public class UtilTest extends NbTestCase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
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
        assertEquals("foo/bar", Util.relativizeLocation(new File("/src/app"), new File("/src/app"), new File("/src/app/foo/bar")));
        assertEquals("${project.dir}/foo/bar", Util.relativizeLocation(new File("/src/app"), new File("/proj/app"), new File("/src/app/foo/bar")));
        assertEquals("/other/foo/bar", Util.relativizeLocation(new File("/src/app"), new File("/src/app"), new File("/other/foo/bar")));
        assertEquals("/other/foo/bar", Util.relativizeLocation(new File("/src/app"), new File("/proj/app"), new File("/other/foo/bar")));
        // Mentioned incidentally in #54428:
        assertEquals(".", Util.relativizeLocation(new File("/src/app"), new File("/src/app"), new File("/src/app")));
        assertEquals("${project.dir}", Util.relativizeLocation(new File("/src/app"), new File("/proj/app"), new File("/src/app")));
    }

}
