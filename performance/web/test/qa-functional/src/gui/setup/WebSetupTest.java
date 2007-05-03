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

package gui.setup;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildProjectAction;
import org.netbeans.junit.ide.ProjectSupport;


public class WebSetupTest extends org.netbeans.jellytools.JellyTestCase {
    
    public WebSetupTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testOpenWebProject() {
        ProjectSupport.openProject(System.getProperty("xtest.data")+"/TestWebProject");
        buildProject("TestWebProject");
    }
    
    public void testOpenWebFoldersProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/PerformanceTestFolderWebApp");
        buildProject("PerformanceTestFolderWebApp");
    }
    
    private void buildProject(String name) {
        new BuildProjectAction().perform(new ProjectsTabOperator().
            getProjectRootNode(name));
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }
}
