package gui;

import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jam.*;
import org.netbeans.test.oo.gui.jello.*;
import org.netbeans.test.oo.gui.jelly.beans.JBWizard;
import org.netbeans.test.oo.gui.jelly.java.JavaWizard;

import org.netbeans.jemmy.operators.JDialogOperator;
import javax.swing.JDialog;

import javax.swing.tree.TreePath;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;


import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import java.io.File;


public class BeanInfoGeneration extends NbTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";
    
    private static boolean mount   = true;
    private static boolean unmount = false;
    
    /** Need to be defined because of JUnit */
    public BeanInfoGeneration(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BeanInfoGeneration("testCheckNodes"));
        suite.addTest(new BeanInfoGeneration("testIncludeExclude"));
        suite.addTest(new BeanInfoGeneration("testBeanInfoNode"));
        suite.addTest(new BeanInfoGeneration("testPropertiesNode"));
        suite.addTest(new BeanInfoGeneration("testNodesDescription"));
        suite.addTest(new BeanInfoGeneration("testGenerateNewBeanInfo"));
        suite.addTest(new BeanInfoGeneration("testRegenerateBeanInfo"));
        suite.addTest(new BeanInfoGeneration("testCheckBeanInfoCompilability"));
        
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
//        JavaWizard jw = JavaWizard.launch(JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes") + "|" + JelloBundle.getString("org.netbeans.modules.java.Bundle","Templates/Classes/Class.java"),        sampleDir);
//        jw.setName(NAME_TEST_FILE);
//        jw.updatePanel(0);
//        jw.finish();
        
    }
    
    public void tearDown() {
//        JamUtilities.waitEventQueueEmpty(3000);
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        
        String myObject = sampleDir+explorer.delim+NAME_TEST_FILE;
        
        String myObjectBeanInfo = sampleDir+explorer.delim+NAME_TEST_FILE + "BeanInfo";
        JamUtilities.waitEventQueueEmpty(1500);                
        explorer.pushPopupMenu("Delete", myObject);
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        JamUtilities.waitEventQueueEmpty(1500);        
        
        explorer.pushPopupMenu("Delete", myObjectBeanInfo);
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        JamUtilities.waitEventQueueEmpty(1500);                
        new JelloSaveCancelDialog("Question").cancel();
        
        if (unmount) {
            explorer.pushPopupMenu("Unmount Filesystem", sampleDir);
            
        }
        JamUtilities.waitEventQueueEmpty(1500);
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
    
    
    public void testGenerateNewBeanInfo() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        och.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE + "BeanInfo");
        ref(editor.getText());
        compareReferenceFiles();
        
    }
    
    public void testIncludeExclude() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        
        JamTree jamTree = och.getJamTree();
        JamTreeNode jamTreeNode = new JamTreeNode("BeanInfo, Event Sources");
        jamTree.select(jamTreeNode);
        for (int i=0; i<jamTree.getModel().getChildCount(jamTree.getObjectForPath(jamTreeNode)); i++ ) {
            jamTree.select(new JamTreeNode("BeanInfo, Event Sources, " + jamTree.getModel().getChild( jamTree.getObjectForPath(jamTreeNode) , i).toString()) );
            JamUtilities.waitEventQueueEmpty(1000);
            och.getJamTabbedPane().selectPage("Properties");
            JelloPropertiesSheet sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
            sheet.edit("Include in BeanInfo");
            sheet.setFalse();
        }
        och.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE + "BeanInfo");
        ref(editor.getText());
        compareReferenceFiles();
    }
    
    public void testRegenerateBeanInfo() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        och.ok();
        Editor editor = new Editor(NAME_TEST_FILE + "BeanInfo");
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoInitial.ref")));
            out.print(editor.getText());
            out.close();            
        } catch(java.io.IOException exc) {
            exc.printStackTrace();
        }               
        compareReferenceFiles("testRegenerateBeanInfoInitial.ref", "testRegenerateBeanInfoInitial.pass", "testRegenerateBeanInfoInitial.diff");

        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        JamUtilities.waitEventQueueEmpty(1000);
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+NAME_NON_INDEX_PROPERTY
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 
        JamUtilities.waitEventQueueEmpty(1000);        
        explorer.pushPopupMenuNoBlock("Delete", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        +explorer.delim+NAME_INDEX_PROPERTY
        );
        new JelloYesNoDialog("Confirm Object Deletion").yes();
        new JelloYesNoDialog("Question").yes(); 

        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        och.ok();

        try {
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoModified.ref")));
            out.print(editor.getText());
            out.close();            
        } catch(java.io.IOException exc) {
            exc.printStackTrace();
        }               
        compareReferenceFiles("testRegenerateBeanInfoModified.ref", "testRegenerateBeanInfoModified.pass", "testRegenerateBeanInfoModified.diff");
        
    }

    public void testCheckNodes() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        
        JamTree jamTree = och.getJamTree();       
        jamTree.select(new JamTreeNode("BeanInfo, Bean, TestFile"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Properties, nonIndexProperty"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Properties, indexProperty"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, containerListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, focusListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, vetoableChangeListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, propertyChangeListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, actionListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        jamTree.select(new JamTreeNode("BeanInfo, Methods"));
        JamUtilities.waitEventQueueEmpty(1000);      
        och.ok();        
    }    
    
    public void testBeanInfoNode() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        JamTree jamTree = och.getJamTree();       
        jamTree.select(new JamTreeNode("BeanInfo"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        JelloPropertiesSheet sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Default Property Index");
        sheet.setText("123");
        sheet.edit("Default Event Index");
        sheet.setText("456");        
        och.ok();
        Editor editor = new Editor(NAME_TEST_FILE + "BeanInfo");
        ref(editor.getText());
        compareReferenceFiles();
    }    

    public void testPropertiesNode() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        
        JamTree jamTree = och.getJamTree();       
        jamTree.select(new JamTreeNode("BeanInfo, Bean"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        JelloPropertiesSheet sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Get From Introspection");
        sheet.setTrue();

        jamTree.select(new JamTreeNode("BeanInfo, Properties"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Get From Introspection");
        sheet.setTrue();

        jamTree.select(new JamTreeNode("BeanInfo, Event Sources"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Get From Introspection");
        sheet.setTrue();

        jamTree.select(new JamTreeNode("BeanInfo, Methods"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Get From Introspection");
        sheet.setTrue();        
        JamUtilities.waitEventQueueEmpty(1000);      
        och.ok();
        Editor editor = new Editor(NAME_TEST_FILE + "BeanInfo");
        ref(editor.getText());
        compareReferenceFiles();
    }    
    
    public void testNodesDescription() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");
        
        JamTree jamTree = och.getJamTree();       

        jamTree.select(new JamTreeNode("BeanInfo, Bean, TestFile"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        JelloPropertiesSheet sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Name");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Expert");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Hidden");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Preferred");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Display Name Code");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Short Description Code");
        JamUtilities.waitEventQueueEmpty(750);
        och.getJamTabbedPane().selectPage("Expert");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Expert"));
        sheet.edit("Customizer Name Code");
        JamUtilities.waitEventQueueEmpty(750);        
        
        jamTree.select(new JamTreeNode("BeanInfo, Properties, indexProperty"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Name");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Expert");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Hidden");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Preferred");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Display Name Code");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Short Description Code");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Include in BeanInfo");                       
        JamUtilities.waitEventQueueEmpty(750);
        och.getJamTabbedPane().selectPage("Expert");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Expert"));
        sheet.edit("Bound");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Constrained");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Mode");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Property Editor Class");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Non-Indexed Getter");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Non-Indexed Setter");
        JamUtilities.waitEventQueueEmpty(750);

        jamTree.select(new JamTreeNode("BeanInfo, Event Sources, focusListener"));
        JamUtilities.waitEventQueueEmpty(1000);
        och.getJamTabbedPane().selectPage("Properties");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Properties"));
        sheet.edit("Name");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Expert");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Hidden");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Preferred");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Display Name Code");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Short Description Code");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("Include in BeanInfo");                       
        JamUtilities.waitEventQueueEmpty(750);
        och.getJamTabbedPane().selectPage("Expert");
        sheet = new JelloPropertiesSheet(och.getJamTabbedPane().getPage("Expert"));
        sheet.edit("Unicast");
        JamUtilities.waitEventQueueEmpty(750);
        sheet.edit("In Default Event Set");
        JamUtilities.waitEventQueueEmpty(750);        
        och.ok();
        
        
    }    

    public void testCheckBeanInfoCompilability() {
        createContent();
        Explorer explorer = new Explorer();
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("BeanInfo Editor...", sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        JelloOKCancelHelpDialog och = new JelloOKCancelHelpDialog("BeanInfo Editor");        
        och.ok();
        JamUtilities.waitEventQueueEmpty(1000);                
        explorer.pushPopupMenuNoBlock("Compile", sampleDir
        +explorer.delim+NAME_TEST_FILE+"BeanInfo"
        );                
        JamStatusText jamStatusText = new JamStatusText("Finished TestFileBeanInfo.");
        jamStatusText.resetWaitMultiplier();            
        jamStatusText.verify();            

        
    }    
        
    public void testUnmount() {
        System.out.println("testUnmount");
        unmount = true;
    }
    
}
