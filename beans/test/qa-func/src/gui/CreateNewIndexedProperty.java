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


public class CreateNewIndexedProperty extends NbTestCase {

    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY = "indexProperty";
    private static final String NAME_WRONG = "123";
    private static final String TYPE_WRONG = "+++";

    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";
    
    private boolean mount   = true;
    
    
    /** Need to be defined because of JUnit */
    public CreateNewIndexedProperty(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateNewIndexedProperty("testName"));        
        suite.addTest(new CreateNewIndexedProperty("testType"));        
        suite.addTest(new CreateNewIndexedProperty("testMode"));        
        suite.addTest(new CreateNewIndexedProperty("testBound"));        
        suite.addTest(new CreateNewIndexedProperty("testConstrained"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateField"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateReturnStatement"));        
        suite.addTest(new CreateNewIndexedProperty("testGenerateSetStatement"));     
        suite.addTest(new CreateNewIndexedProperty("testGenerateNonIndexedGetterWithReturnStatement"));     
        suite.addTest(new CreateNewIndexedProperty("testGenerateIndexedSetter"));             
        suite.addTest(new CreateNewIndexedProperty("testGeneratePropertyChangeSupport"));        
        return suite;
    }

    /** testTest method */
    public void testTest() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
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
        JellyProperties.setJemmyDebugTimeouts();
        JellyProperties.setDefaults();
        if (mount) {
            new JelloRepository().findOrMount(sampleDir);
            mount = false;
        }        
  /***/
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.selectNode(sampleDir);
        MainFrame mf = MainFrame.getMainFrame();
        mf.pushFileMenu("New...");
        JamDialog dialog = new JamDialog("New Wizard");
        DialogNode node = new DialogNode(dialog, new JamTree(dialog), "Templates, Java Classes");
        node.expand();
        node.getChild("Class").select();
        new JamButton(dialog, "Next >").doClick();
        dialog = new JamDialog("New Wizard - Class");
        dialog.getJamTextField(0).setText(NAME_TEST_FILE);
        new JamButton(dialog, "Finish").doClick();                
/***/      

//        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"),
//        sampleDir);
//        jw.setName(NAME_TEST_FILE);        
//        jw.finish();
        
    }
    
    /** tearDown method */
    public void tearDown() {

        Explorer explorer = new Explorer();            
        explorer = Explorer.find();          
        explorer.switchToFilesystemsTab();                                
        String myObject = sampleDir+explorer.delim+NAME_TEST_FILE;
//        JamUtilities.waitEventQueueEmpty(3000);        
        explorer.pushPopupMenu("Delete", myObject);
//        JamUtilities.waitEventQueueEmpty(1500);
        new JelloYesNoDialog("Confirm Object Deletion").yes();        
//        JamUtilities.waitEventQueueEmpty(1500);        
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );     
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));        
        
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_WRONG);        
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("String");
        
        new JamButton(okCancelHelpDialog, "Ok").doClickNoBlock();
        
        new JelloOKOnlyDialog(JelloUtilities.getForteFrame(),"Error").ok();
        
        
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);
        
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );     
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));        
        
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);        
        
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText(TYPE_WRONG);
        
        new JamButton(okCancelHelpDialog, "Ok").doClickNoBlock();
        
        new JelloOKOnlyDialog(JelloUtilities.getForteFrame(),"Error").ok();
        
        
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);
        
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );     
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("first");                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("int");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read Only");                       
        okCancelHelpDialog.ok();

        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText("second");                
        jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem("double");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Write Only");                       
        okCancelHelpDialog.ok();

        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_boundCheckBox"));
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_constrainedCheckBox"));
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);        
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }

     /** testGenerateNonIndexedGetterWithReturnStatement method */
    public void testGenerateNonIndexedGetterWithReturnStatement() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBox.setSelected(true);

        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
   
    /** testGenerateIndexedSetter method */
    public void testGenerateIndexedSetter() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
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
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );             
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty"));                
        JamTextField jField = new JamTextField(okCancelHelpDialog, 0);
        jField.setText(NAME_INDEX_PROPERTY);                
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setEditableText("MyType");
        jComboBox = new JamComboBox(okCancelHelpDialog, 1);
        jComboBox.setSelectedItem("Read / Write");                       
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBox.setSelected(true);
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBox.setSelected(true);        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_supportCheckBox"));
        jCheckBox.setSelected(true);

        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();               
    }
    
}


