package gui;

import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jam.*;
import org.netbeans.test.oo.gui.jello.*;
import org.netbeans.test.oo.gui.jelly.java.JavaWizard;

import org.netbeans.test.oo.gui.jello.JelloPropertiesPane;
import org.netbeans.test.oo.gui.jello.JelloPropertiesSheet;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import java.io.File;


public class CreateNewNonIndexedProperty extends NbTestCase {

    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    private static final String NAME_WRONG = "123";
    private static final String TYPE_WRONG = "+++";

    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";
    
    private boolean mount   = true;
    
    
    /** Need to be defined because of JUnit */
    public CreateNewNonIndexedProperty(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateNewNonIndexedProperty("testName"));        
        suite.addTest(new CreateNewNonIndexedProperty("testType"));        
        suite.addTest(new CreateNewNonIndexedProperty("testMode"));        
        suite.addTest(new CreateNewNonIndexedProperty("testBound"));        
        suite.addTest(new CreateNewNonIndexedProperty("testConstrained"));        
        suite.addTest(new CreateNewNonIndexedProperty("testGenerateField"));        
        suite.addTest(new CreateNewNonIndexedProperty("testGenerateReturnStatement"));        
        suite.addTest(new CreateNewNonIndexedProperty("testGenerateSetStatement"));        
        suite.addTest(new CreateNewNonIndexedProperty("testGeneratePropertyChangeSupport"));        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }

    /** setUp method  */
    public void setUp() {
        // redirect jemmy trace and error output to a log
        JellyProperties.setJemmyOutput(new PrintWriter(getLog(), true), new PrintWriter(getRef(), true));
        JellyProperties.setDefaults();
        if (mount) {
            new JelloRepository().findOrMount(sampleDir);
            mount = false;
        }        
        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"), sampleDir);
        jw.setName(NAME_TEST_FILE);        
        jw.finish();
        
    }
    
    /** tearDown method */
    public void tearDown() {
        Explorer explorer = new Explorer();            
        explorer = Explorer.find();          
        explorer.switchToFilesystemsTab();                                
        String myObject = sampleDir+explorer.delim+NAME_TEST_FILE;
        JamUtilities.waitEventQueueEmpty(3000);
        explorer.pushPopupMenu("Delete", myObject);
        new JelloYesNoDialog("Confirm Object Deletion").yes();        
        JamUtilities.waitEventQueueEmpty(1500);
        if (!mount) {
            explorer.pushPopupMenu("Unmount Filesystem", sampleDir);            
        }
        JamUtilities.waitEventQueueEmpty(1500);
    }

    
    /** testName method */
    public void testName() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );     
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));        
        
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_WRONG);        
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
        
        new JamButton(okCancelHelpDialog, "Ok").doClickNoBlock();
        
        new JelloOKOnlyDialog(JelloUtilities.getForteFrame(),"Error").ok();
        
        
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_NON_INDEX_PROPERTY);
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
                       
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

    /** testType method */
    public void testType() {
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
        jComboBox.setEditableText(TYPE_WRONG);
        
        new JamButton(okCancelHelpDialog, "Ok").doClickNoBlock();
        
        new JelloOKOnlyDialog(JelloUtilities.getForteFrame(),"Error").ok();
        
        
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_NON_INDEX_PROPERTY);
        
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("double");
                       
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
    
    /** testMode method */
    public void testMode() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );     
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("first");                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("int");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read Only");                       
        okCancelHelpDialog.ok();

        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));                
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("second");                
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("double");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Write Only");                       
        okCancelHelpDialog.ok();

        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty"));                
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("third");                
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("long");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
    
    /** testBound method */
    public void testBound() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        okCancelHelpDialog.ok();

        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

    /** testConstrained method */
    public void testConstrained() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

    /** testGenerateField method */
    public void testGenerateField() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
    
    /** testGenerateReturnStatement method */
    public void testGenerateReturnStatement() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBox.setSelected(true);
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

    /** testGenerateSetStatement method */
    public void testGenerateSetStatement() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

    /** testGeneratePropertyChangeSupport method */
    public void testGeneratePropertyChangeSupport() {
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
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);

        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);

        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
    
}


