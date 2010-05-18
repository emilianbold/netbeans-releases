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
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.graph.components.AddTablePanel;
import org.netbeans.modules.edm.editor.utils.MashupModelHelper;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class implements the action for adding new table.
 * @author Nithya
 */
public class AddTableAction extends AbstractAction {

    /**
     * member variable for mashup data object.
     */
    private MashupDataObject mObj;

    /**
     * Implements actionPerformed. 
     */
    public void actionPerformed(ActionEvent e) {
        JLabel panelTitle = new JLabel(NbBundle.getMessage(AddTableAction.class, "LBL_Select_Source_Tables"));
        panelTitle.setFont(panelTitle.getFont().deriveFont(Font.BOLD));
        panelTitle.setFocusable(false);
        panelTitle.setHorizontalAlignment(SwingConstants.LEADING);
        AddTablePanel addPanel = new AddTablePanel(mObj);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panelTitle, BorderLayout.NORTH);
        contentPane.add(addPanel, BorderLayout.CENTER);

        
        DialogDescriptor dd = new DialogDescriptor(contentPane, NbBundle.getMessage(AddTableAction.class, "TITLE_Select_Source_Table_Window"));
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setSize(new Dimension(600, 450));
        dlg.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
            MashupModelHelper.getModel(mObj.getModel(), addPanel.getTables());
            try {
                mObj.establishRuntimeInputs(mObj.getModel(), mObj.getModel().getSQLDefinition().getSourceTables());
            } catch (EDMException ex) {
                Exceptions.printStackTrace(ex);
            }
            mObj.getGraphManager().refreshGraph();
            mObj.getGraphManager().layoutGraph();
            mObj.getMashupDataEditorSupport().synchDocument();
            mObj.getMashupDataEditorSupport().syncModel();
            mObj.getModel().setDirty(true);
            mObj.setModified(true);
        }
    }

    public AddTableAction(MashupDataObject dObj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.ADDTABLE)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
    }

    public AddTableAction(MashupDataObject dObj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.ADDTABLE)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AddTableAction.class, "TOOLTIP_Add_Source_Table"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
    }
}