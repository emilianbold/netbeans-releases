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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.properties;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.util.HgUtils;

import javax.swing.*;
import java.io.File;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * Properties for mercurial: 
 * Set hg repository properties
 * 
 * @author John Rice
 */
public class PropertiesAction extends AbstractAction {
    
    private final VCSContext context;

    public PropertiesAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        File root = HgUtils.getRootFile(context);
        if (root == null) {
            return;
        }
        final PropertiesPanel panel = new PropertiesPanel();

        final PropertiesTable propTable;

        propTable = new PropertiesTable(PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});

        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();

        panel.propsPanel.setLayout(new BorderLayout());

        panel.propsPanel.add(component, BorderLayout.CENTER);

        HgProperties hgProperties = new HgProperties(panel, propTable, root);        

        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(PropertiesAction.class, "CTL_PropertiesDialog_Title", null), true, null); // NOI18N
        final JButton okButton =  new JButton(org.openide.util.NbBundle.getMessage(PropertiesAction.class, "CTL_Properties_Action_OK")); // NOI18N
        dd.setOptions(new Object[] {okButton, 
                                    org.openide.util.NbBundle.getMessage(PropertiesAction.class, "CTL_Properties_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(PropertiesAction.class));
        panel.putClientProperty("contentTitle", null);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.pack();
        dialog.setVisible(true);
        if (dd.getValue() == okButton) {
            hgProperties.setProperties();
        }
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    } 
}
