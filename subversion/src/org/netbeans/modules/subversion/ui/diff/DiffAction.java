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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;

import java.awt.event.ActionEvent;
import java.io.File;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.ErrorManager;

/**
 * Diff action
 *
 * @author Petr Kuzel
 */
public class DiffAction extends ContextAction {

    protected String getBaseName() {
        return "CTL_MenuItem_Diff";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE
             | FileInformation.STATUS_REMOTE_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
    }
    
    protected void performContextAction(ActionEvent e) {
        Context ctx = getContext();

        // TODO retrieve given versions and pass structures to diif module
    }
}
