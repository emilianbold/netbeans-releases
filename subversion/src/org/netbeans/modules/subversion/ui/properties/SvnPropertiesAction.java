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
package org.netbeans.modules.subversion.ui.properties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.io.File;
import javax.swing.JComponent;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;


/**
 * 
 * 
 * @author Peter Pis
 */
public final class SvnPropertiesAction extends ContextAction {
    
    protected boolean enable(Node[] nodes) {
        return getContext(nodes).getRootFiles().length == 1;
    }
    
    public String getName() {
        return NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesAction");
    }
    
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Properties";
    }

    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        String ctxDisplayName = getContextDisplayName(nodes);
        File[] roots = ctx.getRootFiles();
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(repositoryUrl);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not hapen
            return;
        }
        
        final PropertiesPanel panel = new PropertiesPanel();
        final PropertiesTable propTable;
        propTable = new PropertiesTable(PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});
        panel.setPropertiesTable(propTable);
      
        JComponent component = propTable.getComponent();
        panel.propsPanel.setLayout(new BorderLayout());
        panel.propsPanel.add(component, BorderLayout.CENTER);
        SvnProperties svnProperties = new SvnProperties(panel, propTable, roots[0]);        
        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesDialog_Title", ctxDisplayName)); // NOI18N
        dd.setModal(true);
        dd.setOptions(new Object[] {org.openide.util.NbBundle.getMessage(SvnPropertiesAction.class, "CTL_Properties_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(SvnPropertiesAction.class));
        
        panel.putClientProperty("contentTitle", ctxDisplayName);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.pack();
        dialog.setVisible(true);
    }
}

