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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Clean Project" popup menu item on project's root node.
 * @see Action
 * @see org.netbeans.jellytools.nodes.ProjectRootNode
 * @author Jiri.Skrivanek@sun.com
 */
public class CleanProjectAction extends Action {
    
    private static final String cleanProjectPopup = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_CleanAction_Name");

    /** creates new CleanProjectAction instance */    
    public CleanProjectAction() {
        super(null, cleanProjectPopup);
    }
}