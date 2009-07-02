/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.MBean;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;


/**
 * Create new JMX MBean files :
 * - MBean from existing java file
 * - MBean from existing java file wrapped as MXBean
 * The java file wrapper contains getters and setters.
 */
public class CreateAttributeWrapperMBean extends MBeanWizardTestCase {
    
    // MBean names
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_1 = "AttributeWrappedMBean1";
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_2 = "AttributeWrappedMBean2";
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_3 = "AttributeWrappedMBean3";
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_4 = "AttributeWrappedMBean4";
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_5 = "AttributeWrappedMBean5";
    private static final String ATTRIBUTE_WRAPPED_MBEAN_NAME_6 = "AttributeWrappedMBean6";
    
    // Attributes names used when updating wrapper java class
    private static String ATTRIBUTE_1 = "Attribute1";
    private static String ATTRIBUTE_2 = "Attribute2";
    private static String ATTRIBUTE_3 = "Attribute3";
    private static String ATTRIBUTE_4 = "Attribute4";
    private static String ATTRIBUTE_5 = "Attribute5";
    private static String ATTRIBUTE_6 = "Attribute6";
    private static String GENERIC_ATTRIBUTE_1 = "GenericAttribute1";
    private static String GENERIC_ATTRIBUTE_2 = "GenericAttribute2";
    
    // Depending on the MBean, the attributes wizard execution will differ
    private MBean mbean = null;
    
    /** Need to be defined because of JUnit */
    public CreateAttributeWrapperMBean(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean1"));
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean2"));
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean3"));
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean4"));
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean5"));
        suite.addTest(new CreateAttributeWrapperMBean("createWrappedMBean6"));
        return suite;
    }
    
    public void setUp() {
        // Select project node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL);
        // Initialize the wrapper java classes
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                ATTRIBUTE_WRAPPER_NAME_1);
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                ATTRIBUTE_WRAPPER_NAME_2);
    }
    
    //========================= JMX CLASS =================================//
    
    /**
     * MBean from existing java class exposing all attributes
     */
    public void createWrappedMBean1() {
        
        System.out.println("============  createWrappedMBean1  ============");
        
        String description = "MBean from existing java class with all attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_1,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class exposing minimal attributes
     */
    public void createWrappedMBean2() {
        
        System.out.println("============  createWrappedMBean2  ============");
        
        String description = "MBean from existing java class with minimal attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_2,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class exposing generic attributes
     */
    public void createWrappedMBean3() {
        
        System.out.println("============  createWrappedMBean3  ============");
        
        String description = "MBean from existing java class with generic attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_3,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_2,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean exposing all attributes
     */
    public void createWrappedMBean4() {
        
        System.out.println("============  createWrappedMBean4  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with all attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_4,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean exposing minimal attributes
     */
    public void createWrappedMBean5() {
        
        System.out.println("============  createWrappedMBean5  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with minimal attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_5,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean exposing generic attributes
     */
    public void createWrappedMBean6() {
        
        System.out.println("============  createWrappedMBean6  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with generic attributes";
        mbean = new MBean(
                ATTRIBUTE_WRAPPED_MBEAN_NAME_6,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_2,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    //========================= Panel discovery ==================================//
    
    /**
     * New file JMX wizard execution.
     * Depending on the JMX file type, the wizard execution will have different
     * behavior/fields.
     */
    protected void wizardExecution(String fileType, MBean mbean) {
        
        // New File wizard execution
        // -------------------------
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                fileType);
        nfwo.next();
        
        // Name and Location wizard execution
        // ----------------------------------
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                mbean.getName(),
                mbean.getPackage(),
                mbean.getDescription(),
                mbean.getClassToWrap(),
                mbean.isObjectWrappedAsMXBean());
        // Check Name and Location wizard
        System.out.println("Check name and location wizard");
        checkNameAndLocationWizard(nfnlso, mbean);
        // Get the generated files before switching to next wizard
        String mbeanCreatedClassFile = nfnlso.txtCreatedFile().getText();
        String mbeanCreatedInterfaceFile = getTextFieldContent(
                CREATED_FILE_TEXT_FIELD, nfnlso);
        
        // Attributes wizard execution
        // ------------------------------------------
        nfnlso.next();
        // Check MBean attributes wizard
        System.out.println("Check MBean attributes wizard " +
                "before attributes selection");
        checkMBeanAttributesWizardBeforeSelection(nfnlso, fileType);
        // Select/unselect MBean attributes
        System.out.println("Select/unselect and update MBean attributes");
        updateMBeanAttributes(nfnlso);
        sleep(2000);
        // Check MBean attributes wizard
        System.out.println("Check MBean attributes wizard " +
                "after attributes selection");
        checkMBeanAttributesWizardAfterSelection(nfnlso, fileType);

        nfnlso.next();
        sleep(2000);
        nfnlso.finish();
        
        // Check generated files
        System.out.println("Check created files");
        checkCreatedFiles(mbeanCreatedClassFile, mbeanCreatedInterfaceFile, mbean);
    }
    
    /**
     * Select/unselect and update wrapped attributes.
     */
    private void updateMBeanAttributes(
            NewJavaFileNameLocationStepOperator nfnlso) {
        
        JTableOperator jto = getTableOperator(WRAPPER_ATTRIBUTE_TABLE, nfnlso);
        // Give the focus to the table operator
        giveAPIFocus(jto);
        
        // Depending on the MBean, performs the following :
        // - check/uncheck attributes to keep
        // - modify access
        // - modify description
        
        // Expose all attributes
        if (mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_1) ||
                mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_4)) {
            
            // Set ATTRIBUTE_3 access to read only
            jto.selectCell(jto.findCellRow(ATTRIBUTE_3),
                    jto.findColumn(ATTRIBUTE_ACCESS_COLUMN_NAME));
            selectComboBoxItem(WRAPPER_ATTRIBUTE_ACCESS_BOX, jto, READ_ONLY);
            // Unselect ATTRIBUTE_6 returning java.util.Date
            // As there is 2 setters defined for ATTRIBUTE_6 (returning float and Date),
            // we retreive the row index using the returned type value instead
            // of the attribute name
            unselectAttribute(jto, "java.util.Date");
        }
        // Expose minimal attributes
        else if (mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_2) ||
                mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_5)) {
            
            // Unselect ATTRIBUTE_1, ATTRIBUTE_3 and ATTRIBUTE_5
            unselectAttribute(jto, ATTRIBUTE_1);
            unselectAttribute(jto, ATTRIBUTE_3);
            unselectAttribute(jto, ATTRIBUTE_5);
            // Unselect ATTRIBUTE_6 returning float
            // As there is 2 setters defined for ATTRIBUTE_6 (returning float and Date),
            // we retreive the row index using the returned type value instead
            // of the attribute name
            unselectAttribute(jto, "float");
        }
        // Expose generic attributes
        else if(mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_3) ||
                mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_6)) {
            // Nothing to do
            // We just want to check that generation is successfull
        }
    }
    
    /**
     * Check Attributes wizard before MBean attributes selection
     */
    private void checkMBeanAttributesWizardBeforeSelection(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {
        
        JTableOperator jto = getTableOperator(WRAPPER_ATTRIBUTE_TABLE, nfnlso);
        int rowCount = 0;
        
        System.out.println("Check all MBean attributes are selected");

        // Wrapped java class is ATTRIBUTE_WRAPPER_NAME_1
        if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1)) {
            rowCount = jto.getRowCount();
            if (rowCount != 7) {
                System.out.println("(ERROR) Found " + rowCount + " attributes" +
                        " instead of expected 7");
            }
            assertEquals(7, rowCount);
            verifyAttributeSelection(jto, ATTRIBUTE_1, true);
            verifyAttributeSelection(jto, ATTRIBUTE_2, true);
            verifyAttributeSelection(jto, ATTRIBUTE_3, true);
            verifyAttributeSelection(jto, ATTRIBUTE_4, true);
            verifyAttributeSelection(jto, ATTRIBUTE_5, true);
            // As there is 2 setters defined for ATTRIBUTE_6 (returning float and Date),
            // we retreive the row index using the returned type value instead
            // of the attribute name
            verifyAttributeSelection(jto, "float", true);
            verifyAttributeSelection(jto, "java.util.Date", true);
        }
        // Wrapped java class is ATTRIBUTE_WRAPPER_NAME_2
        else if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_2)) {
            rowCount = jto.getRowCount();
            if (rowCount != 3) {
                System.out.println("(ERROR) Found " + rowCount + " attributes" +
                        " instead of expected 3");
            }
            assertEquals(3, rowCount);
            verifyAttributeSelection(jto, ATTRIBUTE_1, true);
            verifyAttributeSelection(jto, GENERIC_ATTRIBUTE_1, true);
            verifyAttributeSelection(jto, GENERIC_ATTRIBUTE_2, true);
        }
        super.checkMBeanAttributesWizard(nfnlso, fileType);
    }
    
    /**
     * Check Attributes wizard after MBean attributes selection
     */
    private void checkMBeanAttributesWizardAfterSelection(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {
        
        JTableOperator jto = getTableOperator(WRAPPER_ATTRIBUTE_TABLE, nfnlso);
        int rowCount = 0;
        
        System.out.println("Check MBean attributes have been selected/unselected");
 
        // Wrapped java class is ATTRIBUTE_WRAPPER_NAME_1
        if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_1)) {
            rowCount = jto.getRowCount();
            if (rowCount != 7) {
                System.out.println("(ERROR) Found " + rowCount + " attributes" +
                        " instead of expected 7");
            }
            assertEquals(7, rowCount);
            // Expose all attributes
            if (mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_1) ||
                    mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_4)) {
                verifyAttributeSelection(jto, ATTRIBUTE_1, true);
                verifyAttributeSelection(jto, ATTRIBUTE_2, true);
                verifyAttributeSelection(jto, ATTRIBUTE_3, true);
                verifyAttributeSelection(jto, ATTRIBUTE_4, true);
                verifyAttributeSelection(jto, ATTRIBUTE_5, true);
                verifyAttributeSelection(jto, "float", true);
                verifyAttributeSelection(jto, "java.util.Date", false);
            } 
            // Expose minimal attributes
            else if (mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_2) ||
                    mbean.getName().equals(ATTRIBUTE_WRAPPED_MBEAN_NAME_5)) {
                verifyAttributeSelection(jto, ATTRIBUTE_1, false);
                verifyAttributeSelection(jto, ATTRIBUTE_2, true);
                verifyAttributeSelection(jto, ATTRIBUTE_3, false);
                verifyAttributeSelection(jto, ATTRIBUTE_4, true);
                verifyAttributeSelection(jto, ATTRIBUTE_5, false);
                verifyAttributeSelection(jto, "float", false);
                verifyAttributeSelection(jto, "java.util.Date", true);
            }
        }
        // Wrapped java class is ATTRIBUTE_WRAPPER_NAME_2
        else if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + ATTRIBUTE_WRAPPER_NAME_2)) {
            rowCount = jto.getRowCount();
            if (rowCount != 3) {
                System.out.println("(ERROR) Found " + rowCount + " attributes" +
                        " instead of expected 3");
            }
            assertEquals(3, rowCount);
            verifyAttributeSelection(jto, ATTRIBUTE_1, true);
            verifyAttributeSelection(jto, GENERIC_ATTRIBUTE_1, true);
            verifyAttributeSelection(jto, GENERIC_ATTRIBUTE_2, true);
        }
        super.checkMBeanAttributesWizard(nfnlso, fileType);
    }
    
    
    private void unselectAttribute(JTableOperator jto, String name) {
        int rowIndex = jto.findCellRow(name);
        int columnIndex = jto.findColumn(ATTRIBUTE_EXPOSE_COLUMN_NAME);
        jto.selectCell(rowIndex, columnIndex);
    }
    
    private void verifyAttributeSelection(
            JTableOperator jto, String name, boolean selected) {
        int rowIndex = jto.findCellRow(name);
        int columnIndex = jto.findColumn(ATTRIBUTE_EXPOSE_COLUMN_NAME);
        if (selected) {
            assertTrue((Boolean)getTableCellValue(jto, rowIndex, columnIndex));
        } else {
            assertFalse((Boolean)getTableCellValue(jto, rowIndex, columnIndex));
        }
    }
    
    //========================= JAVA CLASS generation ===========================//
    
    /**
     * Wrapper java class
     */
    protected void createWrapperJavaClass(String javaClassName) {
        
        super.createWrapperJavaClass(javaClassName);
        
        // Select class node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL + "|" + SOURCE_PACKAGES + "|" +
                PACKAGE_COM_FOO_BAR + "|" + javaClassName);
        // Update the created java file
        updateWrapperJavaClass(javaClassName);
    }
    
    private void updateWrapperJavaClass(String fileName) {
        EditorOperator eo = new EditorOperator(fileName);
        
        // Update WrapperJavaClass1
        if (fileName.equals(ATTRIBUTE_WRAPPER_NAME_1)) {
            // Add extension to the java class
            eo.setCaretPosition("public class " + fileName + " ", false);
            eo.insert("extends Super" + fileName + " ");
            // Add methods to the java class
            // Set caret position after the first occurence of "}",
            // that is after the constructor declaration
            //JF: constructor is not always generated by NetBeans,
            //JF: so rather set caret after the first "{"
            //JF: which is the beginning of class body.
            // eo.setCaretPosition("}", false);
            eo.setCaretPosition("{", false);
            eo.insert(addGetAttribute1() +
                    addGetAttribute2() +
                    addIsAttribute2() +
                    addGetAttribute3() +
                    addSetAttribute3() +
                    addGetAttribute4() +
                    addSetAttribute4() +
                    addGetAttribute5_Double() +
                    addSetAttribute5() +
                    addSetAttribute6_Float() +
                    addSetAttribute6_Date());
            // Add inner class declaration
            eo.insert("}\n\n");
            eo.insert("class Super" + fileName + " {\n" +
                    addGetAttribute5_Object() +
                    addSetAttribute6_Object());
        }
        // Update WrapperJavaClass2
        else if (fileName.equals(ATTRIBUTE_WRAPPER_NAME_2)) {
            // Add generics to the java class declaration
            eo.setCaretPosition("public class " + fileName, false);
            eo.insert("<Z,Q> ");
            // Add methods to the java class
            // Set caret position after the first occurence of "}",
            // that is after the constructor declaration
            //JF: constructor is not always generated by NetBeans,
            //JF: so rather set caret after the first "{"
            //JF: which is the beginning of class body.
            // eo.setCaretPosition("}", false);
            eo.setCaretPosition("{", false);
            eo.insert(addGetGenericAttribute1() +
                    addGetGenericAttribute2() +
                    addGetAttribute1());
        }
        
        eo.save();
    }
    
    private String addGetAttribute1() {
        return "\n\tpublic String get" + ATTRIBUTE_1 + "() {" +
                "\n\t\treturn \"\";" +
                "\n\t}\n";
    }
    
    private String addGetAttribute2() {
        return  "\n\tpublic boolean get" + ATTRIBUTE_2 + "() {" +
                "\n\t\treturn false;" +
                "\n\t}\n";
    }
    
    private String addIsAttribute2() {
        return  "\n\tpublic boolean is" + ATTRIBUTE_2 + "() {" +
                "\n\t\treturn false;" +
                "\n\t}\n";
    }
    
    private String addGetAttribute3() {
        return  "\n\tpublic java.util.List get" + ATTRIBUTE_3 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addSetAttribute3() {
        return  "\n\tpublic void set" + ATTRIBUTE_3 + "(java.util.List l) {" +
                "\n\t}\n";
    }
    
    private String addGetAttribute4() {
        return  "\n\tpublic String[] get" + ATTRIBUTE_4 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addSetAttribute4() {
        return  "\n\tpublic void set" + ATTRIBUTE_4 + "(String[] s) {" +
                "\n\t}\n";
    }
    
    private String addGetAttribute5_Double() {
        return  "\n\tpublic Double get" + ATTRIBUTE_5 + "() {" +
                "\n\t\treturn new Double(0);" +
                "\n\t}\n";
    }
    
    private String addGetAttribute5_Object() {
        return  "\n\tpublic Object get" + ATTRIBUTE_5 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addSetAttribute5() {
        return  "\n\tpublic void set" + ATTRIBUTE_5 + "(Double d) {" +
                "\n\t}\n";
    }
    
    private String addSetAttribute6_Float() {
        return  "\n\tpublic void set" + ATTRIBUTE_6 + "(float f) {" +
                "\n\t}\n";
    }
    
    private String addSetAttribute6_Date() {
        return  "\n\tpublic void set" + ATTRIBUTE_6 + "(java.util.Date d) {" +
                "\n\t}\n";
    }
    
    private String addSetAttribute6_Object() {
        return  "\n\tpublic void set" + ATTRIBUTE_6 + "(Object o) {" +
                "\n\t}\n";
    }
    
    private String addGetGenericAttribute1() {
        return "\n\tpublic Z get" + GENERIC_ATTRIBUTE_1 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addGetGenericAttribute2() {
        return "\n\tpublic <T> T get" + GENERIC_ATTRIBUTE_2 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
}


