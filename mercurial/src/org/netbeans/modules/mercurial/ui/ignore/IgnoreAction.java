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

package org.netbeans.modules.mercurial.ui.ignore;

import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.*;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.*;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Adds/removes files to repository .hgignore.
 *
 * @author Maros Sandor
 */
public class IgnoreAction extends AbstractAction {
    
    private final VCSContext context;

    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    public IgnoreAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
    
    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        if (files.length == 0) return UNDEFINED; 
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".hg")) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                if (actionStatus == UNIGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = IGNORING;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                if (actionStatus == IGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = UNIGNORING;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        return getActionStatus(files) != UNDEFINED;
    }

    public void actionPerformed(ActionEvent e) {
        final File[] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        final int actionStatus = getActionStatus(files);

        if (actionStatus != IGNORING && actionStatus != UNIGNORING) {
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
        
        final File repository = HgUtils.getRootFile(context);
        if (repository == null) return;
        String projName = HgProjectUtils.getProjectName(repository);
        if (projName == null) {
            File projFile = HgUtils.getProjectFile(context);
            projName = HgProjectUtils.getProjectName(projFile);
        }
        final String prjName = projName;
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository.getAbsolutePath());
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                try {
                    if (actionStatus == IGNORING) {
                        HgUtils.addIgnored(repository, files);
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(IgnoreAction.class,
                                "MSG_IGNORE_TITLE")); // NOI18N
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(IgnoreAction.class,
                                "MSG_IGNORE_TITLE_SEP")); // NOI18N
                        if (files.length == 1) {
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(IgnoreAction.class,
                                    "MSG_IGNORE_INIT_SEP_ONE", files.length, prjName)); // NOI18N
                         } else {
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(IgnoreAction.class,
                                    "MSG_IGNORE_INIT_SEP", files.length, prjName)); // NOI18N
                          }
                    } else {
                        HgUtils.removeIgnored(repository, files);
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(IgnoreAction.class,
                                "MSG_UNIGNORE_TITLE")); // NOI18N
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(IgnoreAction.class,
                                "MSG_UNIGNORE_TITLE_SEP")); // NOI18N
                        if (files.length == 1) {
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(IgnoreAction.class,
                                    "MSG_UNIGNORE_INIT_SEP_ONE", files.length, prjName)); // NOI18N
                         } else {
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(IgnoreAction.class,
                                    "MSG_UNIGNORE_INIT_SEP", files.length, prjName)); // NOI18N
                          }
                    }
                } catch (IOException ex) {
                   Mercurial.LOG.log(Level.FINE, "IgnoreAction(): File {0} - {1}", // NOI18N
                        new Object[] {repository.getAbsolutePath(), ex.toString()});
                }
                // refresh files manually
                for (File file : files) {
                    Mercurial.getInstance().getFileStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    HgUtils.outputMercurialTab("\t" + file.getAbsolutePath()); // NOI18N
                }
                if (actionStatus == IGNORING) {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(IgnoreAction.class,
                            "MSG_IGNORE_DONE")); // NOI18N
                } else {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(IgnoreAction.class,
                            "MSG_UNIGNORE_DONE")); // NOI18N
                }
                // HgUtils.outputMercurialTab(""); // NOI18N
            }
        };
        support.start(rp, repository.getAbsolutePath(), org.openide.util.NbBundle.getMessage(IgnoreAction.class, "LBL_Ignore_Progress")); // NOI18N
    }
}
