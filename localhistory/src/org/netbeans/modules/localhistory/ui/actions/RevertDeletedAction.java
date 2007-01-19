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
package org.netbeans.modules.localhistory.ui.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FlatFolder;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * // XXX context action?
 * @author Tomas Stupka
 */
public class RevertDeletedAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public RevertDeletedAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected void performAction(final Node[] activatedNodes) {
                               
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                VCSContext ctx = VCSContext.forNodes(activatedNodes);
                Set<File> rootSet = ctx.getRootFiles();        
                if(rootSet == null || rootSet.size() < 1) { 
                    return;
                }                                        
                for (File file : rootSet) {            
                    if(file instanceof FlatFolder) {
                        revert(file);
                    } else {
                        revertRecursively(file);
                    }
                }                                       
            }
        });
        // XXX refresh view
    }

    private void revertRecursively(File file) {
        revert(file);
        File[] files = file.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                revertRecursively(f);   
            }            
        }
    }
    
    private void revert(File file) {
        StoreEntry[] entries = LocalHistory.getInstance().getLocalHistoryStore().getDeletedFiles(file);
        for(StoreEntry se : entries) {
            revert(se);
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {     
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<File> rootSet = ctx.getRootFiles();        
        if(rootSet == null || rootSet.size() < 1) { 
            return false;
        }                        
        for (File file : rootSet) {            
            if(file != null && !file.isDirectory()) {
                return false;
            }
        }        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowRevertDeleted");        
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowLocalHistoryAction.class);
    }

    private static void revert(StoreEntry se) {        
        File file = se.getFile();
        File storeFile = se.getStoreFile();
                
        InputStream is = null;
        OutputStream os = null;
        try {               
            if(!storeFile.isFile()) {
                FileUtil.createFolder(file);             
            } else {            
                FileObject fo = FileUtil.createData(file);                

                os = getOutputStream(fo);     
                is = se.getStoreFileInputStream();                    
                FileUtil.copy(is, os);            
            }
        } catch (Exception e) {            
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            return;
        } finally {
            try {
                if(os != null) { os.close(); }
                if(is != null) { is.close(); }
            } catch (IOException e) {}
        } 
    }
    
    private static OutputStream getOutputStream(FileObject fo) throws FileAlreadyLockedException, IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return fo.getOutputStream();                
            } catch (IOException ioe) {            
                retry++;
                if (retry > 7) {
                    throw ioe;
                }
                Thread.sleep(retry * 30);
            } 
        }                    
    }
    
}
