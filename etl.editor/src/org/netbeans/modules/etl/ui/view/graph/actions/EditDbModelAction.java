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
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * Action for editing database properties like user name, password, etc.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class EditDbModelAction extends GraphAction {

    private static final URL dbmodelNamesUrl = EditDbModelAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/DatabaseProperties.png");

    public EditDbModelAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(EditDbModelAction.class, "ACTION_EDITDBMODEL"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(dbmodelNamesUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditDbModelAction.class, "ACTION_EDITDBMODEL_TOOLTIP"));
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
        ETLCollaborationModel collabModel = DataObjectProvider.getProvider()
                                                .getActiveDataObject().getModel();

        if (etlEditor != null && collabModel != null) {
            List srcDBModels = collabModel.getSourceDatabaseModels();
            List tgtDBModels = collabModel.getTargetDatabaseModels();

            if (!srcDBModels.isEmpty() || !tgtDBModels.isEmpty()) {
                etlEditor.editDBModel();
            } else {
                String noDBModelMsg = NbBundle.getMessage(EditDbModelAction.class, "ERROR_no_dbmodel_to_edit");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(noDBModelMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
