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

package org.netbeans.modules.subversion.ui.commit;

import java.io.*;
import org.netbeans.modules.subversion.settings.*;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.FileInformation;
import org.openide.nodes.*;

/**
 *
 * @author Petr Kuzel
 */
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
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
    
    public int getActionStatus(Node[] nodes) {
        SvnModuleConfig config = SvnModuleConfig.getDefault();
        File [] files = getContext(nodes).getFiles();
        int status = UNDEFINED;
        for (int i = 0; i < files.length; i++) {
            if (config.isExcludedFromCommit(files[i].getAbsolutePath())) {
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

    public void performContextAction(final Node[] nodes) {
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                SvnModuleConfig config = SvnModuleConfig.getDefault();
                int status = getActionStatus(nodes);
                File [] files = getContext(nodes).getFiles();
                if (status == EXCLUDING) {
                    for (int i = 0; i < files.length; i++) {
                        if(isCanceled()) {
                            return;
                        }
                        File file = files[i];
                        config.addExclusionPath(file.getAbsolutePath());
                    }
                } else if (status == INCLUDING) {
                    for (int i = 0; i < files.length; i++) {
                        if(isCanceled()) {
                            return;
                        }
                        File file = files[i];
                        config.removeExclusionPath(file.getAbsolutePath());
                    }
                }
            }
        };
        support.start(createRequestProcessor(nodes));
    }

}
