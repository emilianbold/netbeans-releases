/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.diff;

import java.util.*;
import org.netbeans.modules.subversion.settings.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;

import java.awt.event.ActionEvent;
import java.io.File;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.ErrorManager;

/**
 * Diff action shows local changes
 *
 * @author Petr Kuzel
 */
public class DiffAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Diff";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE
             | FileInformation.STATUS_REMOTE_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
    }
    
    public void diff(Context ctx, int type, String contextName) {
        
        DiffMainPanel panel = new DiffMainPanel(ctx, type, contextName); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName));
        tc.open();
        tc.requestActive();        
    }
    
    protected void performContextAction(Node[] nodes) {
        Context ctx = getContext(nodes);
        String contextName = getContextDisplayName(nodes);
        diff(ctx, Setup.DIFFTYPE_LOCAL, contextName);        
    }
    
    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(Context context, int includeStatus) {
        File[] all = Subversion.getInstance().getStatusCache().listFiles(context, includeStatus);
        List files = new ArrayList();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            String path = file.getAbsolutePath();
            if (SvnModuleConfig.getDefault().isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }
        
        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File [] rootFiles = context.getRootFiles();
        for (int i = 0; i < rootFiles.length; i++) {
            File file = rootFiles[i];
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }
    
}
