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
 * It means that opened Subversion view, Diff view
 * should react to newly opened/closed projects.
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
