/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.util;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;

/**
 * Open the Versioning Output view.
 *
 * @author Maros Sandor
 */
public class OpenVersioningOutputAction extends SystemAction {

    public OpenVersioningOutputAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setIcon(null);
    }

    public String getName() {
        return NbBundle.getMessage(OpenVersioningOutputAction.class, "CTL_MenuItem_OpenVersioningOutput"); // NOI18N
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
        return new HelpCtx(OpenVersioningOutputAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        VersioningOutputTopComponent stc = VersioningOutputTopComponent.getInstance();
        stc.open();
        stc.requestActive();
    }
}
