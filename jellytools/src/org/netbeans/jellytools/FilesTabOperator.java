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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.FilesViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Files TopComponent.<p>
 * Functionality related to Projects tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getProjectNode()).<p>
 *
 * Example:<p>
 * <pre>
 *      FilesTabOperator fto = new FilesTabOperator();
 *      // or when Files pane is not already opened
 *      FilesTabOperator fto = FilesTabOperator.invoke();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = fto.tree();
 *      // work with nodes
 *      Node projectNode = fto.getProjectNode("SampleProject").select();
 *      Node node = new Node(projectNode, "subnode|sub subnode");
 * </pre> 
 *
 * @see FilesViewAction
 */
public class FilesTabOperator extends TopComponentOperator {
    
    static final String FILES_CAPTION = Bundle.getStringTrimmed(
                                            "org.netbeans.modules.project.ui.Bundle", 
                                            "LBL_projectTab_tc");
    private static final FilesViewAction viewAction = new FilesViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Files TopComponent within all IDE. */
    public FilesTabOperator() {
        super(waitTopComponent(null, FILES_CAPTION, 0, new FilesTabSubchooser()));
    }

    /** invokes Files and returns new instance of FilesTabOperator
     * @return new instance of FilesTabOperator */
    public static FilesTabOperator invoke() {
        viewAction.perform();
        return new FilesTabOperator();
    }
    
    /** Getter for Files JTreeOperator
     * @return JTreeOperator of Projects tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }

    /** Gets node representing a project.
     * @param projectName display name of project
     * @return Node instance representing the project specified by name
     */
    public Node getProjectNode(String projectName) {
        return new Node(tree(), projectName);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.project.ui.ProjectTab
     * Used in constructor.
     */
    private static final class FilesTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectTab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.project.ui.ProjectTab";
        }
    }
}
