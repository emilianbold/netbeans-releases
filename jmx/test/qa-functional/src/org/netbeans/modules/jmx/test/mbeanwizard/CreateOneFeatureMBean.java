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

import java.util.ArrayList;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;
import java.util.ArrayList;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create new JMX MBean files with one attribute and one operation :
 * - MBean from existing java file
 * - MBean from existing java file wrapped as MXBean
 * - Standard MBean with metadata
 */
public class CreateOneFeatureMBean extends MBeanWizardTestCase {
    
    // MBean names
    private static final String ONE_FEATURE_MBEAN_NAME_1 = "OneFeatureMBean1";
    private static final String ONE_FEATURE_MBEAN_NAME_2 = "OneFeatureMBean2";
    private static final String ONE_FEATURE_MBEAN_NAME_3 = "OneFeatureMBean3";
    
    /** Need to be defined because of JUnit */
    public CreateOneFeatureMBean(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOneFeatureMBean("createOneFeatureMBean1"));
        suite.addTest(new CreateOneFeatureMBean("createOneFeatureMBean2"));
        suite.addTest(new CreateOneFeatureMBean("createOneFeatureMBean3"));
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
     * MBean from existing java class
     * with one simple attribute and one two parameter operation
     */
    public void createOneFeatureMBean1() {
        
        System.out.println("============  createOneFeatureMBean1  ============");
        
        String description = "MBean from existing java class " +
                "with one simple attribute and one two parameter operation";
        MBean myMBean = new MBean(
                ONE_FEATURE_MBEAN_NAME_1,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME, false,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * with one simple attribute and one two parameter operation
     */
    public void createOneFeatureMBean2() {
        
        System.out.println("============  createOneFeatureMBean2  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with one simple attribute and one two parameter operation";
        MBean myMBean = new MBean(
                ONE_FEATURE_MBEAN_NAME_2,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME, true,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * StandardMBean with metadata
     * with one simple attribute and one two parameter operation
     */
    public void createOneFeatureMBean3() {
        
        System.out.println("============  createOneFeatureMBean3  ============");
        
        String description = "StandardMBean with metadata " +
                "with one simple attribute and one two parameter operation";
        MBean myMBean = new MBean(
                ONE_FEATURE_MBEAN_NAME_3,
                FILE_TYPE_STANDARD_MBEAN_WITH_METADATA,
                PACKAGE_COM_FOO_BAR,
                description,
                null, false,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA, myMBean);
    }
    
    //========================= MBean generation ===========================//
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        Attribute attribute = new Attribute(
                MBEAN_ATTRIBUTE_NAME_1,
                "int", READ_ONLY,
                MBEAN_ATTRIBUTE_DESCRIPTION_1);
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attribute);
        return list;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        Parameter parameter1 = new Parameter(
                MBEAN_PARAMETER_NAME_1,
                "String",
                MBEAN_PARAMETER_DESCRIPTION_1);
        Parameter parameter2 = new Parameter(
                MBEAN_PARAMETER_NAME_2,
                "javax.management.ObjectName",
                MBEAN_PARAMETER_DESCRIPTION_2);
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(parameter1);
        parameters.add(parameter2);
        
        //operation construction
        Operation operation = new Operation(
                MBEAN_OPERATION_NAME_1,
                "void", parameters, null,
                MBEAN_OPERATION_DESCRIPTION_1);
        ArrayList<Operation> list = new ArrayList<Operation>();
        list.add(operation);
        return list;
    }
}
