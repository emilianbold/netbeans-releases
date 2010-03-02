/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.subversion.ui.properties;

import java.awt.EventQueue;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VersioningInfo;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * 
 * @author Peter Pis
 */
public final class VersioningInfoAction extends ContextAction {

    private static final Logger LOG = Logger.getLogger(VersioningInfoAction.class.getName());

    @Override
    protected boolean enable(Node[] nodes) {
        return super.enable(nodes);
    }
    
    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_VERSIONED | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(VersioningInfoAction.class, "CTL_VersioningInfoAction");      // NOI18N
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_VersioningInfo";   // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {       
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }

        File root = roots[0];

        SVNUrl repositoryUrl = null;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if(repositoryUrl == null) {
            LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root }); //NOI18N
            return;
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            private FileStatusCache cache;
            @Override
            protected void perform() {
                Arrays.sort(roots, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });
                final LinkedHashMap<File, Map<String, String>> properties = new LinkedHashMap<File, Map<String, String>>(roots.length);
                cache = Subversion.getInstance().getStatusCache();
                for (File root : roots) {
                    FileInformation fi = cache.getStatus(root);
                    LinkedHashMap<String, String> fileProps = new LinkedHashMap<String, String>();
                    properties.put(root, fileProps);
                    String relativePath = getMessage("LBL_VersioningInfo_Property_Unknown"); //NOI18N
                    try {
                        relativePath = SvnUtils.getRelativePath(root);
                    } catch (SVNClientException ex) {
                        //
                    }
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_RelativePath"), relativePath); //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Status"), fi.getStatusText()); //NOI18N
                    if ((fi.getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        putPropsForVersioned(fileProps, root, fi);
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VersioningInfo.show(properties);
                    }
                });
            }

            private void putPropsForVersioned(LinkedHashMap<String, String> fileProps, File file, FileInformation fi) {
                ISVNStatus status = fi.getEntry(file);
                FileStatusCache.FileLabelCache.FileLabelInfo labelInfo;
                labelInfo = cache.getLabelsCache().getLabelInfo(file, true);

                String repositoryRootUrl = getMessage("LBL_VersioningInfo_Property_Unknown"); //NOI18N
                String binaryString = labelInfo.getBinaryString();
                String stickyString = labelInfo.getStickyString();
                try {
                    SVNUrl url = SvnUtils.getRepositoryRootUrl(file);
                    if (url != null) {
                        repositoryRootUrl = url.toString();
                    }
                } catch (SVNClientException ex) {
                    //
                }
                fileProps.put(getMessage("LBL_VersioningInfo_Property_RepositoryUrl"), status.getUrlString()); //NOI18N
                fileProps.put(getMessage("LBL_VersioningInfo_Property_RepositoryRootUrl"), repositoryRootUrl); //NOI18N
                if (status.getRevision().getNumber() > 0) {
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Revision"), status.getRevision().toString()); //NOI18N
                }
                if (!"".equals(binaryString)) { //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Mime"), binaryString); //NOI18N
                }
                if (!"".equals(stickyString)) { //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Branch"), stickyString); //NOI18N
                }
                if ((fi.getStatus() & FileInformation.STATUS_IN_REPOSITORY) != 0) {
                    try {
                        SvnClient client = Subversion.getInstance().getClient(file);
                        ISVNInfo info = client.getInfo(status.getUrl());
                        if (info != null) {
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedAuthor"), info.getLastCommitAuthor()); //NOI18N
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedDate"), DateFormat.getDateTimeInstance().format(info.getLastChangedDate())); //NOI18N
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedRevision"), info.getLastChangedRevision().toString()); //NOI18N
                        }
                    } catch (SVNClientException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            }
        };
        support.start(rp, repositoryUrl, NbBundle.getMessage(VersioningInfoAction.class, "LBL_VersioningInfo_Progress")); //NOI18N
    }

    private static String getMessage (String resourceName) {
        return NbBundle.getMessage(VersioningInfoAction.class, resourceName);
    }
}
