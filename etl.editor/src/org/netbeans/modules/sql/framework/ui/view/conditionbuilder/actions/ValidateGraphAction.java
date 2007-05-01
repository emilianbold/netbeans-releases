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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionViewManager;
import org.openide.util.NbBundle;


/**
 * Action to validate the sql entered by the user
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ValidateGraphAction extends GraphAction {

    private static URL showSqlUrl = ValidateGraphAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/validate.png");

    public ValidateGraphAction() {
        // Action name
        this.putValue(Action.NAME, NbBundle.getMessage(ValidateGraphAction.class, "ACTION_VALIDATEGRAPH"));

        // Action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(showSqlUrl));

        // Action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ValidateGraphAction.class, "ACTION_VALIDATEGRAPH_TOOLTIP"));

    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        ConditionBuilderView cView = ConditionViewManager.getDefault().getCurrentConditionBuilderView();
        if (cView != null) {
            cView.doGraphValidation();
        }
    }

}

