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
public class CreateOperationWrapperMBean extends JellyTestCase {
    
    public final String PROJECT_NAME = "MBeanFunctionalTest";
    public final String PACKAGE_NAME = "com.foo.bar";
    public final String CLASS_TO_WRAP = "WrappedOperation";
    
    /** Need to be defined because of JUnit */
    public CreateOperationWrapperMBean(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOperationWrapperMBean("createClass"));
        suite.addTest(new CreateOperationWrapperMBean("constructTest15MBean"));
        suite.addTest(new CreateOperationWrapperMBean("constructTest16MBean"));
        
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
    
    public void constructTest15MBean() {
        
        MBean wrapper3 = createThirdWrapperMBean();
        wizardExecution(wrapper3);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest16MBean() {
        
        MBean wrapper4 = createFourthWrapperMBean();
        wizardExecution(wrapper4);
        
        //TODO Diff between generated and master files
        
    }
    
  //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init("JMX MBean", 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        optionStep(nfwo, mbean);
        nfwo.next();
        nfwo.next();
        
        operationStep(nfwo, mbean);
        nfwo.next();
      
        nfwo.next();
      
        junitStep(nfwo, mbean);
        nfwo.finish();
    }
   
    //========================= Class Name generation ===========================//
    
    private MBean createThirdWrapperMBean() {
        return new MBean(
                "constructTest15MBean", 
                PACKAGE_NAME, 
                "Wrapped ExtendedStandardMBean with all operations",
                PACKAGE_NAME+"."+CLASS_TO_WRAP);
    }
    
    private MBean createFourthWrapperMBean() {
        return new MBean(
                "constructTest16MBean", 
                PACKAGE_NAME, 
                "Wrapped ExtendedStandardMBean with no operations",
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
        eo.deleteLine(33);
        eo.deleteLine(34);
    }
     
    private String createWrapperClass(String fileName) {
        return "public class " + fileName + " extends Super" +fileName+ " { \n" +
                "\t" + operation0() +
                "\t" + operation1() +
                "\t" + operation2() +
                "} \n \n" +
               createWrapperSuperClass("Super"+fileName)+ "\n ";
    }
    
    private String createWrapperSuperClass(String fileName) {
        return  "class " +fileName+ " { \n" +
                "\t public Integer op2(Object[] s, int t) { \n" +
                "\t return new Integer(0); \n" +
                "\t } \n"+
                "} \n";
    }
    
    private String operation0() {
        return "public void op0() throws java.lang.IllegalStateException {} \n";
    }
    
    private String operation1() {
        return  "public boolean op1(java.util.List l) { \n" +
                "\t return false; \n" +
                "\t } \n";
    }
    
    private String operation2() {
        return  "public Integer op2(String[] s, int t) { \n" +
                "\t return new Integer(0); \n" +
                "\t } \n";
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
    
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled("wrapperOperationTable",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("wrapperOpRemoveJButton",nfwo));
        
        if (mbean.getMBeanName().equals("constructTest16MBean")) {
           // check/uncheck attributes to keep
            JTableOperator jto = JellyToolsHelper.getPanelTableOperator("wrapperOperationTable", nfwo);
            JTableMouseDriver mouseDriver = new JTableMouseDriver(); 
            mouseDriver.selectCell(jto,0,0);
            mouseDriver.selectCell(jto,1,0);
            mouseDriver.selectCell(jto,2,0);
        }
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
