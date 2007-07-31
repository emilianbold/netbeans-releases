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

package org.netbeans.modules.subversion.ui.status;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.awt.event.ActionEvent;
import org.netbeans.modules.subversion.Subversion;

import javax.swing.*;

/**
 * Open the Subversion view. It focuses recently opened
 * view unless it's not initialized yet. For uninitialized
 * view it behaves like StatusProjectsAction without
 * on-open refresh.
 *
 * @author Petr Kuzel
 */
public class OpenVersioningAction extends ShowAllChangesAction {

    public OpenVersioningAction() {
        putValue("noIconInMenu", Boolean.FALSE); // NOI18N
        setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/versioning-view.png"))); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(OpenVersioningAction.class, "CTL_MenuItem_OpenVersioning"); // NOI18N
    }

    /**
     * Window/Versioning should be always enabled.
     * 
     * @return true
     */ 
    public boolean isEnabled() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenVersioningAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
                
        SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        if (stc.hasContext() == false) {
            super.actionPerformed(e);
        } else {
            stc.open();
            stc.requestActive();
        }
    }

    protected boolean shouldPostRefresh() {
        return false;
    }
}
