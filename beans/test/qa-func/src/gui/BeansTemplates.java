package gui;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.modules.form.FormEditorOperator;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;


import org.netbeans.junit.NbTestSuite;
import org.openide.actions.SaveAllAction;


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
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        mountedSampleDir = Utilities.mountSampledir();
    }
    
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_JAVA_BEAN + ".java");
        Utilities.delete(NAME_BEAN_INFO + ".java");
        Utilities.delete(NAME_BEAN_INFO_NO_ICON + ".java");
        Utilities.delete(NAME_CUSTOMIZER + ".java");
        Utilities.delete(NAME_PROPERTY_EDITOR + ".java");
    }
    
    public void testJavaBean() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        ctso.setComparator(comparator);
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_JAVA_BEAN);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        new EventTool().waitNoEvent(500);
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = new EditorOperator(ewo, NAME_JAVA_BEAN);
        new EventTool().waitNoEvent(500);
        eo.select(9,12);
        new EventTool().waitNoEvent(500);
        new DeleteAction().perform();
        new DeleteAction().perform();
        eo.select(1,5);
        new EventTool().waitNoEvent(500);
        new DeleteAction().perform();
        ref(eo.getText());
        compareReferenceFiles();
    }
    
    public void testBeanInfo() {
        ExplorerOperator explorerOperator = new ExplorerOperator();
        explorerOperator.selectPageFilesystems();
        Node repositoryRootNode = new ExplorerOperator().repositoryTab().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfo.java");
        new EventTool().waitNoEvent(1000);
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
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfoNoIcon.java");
        new EventTool().waitNoEvent(1000);
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
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Customizer.java");
        new EventTool().waitNoEvent(1000);
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
        javaNode.performPopupActionNoBlock(Bundle.getString("org.openide.actions.Bundle", "Edit"));
        new EventTool().waitNoEvent(100);
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
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/PropertyEditor.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_PROPERTY_EDITOR);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        new EventTool().waitNoEvent(500);
        
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.getEditor();
        EditorOperator eo = new EditorOperator(ewo, NAME_PROPERTY_EDITOR);
        eo.select(1,5);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
//        new EventTool().waitNoEvent(5000);
        
        eo.select(4,7);
        new EventTool().waitNoEvent(500);
        new DeleteAction().performAPI(eo);
        ref(eo.getText());
        compareReferenceFiles();
    }
    
}
