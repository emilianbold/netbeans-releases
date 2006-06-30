/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.FolderNode;
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
    
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
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
        new PropertiesAction().perform();
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
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
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
        new EventTool().waitNoEvent(10000);
        
        writeResult(NAME_JAVA_BEAN);
        compareReferenceFiles();
    }
    
    private void writeResult(String name) {
        new EventTool().waitNoEvent(1000);
        new EditorOperator(name);
        ref(Utilities.unify(Utilities.getAsString(name+".java")));
        new EventTool().waitNoEvent(500);
    }
    
    public void testBeanInfo() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
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
        
        writeResult(NAME_BEAN_INFO);
        compareReferenceFiles();
    }
    
    public void testBeanInfoNoIcon() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
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
        
        writeResult(NAME_BEAN_INFO_NO_ICON);
        compareReferenceFiles();
    }
    
    public void testCustomizer() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
//        mainWindowOper.switchToGUIEditingWorkspace();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
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
        
        writeResult(NAME_CUSTOMIZER);
        compareReferenceFiles();
    }
    
    public void testPropertyEditor() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
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

        writeResult(NAME_PROPERTY_EDITOR);
        compareReferenceFiles();
    }
    
}
