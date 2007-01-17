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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Context;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Performs diff action by fetching the appropriate file from repository
 * and shows the diff. It actually does not execute "cvs diff" but rather uses our
 * own diff algorithm. 
 * 
 * @author Maros Sandor
 */
public class DiffExecutor {

    private final Context   context;
    private final String    contextName;

    public DiffExecutor(Context context, String contextName) {
        this.context = context;
        this.contextName = contextName;
    }

    public DiffExecutor(String contextName) {
        this.contextName = contextName;
        this.context = null;
    }

    /**
     * Opens GUI component that shows differences between current base revisions
     * and HEAD revisions.
     */ 
    public void showRemoteDiff(ExecutorGroup group) {
        showDiff(Setup.DIFFTYPE_REMOTE, group);
    }
    
    /**
     * Opens GUI component that shows differences between current working files
     * and HEAD revisions.
     */ 
    public void showAllDiff(ExecutorGroup group) {
        showDiff(Setup.DIFFTYPE_ALL, group);
    }
    
    /**
     * Opens GUI component that shows differences between current working files
     * and repository versions they are based on.
     */ 
    public void showLocalDiff(ExecutorGroup group) {
        showDiff(Setup.DIFFTYPE_LOCAL, group);
    }

    public void showDiff(File file, String rev1, String rev2) {
        DiffMainPanel panel = new DiffMainPanel(file, rev1, rev2);
        openDiff(panel, null);
    }

    private void showDiff(int type, ExecutorGroup group) {
        VersionsCache.getInstance().purgeVolatileRevisions();
        DiffMainPanel panel = new DiffMainPanel(context, type, contextName, group); // spawns bacground DiffPrepareTask
        openDiff(panel, group);
    }
    
    private void openDiff(final JComponent c, final ExecutorGroup group) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DiffTopComponent tc = new DiffTopComponent(c);
                tc.setName(NbBundle.getMessage(DiffExecutor.class, "CTL_DiffPanel_Title", contextName));
                tc.open();
                tc.requestActive();
                tc.setGroup(group);
            }
        });
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
        CvsFileTableModel model = CvsVersioningSystem.getInstance().getFileTableModel(context, includeStatus);
        CvsFileNode [] nodes = model.getNodes();
        List files = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            File file = nodes[i].getFile();
            if (CvsModuleConfig.getDefault().isExcludedFromCommit(file) == false) {
                files.add(file);
            }
        }
        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        File [] rootFiles = context.getRootFiles();
        for (int i = 0; i < rootFiles.length; i++) {
            File file = rootFiles[i];
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    private static class DiffTopComponent extends TopComponent implements DiffSetupSource {

        public DiffTopComponent() {
        }
        
        public DiffTopComponent(JComponent c) {
            setLayout(new BorderLayout());
            c.putClientProperty(TopComponent.class, this);
            add(c, BorderLayout.CENTER);
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
        }
        
        public int getPersistenceType(){
            return TopComponent.PERSISTENCE_NEVER;
        }

        protected void componentClosed() {
            ((DiffMainPanel) getComponent(0)).componentClosed();
            super.componentClosed();
        }

        protected String preferredID(){
            return "DiffExecutorTopComponent";    // NOI18N       
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(getClass());
        }

        protected void componentActivated() {
            super.componentActivated();
            DiffMainPanel mainPanel = ((DiffMainPanel) getComponent(0));
            mainPanel.requestActive();
        }

        public Collection getSetups() {
            DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
            return mainPanel.getSetups();
        }

        public String getSetupDisplayName() {
            DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
            return mainPanel.getSetupDisplayName();

        }

        public void setGroup(ExecutorGroup group) {
            DiffMainPanel mainPanel = ((DiffMainPanel) getComponent(0));
            mainPanel.setGroup(group);
        }
    }
    
}
