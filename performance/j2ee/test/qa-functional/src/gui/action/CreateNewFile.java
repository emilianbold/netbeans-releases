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

package gui.action;

import gui.Utils;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/**
 * Test of Open File Dialog
 *
 * @author  lmartinek@netbeans.org
 */
public class CreateNewFile extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewFileWizardOperator wizard;
    
    private String project;
    private String category;
    private String fileType;
    private String fileName;
    private String packageName;
    private boolean isEntity = false;
    /**
     * Creates a new instance of CreateNewFile 
     */
    public CreateNewFile(String testName) {
        super(testName);
        expectedTime = 5000;
    }
    
    /**
     * Creates a new instance of CreateNewFile 
     */
    public CreateNewFile(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 5000;
    }
    
    public void testCreateNewSessionBean() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Enterprise";
        fileType = "Session Bean";
        fileName = "NewTestSession";
        packageName = "test.newfiles";
        doMeasurement();
    }
    
    public void testCreateNewEntityBean() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Enterprise";
        fileType = "Entity Bean";
        fileName = "NewTestEntity";
        packageName = "test.newfiles";
        isEntity = true;
        doMeasurement();
    }
    
    public void testCreateNewWebService() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Web Services";
        fileType = "Web Service";
        fileName = "NewWebService";
        packageName = "test.newfiles";
        doMeasurement();
    }
     
    public void initialize() {
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-EJBModule"), "Source Packages|test|TestSessionRemote.java"));
    }
    
    public void shutdown() {
        new EditorOperator("TestSessionRemote.java").closeDiscard();
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
        new NewFileAction().performMenu();
        wizard = new NewFileWizardOperator();
        wizard.selectProject(project);
        wizard.selectCategory(category);
        wizard.selectFileType(fileType);
        wizard.next();
        JTextFieldOperator eBname;
        if(isEntity==true)
             eBname = new JTextFieldOperator(wizard,1);
        else
             eBname = new JTextFieldOperator(wizard);
        eBname.setText(fileName+Utils.getTimeIndex());
        new JComboBoxOperator(wizard,1).enterText(packageName);
    }

    public ComponentOperator open() {
//        wizard.finish();
        return new EditorOperator(fileName);
    }
    
    public void close() {
        if (testedComponentOperator != null){
            ((EditorOperator)testedComponentOperator).save();
            ((EditorOperator)testedComponentOperator).close();
        }    
    }
    
}
