package gui;

import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jam.*;
import org.netbeans.test.oo.gui.jello.*;
import org.netbeans.test.oo.gui.jelly.java.JavaWizard;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import java.io.File;


public class NewMulticastEventSource extends NbTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
            
    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";

    
    private static boolean mount   = true;
    private static boolean unmount = false;
    
        
    /** Need to be defined because of JUnit */
    public NewMulticastEventSource(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NewMulticastEventSource("testGenerateEmpty"));        
        suite.addTest(new NewMulticastEventSource("testGenerateArrayListImplementation"));        
        suite.addTest(new NewMulticastEventSource("testGenerateEventListenerListImplementation"));        
        suite.addTest(new NewMulticastEventSource("testGenerateEventFiringMethods"));        
        suite.addTest(new NewMulticastEventSource("testPassEventAsParameter"));                                
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
            explorer.pushPopupMenu("Unmount Filesystem", sampleDir);
            
        }
        JamUtilities.waitEventQueueEmpty(1500);
    }  

     public void testGenerateEmpty() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_emptyRadioButton")).doClick();        
                               
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }

     public void testGenerateArrayListImplementation() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton")).doClick();
                        
//        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
//        jCheckBox.setSelected(true);
        
//        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
//        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }

     public void testGenerateEventListenerListImplementation() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton")).doClick();
                        
//        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
//        jCheckBox.setSelected(true);
        
//        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
//        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }
     
     public void testGenerateEventFiringMethods() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton")).doClick();
                        
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
//        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
//        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }

     public void testPassEventAsParameter() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton")).doClick();
                        
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }

     public void testUnmount() {
        unmount = true;
    }
   
}