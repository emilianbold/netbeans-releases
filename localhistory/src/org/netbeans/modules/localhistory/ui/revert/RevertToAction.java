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
package org.netbeans.modules.localhistory.ui.revert;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Set;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.actions.AbstractRevertAction;
import org.netbeans.modules.localhistory.ui.revert.RevertChanges;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FlatFolder;
import org.openide.LifecycleManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RevertToAction extends AbstractRevertAction {


    protected void performAction(final Node[] activatedNodes) {
        // XXX try to save files in invocation context only
        // list somehow modified file in the context and save
        // just them.
        // The same (global save) logic is in CVS, no complaint
        LifecycleManager.getDefault().saveAll();
        
        // XXX progress support ???
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                 
                VCSContext ctx = VCSContext.forNodes(activatedNodes);
                final Set<File> rootSet = ctx.getRootFiles();        
                RevertChanges revertChanges = new RevertChanges(rootSet);
                revertChanges.show();
                long ts = revertChanges.getTimeStamp();
                for(File root : rootSet) {    
                    if(root.isFile()) {
                        revertFile(root, ts);    
                    } else {
                        revertFolder(root, ts);
                    }
                }
            }
        });
        // XXX refresh view
    }

    private void revertFile(File file, long ts) {
        StoreEntry entry = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntry(file, ts);
        revert(entry);        
    }
    
    private void revertFolder(File root, long ts) {        
        // revert all files in the folder
        File[] files = root.listFiles();                    
        StoreEntry[] entries = LocalHistory.getInstance().getLocalHistoryStore().getFolderState(root, files, ts);                    
        for(StoreEntry entry : entries) {
            revert(entry);                       
        }     
        
        if(root instanceof FlatFolder) {            
            return; // only one level revert
        }
        
        // get the root folders actuall children and 
        // revert also the directories between them
        File[] revertedFolders = root.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });                                                                               
        for(File revertedFolder : revertedFolders) {
            revertFile(revertedFolder, ts);
        }                    
    }
    
    protected boolean enable(Node[] activatedNodes) {
        // XXX multi- or single node 
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }        
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<File> rootSet = ctx.getRootFiles();        
        return rootSet != null && rootSet.size() > 0;
    }

    public String getName() {
        return getMenuName();
    }       
    
    public static String getMenuName() {
        return NbBundle.getMessage(RevertToAction.class, "LBL_RevertToAction");
    }
}
