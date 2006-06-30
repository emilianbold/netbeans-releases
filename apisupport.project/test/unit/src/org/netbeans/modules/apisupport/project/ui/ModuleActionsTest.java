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

package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;

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
    
}
