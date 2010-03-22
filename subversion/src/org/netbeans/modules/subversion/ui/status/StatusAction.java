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

package org.netbeans.modules.subversion.ui.status;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * Context sensitive status action. It opens the Subversion
 * view and sets its context.
 *
 * @author Petr Kuzel
 */
public class StatusAction  extends ContextAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED;
    
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ShowChanges"; // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performContextAction(Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = SvnUtils.getCurrentContext(nodes);
        final SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        stc.setContentTitle(getContextDisplayName(nodes));
        stc.setContext(ctx);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final Context context, SvnProgressSupport support) {

        if (context == null || context.getRoots().size() == 0) {
            return;
        }
            
        try {
            SvnClient client;            
            try {
                client = Subversion.getInstance().getClient(context, support);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }
            Subversion.getInstance().getStatusCache().refreshCached(context);
            File[] roots = context.getRootFiles();
            for (int i = 0; i < roots.length; i++) {
                executeStatus(roots[i], client, support);
                if (support.isCanceled()) {
                    return;
                }
            }
        } catch (SVNClientException ex) {
            if(!support.isCanceled()) {
                support.annotate(ex);
            } else {
                Subversion.LOG.log(Level.INFO, "Action canceled", ex);
            }
        }
    }

    public static void executeStatus(File root, SvnClient client, SvnProgressSupport support) throws SVNClientException {
        if (support != null && support.isCanceled()) {
            return;
        }
        ISVNStatus[] statuses = client.getStatus(root, true, false, true); // cache refires events
        if (support != null && support.isCanceled()) {
            return;
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int s = 0; s < statuses.length; s++) {
            if (support != null && support.isCanceled()) {
                return;
            }
            ISVNStatus status = statuses[s];
            File file = status.getFile();
            if (file.isDirectory() && status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                // could have been created externally and the cache ignores by designe
                // a newly created folders children.
                // As this is the place were such files should be recognized,
                // we will force the refresh recursivelly.
                cache.refreshRecursively(file, false);
            } else {
                cache.refresh(file, status);
            }
        }
    }
}
