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
 *      ProjectsTabOperator pto = new ProjectsTabOperator();
 *      // or when Projects pane is not already opened
 *      //ProjectsTabOperator pto = ProjectsTabOperator.invoke();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = pto.tree();
 *      // work with nodes
 *      ProjectRootNode prn = pto.getProjectRootNode("SampleProject").select();
 *      Node node = new Node(prn, "subnode|sub subnode");
 * </pre> 
 *
 * @see ProjectViewAction
 * @see ProjectRootNode
 */
public class ProjectsTabOperator extends TopComponentOperator {
    
    static final String PROJECT_CAPTION = Bundle.getStringTrimmed(
                                            "org.netbeans.modules.project.ui.Bundle", 
                                            "LBL_projectTabLogical_tc");
    private static final ProjectViewAction viewAction = new ProjectViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Projects TopComponent within all IDE. */
    public ProjectsTabOperator() {
        super(waitTopComponent(null, PROJECT_CAPTION, 0, new ProjectsTabSubchooser()));
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

    /** Gets ProjectRootNode
     * @param projectName display name of project
     * @return ProjectsRootNode */    
    public ProjectRootNode getProjectRootNode(String projectName) {
        return new ProjectRootNode(tree(), projectName);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.projects.ui.ProjectTab
     * Used in constructor.
     */
    private static final class ProjectsTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectTab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.projects.ui.ProjectTab";
        }
    }
}
