package gui;

import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jam.*;
import org.netbeans.test.oo.gui.jello.*;
import org.netbeans.test.oo.gui.jelly.beans.JBWizard;
import org.netbeans.test.oo.gui.jelly.java.JavaWizard;

import org.netbeans.jemmy.operators.JDialogOperator;
import javax.swing.JDialog;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;


public class ChangingOfBeanPropertyProperties  extends NbTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";
    
    private static boolean mount   = true;
    private static boolean unmount = false;
    
    /** Need to be defined because of JUnit */
    public ChangingOfBeanPropertyProperties (String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();        
        suite.addTest(new ChangingOfBeanPropertyProperties ("testChangePropertyNameAndType"));
        suite.addTest(new ChangingOfBeanPropertyProperties ("testChangeMode"));
//        suite.addTest(new ChangingOfBeanPropertyProperties ("testChangeOptionsForListener"));
        suite.addTest(new ChangingOfBeanPropertyProperties ("testDeleteAnyPropertiesAndEvents"));
        suite.addTest(new ChangingOfBeanPropertyProperties ("testChangeSourceCode"));        
        suite.addTest(new ChangingOfBeanPropertyProperties ("testChangeOfStyleOfDeclaredVariable"));

        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }
    
    public void setUp() {
        // redirect jemmy trace and error output to a log
        JellyProperties.setJemmyOutput(new PrintWriter(getLog(), true), new PrintWriter(getLog(), true));
        JellyProperties.setJemmyDebugTimeouts();
        JellyProperties.setDefaults();
        if (mount) {
            new JelloRepository().findOrMount(sampleDir);
            mount = false;
        }
        
    }
    
    public void tearDown() {
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        
        String myObject = sampleDir+explorer.delim+NAME_TEST_FILE;
//        JamUtilities.waitEventQueueEmpty(3000);                
        explorer.pushPopupMenu("Delete", myObject);
        new JelloYesNoDialog("Confirm Object Deletion").yes();
//        JamUtilities.waitEventQueueEmpty(1500);        
        if (unmount) {
            System.out.println("UNMOUNTING");
            explorer.pushPopupMenu("Unmount Filesystem", sampleDir);
            
        }
        JamUtilities.waitEventQueueEmpty(1000);
    } 

    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - add a new property
     */    
    public void testChangeOfStyleOfDeclaredVariable() {
        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"),
        sampleDir);
        jw.setName(NAME_TEST_FILE);        
        jw.finish();

        MainFrame mf = MainFrame.getMainFrame();
        //mf.switchToEditing();
        mf.pushMenu("Tools|Options...");
        Options opt = new Options();
        PropertiesWindow pw = opt.getPropertiesWindow("Editing"+opt.delim+"Beans Property");
        pw.setSelectedItem("Style of Declared Variable", 0);
//start of the first property
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("firstName");
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("int");
               
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
                
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();        
//end of the first property
        pw.setSelectedItem("Style of Declared Variable", 1);
//start of the second property
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
                
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("secondName");
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
               
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
                
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();        
//end of the second property
        pw.close();
        opt.close();             
        
        Editor editor = new Editor(NAME_TEST_FILE);
        editor.select(1,10);
        editor.deleteSelectedText();
        
        ref(editor.getText());
        compareReferenceFiles();
    }   
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = 0
     *  - add a new property with an initial value
     *  - change of property type a name
     */    
    public void testChangePropertyNameAndType() {
        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"),
        sampleDir);
        jw.setName(NAME_TEST_FILE);        
        jw.finish();

        MainFrame mf = MainFrame.getMainFrame();
        //mf.switchToEditing();
        mf.pushMenu("Tools|Options...");
        Options opt = new Options();
        PropertiesWindow pw = opt.getPropertiesWindow("Editing"+opt.delim+"Beans Property");
//	JamUtilities.waitEventQueueEmpty(3000);                
        pw.setSelectedItem("Style of Declared Variable", 0);
        pw.close();
        opt.close();             

        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("initialName");
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        //jComboBox.setSelectedItem("initialType");
        jComboBox.setEditableText("initialType");
                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
                        
        okCancelHelpDialog.ok();        
//end of the first property

        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenu("Properties", sampleDir+explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
                           +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
                           +explorer.delim+"initialName");
        PropertiesWindow pwe = new PropertiesWindow("initialName");
        pwe.switchToTab("Properties");
        pwe.openEditDialog("Name");
        JelloPropertyDialog dialog = new JelloPropertyDialog();
        dialog.setValue("requiredName");
        new JamButton(dialog, "Ok").doClickNoBlock();        
        new JelloYesNoDialog(JelloUtilities.getForteFrame(),"Question").yes();        
        pwe.setText("Type","requiredType");        
        new JelloYesNoDialog(JelloUtilities.getForteFrame(),"Question").yes();
        pwe.close();
        
        Editor editor = new Editor(NAME_TEST_FILE);
        editor.select(1,10);
        editor.deleteSelectedText();
        
        ref(editor.getText());
        compareReferenceFiles();
        
    }

    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - Add a new property
     *  - Change of the first property mode to Read Only
     *  - Change of the second property mode to Write Only
     */        
    public void testChangeMode() {
        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"),
        sampleDir);
        jw.setName(NAME_TEST_FILE);        
        jw.finish();

        MainFrame mf = MainFrame.getMainFrame();
        //mf.switchToEditing();
        mf.pushMenu("Tools|Options...");
        Options opt = new Options();
        PropertiesWindow pw = opt.getPropertiesWindow("Editing"+opt.delim+"Beans Property");
        pw.setSelectedItem("Style of Declared Variable", 0);
//start of the first property
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("firstName");
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("int");
               
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
                
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();        
//end of the first property
//        JamUtilities.waitEventQueueEmpty(3000);                
        pw.setSelectedItem("Style of Declared Variable", 1);
//start of the second property
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
                
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("secondName");
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
               
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
                
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();        
//end of the second property
        pw.close();
        opt.close();             
        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
//        JamUtilities.waitEventQueueEmpty(5000);                
        explorer.pushPopupMenu("Properties", sampleDir+explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
                           +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
                           +explorer.delim+"firstName");
        PropertiesWindow pwe = new PropertiesWindow("firstName");
        pwe.switchToTab("Properties");
        pwe.setSelectedItem("Mode","Read Only");        
        new JelloYesNoDialog(JelloUtilities.getForteFrame(),"Question").yes();
        pwe.close();
        explorer.pushPopupMenu("Properties", sampleDir+explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
                           +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
                           +explorer.delim+"secondName");
        pwe = new PropertiesWindow("secondName");
        pwe.switchToTab("Properties");
        pwe.setSelectedItem("Mode","Write Only");        
        new JelloYesNoDialog(JelloUtilities.getForteFrame(),"Question").yes();
        pwe.close();

        Editor editor = new Editor(NAME_TEST_FILE);
        editor.select(1,10);
        editor.deleteSelectedText();
        
        ref(editor.getText());
        compareReferenceFiles();
    }   

    public void testChangeSourceCode() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Bean.java"), sampleDir);
        jbw.setName(NAME_TEST_FILE);        
        jbw.updatePanel(0);
        jbw.finish();

        Editor editor = new Editor(NAME_TEST_FILE);
        editor.find(NAME_TEST_FILE);
        editor.changeCaretPosition(1,1);
        editor.insertRow("    private static final String PROP_MY_PROPERTY = \"MyProperty\";", 16);

        editor.insertRow("    private String myProperty;", 19);

        editor.insertRow("    public String getMyProperty() {", 38);
        editor.insertRow("        return myProperty;", 39);
        editor.insertRow("    }", 40);
    
        editor.insertRow("    public void setMyProperty(String value) {", 42);
        editor.insertRow("        String oldValue = myProperty;", 43);
        editor.insertRow("        myProperty = value;", 44);
        editor.insertRow("        propertySupport.firePropertyChange(PROP_MY_PROPERTY, oldValue, myProperty);", 45);
        editor.insertRow("    }", 46);
        editor.insertRow("", 47);

        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenu("Properties", sampleDir+explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
                           +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
                           +explorer.delim+"myProperty");
        PropertiesWindow pwe = new PropertiesWindow("myProperty");
        assertEquals("Estimated Field","String myProperty",pwe.getValue("Estimated Field"));
        assertEquals("Getter","getMyProperty ()",pwe.getValue("Getter"));
        assertEquals("Mode","Read / Write",pwe.getValue("Mode"));
        assertEquals("Name of Property","myProperty",pwe.getValue("Name"));
        assertEquals("Setter","setMyProperty (String)",pwe.getValue("Setter"));
        assertEquals("Type","String",pwe.getValue("Type"));
        pwe.close();                
    }

    public void testChangeOptionsForListener() {
        System.out.println("This testcase cannot be performed due a problem with properties window after change of listener");
    }

    private void createContent() {
        // Start - NonIndexProperty
        Explorer explorer = new Explorer();
        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));
        
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_NON_INDEX_PROPERTY);
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
        
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
        
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        // End - NonIndexProperty
        
        // Start - IndexProperty
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));
        
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        // End - IndexProperty
        
        // Start - UnicastEventSource
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES"));
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton")).doClick();
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        // End - UnicastEventSource
        
        // Start - MulticastEventSourceArrayListImpl
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(1);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton")).doClick();
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        // End - MulticastEventSourceArrayListImpl
        
        // Start - MulticastEventSourceEventListenerListImpl
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(2);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton")).doClick();
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        // End - MulticastEventSourceEventListenerListImpl
    }    
    
    public void testDeleteAnyPropertiesAndEvents() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Bean.java"), sampleDir);
        jbw.setName(NAME_TEST_FILE);        
        jbw.updatePanel(0);
        jbw.finish();
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        Editor editor = new Editor(NAME_TEST_FILE);
        editor.select(1,6);
        editor.deleteSelectedText();
        editor.select(2,6);
        editor.deleteSelectedText();
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsInitial.ref")));
            out.print(editor.getText());
            out.close();            
        } catch(java.io.IOException exc) {
            exc.printStackTrace();
        }               
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsInitial.ref", "testDeleteAnyPropertiesAndEventsInitial.pass", "testDeleteAnyPropertiesAndEventsInitial.diff");
// Delete nonIndexProperty
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+NAME_NON_INDEX_PROPERTY
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 
// Delete indexProperty
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+NAME_INDEX_PROPERTY
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 
// Delete action listener
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+"actionListener"
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 
// Delete focus listener
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+"focusListener"
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 
        
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsModified.ref")));
            out.print(editor.getText());
            out.close();            
        } catch(java.io.IOException exc) {
            exc.printStackTrace();
        }               
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsModified.ref", "testDeleteAnyPropertiesAndEventsModified.pass", "testDeleteAnyPropertiesAndEventsModified.diff");        
        
    }
    
    public void testUnmount() {
        System.out.println("testUnmount");
        unmount = true;
    }

}
