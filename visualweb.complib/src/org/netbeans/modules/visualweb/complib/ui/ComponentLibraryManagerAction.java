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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.complib.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * This Action does the same thing as ManageComponentLibrariesAction but has a
 * different menu label. Apparently there isn't an easy way in NB to share the
 * same Action class and use two different labels.
 *
 * @author Edwin Goei
 */
public class ComponentLibraryManagerAction extends CallableSystemAction {
    private String actionName;

    public ComponentLibraryManagerAction() {
        // Action invoked from non-context menu, eg. Tools->Component Library
        // Manager
        this.actionName = NbBundle.getMessage(
                ComponentLibraryManagerAction.class,
                "ComponentLibraryManagerAction.ComponentLibraryManager"); // NOI18N
    }

    public void performAction() {
        new CompLibManagerPanel().showDialog();
    }

    public String getName() {
        return actionName;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
}
