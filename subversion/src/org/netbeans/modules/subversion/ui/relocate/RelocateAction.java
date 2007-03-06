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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * RelocateAction.java
 *
 * Created on 03 March 2007, 11:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author  Peter Pis
 */

package org.netbeans.modules.subversion.ui.relocate;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class RelocateAction extends AbstractAction {
    
    VCSContext ctx;
    SvnProgressSupport support;
    
    /** Creates a new instance of RelocateAction */
    public RelocateAction(String name, VCSContext ctx) {
        super(name);
        this.ctx = ctx;
    }
    
    public boolean isEnabled() {
        Set<File> roots = ctx.getRootFiles();
        File file = roots.iterator().next();
        return (roots.size() == 1 & file.isDirectory());
    }
    
    public void actionPerformed(ActionEvent event) {
        ResourceBundle loc = NbBundle.getBundle(RelocateAction.class);
        
        final RelocatePanel panel = new RelocatePanel();
        panel.getCurrentURL().setText(getCurrentURL());
        panel.getWorkingCopy().setText(getWorkingCopy());
        
        
        final JButton btnRelocate = new JButton(loc.getString("CTL_Relocate_Action_Name"));
        btnRelocate.setEnabled(false);
        btnRelocate.setToolTipText(loc.getString("TT_Relocate_Action"));
        
        panel.getNewURL().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }

            public void removeUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }

            public void changedUpdate(DocumentEvent event) {
                validate(panel, btnRelocate);
            }          
        });
        JButton btnCancel = new JButton(loc.getString("CTL_Relocate_Action_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RelocateAction.class, "ACSD_Relocate_Action_Cancel"));  // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel,  loc.getString("CTL_Relocate_Title"),  true, new Object [] {btnRelocate, btnCancel}, btnRelocate, DialogDescriptor.BOTTOM_ALIGN, null, null);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(new HelpCtx(RelocateAction.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(loc.getString("ACSD_Relocate"));
        
        dialog.setVisible(true);
        if (descriptor.getValue() != btnRelocate) 
            return;
        
        panel.getNewURL().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                validate(panel, btnRelocate);
            }
        });
        
        final String newUrl = panel.getNewURL().getText();
        
        File root = ctx.getRootFiles().iterator().next();
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        final String wc = root.getAbsolutePath();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport() {
                SvnClient client = null;
                protected void perform() {
                    try {
                        client = Subversion.getInstance().getClient(repositoryUrl);
                    } catch (SVNClientException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        return;
                    }
                    
                    try {
                        client.relocate(repositoryUrl.toString(), newUrl, wc, true);
                    } catch (SVNClientException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
                    }
                }
            };
            support.start(rp, repositoryUrl, loc.getString("LBL_Relocate_Progress"));
        } finally {
            support = null;
        }
    }
    
    private String getCurrentURL() {
        File root = ctx.getRootFiles().iterator().next();
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        return repositoryUrl.toString();
    }
    
    private String getWorkingCopy() {
        File root = ctx.getRootFiles().iterator().next();
        final String working = root.getAbsolutePath();
        return working;
    }
    
    public void validate(RelocatePanel panel, JButton btnOk) {
        try {
            new SVNUrl(panel.getNewURL().getText());
            btnOk.setEnabled(true);
        } catch (MalformedURLException e) {
            btnOk.setEnabled(false);
        }
        
    }
}