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

package org.netbeans.modules.jmx.test.mbeanwizard;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.MBean;




/**
 *
 * @author an156382
 */
public class CreateAttributeWrapperMBean extends JellyTestCase {
    
    public final String PROJECT_NAME = "MBeanFunctionalTest";
    public final String PACKAGE_NAME = "com.foo.bar";
    public final String CLASS_TO_WRAP = "WrappedAttribute";
    
    /** Need to be defined because of JUnit */
    public CreateAttributeWrapperMBean(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateAttributeWrapperMBean("createClass"));
        suite.addTest(new CreateAttributeWrapperMBean("constructTest13MBean"));
        suite.addTest(new CreateAttributeWrapperMBean("constructTest14MBean"));
        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        JellyToolsHelper.grabProjectNode(PROJECT_NAME);
    }
    
    public void tearDown() {
        
    }
    
    public void createClass() {
        createMissingClass(PROJECT_NAME, CLASS_TO_WRAP, PACKAGE_NAME);
    }
    
    public void constructTest13MBean() {
        
        MBean wrapper1 = createFirstWrapperMBean();
        wizardExecution(wrapper1);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest14MBean() {
        
        MBean wrapper2 = createSecondWrapperMBean();
        wizardExecution(wrapper2);
        
        //TODO Diff between generated and master files
        
    }
    
  //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init("JMX MBean", 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo, mbean);
        nfwo.next();
      
        nfwo.next();
        nfwo.next();
      
        junitStep(nfwo, mbean);
        nfwo.finish();
    }
   
    //========================= Class Name generation ===========================//
    
    private MBean createFirstWrapperMBean() {
        return new MBean(
                "constructTest13MBean", 
                PACKAGE_NAME, 
                "Wrapped ExtendedStandardMBean with all attributes",
                PACKAGE_NAME+"."+CLASS_TO_WRAP);
    }
    
    private MBean createSecondWrapperMBean() {
        return new MBean(
                "constructTest14MBean", 
                PACKAGE_NAME, 
                "Wrapped ExtendedStandardMBean with minimal attributes",
                PACKAGE_NAME+"."+CLASS_TO_WRAP);
    }
   
     //========================= Utility class generation  ===========================//
     
     public void createMissingClass(String projectName, String fileName, 
            String packageName) {
        //creates a reference on the current wizard
        NewFileWizardOperator nfwoForFile = NewFileWizardOperator.invoke();
        
        //select the config wizard
        nfwoForFile.selectProject(projectName);
        nfwoForFile.selectCategory("Java Classes");
        nfwoForFile.selectFileType("Java Class");
        nfwoForFile.next();
        
        NewFileNameLocationStepOperator nfnlsoForFile =
                new NewFileNameLocationStepOperator();
        
        //sets the file name
        nfnlsoForFile.setObjectName(fileName);
        JTextField folder = JellyToolsHelper.findFolderJTextField(fileName, 
                nfnlsoForFile.getContentPane());
        folder.setText(packageName);
        
        nfwoForFile.finish();
        
        //grabs project node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        
        JTreeOperator tree = pto.tree();
        ProjectRootNode prn = pto.getProjectRootNode("MBeanFunctionalTest");
        
        prn.select();
        Node node = new Node(prn, "Source Packages|" + packageName + "|" + fileName);
        
        node.select();
        
        EditorOperator eo = new EditorOperator(fileName);
        eo.deleteLine(17);
        eo.deleteLine(17);
        eo.deleteLine(17);
        eo.deleteLine(17);
        eo.setCaretPositionToLine(17);
  
        eo.insert(createWrapperClass(fileName));
        eo.deleteLine(60);
    }
     
    private String createWrapperClass(String fileName) {
        return "public class " + fileName + " extends Super" +fileName+ " { \n" +
                "\t" + attribute0() +
                "\t" + attribute1() +
                "\t" + attribute2() +
                "\t" + attribute3() +
                "\t" + attribute4() +
                "\t" + attribute5() +
                "} \n \n" +
               createWrapperSuperClass("Super"+fileName)+ "\n ";
    }
    
    private String createWrapperSuperClass(String fileName) {
        return  "class " +fileName+ " { \n" +
                "\t public void setAttribute5(Object d) {} \n" +
                "\n" +
                "\t public Object getAttribute4() { \n" +
                "\t return null; \n" +
                "\t } \n";
    }
    
    private String attribute0() {
        return "public String getAttribute0() { \n" +
                "\t return \"\"; \n " +
                "\t} \n";
    }
    
    private String attribute1() {
        return  "public boolean isAttribute1() { \n " +
                "\t return false; \n" +
                "\t} \n \n" +
                "\t public boolean getAttribute1() {\n " +
                "\t return false; \n" +
                "\t} \n";
    }
    
    private String attribute2() {
        return  "public java.util.List getAttribute2() { \n" +
                "\t return null; \n" +
                "\t} \n \n" +
                "\t public void setAttribute2(java.util.List l) {} \n \n";
    }
    
    private String attribute3() {
        return  "public String[] getAttribute3() { \n" +
                "\t return null; \n" +
                "\t} \n \n" +
                "\t public void setAttribute3(String[] s) {} \n \n";
    }
    
    private String attribute4() {
        return  "public Double getAttribute4() { \n " +
                "\t return new Double(0); \n" +
                "\t} \n \n" +
                "\t public void setAttribute4(Double d) {} \n \n";
    }
    
    private String attribute5() {
        return  "public void setAttribute5(float f) {} \n" +
                "\t public void setAttribute5(java.util.Date d) {} \n \n";
    }
     
    //========================= Class Name generation ===========================//
    
    private String getCompleteGeneratedFileName(NewFileWizardOperator nfwo) {
        return JellyToolsHelper.getTextFieldContent("generatedFileJTextField", nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+".java");
    }
    
    private String getInterfaceName(String completeFileName) {
        String itfWithExtension = completeFileName.substring(
                completeFileName.lastIndexOf(File.separatorChar)+1);
        return itfWithExtension.substring(0, itfWithExtension.lastIndexOf('.'));
    }
    
    //========================= Panel discovery ==================================//
    
    private void optionStep(NewFileWizardOperator nfwo, MBean mbean) {
        // get the generated file name for campare with master files
        String completeGeneratedFileName = getCompleteGeneratedFileName(nfwo);
        String className = getClassName(completeGeneratedFileName, mbean.getMBeanName());
        String itfName = getInterfaceName(completeGeneratedFileName);
        
        JellyToolsHelper.changeCheckBoxSelection("ExistingClassCheckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("ExistingClassCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("ExistingClassTextField", nfwo));
        JellyToolsHelper.setTextFieldContent("ExistingClassTextField", nfwo, mbean.getClassToWrap());
        assertEquals(mbean.getClassToWrap(), 
                JellyToolsHelper.getTextFieldContent("ExistingClassTextField", nfwo));
        
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("ImplementMBeanItf", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("ImplementMBeanItf", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("PreRegisterParam", nfwo));
        
        JellyToolsHelper.setTextFieldContent("mbeanDescriptionJTextField", nfwo,
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                "mbeanDescriptionJTextField", nfwo));
    }
    
    private void attributeStep(NewFileWizardOperator nfwo, MBean mbean) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled("wrapperAttributeTable",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("wrapperAttributeRemoveButton",nfwo)); 
        
        // check/uncheck attributes to keep
        JTableOperator jto = JellyToolsHelper.getPanelTableOperator("wrapperAttributeTable", nfwo);
        JTableMouseDriver mouseDriver = new JTableMouseDriver();
        
        if (mbean.getMBeanName().equals("constructTest13MBean"))
            wrapper13Attributes(nfwo, mouseDriver, jto);
        else 
            wrapper14Attributes(nfwo, mouseDriver, jto);
    }
    
    private void wrapper13Attributes(NewFileWizardOperator nfwo,
            JTableMouseDriver mouseDriver, JTableOperator jto) {
        //setting attribute 2 to read only
        mouseDriver.selectCell(jto, 2, 3);
        JellyToolsHelper.changeTableComboBoxItem("wrapperAttrAccessBox", jto, "ReadOnly");
        mouseDriver.selectCell(jto, 2, 3);
        assertEquals("ReadOnly",JellyToolsHelper.getTableComboBoxItem("wrapperAttrAccessBox", jto));
        // deselecting attribute 6
        mouseDriver.selectCell(jto, 6, 0);
    }
    
    private void wrapper14Attributes(NewFileWizardOperator nfwo,
            JTableMouseDriver mouseDriver, JTableOperator jto) {
        // deselecting attribute 0,2,4 and 5
        mouseDriver.selectCell(jto, 0, 0);
        mouseDriver.selectCell(jto, 2, 0);
        mouseDriver.selectCell(jto, 4, 0);
        mouseDriver.selectCell(jto, 5, 0);
    }
 
    private void junitStep(NewFileWizardOperator nfwo, MBean mbean) {
        JellyToolsHelper.changeCheckBoxSelection("junitJChckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("junitJChckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("junitJChckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfClassToTest", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfClassToTest", nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfTestClass", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfTestClass", nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("defaultMethodBodyJCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("defaultMethodBodyJCheckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("javaDocJCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("javaDocJCheckBox", nfwo));
        
    }
    
    private boolean checkMBeanTypeButtons(NewFileWizardOperator nfwo, MBean mbean) {
        String type = mbean.getMBeanType();
           if (type.equals("StandardMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("ExtendedStandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("DynamicMBean", nfwo));
                return true;
           } else if (type.equals("ExtendedStandardMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("StandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("DynamicMBean", nfwo));
                return true;
        } else if (type.equals("DynamicMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("StandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("ExtendedStandardMBean", nfwo));
                return true;
        }
        return false;
    }
     
}

