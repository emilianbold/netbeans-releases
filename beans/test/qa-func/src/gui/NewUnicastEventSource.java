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
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.modules.form.FormEditorOperator;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import java.io.File;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;


public class NewUnicastEventSource extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
            
    private static final String sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";

    
    private static boolean mount   = true;
    private static boolean unmount = false;
    
        
    /** Need to be defined because of JUnit */
    public NewUnicastEventSource(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NewUnicastEventSource("testGenerateEmpty"));        
        suite.addTest(new NewUnicastEventSource("testGenerateImplementation"));        
        suite.addTest(new NewUnicastEventSource("testGenerateEventFiringMethods"));        
        suite.addTest(new NewUnicastEventSource("testPassEventAsParameter"));        
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
        mountSampledir();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String template = "Java Classes" + "|" + "Class";
        ctso.selectTemplate(template);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        new EventTool().waitNoEvent(500);
        tlso.setName(NAME_TEST_FILE);
        new EventTool().waitNoEvent(500);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();                
    }
    
    /** tearDown method */
    public void tearDown() {
        ExplorerOperator explorer = new ExplorerOperator();
        explorer.selectPageProject();
        explorer.selectPageRuntime();
        explorer.selectPageFilesystems();
        Node repositoryRootNode = explorer.repositoryTab().getRootNode();
        try {
            new SaveAllAction().perform();
        } catch (Exception e) {
            // OK - not enabled, nothing to save
        }       
        new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE).select();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE); // NOI18N
        javaNode.delete();
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        FilesystemNode fsNode = new FilesystemNode(repositoryRootNode, sampleDir);
        fsNode.unmount();
    }

    /** testGenerateEmpty method */
    public void testGenerateEmpty() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_emptyRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(1000);

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }
    
     /** testGenerateImplementation method */
     public void testGenerateImplementation() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();        

        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(1000);

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     /** testGenerateEventFiringMethods method */
     public void testGenerateEventFiringMethods() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();        

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(1000);

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     /** testPassEventAsParameter method */     
     public void testPassEventAsParameter() {
//
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");

        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();        

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        new EventTool().waitNoEvent(2000);
                               
        nbDialogOperator.btOK().pushNoBlock();

        new EventTool().waitNoEvent(1000);

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE);
        eo.select(1,10);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();                               
//                                       
    }

     /** testUnicastEventSource
     public void testUnicastEventSource() {
        Explorer explorer = new Explorer();        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();
        explorer.pushPopupMenuNoBlock("Add"+"|"+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"), sampleDir
        +explorer.delim+NAME_TEST_FILE+explorer.delim+"class "+NAME_TEST_FILE
        +explorer.delim+JelloBundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")
        );
        
        JelloOKCancelHelpDialog okCancelHelpDialog = new JelloOKCancelHelpDialog(JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES"));
               
        JamComboBox jComboBox = new JamComboBox(okCancelHelpDialog, 0);
        jComboBox.setSelectedItem(0);
        
        new JamRadioButton(okCancelHelpDialog,JelloBundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton")).doClick();
                        
        JamCheckBox jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBox.setSelected(true);
        
        jCheckBox = new JamCheckBox(okCancelHelpDialog, JelloBundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBox.setSelected(true);
        
        okCancelHelpDialog.ok();
        
        Editor editor = new Editor(NAME_TEST_FILE);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles(); 
    }
    
     /** Mounts <userdir>/sampledir through API
     * @return absolute path of mounted dir
     */
    private boolean mountSampledir() {
        new EventTool().waitNoEvent(1000);
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        String mountPoint = userdir+File.separator+"sampledir"; // NOI18N
        mountPoint = mountPoint.replace('\\', '/');
        FileSystem fs = Repository.getDefault().findFileSystem(mountPoint);
        if (fs == null) {            
            // invoke "File|Mount Filesystem" from main menu
            new MountLocalAction().performMenu();
            // wait for "New Wizard"
            NewWizardOperator newWizardOper = new NewWizardOperator();
            // select "Local Directory"
            JTreeOperator tree = new JTreeOperator(newWizardOper);
            String localDirLabel = Bundle.getString("org.netbeans.core.Bundle", "Templates/Mount/org-netbeans-core-ExLocalFileSystem.settings"); // NOI18N
            new Node(tree, localDirLabel).select();
            newWizardOper.next();
            // select sampledir in file chooser
            File file = new File(mountPoint);
            new JFileChooserOperator().setSelectedFile(file);
            // finish wizard
            newWizardOper.finish();
        }       
        return true;
    }
  
}