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
import org.netbeans.modules.jmx.test.helpers.JellyConstants;
import org.netbeans.modules.jmx.test.helpers.MBean;




/**
 *
 * @author an156382
 */
public class CreateOperationWrapperMBean extends JellyTestCase {
    
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
        JellyToolsHelper.grabProjectNode(JellyConstants.PROJECT_NAME);
    }
    
    public void tearDown() {
        
    }
    
    public void createClass() {
        createMissingClass(JellyConstants.PROJECT_NAME, 
                JellyConstants.OPCLASS_TO_WRAP, JellyConstants.PACKAGE_NAME);
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
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init(JellyConstants.CATEGORY, 
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
                JellyConstants.MBEAN_FIFTEEN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_FIFTEEN_COMMENT,
                JellyConstants.PACKAGE_NAME+JellyConstants.PT+
                JellyConstants.OPCLASS_TO_WRAP);
    }
    
    private MBean createFourthWrapperMBean() {
        return new MBean(
                JellyConstants.MBEAN_SIXTEEN, 
                JellyConstants.PACKAGE_NAME, 
                JellyConstants.MBEAN_SIXTEEN_COMMENT,
                JellyConstants.PACKAGE_NAME+JellyConstants.PT+
                JellyConstants.OPCLASS_TO_WRAP);
    }
   
     //========================= Utility class generation  ===========================//
     
     public void createMissingClass(String projectName, String fileName, 
            String packageName) {
        //creates a reference on the current wizard
        NewFileWizardOperator nfwoForFile = NewFileWizardOperator.invoke();
        
        //select the config wizard
        nfwoForFile.selectProject(projectName);
        nfwoForFile.selectCategory(JellyConstants.JAVA_FILE_CATEG);
        nfwoForFile.selectFileType(JellyConstants.JAVA_FILE_TYPE);
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
        ProjectRootNode prn = pto.getProjectRootNode(JellyConstants.PROJECT_NAME);
        
        prn.select();
        Node node = new Node(prn, JellyConstants.SRC_PKG + packageName + 
                JellyConstants.PIPE + fileName);
        
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
        return JellyToolsHelper.getTextFieldContent(JellyConstants.GENFILE_TXT, nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+
                JellyConstants.JAVA_EXT);
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
        
        JellyToolsHelper.changeCheckBoxSelection(JellyConstants.EXISTINGCLASS_CBX, nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.EXISTINGCLASS_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        JellyToolsHelper.setTextFieldContent(JellyConstants.EXISTINGCLASS_TXT, nfwo, mbean.getClassToWrap());
        assertEquals(mbean.getClassToWrap(), 
                JellyToolsHelper.getTextFieldContent(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.PREREGPARAM_CBX, nfwo));
        
        JellyToolsHelper.setTextFieldContent(JellyConstants.MBEANDESCR_TXT, nfwo,
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                JellyConstants.MBEANDESCR_TXT, nfwo));
    }
    
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.W_OPER_TBL,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.W_OPER_REM_BTN,nfwo));
        
        if (mbean.getMBeanName().equals(JellyConstants.MBEAN_SIXTEEN)) {
           // check/uncheck attributes to keep
            JTableOperator jto = JellyToolsHelper.getPanelTableOperator(JellyConstants.W_OPER_TBL, nfwo);
            JTableMouseDriver mouseDriver = new JTableMouseDriver(); 
            mouseDriver.selectCell(jto,JellyConstants.LINE_ZERO, JellyConstants.INCLUDE_COL);
            mouseDriver.selectCell(jto,JellyConstants.LINE_ONE,JellyConstants.INCLUDE_COL);
            mouseDriver.selectCell(jto,JellyConstants.LINE_TWO, JellyConstants.INCLUDE_COL);
        }
    }
    
    private void junitStep(NewFileWizardOperator nfwo, MBean mbean) {
        JellyToolsHelper.changeCheckBoxSelection(JellyConstants.JU_CBX, nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JU_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JU_CBX, nfwo));
        
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.CLASSTOTEST_TXT, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.CLASSTOTEST_TXT, nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.TESTCLASS_TXT, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.TESTCLASS_TXT, nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.DEFMETHBODY_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.DEFMETHBODY_CBX, nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JAVADOC_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JAVADOC_CBX, nfwo));
        
    }
    
    private boolean checkMBeanTypeButtons(NewFileWizardOperator nfwo, MBean mbean) {
        String type = mbean.getMBeanType();
           if (type.equals(JellyConstants.STDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
           } else if (type.equals(JellyConstants.EXTSTDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
        } else if (type.equals(JellyConstants.DYNMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                return true;
        }
        return false;
    }
     
}
