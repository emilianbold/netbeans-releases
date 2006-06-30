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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
        suite.addTest(new CreateActionsProject("createNotAStdMBean"));
        suite.addTest(new CreateActionsProject("createUserException"));
        suite.addTest(new CreateActionsProject("createRegistMBean"));
        suite.addTest(new CreateActionsProject("createRegistWrapClass"));
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
    
    public void createRegistWrapClass() {
        String className = "RegistWrapClass";
        
        // create an interface
        String intfContent =
                JellyToolsHelper.getFileContent(CreateActionsProject.class, className + "Intf");
        JellyToolsHelper.createJavaFile(PROJECT,className + "Intf",PACKAGE,intfContent);
        
        // create a class
        String content =
                JellyToolsHelper.getFileContent(CreateActionsProject.class, className);
        JellyToolsHelper.createJavaFile(PROJECT,className,PACKAGE,content);
        
    }
    
    public void createNotAStdMBean() {
        String className = "NotAStdMBean";
        
        // create a Dynamic MBean class which is not a Standard MBean.
        String content =
                JellyToolsHelper.getFileContent(CreateActionsProject.class, className);
        JellyToolsHelper.createJavaFile(PROJECT,className,PACKAGE,content);
    }
    
    public void createRegistMBean() {
        String className = "RegistMBean";
        
        // create a Dynamic MBean class which is not a Standard MBean.
        String content =
                JellyToolsHelper.getFileContent(CreateActionsProject.class, className);
        JellyToolsHelper.createJavaFile(PROJECT,className,PACKAGE,content);
    }
    
    public void createUserException() {
        String className = "UserException";
        
        // create an exception class file.
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
    public static final String PROJECT = "JMXTESTActionsFunctionalTest";
    public static final String MENU = "Management";
    public static final String DYNAMIC = "DynamicSupport";
    public static final String MBEAN = "MBean";
    public static final String SUPER = "Super";
}
