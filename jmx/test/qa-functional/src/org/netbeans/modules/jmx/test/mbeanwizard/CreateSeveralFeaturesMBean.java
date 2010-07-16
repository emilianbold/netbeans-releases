/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.jmx.test.mbeanwizard;

import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;
import org.netbeans.modules.jmx.test.helpers.Exception;
import java.util.ArrayList;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;


/**
 * Create new JMX MBean files with several attributes and operations :
 * - MBean from existing java file
 * - MBean from existing java file wrapped as MXBean
 * - Standard MBean with metadata
 */
public class CreateSeveralFeaturesMBean extends MBeanWizardTestCase {
    
    // MBean names
    private static final String SEVERAL_FEATURES_MBEAN_NAME_1 = "SeveralFeaturesMBean1";
    private static final String SEVERAL_FEATURES_MBEAN_NAME_2 = "SeveralFeaturesMBean2";
    private static final String SEVERAL_FEATURES_MBEAN_NAME_3 = "SeveralFeaturesMBean3";
    
    /** Need to be defined because of JUnit */
    public CreateSeveralFeaturesMBean(String name) {
        super(name);
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
     * with several attributes and operations
     */
    public void testCreateSeveralFeaturesMBean1() {
        
        System.out.println("==========  createSeveralFeaturesMBean1  ==========");
        
        String description = "MBean from existing java class " +
                "with several attributes and operations";
        MBean myMBean = new MBean(
                SEVERAL_FEATURES_MBEAN_NAME_1,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME, false,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * with several attributes and operations
     */
    public void testCreateSeveralFeaturesMBean2() {
        
        System.out.println("==========  createSeveralFeaturesMBean2  ==========");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with several attributes and operations";
        MBean myMBean = new MBean(
                SEVERAL_FEATURES_MBEAN_NAME_2,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME, true,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * StandardMBean with metadata
     * with several attributes and operations
     */
    public void testCreateSeveralFeaturesMBean3() {
        
        System.out.println("==========  createSeveralFeaturesMBean3  ==========");
        
        String description = "StandardMBean with metadata " +
                "with several attributes and operations";
        MBean myMBean = new MBean(
                SEVERAL_FEATURES_MBEAN_NAME_3,
                FILE_TYPE_STANDARD_MBEAN_WITH_METADATA,
                PACKAGE_COM_FOO_BAR,
                description,
                null, false,
                constructMBeanAttributes(), constructMBeanOperations(), null);
        wizardExecution(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA, myMBean);
    }
    
    //========================= MBean generation ===========================//
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        Attribute attribute1 = new Attribute(
                MBEAN_ATTRIBUTE_NAME_1,
                "int",
                READ_ONLY,
                MBEAN_ATTRIBUTE_DESCRIPTION_1);
        Attribute attribute2 = new Attribute(
                MBEAN_ATTRIBUTE_NAME_2,
                "java.util.Date",
                READ_WRITE,
                MBEAN_ATTRIBUTE_DESCRIPTION_2);
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attribute1);
        list.add(attribute2);
        return list;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        
        // First Parameter construction
        Parameter parameter1 = new Parameter(
                MBEAN_PARAMETER_NAME_1,
                "java.lang.String",
                MBEAN_PARAMETER_DESCRIPTION_1);
        Parameter parameter2 = new Parameter(
                MBEAN_PARAMETER_NAME_2,
                "boolean",
                MBEAN_PARAMETER_DESCRIPTION_2);
        Parameter parameter3 = new Parameter(
                MBEAN_PARAMETER_NAME_3,
                "byte",
                MBEAN_PARAMETER_DESCRIPTION_3);
        Parameter parameter4 = new Parameter(
                MBEAN_PARAMETER_NAME_4,
                "char",
                MBEAN_PARAMETER_DESCRIPTION_4);
        Parameter parameter5 = new Parameter(
                MBEAN_PARAMETER_NAME_5,
                "java.util.Date",
                MBEAN_PARAMETER_DESCRIPTION_5);
        Parameter parameter6 = new Parameter(
                MBEAN_PARAMETER_NAME_6,
                "int",
                MBEAN_PARAMETER_DESCRIPTION_6);
        Parameter parameter7 = new Parameter(
                MBEAN_PARAMETER_NAME_7,
                "long",
                MBEAN_PARAMETER_DESCRIPTION_7);
        Parameter parameter8 = new Parameter(
                MBEAN_PARAMETER_NAME_8,
                "javax.management.ObjectName",
                MBEAN_PARAMETER_DESCRIPTION_8);
        Parameter parameter9 = new Parameter(
                MBEAN_PARAMETER_NAME_9,
                "float",
                MBEAN_PARAMETER_DESCRIPTION_9);
        Parameter parameter10 = new Parameter(
                MBEAN_PARAMETER_NAME_10,
                "double",
                MBEAN_PARAMETER_DESCRIPTION_10);
        
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);
        parameters.add(parameter4);
        parameters.add(parameter5);
        parameters.add(parameter6);
        parameters.add(parameter7);
        parameters.add(parameter8);
        parameters.add(parameter9);
        parameters.add(parameter10);
        
        // Exception construction
        Exception exception1 = new Exception(
                MBEAN_EXCEPTION_CLASS_1, MBEAN_EXCEPTION_DESCRIPTION_1);
        ArrayList<Exception> exceptions = new ArrayList<Exception>();
        exceptions.add(exception1);
        
        // Operation construction
        Operation operation1 = new Operation(
                MBEAN_OPERATION_NAME_1,
                "void", null, null,
                MBEAN_OPERATION_DESCRIPTION_1);
        Operation operation2 = new Operation(
                MBEAN_OPERATION_NAME_2,
                "int", null, exceptions,
                MBEAN_OPERATION_DESCRIPTION_2);
        Operation operation3 = new Operation(
                MBEAN_OPERATION_NAME_3,
                "java.util.Date", parameters, null,
                MBEAN_OPERATION_DESCRIPTION_3);
        ArrayList<Operation> list = new ArrayList<Operation>();
        list.add(operation1);
        list.add(operation2);
        list.add(operation3);
        return list;
    }
}

