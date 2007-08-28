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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 * Tests {@link CustomizerLibraries}.
 *
 * @author Martin Krauskopf
 */
public class CustomizerLibrariesTest extends TestBase {
    
    public CustomizerLibrariesTest(String testName) {
        super(testName);
    }
    
    public void testCustomizerLibrariesCanBeGCedAfterProjectIsClosed() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = SingleModulePropertiesTest.loadProperties(p);
        ProjectCustomizer.Category cat = ProjectCustomizer.Category.create("XX", "xx", null);
                
        CustomizerLibraries panel = new CustomizerLibraries(props, cat);
        panel.refresh();
        Reference<?> ref = new WeakReference<Object>(panel);
        OpenProjects.getDefault().close(new Project[] { p });
        panel = null;
        p = null;
        props = null;
        assertGC("CustomizerLibraries panel cannot be GCed", ref);
    }
    
}