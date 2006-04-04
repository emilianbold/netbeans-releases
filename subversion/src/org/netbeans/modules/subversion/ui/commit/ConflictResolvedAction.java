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

package org.netbeans.modules.subversion.ui.commit;

import java.io.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
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

    protected void performContextAction(Node[] nodes, SvnProgressSupport support) {
        Context ctx = getContext(nodes);
        SvnClient client = null;;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        if (client == null) {
            return;
        }
                
        File[] files = ctx.getFiles();

        for (int i = 0; i<files.length; i++) {
            if(support.isCanceled()) {
                return;
            }
            File file = files[i];
            try {
                client.resolved(file);
                if(support.isCanceled()) {
                    return;
                }
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

}
