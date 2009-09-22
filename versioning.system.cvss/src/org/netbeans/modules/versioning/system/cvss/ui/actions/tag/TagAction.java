/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;

import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.versioning.util.Utils;

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
        File[] roots = getContext(nodes).getFiles();
        File[][] flatFiles = Utils.splitFlatOthers(getContext(nodes).getFiles());
                
        TagCommand cmd = new TagCommand();
        copy (cmd, commandTemplate);
        
        String title = MessageFormat.format(NbBundle.getBundle(TagAction.class).getString("CTL_TagDialog_Title"), getContextDisplayName(nodes));
        
        TagSettings settings = new TagSettings(roots);
        settings.setCommand(cmd);
        
        JButton tag = new JButton(NbBundle.getMessage(TagAction.class, "CTL_TagDialog_Action_Tag"));
        settings.putClientProperty("OKButton", tag);
        settings.refreshComponents();
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
        
        if (flatFiles[0].length > 0) {
            cmd.setRecursive(false);
        } 
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
        c1.setDeleteTag(c2.isDeleteTag());
        c1.setOverrideExistingTag(c2.isOverrideExistingTag());
    }
}
