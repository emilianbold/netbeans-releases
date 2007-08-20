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

package org.netbeans.modules.mercurial.ui.commit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.*;

/**
 *
 * @author Petr Kuzel
 */
public final class ExcludeFromCommitAction extends AbstractAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    private final VCSContext context;

    public ExcludeFromCommitAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    protected boolean enable(VCSContext ctx) {
        return getActionStatus(ctx) != UNDEFINED;
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected String getBaseName(VCSContext ctx) {
        int actionStatus = getActionStatus(ctx);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "popup_commit_exclude"; // NOI18N
        case INCLUDING:
            return "popup_commit_include"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(VCSContext ctx) {
        HgModuleConfig config = HgModuleConfig.getDefault();
        int status = UNDEFINED;
        if (ctx == null) ctx = context;
        Set<File> files = ctx.getRootFiles();
        for (File file : files) {
            if (config.isExcludedFromCommit(file.getAbsolutePath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    public void actionPerformed(ActionEvent e) {
        final VCSContext ctx = context;
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                HgModuleConfig config = HgModuleConfig.getDefault();
                int status = getActionStatus(ctx);
                Set<File> files = ctx.getRootFiles();
                List<String> paths = new ArrayList<String>(files.size());
                for (File file : files) {
                    paths.add(file.getAbsolutePath());
                }
                if (isCanceled()) return;
                if (status == EXCLUDING) {
                    config.addExclusionPaths(paths);
                } else if (status == INCLUDING) {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(rp, "", "");
    }
}
