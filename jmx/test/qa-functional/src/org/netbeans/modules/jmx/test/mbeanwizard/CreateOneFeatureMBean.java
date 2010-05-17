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
    public void testCreateOneFeatureMBean1() {
        
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
    public void testCreateOneFeatureMBean2() {
        
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
    public void testCreateOneFeatureMBean3() {
        
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
                "java.lang.String",
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
