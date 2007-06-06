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
package org.netbeans.modules.subversion.ui.relocate;

import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author  Peter Pis
 */
public class RelocateAction extends ContextAction {
         
    protected boolean enable(Node[] nodes) {
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        if(roots == null || roots.length != 1) {
            return false;
        }
        File file = roots[0];
        return file != null && file.isDirectory();
    }
    
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    private String getCurrentURL(File root) {       
        try {
            SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
            return repositoryUrl.toString();
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);            
        }                            
        return "";
    }        
    
    private String getWorkingCopy(File root) {
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

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_Relocate_Title";
    }

    protected void performContextAction(Node[] nodes) {
        ResourceBundle loc = NbBundle.getBundle(RelocateAction.class);
        
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        if (roots == null) {
            return;
        }
        
        final RelocatePanel panel = new RelocatePanel();
        panel.getCurrentURL().setText(getCurrentURL(roots[0]));
        panel.getWorkingCopy().setText(getWorkingCopy(roots[0]));
        
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
        
        final String newUrl = panel.getNewURL().getText();
        
        final SVNUrl repositoryUrl;
        try {            
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                  
        final String wc = roots[0].getAbsolutePath();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            SvnClient client = null;
            protected void perform() {                    
                try {
                    client = Subversion.getInstance().getClient(repositoryUrl);
                    client.relocate(repositoryUrl.toString(), newUrl, wc, true);
                } catch (SVNClientException ex) {
                    annotate(ex);
                } 
            }
        };
        support.start(rp, repositoryUrl, loc.getString("LBL_Relocate_Progress"));
    }
}