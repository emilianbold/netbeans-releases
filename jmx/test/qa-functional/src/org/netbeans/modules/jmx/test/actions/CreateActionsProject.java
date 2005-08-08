/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.actions;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;

/**
 *
 * @author an156382
 */
public class CreateActionsProject extends JellyTestCase {
    
    /** Creates a new instance of CreateAgentProject */
    public CreateActionsProject(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateActionsProject("createProject"));
        suite.addTest(new CreateActionsProject("createNotAMBean"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        
    }
    
    public void tearDown() {
        
    }
    
    public void createNotAMBean() {
        String className = "NotAMBean";
        
        // create a class which is not an MBean class
        String content =
                JellyToolsHelper.getFileContent(CreateActionsProject.class, className);
        JellyToolsHelper.createJavaFile(PROJECT,className,PACKAGE,content);
        
    }
    
    /**
     * Functional test which constructs a J2SE project to generate Agents
     *
     */
    public void createProject() {
        
        NewProjectWizardOperator project = NewProjectWizardOperator.invoke();
        project.selectCategory("General");
        project.selectProject("Java Application");
        project.next();
        NewFileNameLocationStepOperator projectName = 
                new NewFileNameLocationStepOperator();
        
        projectName.setObjectName(PROJECT);
       
        project.finish();
    }
    
    public static final String PACKAGE = "com.foo.bar";
    public static final String PROJECT = "ActionsFunctionalTest";
}
