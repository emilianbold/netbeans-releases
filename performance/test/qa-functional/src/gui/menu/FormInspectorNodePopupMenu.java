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
package gui.menu;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 * Test of popup menu on node in Component Inspector.
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class FormInspectorNodePopupMenu extends ValidatePopupMenuOnNodes {
    
    
    /** Creates a new instance of FormInspectorNodePopupMenu */
    public FormInspectorNodePopupMenu(String testName) {
        super(testName);
        WAIT_AFTER_PREPARE = 1000;
    }
    
    /** Creates a new instance of FormInspectorNodePopupMenu */
    public FormInspectorNodePopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_PREPARE = 1000;
    }
    
    public void testFormNodePopupMenuInspector(){
        doMeasurement();
    }
   
     public void initialize(){
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"),"Source Packages|org.netbeans.test.performance" + '|' +  "JFrame20kB.java"));
     }
    
    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }    
    public void prepare(){
        String path = "[JFrame]"; // NOI18N
        dataObjectNode = new Node(new ComponentInspectorOperator().treeComponents(), path);
        super.prepare();
    }
}
