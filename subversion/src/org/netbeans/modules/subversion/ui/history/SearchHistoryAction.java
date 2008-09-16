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

package org.netbeans.modules.subversion.ui.history;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import javax.swing.*;
import java.io.File;
import java.util.*;
import org.netbeans.modules.subversion.Subversion;

/**
 * Opens Search History Component.
 * 
 * @author Maros Sandor
 */
public class SearchHistoryAction extends ContextAction {

    static final int DIRECTORY_ENABLED_STATUS = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    static final int FILE_ENABLED_STATUS = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_SearchHistory"; // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FILE_ENABLED_STATUS;
    }

    protected int getDirectoryEnabledStatus() {
        return DIRECTORY_ENABLED_STATUS;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    protected void performContextAction(Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }        
        String title = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", getContextDisplayName(nodes)); // NOI18N
        openHistory(getContext(nodes), title);
    }

    private void openHistory(final Context context, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(context);
                tc.setDisplayName(title);
                tc.open();
                tc.requestActive();
                File [] files = context.getFiles();
                if (files.length == 1 && files[0].isFile() || files.length > 1 && Utils.shareCommonDataObject(files)) {
                    tc.search();
                }
            }
        });
    }

    /**
     * Opens the Seach History panel with given pre-filled values. The search is executed in default context
     * (all open projects). 
     * 
     * @param title title of the search
     * @param commitMessage commit message to search for
     * @param username user name to search for
     * @param date date of the change in question
     */ 
    public static void openSearch(String title, String commitMessage, String username, Date date) {
        openSearch(getDefaultContext(), title, commitMessage, username, date);
    }

    public static void openSearch(Context context, String title, String commitMessage, String username, Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // annotations do not include time information, we must search whole day
        c.add(Calendar.DATE, 1);
        Date to = c.getTime();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        Date from = c.getTime();
        
        if (commitMessage != null && commitMessage.indexOf('\n') != -1) {
            commitMessage = commitMessage.substring(0, commitMessage.indexOf('\n'));
        }
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(context, commitMessage, username, from, to);
        String tcTitle = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", title); // NOI18N
        tc.setDisplayName(tcTitle);
        tc.open();
        tc.requestActive();
        tc.search();
    }

    private static Context getDefaultContext() {
        Project [] projects = OpenProjects.getDefault().getOpenProjects();
        return SvnUtils.getProjectsContext(projects);
    }

    /**
     * Opens search panel in the context of the given repository URL.
     * 
     * @param repositoryUrl URL to search
     * @param localRoot local working copy root that corresponds to the repository URL 
     * @param revision revision to search for
     */ 
    public static void openSearch(SVNUrl repositoryUrl, File localRoot, long revision) {
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repositoryUrl, localRoot, revision);
        String tcTitle = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", repositoryUrl); // NOI18N
        tc.setDisplayName(tcTitle);
        tc.open();
        tc.requestActive();
        tc.search();
    }
}
