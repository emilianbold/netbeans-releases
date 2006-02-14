/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test wizard logic.
 * @author Milos Kleint, Jesse Glick
 */
public class LibraryStartVisualPanelTest extends NbTestCase {
    
    public LibraryStartVisualPanelTest(String testName) {
        super(testName);
    }

    public void testPopulateProjectData() throws Exception {
        Map/*<String,String>*/ contents = new HashMap();
        contents.put("org/apache/commons/logging/Log.class", "");
        contents.put("1.0-beta/X.class", ""); // #72669
        contents.put("org/apache/commons/logging/impl/NoOpLog.class", "");
        File libraryPath = new File(getWorkDir(), "test-library-0.1_01.jar");
        TestBase.createJar(libraryPath, contents, new Manifest());
        NewModuleProjectData data = new NewModuleProjectData();
        LibraryStartVisualPanel.populateProjectData(data, libraryPath.getAbsolutePath(), true);
        assertEquals("test-library", data.getProjectName());
        assertEquals("org.apache.commons.logging", data.getCodeNameBase());
    }
    
}
