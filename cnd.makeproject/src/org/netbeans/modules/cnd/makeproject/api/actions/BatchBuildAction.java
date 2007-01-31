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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public class BatchBuildAction extends CallableSystemAction {
    public String getName() {
	return getString("BatchBuildActionName"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
	performAction();
    }

    public void performAction() {
	Action action = MainProjectSensitiveActions.mainProjectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, getString("BatchBuildActionName"), null);
	JButton jButton = new JButton(action);
	jButton.doClick();
    }

    public HelpCtx getHelpCtx() {
	return null;
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(BatchBuildAction.class);
	}
	return bundle.getString(s);
    }
}
