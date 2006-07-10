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
package org.netbeans.modules.collab.ui.actions;

import org.openide.util.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class StatusBusyAction extends StatusActionBase {
    /**
     *
     *
     */
    protected String getDisplayName() {
        return NbBundle.getMessage(StatusBusyAction.class, "LBL_ChangeStatusAction_BUSY"); // NOI18N
    }

    /**
     *
     *
     */
    protected int getStatus() {
        return CollabPrincipal.STATUS_BUSY;
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/busy_png.gif"; // NOI18N
    }

    /**
     *
     *
     */
    protected void setSessionStatus(CollabSession session) {
        try {
            session.setVisibleToAll();
            session.publishStatus(
                CollabPrincipal.STATUS_BUSY, NbBundle.getMessage(StatusBusyAction.class, "LBL_ChangeStatusAction_BUSY")
            ); // NOI18N
        } catch (CollabException e) {
            Debug.debugNotify(e);
        }
    }
}
