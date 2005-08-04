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
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JTable;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Notification;
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;



/**
 *
 * @author an156382
 */
public class CreateOneFeatureMBean extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public CreateOneFeatureMBean(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOneFeatureMBean("constructTest2MBean"));
        //suite.addTest(new CreateOneFeatureMBean(""));
        //suite.addTest(new CreateOneFeatureMBean(""));
        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        JellyToolsHelper.grabProjectNode("MBeanFunctionalTest");
    }
    
    public void tearDown() {
        
    }
    
    public void constructTest2MBean() {
        
        MBean standardMBean = createStandardMBean();
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest6MBean() {
        
        MBean extendedMBean = createExtendedStandardMBean();
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest10MBean() {
        
        MBean dynamicMBean = createDynamicMBean();
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    /****************************************************************************/
    /************************* SPECIRFIC HELPER METHODS *************************/
    /****************************************************************************/
    
    //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init("JMX MBean", 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo, mbean);
        nfwo.next();
       
        operationStep(nfwo, mbean);
        nfwo.next();
        
        notificationStep(nfwo, mbean);
        nfwo.next();
      
        junitStep(nfwo);
        nfwo.finish();
    }
    
    //========================= Class Name generation ===========================//
    
    private MBean createStandardMBean() {
        
        return new MBean(
                "ConstructTest2MBean", 
                "StandardMBean", 
                "com.foo.bar", 
                "StandardMBean with one simple attribute, one two parameter operation and one notification", 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications());
    }
    
    private MBean createExtendedStandardMBean() {
        
        return new MBean(
                "ConstructTest6MBean", 
                "ExtendedStandardMBean", 
                "com.foo.bar", 
                "Extended StandardMBean with one simple attribute, one two parameter operation and " +
                "one notification", 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications());
    }
    
    private MBean createDynamicMBean() {
        
        return new MBean(
                "ConstructTest10MBean", 
                "DynamicMBean", 
                "com.foo.bar", 
                "DynamicMBean with one simple attribute, one two parameter operation and one notification", 
                constructMBeanAttributes(), 
                constructMBeanOperations(), 
                constructMBeanNotifications());
    }
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        
        Attribute mBeanAttribute = new Attribute("firstAttribute", "int", "ReadOnly", 
                "First Attribute description");
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        attrs.add(mBeanAttribute);
        
       return attrs;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        
        Parameter mBeanOperationParameter1 = new Parameter("firstParameter", "String",
                "First Parameter Description");
        Parameter mBeanOperationParameter2 = new Parameter("secondParameter", "ObjectName",
                "Second Parameter Description");
        ArrayList<Parameter> params = new ArrayList<Parameter>();
        params.add(mBeanOperationParameter1);
        params.add(mBeanOperationParameter2);
        
        //operation construction
        Operation mBeanOperation = new Operation("FirstOperation", "void", params, null, 
                "First Operation Description");
        ArrayList<Operation> ops = new ArrayList<Operation>();
        ops.add(mBeanOperation);
        
        return ops;
    }
    
    private ArrayList<Notification> constructMBeanNotifications() {
        
        //Notification construction
        Notification mBeanNotification = new Notification("javax.management.Notification",
                "First Notification Description", null);
        ArrayList<Notification> notifs = new ArrayList<Notification>();
        notifs.add(mBeanNotification);
        
        return notifs;
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
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("ExistingClassCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEnabled("ExistingClassTextField", nfwo));
        
        JellyToolsHelper.changeRadioButtonSelection(mbean.getMBeanType(), nfwo, true);
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        JellyToolsHelper.changeCheckBoxSelection("ImplementMBeanItf", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("ImplementMBeanItf", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("PreRegisterParam", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("PreRegisterParam", nfwo));
        
        JellyToolsHelper.setTextFieldContent("mbeanDescriptionJTextField", nfwo, 
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                "mbeanDescriptionJTextField", nfwo));
    }
    
    private void attributeStep(NewFileWizardOperator nfwo, MBean mbean) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled("attributeTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("attrAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("attrRemoveJButton",nfwo)); 
        
        fillMBeanAttributes(nfwo, mbean);
    }
    
    private void fillMBeanAttributes(NewFileWizardOperator nfwo, MBean mbean) {
        for (int i = 0; i < mbean.getNumberOfAttributes(); i++) {
            Attribute attr = mbean.getMBeanAttribute(i);
            
            JellyToolsHelper.clickOnPanelButton("attrAddJButton", nfwo);
            JTableOperator jto = JellyToolsHelper.getPanelTableOperator("attributeTable", nfwo);
            JTableMouseDriver mouseDriver = new JTableMouseDriver();
            mouseDriver.editCell(jto, i, 0, attr.getName());
            mouseDriver.editCell(jto, i, 1, attr.getType());
            mouseDriver.selectCell(jto, i, 2);
            JellyToolsHelper.changeTableComboBoxItem("attrAccessBox", jto, attr.getAccess());
            mouseDriver.editCell(jto, i, 3, attr.getComment());
        }
    }
        
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled("methodTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("methAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("methRemoveJButton",nfwo));
        /*
        JTable opTable = JellyToolsHelper.getPanelTable("methodTable", nfwo);
        JTableOperator jto = JellyToolsHelper.getPanelTableOperator("methodTable", nfwo);
        JTableMouseDriver panelTableMouseDriver = new JTableMouseDriver();

        for (int i = 0; i < mbean.getNumberOfOperations(); i++) {
            JellyToolsHelper.clickOnPanelButton("methAddJButton", nfwo);
            Operation oper = mbean.getMBeanOperation(i);
            
            panelTableMouseDriver.editCell(jto, i, 0, oper.getOperationName());
            panelTableMouseDriver.selectCell(jto, i, 1);
            JellyToolsHelper.changeComboBoxItem("methTypeBox", nfwo,
                    oper.getOperationReturnType());
            
            //sets the operation description first
            panelTableMouseDriver.editCell(jto, i, 4, oper.getOperationComment());
            
            jto.editCellAt(i, 2);
            fillOperationParameter(oper, jto, opTable);
        }
        // REM: No Exception handling yet
         **/
        fillMBeanOperation(nfwo, mbean);
    }
    
    private void fillMBeanOperation(NewFileWizardOperator nfwo, MBean mbean) {
        JTable opTable = JellyToolsHelper.getPanelTable("methodTable", nfwo);
        JTableOperator jto = JellyToolsHelper.getPanelTableOperator("methodTable", nfwo);
        JTableMouseDriver panelTableMouseDriver = new JTableMouseDriver();

        for (int i = 0; i < mbean.getNumberOfOperations(); i++) {
            JellyToolsHelper.clickOnPanelButton("methAddJButton", nfwo);
            Operation oper = mbean.getMBeanOperation(i);
            
            panelTableMouseDriver.editCell(jto, i, 0, oper.getOperationName());
            panelTableMouseDriver.selectCell(jto, i, 1);
            JellyToolsHelper.changeComboBoxItem("methTypeBox", nfwo,
                    oper.getOperationReturnType());
            
            //sets the operation description first
            panelTableMouseDriver.editCell(jto, i, 4, oper.getOperationComment());
            
            jto.editCellAt(i, 2);
            fillOperationParameter(oper, jto, opTable);
            fillOperationException(oper);
        }
    }
    
    private void fillOperationParameter(Operation oper, JTableOperator jto,
            JTable opTable) {
        if (oper.getOperationParameterSize() != 0) {
                
                JellyToolsHelper.clickOnTableButton("methAddParamButton", jto);
                JellyToolsHelper.tempo(1000);
                
                DialogOperator popup = JellyToolsHelper.getTablePopup(opTable);
                JButtonOperator butOp = JellyToolsHelper.getPopupButton(
                        "addParamJButton", popup);
                for (int j = 0; j < oper.getOperationParameterSize(); j++) {
                    
                    butOp.clickMouse();
                    
                    JTableOperator jto2 = JellyToolsHelper.getPopupTableOperator("ParamPopupTable", popup);
                    JTableMouseDriver mouseDriver = new JTableMouseDriver();
                    
                    Parameter p = oper.getOperationParameter(j);
                    
                    mouseDriver.editCell(jto2, j, 0, p.getParamName());
                    mouseDriver.editCell(jto2, j, 1, p.getParamType());
                    mouseDriver.editCell(jto2, j, 2, p.getParamComment());
                        
                }
                //close the popup
                JellyToolsHelper.clickOnPopupButton("closeJButton", popup);
        }
    }
    
    private void fillOperationException(Operation oper) {
        if (oper.getOperationExceptionSize() != 0) {
            //TODO add treatement here
        }
    }
    
    private void notificationStep(NewFileWizardOperator nfwo, MBean mbean) {
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("implNotifEmitCheckBox", nfwo));
        JellyToolsHelper.changeCheckBoxSelection("implNotifEmitCheckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("implNotifEmitCheckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("genDelegationCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("genSeqNbCheckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyTableEnabled("notificationTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("notifAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("notifRemJButton",nfwo));
        
        fillMBeanNotification(nfwo, mbean);
    }
    
    private void fillMBeanNotification(NewFileWizardOperator nfwo, MBean mbean) {
        JTable opTable = JellyToolsHelper.getPanelTable("notificationTable", nfwo);
        JTableOperator jto = JellyToolsHelper.getPanelTableOperator("notificationTable", nfwo);
        JTableMouseDriver panelTableMouseDriver = new JTableMouseDriver();

        for (int i = 0; i < mbean.getNumberOfNotifications(); i++) {
            JellyToolsHelper.clickOnPanelButton("notifAddJButton", nfwo);
            Notification notif = mbean.getMBeanNotification(i);
            
            panelTableMouseDriver.selectCell(jto, i, 0);
            JellyToolsHelper.changeComboBoxItem("notifClassBox", nfwo,
                    notif.getNotificationClass());
            panelTableMouseDriver.editCell(jto, i, 1, notif.getNotificationComment());
            
            fillNotificationType(notif);
        }
    }
    
    private void fillNotificationType(Notification notif) {
        if (notif.getNotificationTypeCount() != 0) {
            //TODO add treatement here
        }
    }
    
    private void junitStep(NewFileWizardOperator nfwo) {
        JellyToolsHelper.changeCheckBoxSelection("junitJChckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("junitJChckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("junitJChckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfClassToTest", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfClassToTest", nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfTestClass", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfTestClass", nfwo));
        
        JellyToolsHelper.changeCheckBoxSelection("defaultMethodBodyJCheckBox", nfwo, false);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("defaultMethodBodyJCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("defaultMethodBodyJCheckBox", nfwo));
        
        JellyToolsHelper.changeCheckBoxSelection("javaDocJCheckBox", nfwo, false);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("javaDocJCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("javaDocJCheckBox", nfwo));
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
