/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.nodes;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ExploreFromHereAction;
import org.netbeans.jellytools.actions.FindAction;

/** Node representing Source packages node under project node. 
 * @author Jiri.Skrivanek@sun.com
 */
public class SourcePackagesNode extends Node {

    private static final String SOURCE_PACKAGES_LABEL = Bundle.getString(
                                "org.netbeans.modules.java.j2seproject.Bundle",
                                "NAME_src.dir");
    static final ExploreFromHereAction exploreFromHereAction = new ExploreFromHereAction();
    static final FindAction findAction = new FindAction();
    
    /** Finds Source Packages node under project with given name
     * @param projectName display name of project
     */
    public SourcePackagesNode(String projectName) {
        super(new ProjectsTabOperator().getProjectRootNode(projectName), SOURCE_PACKAGES_LABEL);
    }

    /** Finds Source Packages node under given project node.
     * @param projectNode project node in the Projects view
     */
    public SourcePackagesNode(Node projectNode) {
        super(projectNode, SOURCE_PACKAGES_LABEL);
    }

    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            exploreFromHereAction,
            findAction,
        });
    }
    
    /** performs ExploreFromHereAction with this node */    
    public void exploreFromHere() {
        exploreFromHereAction.perform(this);
    }
    
    /** performs FindAction with this node */    
    public void find() {
        findAction.perform(this);
    }
}
