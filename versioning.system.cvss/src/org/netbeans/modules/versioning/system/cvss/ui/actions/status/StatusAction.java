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

package org.netbeans.modules.versioning.system.cvss.ui.actions.status;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;

import java.awt.event.ActionEvent;

/**
 * Opens the Versioning window.
 * 
 * @author Maros Sandor
 */
public class StatusAction extends AbstractSystemAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED;  
    
    protected String getBaseName() {
        return "CTL_MenuItem_Status";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performCvsAction(ActionEvent ev) {
        Context ctx = Utils.getCurrentContext();
        CvsSynchronizeTopComponent stc = CvsSynchronizeTopComponent.getInstance();
        stc.setContentTitle(getContextDisplayName());
        stc.setContext(ctx);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }
}
