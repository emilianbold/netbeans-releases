/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestProjectsUtils.java
 *
 * Created on July 22, 2004, 11:48 AM
 */

package projects;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;

import org.netbeans.jellytools.actions.BuildProjectAction;

import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 */
public class TestProjectUtils {
    
    /** Creates a new instance of TestProjectsUtils */
    public TestProjectUtils() {
    }
    
    /**
     * Verifies that project exists in Projects tab
     * @param projName Name of the project
     */
    public static void verifyProjectExists(String projName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        JTreeOperator tree = pto.tree();
        ProjectRootNode prn = pto.getProjectRootNode(projName);
        prn.select();
        prn.expand();
    }
    
    /**
     * Verifies that main class was opened in editor
     * @param mainClass Name of the main class (as shown in editor tab)
     */
    public static void verifyMainClassInEditor(String mainClass) {
        EditorOperator eo = new EditorOperator(mainClass);
    }
    
    /**
     * Verifies that project can be built by action 'Build Project' on project root node
     * @param projName Name of the project
     */
    public static void verifyProjectBuilds(String projName) {
        
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(projName);
        
        BuildProjectAction buildProjectAction = new BuildProjectAction();
        buildProjectAction.perform(prn);
        
        //MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        //mainWindow.waitStatusText("Finished building");
        
        // Output should be checked for BUILD SUCCESSFUL
        OutputTabOperator outputOper = new OutputTabOperator(projName);
        outputOper.waitText("BUILD SUCCESSFUL");
        
    }
    
    public static void verifyProjectRuns() {
        
    }
    
    public static void addLibrary(String name, String[] cpEntries, String[] srcEntries, String[] jdocEntries) {
        
        new ActionNoBlock("Tools|Library Manager", null).performMenu();
        NbDialogOperator libManOper = new NbDialogOperator("Library Manager");
        JButtonOperator newLibButtonOper = new JButtonOperator(libManOper, "New Library");
        newLibButtonOper.push();
        
        NbDialogOperator newLibOper = new NbDialogOperator("New Library");
        JTextFieldOperator jtfo = new JTextFieldOperator(newLibOper, 0);
        jtfo.clearText();
        jtfo.setText(name);
        newLibOper.ok();
        
        // here I should select the created library in the tree?
        
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(libManOper);
        
        jtpo.selectPage("Classpath"); // should be already selected, but just for sure
        if (cpEntries != null || cpEntries.length > 0) {
            JButtonOperator addJarButtonOper = new JButtonOperator(jtpo, "Add JAR/Folder");
            for (int i = 0; i < cpEntries.length; i++) {
                addJarButtonOper.push();
                JFileChooserOperator jfco = new JFileChooserOperator();
                jfco.setSelectedFile(new java.io.File(cpEntries[i]));
                JButtonOperator confirmButton = new JButtonOperator(jfco, "Add JAR/Folder");
                confirmButton.push();
            }
        } else {
            // missing cp entries must be handled, e.g. by throwing Exception
        }
        
        if (srcEntries != null || srcEntries.length > 0) {
            jtpo.selectPage("Sources");
            JButtonOperator addJarButtonOper = new JButtonOperator(jtpo, "Add JAR/Folder");
            for (int i = 0; i < srcEntries.length; i++) {
                addJarButtonOper.push();
                JFileChooserOperator jfco = new JFileChooserOperator();
                jfco.setSelectedFile(new java.io.File(srcEntries[i]));
                JButtonOperator confirmButton = new JButtonOperator(jfco, "Add JAR/Folder");
                confirmButton.push();
            }
        }
        
        if (jdocEntries != null || jdocEntries.length > 0) {
            jtpo.selectPage("Javadoc");
            JButtonOperator addZipButtonOper = new JButtonOperator(jtpo, "Add ZIP/Folder");
            for (int i = 0; i < jdocEntries.length; i++) {
                addZipButtonOper.push();
                JFileChooserOperator jfco = new JFileChooserOperator();
                jfco.setSelectedFile(new java.io.File(jdocEntries[i]));
                JButtonOperator confirmButton = new JButtonOperator(jfco, "Add ZIP/Folder");
                confirmButton.push();
            }
        }
        
        libManOper.ok();
        
    }
    
    public static void addPlatform(String platName, String folderPath) {
        
        new ActionNoBlock("Tools|Java Platform Manager", null).performMenu();
        NbDialogOperator platManOper = new NbDialogOperator("Java Platform Manager");
        JButtonOperator addPlatformButtonOper = new JButtonOperator(platManOper, "Add Platform");
        addPlatformButtonOper.push();
        
        JFileChooserOperator jfco = new JFileChooserOperator();
        jfco.setSelectedFile(new java.io.File(folderPath));
        
        NbDialogOperator nbdo = new NbDialogOperator("Add Java Platform");
        
        // wait for button being enabled
        JButtonOperator nextButton = new JButtonOperator(nbdo, "Next");
        try { nextButton.waitComponentEnabled(); } 
            catch (InterruptedException ie) {}
        nextButton.push();
        
        // wait for platform to be scanned        
        JButtonOperator finishButton = new JButtonOperator(nbdo, "Finish");
        try { finishButton.waitComponentEnabled(); } 
            catch (InterruptedException ie) {}
        // set name of the platform
        JTextFieldOperator jtfo = new JTextFieldOperator(nbdo, 0);
        jtfo.clearText();
        jtfo.setText(platName);
        
        finishButton.push();
        
        platManOper.close();
        
    }
    
}
