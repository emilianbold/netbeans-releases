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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;

import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

/**
 * Performs the CVS 'tag' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class TagAction extends AbstractSystemAction {
    
    private static TagCommand   commandTemplate = new TagCommand();
    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;


    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Tag";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    public void performCvsAction(Node[] nodes) {
        File [] roots = getContext(nodes).getFiles();
                
        TagCommand cmd = new TagCommand();
        copy (cmd, commandTemplate);
        
        String title = MessageFormat.format(NbBundle.getBundle(TagAction.class).getString("CTL_TagDialog_Title"), 
                                         new Object[] { getContextDisplayName(nodes) });
        
        TagSettings settings = new TagSettings(roots);
        settings.setCommand(cmd);
        
        JButton tag = new JButton(NbBundle.getMessage(TagAction.class, "CTL_TagDialog_Action_Tag"));
        settings.putClientProperty("OKButton", tag);
        settings.onTagNameChange();
        tag.setToolTipText(NbBundle.getMessage(TagAction.class,  "TT_TagDialog_Action_Tag"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { tag, DialogDescriptor.CANCEL_OPTION },
                tag,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(TagAction.class),
                null);
        descriptor.setClosingOptions(null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TagAction.class, "ACSD_TagDialog"));
        dialog.setVisible(true);
        if (descriptor.getValue() != tag) return;

        settings.updateCommand(cmd);
        copy(commandTemplate, cmd);
        cmd.setFiles(roots);

        ExecutorGroup group = new ExecutorGroup(getRunningName(nodes));
        group.addExecutors(TagExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null));
        group.execute();
    }

    protected boolean asynchronous() {
        return false;
    }
    
    private void copy(TagCommand c1, TagCommand c2) {
        c1.setTag(c2.getTag());
        c1.setCheckThatUnmodified(c2.isCheckThatUnmodified());
        c1.setOverrideExistingTag(c2.isOverrideExistingTag());
    }
}
