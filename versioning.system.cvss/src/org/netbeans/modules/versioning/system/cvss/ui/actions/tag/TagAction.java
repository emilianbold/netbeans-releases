/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
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


    protected String getBaseName() {
        return "CTL_MenuItem_Tag";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void actionPerformed(ActionEvent ev) {
        File [] roots = getFilesToProcess();
                
        TagCommand cmd = new TagCommand();
        copy (cmd, commandTemplate);
        
        String title = MessageFormat.format(NbBundle.getBundle(TagAction.class).getString("CTL_TagDialog_Title"), 
                                         new Object[] { getContextDisplayName() });
        
        TagSettings settings = new TagSettings();
        settings.setCommand(cmd);
        DialogDescriptor descriptor = new DialogDescriptor(settings, title); 
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return;

        settings.updateCommand(cmd);
        copy(commandTemplate, cmd);
        cmd.setFiles(roots);
        
        TagExecutor [] executors = TagExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
        ExecutorSupport.notifyError(executors);
    }
    
    private void copy(TagCommand c1, TagCommand c2) {
        c1.setTag(c2.getTag());
        c1.setCheckThatUnmodified(c2.isCheckThatUnmodified());
        c1.setOverrideExistingTag(c2.isOverrideExistingTag());
    }
}
