/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.actions;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

/**
 * Abstract base for Show All Changes, Show All Diffs,
 * Update All and Commit All actions.
 *
 * <p>TODO add context listening and resetting logic
 *
 * @author Petr Kuzel
 */
public abstract class AbstractAllAction extends SystemAction {
    
    /** Creates a new instance of AbstractAllAction */
    public AbstractAllAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N                
    }

    /**
     * Enabled for at least one opened project
     */
    public boolean isEnabled() {
        if (super.isEnabled()) {
            Project projects[] = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                Project project = projects[i];
                if (SvnUtils.isVersionedProject(project)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
}
