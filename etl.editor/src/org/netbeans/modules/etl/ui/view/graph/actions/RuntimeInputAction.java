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

package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.openide.util.NbBundle;


/**
 * To change this template use Options | File Templates.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class RuntimeInputAction extends GraphAction {

    private static final URL runtimeInputImgUrl = RuntimeInputAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/RuntimeInput.png");

    public RuntimeInputAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(RuntimeInputAction.class, "ACTION_RUNTIMEINPUT"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(runtimeInputImgUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RuntimeInputAction.class, "ACTION_RUNTIMEINPUT_TOOLTIP"));
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopComponent etlEditor = null;
        try {
            etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
        } catch (Exception ex) {
            // ignore
        }
        if (etlEditor != null) {
            etlEditor.addRuntime(SQLConstants.RUNTIME_INPUT);
        }
    }
}

