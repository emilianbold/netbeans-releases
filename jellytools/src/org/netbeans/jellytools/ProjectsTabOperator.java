/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.ProjectViewAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Projects TopComponent.<p>
 * Functionality related to Projects tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 *
 * Example:<p>
 * <pre>
 *      ProjectsTabOperator pto = ProjectsTabOperator.invoke();
 *      // or when Runtime pane is already opened
 *      //ProjectsTabOperator pto = new ProjectsTabOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = pto.tree();
 *      // work with nodes
 *      pto.getRootNode().select();
 *      Node node = new Node(pto.getRootNode(), "subnode|sub subnode");
 * </pre> 
 *
 * @see ProjectViewAction
 * @see ProjectRootNode
 */
public class ProjectsTabOperator extends TopComponentOperator {
    
    static final String PROJECT_CAPTION = Bundle.getStringTrimmed("org.netbeans.modules.projects.Bundle", "CTL_ProjectDesktop_name");
    private static final ProjectViewAction viewAction = new ProjectViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Projects TopComponent through all IDE. */    
    public ProjectsTabOperator() {
        this(null);
    }
    
    /** Search for Projects TopComponent inside given Container
     * @param contOper parent Container 
     * @deprecated Use {@link #ProjectsTabOperator()} instead.
     */
    public ProjectsTabOperator(ContainerOperator contOper) {
        super(waitTopComponent(contOper, PROJECT_CAPTION, 0, new ProjectsTabSubchooser()));
        if(contOper != null) {
            copyEnvironment(contOper);
        }
    }

    /** invokes Projects and returns new instance of ProjectsTabOperator
     * @return new instance of ProjectsTabOperator */    
    public static ProjectsTabOperator invoke() {
        viewAction.perform();
        return new ProjectsTabOperator();
    }
    
    /** Getter for Projects JTreeOperator
     * @return JTreeOperator of Projects tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** getter for ProjectsRootNode
     * @return ProjectsRootNode */    
    public ProjectRootNode getRootNode() {
        return new ProjectRootNode(tree());
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.projects.CurrentProjectNode$ProjectsTab
     * Used in constructor.
     */
    private static final class ProjectsTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectsTab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.projects.CurrentProjectNode$ProjectsTab";
        }
    }
}
