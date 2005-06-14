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
package org.netbeans.jellytools.modules.freeform.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Run Project" popup menu item on project's root node.
 * @see Action
 * @see org.netbeans.jellytools.nodes.FreeformProjectNode
 * @author Martin.Schovanek@sun.com
 */
public class RunFreeformAction extends Action {
    
    private static final String runProject = Bundle.getStringTrimmed(
            "org.netbeans.modules.ant.freeform.Bundle",
            "CMD_run");

    /**
     * creates new RunFreeformAction instance
     */    
    public RunFreeformAction() {
        super(null, runProject);
    }
}