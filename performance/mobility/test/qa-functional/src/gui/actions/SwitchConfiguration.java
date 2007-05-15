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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import gui.MPUtilities;
import gui.window.MIDletEditorOperator;
import java.io.PrintStream;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class SwitchConfiguration extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node openNode;
    private ProjectRootNode projectNode;
    private String targetProject, midletName;
    private WizardOperator propertiesWindow;
    private MIDletEditorOperator editor;
            
    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */    
    public SwitchConfiguration(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */    
    public SwitchConfiguration(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    public void initialize() {
        log(":: initialize");
        targetProject = "MobileApplicationSwitchConfiguration";        
        midletName = "Midlet.java";        
        EditorOperator.closeDiscardAll();        
    } 
    
    public void prepare() {
        log(":: prepare");
        String documentPath = MPUtilities.SOURCE_PACKAGES+"|"+"switchit"+"|"+midletName;
        projectNode = new ProjectsTabOperator().getProjectRootNode(targetProject);
        openNode = new Node(projectNode, documentPath);
        
        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        
        new OpenAction().perform(openNode);
        editor = MIDletEditorOperator.findMIDletEditorOperator(midletName);
        
        projectNode.properties();
        propertiesWindow = new WizardOperator(targetProject);
        
    }

    public ComponentOperator open() {
        log(":: open");
        JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow);
        combo.selectItem(1); // NotDefaultConfiguration
        
        propertiesWindow.ok();
        return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }
    
    public void close() {
        log(":: close");
        projectNode.properties();
        propertiesWindow = new WizardOperator(targetProject);
        
        // switch back to default config
        JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow,0);
        combo.selectItem(0); //DefaultConfiguration
        propertiesWindow.ok();
    }
    
    public void shutdown() {
        log("::shutdown");
        editor.close();
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new SwitchConfiguration("measureTime"));
    }
    
}
