package gui;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

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
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class CreateNewNonIndexedProperty extends JellyTestCase {

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
        System.out.println("########  "+getName()+"  #######");
        Utilities.mountSampledir();
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption("Editing"+"|"+"Beans Property");
        PropertySheetTabOperator propertySheetTabOperator = new PropertySheetTabOperator(optionsOperator);
        new ComboBoxProperty(propertySheetTabOperator, "Style of Declared Variable").setValue("this.property_Value");

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

    
    /** testName method */
    public void testName() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_WRONG);        

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("String");
        
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(3000);

        new NbDialogOperator("Error").ok();
                              
        jTextFieldOperator.clearText();
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);
        
        jComboBoxOperator.setSelectedItem("String");
                       
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    /** testType method */ 
    public void testType() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);
        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText(TYPE_WRONG);
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(3000);
        new NbDialogOperator("Error").ok();

        jTextFieldOperator.clearText();
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);
        jComboBoxOperator.clearText();
        jComboBoxOperator.setSelectedItem("Double");
                       
        nbDialogOperator.btOK().pushNoBlock();
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
    
    /** testMode method */
    public void testMode() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("first");        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("int");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read Only");
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(1000);
        
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);

        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("second");        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("double");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Write Only");
        nbDialogOperator.btOK().pushNoBlock();

        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);

        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText("third");        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("long");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        nbDialogOperator.btOK().pushNoBlock();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
    
    /** testBound method */    
    public void testBound() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    /** testConstrained method */
    public void testConstrained() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(2000);

        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    /** testGenerateField method */
    public void testGenerateField() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();

        new EventTool().waitNoEvent(2000);

        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
    
    /** testGenerateReturnStatement method */
    public void testGenerateReturnStatement() { 
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(2000);

        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    /** testGenerateSetStatement method */
    public void testGenerateSetStatement() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(2000);

        nbDialogOperator.btOK().pushNoBlock();

        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

    /** testGeneratePropertyChangeSupport method */
    public void testGeneratePropertyChangeSupport() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);       
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);        
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("MyType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem("Read / Write");
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(2000);

        nbDialogOperator.btOK().pushNoBlock();
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

}
