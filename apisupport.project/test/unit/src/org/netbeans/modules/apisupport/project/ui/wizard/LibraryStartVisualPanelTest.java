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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        Map<String, String> contents = new HashMap<String, String>();
        contents.put("org/apache/commons/logging/Log.class", "");
        contents.put("1.0-beta/X.class", ""); // #72669
        contents.put("org/apache/commons/logging/impl/NoOpLog.class", "");
        File libraryPath = new File(getWorkDir(), "test-library-0.1_01.jar");
        TestBase.createJar(libraryPath, contents, new Manifest());
        NewModuleProjectData data = new NewModuleProjectData(NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE);
        LibraryStartVisualPanel.populateProjectData(data, libraryPath.getAbsolutePath(), true);
        assertEquals("test-library", data.getProjectName());
        assertEquals("org.apache.commons.logging", data.getCodeNameBase());
    }
    
}
