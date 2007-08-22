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

package org.netbeans.modules.cnd.classview.actions;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.modules.cnd.classview.ClassViewTopComponent;
import org.openide.util.NbPreferences;

/**
 * Shows/Hides class view pane
 * @author Vladimir Kvashin
 */
public class ShowHideClassViewAction extends CallableSystemAction {

    public ShowHideClassViewAction() {
        putValue(NAME, I18n.getMessage("CTL_ClassViewAction")); // NOI18N
        putValue(SHORT_DESCRIPTION, I18n.getMessage("HINT_ClassViewAction")); // NOI18N
    }

    public String getName() {
        return (String) getValue(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }

    public void performAction() {
        TopComponent tc = ClassViewTopComponent.findDefault();
        if (!tc.isOpened()) {
            tc.open();
            Preferences ps = NbPreferences.forModule(ShowHideClassViewAction.class);
            ps.putBoolean("ClassViewWasOpened", true);
        }
        tc.requestActive();
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected String iconResource() {
        return ClassViewTopComponent.ICON_PATH;
    }
}
