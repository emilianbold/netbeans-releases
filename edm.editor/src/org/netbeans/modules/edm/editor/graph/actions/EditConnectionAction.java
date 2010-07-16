/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.property.IPropertySheet;
import org.netbeans.modules.edm.editor.ui.view.DBModelTreeView;
import org.netbeans.modules.edm.editor.ui.view.EditDBModelPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class EditConnectionAction extends AbstractAction {

    private MashupDataObject mObj;
    private MashupGraphManager manager;

    /** Creates a new instance of EditJoinAction */
    public EditConnectionAction(MashupDataObject dObj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.EDITCONNECTION)));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
    }

    public EditConnectionAction(MashupDataObject dObj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.EDITCONNECTION)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditConnectionAction.class, "TOOLTIP_Edit_Database_Properties"));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
    }

    public void actionPerformed(ActionEvent e) {
        JLabel panelTitle = new JLabel(NbBundle.getMessage(EditConnectionAction.class, "TOOLTIP_Edit_Database_Properties"));
        panelTitle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(EditConnectionAction.class, "ACSN_Edit_Database_Properties"));
        panelTitle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditConnectionAction.class, "ACSN_Edit_Database_Properties"));
        panelTitle.setFont(panelTitle.getFont().deriveFont(Font.BOLD));
        panelTitle.setFocusable(false);
        panelTitle.setHorizontalAlignment(SwingConstants.LEADING);
        EditDBModelPanel editPanel = new EditDBModelPanel(mObj.getModel());

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panelTitle, BorderLayout.NORTH);
        contentPane.add(editPanel, BorderLayout.CENTER);

        DialogDescriptor dd = new DialogDescriptor(contentPane, NbBundle.getMessage(EditConnectionAction.class, "TOOLTIP_Edit_Database_Properties"));
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setSize(new Dimension(600, 450));
        dlg.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
            DBModelTreeView dbModelTreeView = editPanel.getDBModelTreeView();
            if (dbModelTreeView != null) {
                IPropertySheet propSheet = dbModelTreeView.getPropSheet();
                if (propSheet != null) {
                    propSheet.commitChanges();
                    //mObj.getMashupDataEditorSupport().synchDocument();
                    mObj.getModel().setDirty(true);
                    mObj.setModified(true);
                    manager.setLog(NbBundle.getMessage(EditConnectionAction.class, "LOG_Database_properties_successfully_modified."));
                }
            }
        }
    }
}