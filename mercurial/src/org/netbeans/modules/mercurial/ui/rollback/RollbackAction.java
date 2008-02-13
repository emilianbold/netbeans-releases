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
package org.netbeans.modules.mercurial.ui.rollback;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.ui.update.ConflictResolvedAction;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Pull action for mercurial: 
 * hg pull - pull changes from the specified source
 * 
 * @author John Rice
 */
public class RollbackAction extends ContextAction {
    
    private final VCSContext context;
    private static File pullPath = null;
            
    public RollbackAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void performAction(ActionEvent e) {
        if(!HgRepositoryContextCache.hasHistory(context)){
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RollbackAction.class,
                    "MSG_ROLLBACK_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RollbackAction.class,
                    "MSG_ROLLBACK_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTab(NbBundle.getMessage(RollbackAction.class, "MSG_NO_ROLLBACK")); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(RollbackAction.class, "MSG_ROLLBACK_DONE")); // NOI18N
            HgUtils.outputMercurialTab(""); // NOI18N
            return;
        }
        rollback(context);
    }
    
    public static void rollback(final VCSContext ctx){
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                
                try {
                    HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(RollbackAction.class,
                                "MSG_ROLLBACK_TITLE")); // NOI18N
                    HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(RollbackAction.class,
                                "MSG_ROLLBACK_TITLE_SEP")); // NOI18N
                    List<String> list = HgCommand.doRollback(root);
                    
                    if(list != null && !list.isEmpty()){                      
                        //HgUtils.clearOutputMercurialTab();
                        
                        if(HgCommand.isNoRollbackPossible(list.get(0))){
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_NO_ROLLBACK"));     // NOI18N                       
                        }else{
                            HgUtils.outputMercurialTab(list.get(0));
                            if (HgCommand.hasHistory(root)) {
                                int response = JOptionPane.showOptionDialog(null,
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_CONFIRM_QUERY") ,  // NOI18N
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_CONFIRM"), // NOI18N
                                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null, null, null);
                            
                                if( response == JOptionPane.YES_OPTION){
                                    HgUtils.outputMercurialTab(
                                            NbBundle.getMessage(RollbackAction.class,
                                            "MSG_ROLLBACK_FORCE_UPDATE", root.getAbsolutePath())); // NOI18N
                                    list = HgCommand.doUpdateAll(root, true, null);
                                    
                                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
                                    if(cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT).length != 0){
                                        ConflictResolvedAction.resolved(ctx);                                       
                                    }
                                    HgUtils.forceStatusRefreshProject(ctx);
                                    Mercurial.getInstance().changesetChanged(root);

                                    if (list != null && !list.isEmpty()){
                                        HgUtils.outputMercurialTab(list);
                                    }
                                } else {
                                    HgUtils.forceStatusRefreshProject(ctx);
                                    Mercurial.getInstance().changesetChanged(root);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_MESSAGE_NOHISTORY") ,  // NOI18N
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_MESSAGE"), // NOI18N
                                        JOptionPane.INFORMATION_MESSAGE,null);
                            
                            }
                        }
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_ROLLBACK_INFO")); // NOI18N
                    }
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                } finally {
                    HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(RollbackAction.class,
                                "MSG_ROLLBACK_DONE")); // NOI18N
                    HgUtils.outputMercurialTab(""); // NOI18N
                }
            }
        };
        support.start(rp, repository,org.openide.util.NbBundle.getMessage(RollbackAction.class, "MSG_ROLLBACK_PROGRESS")); // NOI18N
    }
    
    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    }
}
