package gui;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ChooseTemplateStepOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jellytools.TargetLocationStepOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.MountLocalAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.modules.form.FormEditorOperator;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import java.io.File;
import java.io.IOException;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class NewMulticastEventSource extends JellyTestCase {
    
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
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        Utilities.mountSampledir();
        
        FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
        FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
        
        try {
            DataObject.find(testFile).copy(DataFolder.findFolder(destination));
        } catch (IOException e) {
            fail(e);
        }
    }
    
    /** tearDown method */
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_TEST_FILE + ".java");
    }

     public void testGenerateEmpty() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_emptyRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    
     public void testGenerateArrayListImplementation() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     public void testGenerateEventListenerListImplementation() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
     
     public void testGenerateEventFiringMethods() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();        
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     public void testPassEventAsParameter() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();        
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(3000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

}