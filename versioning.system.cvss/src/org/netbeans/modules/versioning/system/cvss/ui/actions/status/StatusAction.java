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

package org.netbeans.modules.versioning.system.cvss.ui.actions.status;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;
import org.openide.nodes.Node;

import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Opens the Versioning window.
 * 
 * @author Maros Sandor
 */
public class StatusAction extends AbstractSystemAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;  
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Status";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performCvsAction(Node[] nodes) {
        CvsSynchronizeTopComponent stc = CvsSynchronizeTopComponent.getInstance();
        stc.setContext(null);
        stc.open();

        Context ctx = Utils.getCurrentContext(nodes);

        stc.setContentTitle(getContextDisplayName(nodes));
        stc.setContext(ctx);
        stc.requestActive();
        stc.performRefreshAction();
    }

    protected boolean asynchronous() {
        return false;
    }

}
