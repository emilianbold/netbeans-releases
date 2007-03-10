/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package footprint;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;


/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class VisualWebProjectWorkflow extends MemoryFootprintTestCase {
    
    private String webproject;
    
    /**
     * Creates a new instance of VisualWebProjectWorkflow
     * @param testName 
     */
    public VisualWebProjectWorkflow(String testName) {
        super(testName);
        prefix = "Visual Web Project Workflow |";
    }
    
    /**
     * Creates a new instance of VisualWebProjectWorkflow
     */
    public VisualWebProjectWorkflow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        prefix = "Visual Web Project Workflow |";
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
        
        //webproject = FootprintUtilities.createproject("Web", "Visual Web Application", false);
        webproject = "HugeApp";
        
        VWPFootprintUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +webproject); // failing openProject(webproject);
        VWPFootprintUtilities.waitForPendingBackgroundTasks();
        VWPFootprintUtilities.openFile(webproject,webproject.toLowerCase(),"ApplicationBean1.java",true);
        VWPFootprintUtilities.buildproject(webproject);
        //TODO this takes ages / minutes ;( VWPFootprintUtilities.deployProject(webproject);
        
        return null;
    }
    
    public void initialize() {
        super.initialize();
        VWPFootprintUtilities.closeAllDocuments();
        VWPFootprintUtilities.closeMemoryToolbar();
    }
    
    public void close() {
        log("::close");
        VWPFootprintUtilities.closeAllDocuments();
        VWPFootprintUtilities.closeProject(webproject);
    }
    
    private void openProject(String projectName) {
        new ActionNoBlock("File|Open Project...",null).perform(); // NOI18N
        WizardOperator wizard = new WizardOperator("Open Project");  // NOI18N
        JTextComponentOperator path = new JTextComponentOperator(wizard,1);
        
        String buttonCaption = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "BTN_PrjChooser_ApproveButtonText");
        JButtonOperator openButton = new JButtonOperator(wizard,buttonCaption);
        
        String paths = System.getProperty("xtest.tmpdir")+ java.io.File.separator +projectName;
        path.setText(paths);
        openButton.pushNoBlock();
    }
    
}
