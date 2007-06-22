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


import org.netbeans.junit.NbTestSuite;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create utilities for all actions tests :
 * - create a new Java application project
 * - create a new simple Java class
 * - create a new dynamic MBean
 * - create a new Java class which extends java.lang.Exception
 * - create a new Java class which implements a new Java interface
 */
public class CreateActionsProject extends ActionsTestCase {
    
    /** Creates a new instance of CreateAgentProject */
    public CreateActionsProject(String name) {
        super(name);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateActionsProject("createProject"));     
        return suite;
    }
    

    /**
     * Functional test which creates a project
     */
    public void createProject() {
        System.out.println("Create new project " + PROJECT_NAME_ACTION_FUNCTIONAL);
        newProject(
                PROJECT_CATEGORY_JAVA,
                PROJECT_TYPE_JAVA_APPLICATION,
                PROJECT_NAME_ACTION_FUNCTIONAL);
    }
}
