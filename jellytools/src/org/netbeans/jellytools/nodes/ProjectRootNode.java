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
package org.netbeans.jellytools.nodes;

import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Project root node class. It represents root node of a project in Projects
 * view.
 * @see org.netbeans.jellytools.ProjectsTabOperator
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> 
 * @author Jiri.Skrivanek@sun.com 
 */
public class ProjectRootNode extends Node {
    
    static final FindAction findAction = new FindAction();
    static final BuildProjectAction buildProjectAction = new BuildProjectAction();
    static final CleanProjectAction cleanProjectAction = new CleanProjectAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            findAction,
            buildProjectAction,
            propertiesAction
        });
    }
    
    /** creates new ProjectRootNode instance
     * @param treeOperator treeOperator JTreeOperator of tree with Filesystems repository 
     * @param projectName display name of project
     */
    public ProjectRootNode(JTreeOperator treeOperator, String projectName) {
        super(treeOperator, projectName);
    }
    
    /** opens Search Filesystems dialog */    
    public void find() {
        findAction.perform(this);
    }
    
    /** build project */    
    public void buildProject() {
        buildProjectAction.perform(this);
    }
    
    /** Clean project */    
    public void cleanProject() {
        cleanProjectAction.perform(this);
    }
    
    /** opens properties of project */    
    public void properties() {
        propertiesAction.perform(this);
    }
}