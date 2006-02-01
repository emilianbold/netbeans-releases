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

package org.netbeans.modules.subversion.ui.status;

import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.ui.actions.ContextAction;

import java.awt.event.ActionEvent;

/**
 * Context sensitive status action. It opens the Subversion
 * view and sets its context.
 *
 * @author Petr Kuzel
 */
public class StatusAction  extends ContextAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED;  
    
    protected String getBaseName() {
        return "CTL_MenuItem_ShowChanges";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performContextAction(ActionEvent ev) {
        Context ctx = SvnUtils.getCurrentContext(null); // XXX on editor tab
        SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        stc.setContentTitle(getContextDisplayName());
        stc.setContext(ctx);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }
}