package gui;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ChooseTemplateStepOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jellytools.TargetLocationStepOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.MountLocalAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.modules.form.FormEditorOperator;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
/////////////////////
public class BeansTemplates extends JellyTestCase {
    
    private static final String NAME_JAVA_BEAN          = "MyBean";
    private static final String NAME_BEAN_INFO          = "MyBeanInfo";
    private static final String NAME_BEAN_INFO_NO_ICON  = "MyBeanInfoNoIcon";
    private static final String NAME_CUSTOMIZER         = "MyCustomizer";
    private static final String NAME_PROPERTY_EDITOR    = "MyPropertyEditor";
    
    
    
    private boolean mountedSampleDir = false;
    private String  sampleDir = System.getProperty("netbeans.user")+File.separator+"sampledir";
    
    /** Need to be defined because of JUnit */
    public BeansTemplates(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BeansTemplates("testJavaBean"));
        suite.addTest(new BeansTemplates("testBeanInfo"));
        suite.addTest(new BeansTemplates("testBeanInfoNoIcon"));
        suite.addTest(new BeansTemplates("testCustomizer"));
        suite.addTest(new BeansTemplates("testPropertyEditor"));
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
        System.out.println("########  "+getName()+"  #######");
        mountedSampleDir = mountSampledir();
    }
    
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
        if  (getName().equals(("testJavaBean"))) {
            new Node(repositoryRootNode, sampleDir+"|"+NAME_JAVA_BEAN).select();
            JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_JAVA_BEAN); // NOI18N
            javaNode.delete();
        }   if  (getName().equals(("testBeanInfo"))) {
            new Node(repositoryRootNode, sampleDir+"|"+NAME_BEAN_INFO).select();
            JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_BEAN_INFO); // NOI18N
            javaNode.delete();
        }   if  (getName().equals(("testBeanInfoNoIcon"))) {
            new Node(repositoryRootNode, sampleDir+"|"+NAME_BEAN_INFO_NO_ICON).select();
            JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_BEAN_INFO_NO_ICON); // NOI18N
            javaNode.delete();
        }   if  (getName().equals(("testCustomizer"))) {
            new Node(repositoryRootNode, sampleDir+"|"+NAME_CUSTOMIZER).select();
            JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_CUSTOMIZER); // NOI18N
            javaNode.delete();
        }   if  (getName().equals(("testPropertyEditor"))) {
            new Node(repositoryRootNode, sampleDir+"|"+NAME_PROPERTY_EDITOR).select();
            JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_PROPERTY_EDITOR); // NOI18N
            javaNode.delete();
        }
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        FilesystemNode fsNode = new FilesystemNode(repositoryRootNode, sampleDir);
        fsNode.unmount();
        
    }
    
    public void testJavaBean() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_JAVA_BEAN);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_JAVA_BEAN);
        eo.select(1,5);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
        eo.select(5,8);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testBeanInfo() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfo.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_BEAN_INFO);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_BEAN_INFO);
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testBeanInfoNoIcon() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfoNoIcon.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_BEAN_INFO_NO_ICON);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_BEAN_INFO_NO_ICON);
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testCustomizer() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        mainWindowOper.switchToGUIEditingWorkspace();
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Customizer.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_CUSTOMIZER);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        FormEditorOperator feo = new FormEditorOperator();
        feo.close();

        explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();        
        repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_CUSTOMIZER); // NOI18N
        javaNode.performPopupActionNoBlock("Edit");
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.getEditor();
        EditorOperator eo = new EditorOperator(ewo, NAME_CUSTOMIZER);
        eo.select(1,10);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testPropertyEditor() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/PropertyEditor.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_PROPERTY_EDITOR);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.getEditor();
        EditorOperator eo = new EditorOperator(ewo, NAME_PROPERTY_EDITOR);
        eo.select(1,5);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
//        new EventTool().waitNoEvent(5000);
        
        eo.select(4,8);
        new EventTool().waitNoEvent(3500);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
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
