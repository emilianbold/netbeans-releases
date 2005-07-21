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

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.apisupport.project.*;
import org.openide.util.Lookup;

// XXX much more to test

/**
 * Test Actions.
 * @author Jesse Glick
 */
public class ModuleActionsTest extends TestBase {
    
    public ModuleActionsTest(String name) {
        super(name);
    }
    
    public void testDebugFix() throws Exception {
        // Track down #47012.
        Project freeform = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant/freeform")));
        assertNotNull("have project in ant/freeform", freeform);
        ActionProvider ap = (ActionProvider) freeform.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
        FileObject actionsJava = FileUtil.toFileObject(file("ant/freeform/src/org/netbeans/modules/ant/freeform/Actions.java"));
        assertNotNull("have Actions.java", actionsJava);
        assertTrue("Fix enabled on it", ap.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, Lookups.singleton(DataObject.find(actionsJava))));
    }
    
    public void testJavadocPresenceAndEnablement() throws Exception {
        File dir = getWorkDir();
        NbModuleProjectGenerator.createStandAloneModule(dir, "x", "X", "x/Bundle.properties", "x/layer.xml", "default");
        new FileOutputStream(new File(dir, "src/x/X.java".replace('/', File.separatorChar))).close();
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(dir));
        assertNotNull(p);
        p.open();
        ActionProvider ap = (ActionProvider) p.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);
        assertTrue("have Javadoc action", Arrays.asList(ap.getSupportedActions()).contains(JavaProjectConstants.COMMAND_JAVADOC));
        assertFalse("not yet enabled though (no public packages)", ap.isActionEnabled(JavaProjectConstants.COMMAND_JAVADOC, Lookup.EMPTY));
        ProjectXMLManager mgr = new ProjectXMLManager(p.getHelper());
        assertEquals("no pub pkgs yet", 0, mgr.getPublicPackages().length);
        mgr.replacePublicPackages(new String[] {"x"});
        assertEquals("now have pub pkgs", 1, mgr.getPublicPackages().length);
        assertTrue("still have Javadoc action", Arrays.asList(ap.getSupportedActions()).contains(JavaProjectConstants.COMMAND_JAVADOC));
        assertTrue("and now enabled (#61229)", ap.isActionEnabled(JavaProjectConstants.COMMAND_JAVADOC, Lookup.EMPTY));
    }
    
}
