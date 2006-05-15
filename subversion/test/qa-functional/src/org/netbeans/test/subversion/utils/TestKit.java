/*
 * TestKit.java
 *
 * Created on 10 May 2006, 15:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.utils;

import java.io.File;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author peter
 */
public final class TestKit {
    
    /** Creates a new instance of TestKit */
    public TestKit() {
    }
    
    public static File prepareProject(String category, String project, String project_name) throws Exception {
        //create temporary folder for test
        String folder = "work";
        File file = new File("/tmp", folder); // NOI18N
        file.mkdirs();
        RepositoryMaintenance.deleteFolder(file);
        file.mkdirs();
        //PseudoVersioned project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath()); // NOI18N
        new JTextFieldOperator(npnlso, 0).setText(project_name); // NOI18N
        //new JTextFieldOperator(npnlso, 2).setText(folder); // NOI18N
        new NewProjectWizardOperator().finish();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        return file;
    }
    
    public static String getColor(String nodeHtmlDisplayName) {
        
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }

    public static void removeAllData(String projectName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        rootNode.performPopupActionNoBlock("Delete Project");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed(); 
        //TestKit.deleteRecursively(file);
    }
    
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null)
            return -1;
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                for (int j = 0; j < actual.length; j++) {
                    if (((String) expected[i]).equals((String) actual[j])) {
                        result++;
                        j = actual.length;
                    }
                }
            }
            return result;
        }
        return result; 
    }
    
    public static void createNewElements(String projectName, String packageName, String name) {
        //String pack = "xx";
        
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();
        
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }    
}
