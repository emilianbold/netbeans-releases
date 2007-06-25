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

package org.netbeans.modules.jmx.test.mbeanwizard;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.MBean;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create new empty JMX MBean files :
 * - Standard MBean
 * - MXBean
 * - MBean from existing java file
 * - MBean from existing java file wrapped as MXBean
 * - Standard MBean with metadata
 */
public class CreateEmptyMBean extends MBeanWizardTestCase {
    
    // MBean names
    private static final String EMPTY_MBEAN_NAME_1 = "EmptyMBean1";
    private static final String EMPTY_MBEAN_NAME_2 = "EmptyMBean2";
    private static final String EMPTY_MBEAN_NAME_3 = "EmptyMBean3";
    private static final String EMPTY_MBEAN_NAME_4 = "EmptyMBean4";
    private static final String EMPTY_MBEAN_NAME_5 = "EmptyMBean5";
    
    /** Need to be defined because of JUnit */
    public CreateEmptyMBean(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateEmptyMBean("createEmptyMBean1"));
        suite.addTest(new CreateEmptyMBean("createEmptyMBean2"));
        suite.addTest(new CreateEmptyMBean("createEmptyMBean3"));
        suite.addTest(new CreateEmptyMBean("createEmptyMBean4"));
        suite.addTest(new CreateEmptyMBean("createEmptyMBean5"));
        return suite;
    }
    
    public void setUp() {
        // Select project node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL);
        // Initialize the wrapper java class
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                EMPTY_JAVA_CLASS_NAME);
    }
    
    
    //========================= JMX CLASS =================================//
    
    /**
     * StandardMBean without attributes and operations
     */
    public void createEmptyMBean1() {
        
        System.out.println("============  createEmptyMBean1  ============");
        
        String description = "StandardMBean without attributes and operations";
        MBean myMBean = new MBean(
                EMPTY_MBEAN_NAME_1,
                FILE_TYPE_STANDARD_MBEAN,
                PACKAGE_COM_FOO_BAR,
                description,
                null, false, null, null, null);
        wizardExecution(FILE_TYPE_STANDARD_MBEAN, myMBean);
    }
    
    /**
     * MXBean without attributes and operations
     */
    public void createEmptyMBean2() {
        
        System.out.println("============  createEmptyMBean2  ============");
        
        String description = "MXBean without attributes and operations";
        MBean myMBean = new MBean(
                EMPTY_MBEAN_NAME_2,
                FILE_TYPE_MXBEAN,
                PACKAGE_COM_FOO_BAR,
                description,
                null, false, null, null, null);
        wizardExecution(FILE_TYPE_MXBEAN, myMBean);
    }
    
    /**
     * MBean from existing java class without attributes and operations
     */
    public void createEmptyMBean3() {
        
        System.out.println("============  createEmptyMBean3  ============");
        
        String description = "MBean from existing java class " +
                "without attributes and operations";
        MBean myMBean = new MBean(
                EMPTY_MBEAN_NAME_3,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * without attributes and operations
     */
    public void createEmptyMBean4() {
        
        System.out.println("============  createEmptyMBean4  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "without attributes and operations";
        MBean myMBean = new MBean(
                EMPTY_MBEAN_NAME_4,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * StandardMBean with metadata without attributes and operations
     */
    public void createEmptyMBean5() {
        
        System.out.println("============  createEmptyMBean5  ============");
        
        String description = "StandardMBean with metadata " +
                "without attributes and operations";
        MBean myMBean = new MBean(
                EMPTY_MBEAN_NAME_5,
                FILE_TYPE_STANDARD_MBEAN_WITH_METADATA,
                PACKAGE_COM_FOO_BAR,
                description,
                null, false, null, null, null);
        wizardExecution(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA, myMBean);
    }
}
