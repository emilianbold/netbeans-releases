package gui;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;



import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;



public class BeanInfoGeneration extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final int DELAY = 2000;
    
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
        TestRunner.run(suite());
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
        Utilities.delete(NAME_TEST_FILE + "BeanInfo.java");
        
        /*
        FilesystemNode fsNode = new FilesystemNode(repositoryRootNode, sampleDir);
        fsNode.unmount();
         */
    }
    
    private void createContent() {
        // Start - NonIndexProperty
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
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.btOK().pushNoBlock();
        // End - NonIndexProperty
        // Start - IndexProperty
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.btOK().pushNoBlock();
        // End - IndexProperty
        // Start - UnicastEventSource
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");
        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        //new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.btOK().pushNoBlock();
        // End - UnicastEventSource
        // Start - MulticastEventSourceArrayListImpl
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ItemListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        //new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.btOK().pushNoBlock();
        // End - MulticastEventSourceArrayListImpl
        // Start - MulticastEventSourceEventListenerListImpl
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock("Add"+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.FocusListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(100);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.btOK().pushNoBlock();
        
    }
    
    public void testGenerateNewBeanInfo() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1500);
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
        ref(eo.getText());
        compareReferenceFiles();
        
    }
    
    
    public void testIncludeExclude() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        Node node = new Node(jTreeOperator, "Event Sources");
        node.select();
        for (int i=0; i<(new Node(jTreeOperator, "Event Sources").getChildren().length); i++ ) {
            new Node(node,i).select();
            PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
            PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
            new ComboBoxProperty(propertySheetTabOperator, "Include in BeanInfo").setValue("False");
            //new EventTool().waitNoEvent(1000);
        }
        
        new Node(jTreeOperator, "Bean").select();
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("False");
        
        nbDialogOperator.ok();
        
        //new EventTool().waitNoEvent(1500);
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Open");
        
        new EventTool().waitNoEvent(500);
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testRegenerateBeanInfo() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        
        new Node(jTreeOperator, "Bean").select();
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("False");
        
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(750);
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.open();
        new EventTool().waitNoEvent(100);
        try {
            EditorWindowOperator ewo = new EditorWindowOperator();
            EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoInitial.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testRegenerateBeanInfoInitial.ref", "testRegenerateBeanInfoInitial.pass", "testRegenerateBeanInfoInitial.diff");
        Thread thread = new Thread( new java.lang.Runnable() {
            public void run() {
                System.out.println("T H R E A D");
                //new EventTool().waitNoEvent(1000);
                ExplorerOperator explorerOperator = new ExplorerOperator();
                explorerOperator.selectPageFilesystems();
                Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
                Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_NON_INDEX_PROPERTY);
                patternsNode.select();
                patternsNode.performPopupActionNoBlock("Delete");
            }
        });
        thread.start();
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        //new EventTool().waitNoEvent(1500);
        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        //new EventTool().waitNoEvent(2500);
        //
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        dialogTitle = "BeanInfo Editor";
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        new Node(jTreeOperator, "Bean").select();
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("False");
        
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1500);
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Open");
        try {
            EditorWindowOperator ewo = new EditorWindowOperator();
            EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoModified.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testRegenerateBeanInfoModified.ref", "testRegenerateBeanInfoModified.pass", "testRegenerateBeanInfoModified.diff");
    }
    
    public void testCheckNodes() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        System.err.println(nbDialogOperator);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        System.err.println(jTreeOperator);
        Node node = new Node(jTreeOperator, "Bean|TestFile");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Properties|nonIndexProperty");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Properties|indexProperty");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Event Sources|itemListener");
        node.select();
        new EventTool().waitNoEvent(100);
        node = new Node(jTreeOperator, "Event Sources|focusListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Event Sources|vetoableChangeListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Event Sources|propertyChangeListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Event Sources|actionListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, "Methods");
        node.select();
        //new EventTool().waitNoEvent(1000);
        nbDialogOperator.close();
        
    }
    
    public void testBeanInfoNode() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        //Node node = new Node(jTreeOperator, getTreePathHack(jTreeOperator,new TreePath(new Object[] {"BeanInfo"})));
        //node.select();
        jTreeOperator.setSelectionRow(0);
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new TextFieldProperty(propertySheetTabOperator, "Default Property Index").setValue("123");
        //new EventTool().waitNoEvent(1000);
        new TextFieldProperty(propertySheetTabOperator, "Default Event Index").setValue("456");
        //new EventTool().waitNoEvent(1000);
        
        new Node(jTreeOperator, "Bean").select();
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("False");
        nbDialogOperator.ok();
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Open");
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testPropertiesNode() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        new Node(jTreeOperator, "Bean").select();
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("True");
        
        new Node(jTreeOperator, "Properties").select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("True");
        
        new Node(jTreeOperator, "Event Sources").select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("True");
        
        new Node(jTreeOperator, "Methods").select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new ComboBoxProperty(propertySheetTabOperator, "Get From Introspection").setValue("True");
        //new EventTool().waitNoEvent(1000);
        
        nbDialogOperator.ok();
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Open");
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_TEST_FILE+"BeanInfo");
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    
    public void testNodesDescription() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        new Node(jTreeOperator, "Bean|TestFile").select();
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new TextFieldProperty(propertySheetTabOperator, "Name").getValue();
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Expert").setValue(new ComboBoxProperty(propertySheetTabOperator, "Expert").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Hidden").setValue(new ComboBoxProperty(propertySheetTabOperator, "Hidden").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Preferred").setValue(new ComboBoxProperty(propertySheetTabOperator, "Preferred").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Display Name Code").setValue(new TextFieldProperty(propertySheetTabOperator, "Display Name Code").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Short Description Code").setValue(new TextFieldProperty(propertySheetTabOperator, "Short Description Code").getValue());
        //new EventTool().waitNoEvent(750);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Expert");
        new TextFieldProperty(propertySheetTabOperator, "Customizer Name Code").getValue();
        //new EventTool().waitNoEvent(750);
        
        jTreeOperator.setComparator(new DefaultStringComparator(true, true));
        new Node(jTreeOperator, "Properties|indexProperty").select();
        
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new TextFieldProperty(propertySheetTabOperator, "Name").getValue();
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Expert").setValue(new ComboBoxProperty(propertySheetTabOperator, "Expert").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Hidden").setValue(new ComboBoxProperty(propertySheetTabOperator, "Hidden").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Preferred").setValue(new ComboBoxProperty(propertySheetTabOperator, "Preferred").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Short Description Code").setValue(new TextFieldProperty(propertySheetTabOperator, "Short Description Code").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Include in BeanInfo").setValue(new ComboBoxProperty(propertySheetTabOperator, "Include in BeanInfo").getValue());
        //new EventTool().waitNoEvent(750);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Expert");
        new ComboBoxProperty(propertySheetTabOperator, "Bound").setValue(new ComboBoxProperty(propertySheetTabOperator, "Bound").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Constrained").setValue(new ComboBoxProperty(propertySheetTabOperator, "Constrained").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Mode").setValue(new ComboBoxProperty(propertySheetTabOperator, "Mode").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Property Editor Class").setValue(new TextFieldProperty(propertySheetTabOperator, "Property Editor Class").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Non-Indexed Getter").setValue(new ComboBoxProperty(propertySheetTabOperator, "Non-Indexed Getter").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Non-Indexed Setter").setValue(new ComboBoxProperty(propertySheetTabOperator, "Non-Indexed Setter").getValue());
        //new EventTool().waitNoEvent(750);
        
        new Node(jTreeOperator, "Event Sources|focusListener").select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Properties");
        new TextFieldProperty(propertySheetTabOperator, "Name").getValue();
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Expert").setValue(new ComboBoxProperty(propertySheetTabOperator, "Expert").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Hidden").setValue(new ComboBoxProperty(propertySheetTabOperator, "Hidden").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Preferred").setValue(new ComboBoxProperty(propertySheetTabOperator, "Preferred").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Display Name Code").setValue(new TextFieldProperty(propertySheetTabOperator, "Display Name Code").getValue());
        //new EventTool().waitNoEvent(750);
        new TextFieldProperty(propertySheetTabOperator, "Short Description Code").setValue(new TextFieldProperty(propertySheetTabOperator, "Short Description Code").getValue());
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "Include in BeanInfo").setValue(new ComboBoxProperty(propertySheetTabOperator, "Include in BeanInfo").getValue());
        //new EventTool().waitNoEvent(750);
        propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Expert");
        new ComboBoxProperty(propertySheetTabOperator, "Unicast").getValue();
        //new EventTool().waitNoEvent(750);
        new ComboBoxProperty(propertySheetTabOperator, "In Default Event Set").setValue(new ComboBoxProperty(propertySheetTabOperator, "In Default Event Set").getValue());
        //new EventTool().waitNoEvent(750);
        nbDialogOperator.ok();
        
        
    }
    public void testCheckBeanInfoCompilability() {
        createContent();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = "BeanInfo Editor";
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1000);
        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        repositoryRootNode = explorerOperator.repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Compile");
        
        MainWindowOperator.getDefault().waitStatusText("Finished TestFileBeanInfo");
        assertNotNull("Generated BeanInfo is not compilable", Repository.getDefault().findResource("TestFileBeanInfo.class"));
    }
    
}
