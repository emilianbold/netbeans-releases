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
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Represnts <tt>svn resolved</tt> command.
 *
 * @author Petr Kuzel
 */
public class ConflictResolvedAction extends ContextAction {
    
    protected String getBaseName(Node[] activatedNodes) {
        return "resolve";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED_CONFLICT;
    }

    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File[] files = ctx.getFiles();

        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {

                SvnClient client = null;
                try {
                    client = Subversion.getInstance().getClient(ctx, this);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                    //ErrorManager.getDefault().notify(ErrorManager.USER, ex);
                }

                if (client == null) {
                    return;
                }
                
                for (int i = 0; i<files.length; i++) {
                    if(isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    try {
                        ConflictResolvedAction.perform(file, client);
                    } catch (SVNClientException ex) {
                        annotate(ex);                        
                    }
                }
            }
        };
        support.start(createRequestProcessor(nodes));        
    }


    /** Marks as resolved or shows error dialog. */
    public static void perform(File file) throws SVNClientException {
        SvnClient client = Subversion.getInstance().getClient(file);
        perform(file, client);        
    }

    private static void perform(File file, SvnClient client) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        client.resolved(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);

        // auxiliary files disappear, synch with FS
        File parent = file.getParentFile();
        if (parent != null) {
            FileObject folder = FileUtil.toFileObject(parent);
            if (folder != null) {
                folder.refresh();
            }
        }        
    }

}
