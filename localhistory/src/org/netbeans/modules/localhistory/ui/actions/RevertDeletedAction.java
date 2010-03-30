/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.localhistory.ui.actions;

import org.netbeans.modules.localhistory.ui.view.ShowLocalHistoryAction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * @author Tomas Stupka
 */
public class RevertDeletedAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public RevertDeletedAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected void performAction(final Node[] activatedNodes) {
                               
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {
                VCSContext ctx = VCSContext.forNodes(activatedNodes);
                Set<File> rootSet = ctx.getRootFiles();        
                if(rootSet == null || rootSet.size() < 1) { 
                    return;
                }                                        
                for (File file : rootSet) {            
                    if(VersioningSupport.isFlat(file)) {
                        revert(file);
                    } else {
                        revertRecursively(file);
                    }
                }                                       
            }
        });
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
        if(file.exists()) {
            // created externaly?
            if(file.isFile()) {                
                LocalHistory.LOG.warning("Skipping revert for file " + file.getAbsolutePath() + " which already exists.");    
            }  
            // fix history
            // XXX create a new entry vs. fixing the entry timestamp and deleted flag?
            LocalHistory.getInstance().getLocalHistoryStore().fileCreate(file, file.lastModified());
        }
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
            LocalHistory.LOG.log(Level.SEVERE, null, e);
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
