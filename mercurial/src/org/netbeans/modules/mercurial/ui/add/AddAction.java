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
package org.netbeans.modules.mercurial.ui.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Add action for mercurial: 
 * hg add - add the specified files on the next commit
 * 
 * @author John Rice
 */
public class AddAction extends AbstractAction {
    
    private final VCSContext context;

    public AddAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
        
        if(cache.listFiles(context, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY).length != 0)
            return true;

        return false;
    } 
    
    public void actionPerformed(ActionEvent ev) {     
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return;

        FileStatusCache cache = hg.getFileStatusCache();        
        
        File root = hg.getTopmostManagedParent(files[0]);
        List<File> addFiles = new ArrayList();
        
        for (File file : files) {
            if (!file.isDirectory() && (cache.getStatus(file).getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0){
                addFiles.add(file);
            }
        }
        if (addFiles.size() == 0) return;
        
        try {
            HgCommand.doAdd(root, addFiles);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        // hg.versionedFilesChanged();
        for (File file : addFiles){
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
}
